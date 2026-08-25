#!/usr/bin/env python3
"""
将 GitHub release 按发布时间正序同步到 Gitee（自动跳过 Gitee 上已存在的 release）。

用法:
    python3 scripts/sync-releases-github-to-gitee.py [GITHUB_REPO_FULL_NAME] [GITEE_OWNER/REPO]

环境变量:
    GITEE_TOKEN   Gitee 私人令牌（必填）
    GH_TOKEN      GitHub token（可选，未认证时受 API 速率限制）

默认:
    GITHUB_REPO_FULL_NAME = 721806280/lark-notice-plugin
    GITEE_REPO            = xm721806280/lark-notice-plugin
"""
import json, os, sys, urllib.request, urllib.parse, urllib.error, tempfile, mimetypes, uuid

GITHUB_REPO = sys.argv[1] if len(sys.argv) > 1 else "721806280/lark-notice-plugin"
gitee_arg = sys.argv[2] if len(sys.argv) > 2 else "xm721806280/lark-notice-plugin"
GITEE_OWNER, GITEE_REPO = gitee_arg.split("/", 1)
GITHUB_API = f"https://api.github.com/repos/{GITHUB_REPO}"
GITEE_API = f"https://gitee.com/api/v5/repos/{GITEE_OWNER}/{GITEE_REPO}"

TOKEN = os.environ.get("GITEE_TOKEN")
if not TOKEN:
    sys.exit("错误: 请设置环境变量 GITEE_TOKEN（Gitee 私人令牌）")
GH_TOKEN = os.environ.get("GH_TOKEN", "")


def http_get_json(url, headers=None):
    req = urllib.request.Request(url, headers=headers or {})
    with urllib.request.urlopen(req, timeout=120) as resp:
        return json.loads(resp.read())


def github_releases():
    headers = {"Accept": "application/vnd.github+json", "User-Agent": "release-sync"}
    if GH_TOKEN:
        headers["Authorization"] = f"Bearer {GH_TOKEN}"
    page, all_rels = 1, []
    while True:
        data = http_get_json(f"{GITHUB_API}/releases?per_page=100&page={page}", headers)
        if not data:
            break
        all_rels.extend(data)
        if len(data) < 100:
            break
        page += 1
    return sorted([r for r in all_rels if r.get("published_at")], key=lambda r: r["published_at"])


def gitee_existing_tags():
    page, tags = 1, set()
    while True:
        url = f"{GITEE_API}/releases?per_page=100&page={page}&access_token={TOKEN}"
        try:
            data = http_get_json(url)
        except urllib.error.HTTPError as e:
            sys.exit(f"查询 Gitee releases 失败: {e.code} {e.read().decode(errors='replace')[:200]}")
        if not data:
            break
        for r in data:
            tags.add(r.get("tag_name"))
        if len(data) < 100:
            break
        page += 1
    return tags


def download(url, dest):
    req = urllib.request.Request(url, headers={"User-Agent": "release-sync"})
    with urllib.request.urlopen(req, timeout=120) as resp:
        data = resp.read()
    with open(dest, "wb") as f:
        f.write(data)
    return len(data)


def gitee_post_json(path, payload):
    data = urllib.parse.urlencode(payload).encode()
    req = urllib.request.Request(GITEE_API + path, data=data, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            return resp.status, json.loads(resp.read())
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode(errors="replace")


def gitee_upload_file(release_id, filepath, name):
    url = f"{GITEE_API}/releases/{release_id}/attach_files"
    boundary = uuid.uuid4().hex
    parts = [f"--{boundary}".encode(), b'Content-Disposition: form-data; name="access_token"',
             b"", TOKEN.encode(), f"--{boundary}".encode(),
             f'Content-Disposition: form-data; name="file"; filename="{name}"'.encode()]
    parts.append(f"Content-Type: {mimetypes.guess_type(name)[0] or 'application/octet-stream'}".encode())
    parts.append(b"")
    with open(filepath, "rb") as f:
        parts.append(f.read())
    parts.append(f"--{boundary}--".encode())
    req = urllib.request.Request(url, data=b"\r\n".join(parts), method="POST",
                                headers={"Content-Type": f"multipart/form-data; boundary={boundary}"})
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            return resp.status, json.loads(resp.read())
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode(errors="replace")


def main():
    gh = github_releases()
    print(f"GitHub releases: {len(gh)}")
    existing = gitee_existing_tags()
    print(f"Gitee 已有 release tags: {sorted(existing)}")
    to_sync = [r for r in gh if r["tag_name"] not in existing]
    print(f"待同步 ({len(to_sync)}): {[r['tag_name'] for r in to_sync]}")
    if not to_sync:
        print("已是最新，无需同步。")
        return

    results = []
    for r in to_sync:
        tag, name = r["tag_name"], r["name"]
        print(f"\n=== {tag} ({name}) prerelease={r.get('prerelease', False)} ===")
        status, resp = gitee_post_json("/releases", {
            "access_token": TOKEN, "tag_name": tag, "name": name,
            "body": r.get("body") or "", "prerelease": "true" if r.get("prerelease") else "false",
            "target_commitish": r.get("target_commitish") or "master"})
        if status not in (200, 201):
            print(f"  CREATE FAILED {status}: {str(resp)[:300]}")
            results.append((tag, "FAIL", f"{status} {str(resp)[:150]}"))
            continue
        rid = resp.get("id")
        print(f"  created gitee release id={rid}")
        for a in r.get("assets", []):
            aurl = a.get("browser_download_url") or a.get("url")
            aname = a.get("name")
            if not aurl:
                continue
            tmp_path = os.path.join(tempfile.gettempdir(), f"sync_{tag}_{aname}")
            try:
                size = download(aurl, tmp_path)
                print(f"  downloaded {aname} ({size} bytes)")
                st, ar = gitee_upload_file(rid, tmp_path, aname)
                print(f"  uploaded {aname} ok" if st in (200, 201) else f"  UPLOAD FAILED {st}: {str(ar)[:300]}")
            finally:
                if os.path.exists(tmp_path):
                    os.unlink(tmp_path)
        results.append((tag, "OK", rid))

    print("\n=== SUMMARY ===")
    for tag, status, info in results:
        print(f"{tag}: {status} ({info})")
    failed = [t for t, s, _ in results if s != "OK"]
    if failed:
        sys.exit(f"\n{len(failed)} 个 release 同步失败: {failed}")


if __name__ == "__main__":
    main()
