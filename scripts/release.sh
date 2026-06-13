#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  scripts/release.sh <version> [--docs-repo <path>] [--skip-tests] [--push]

Examples:
  scripts/release.sh 2.1.9-rc.6
  scripts/release.sh 2.1.9-rc.6 --docs-repo ../lark-notice-plugin-doc --push

The script:
  - updates pom.xml <revision>
  - verifies docs/guide/CHANGELOG.md contains the version
  - runs release tests unless --skip-tests is passed
  - commits plugin and docs changes
  - creates tag v<version>
  - optionally pushes plugin/docs branches and tag with --push
USAGE
}

version=""
docs_repo="../lark-notice-plugin-doc"
skip_tests=false
push_changes=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --docs-repo)
      docs_repo="${2:-}"
      shift 2
      ;;
    --skip-tests)
      skip_tests=true
      shift
      ;;
    --push)
      push_changes=true
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    -*)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 2
      ;;
    *)
      if [[ -n "$version" ]]; then
        echo "Version was already set to $version; unexpected argument: $1" >&2
        usage >&2
        exit 2
      fi
      version="$1"
      shift
      ;;
  esac
done

if [[ -z "$version" ]]; then
  usage >&2
  exit 2
fi

if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-][A-Za-z0-9]+(\.[0-9]+)?)?$ ]]; then
  echo "Unsupported version format: $version" >&2
  exit 2
fi

tag="v${version}"
repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"
docs_repo_abs="$(cd "$docs_repo" && pwd)"

require_clean_repo() {
  local path="$1"
  local dirty
  dirty="$(git -C "$path" status --porcelain)"
  if [[ -n "$dirty" ]]; then
    echo "Repository has uncommitted changes: $path" >&2
    printf '%s\n' "$dirty" >&2
    exit 1
  fi
}

require_docs_release_state() {
  local path="$1"
  local dirty line changed_path
  dirty="$(git -C "$path" status --porcelain)"
  while IFS= read -r line; do
    [[ -z "$line" ]] && continue
    [[ "$line" == "?? .pnpm-store/"* ]] && continue
    changed_path="${line:3}"
    if [[ "$changed_path" != "docs/guide/CHANGELOG.md" ]]; then
      echo "Docs repository has unexpected changes: $path" >&2
      printf '%s\n' "$dirty" >&2
      exit 1
    fi
  done <<< "$dirty"
}

require_branch() {
  local path="$1"
  local expected="$2"
  local branch
  branch="$(git -C "$path" branch --show-current)"
  if [[ "$branch" != "$expected" ]]; then
    echo "Repository must be on $expected, but $path is on $branch" >&2
    exit 1
  fi
}

require_branch "$repo_root" main
require_branch "$docs_repo_abs" main
require_clean_repo "$repo_root"
require_docs_release_state "$docs_repo_abs"

if git rev-parse "$tag" >/dev/null 2>&1; then
  echo "Tag already exists locally: $tag" >&2
  exit 1
fi

if git ls-remote --exit-code --tags origin "$tag" >/dev/null 2>&1; then
  echo "Tag already exists on origin: $tag" >&2
  exit 1
fi

changelog="$docs_repo_abs/docs/guide/CHANGELOG.md"
if [[ ! -f "$changelog" ]]; then
  echo "Missing changelog: $changelog" >&2
  exit 1
fi

if ! grep -q "^## \\[$version\\]" "$changelog"; then
  echo "Changelog does not contain ## [$version]. Update docs before releasing." >&2
  exit 1
fi

perl -0pi -e "s|<revision>[^<]+</revision>|<revision>$version</revision>|" pom.xml

if [[ "$skip_tests" == false ]]; then
  mvn -q test
  mvn -q -Pjenkins-rule-tests verify
fi

git add pom.xml
git commit -m "chore(release): bump version to $version"

git -C "$docs_repo_abs" add docs/guide/CHANGELOG.md
if ! git -C "$docs_repo_abs" diff --cached --quiet; then
  git -C "$docs_repo_abs" commit -m "docs(changelog): add $version release notes"
fi

git tag "$tag"

if [[ "$push_changes" == true ]]; then
  git -C "$docs_repo_abs" push origin main
  git push origin main "$tag"
else
  cat <<PUSH

Release prepared locally.

Push with:
  git -C "$docs_repo_abs" push origin main
  git push origin main $tag

The GitHub Release workflow will create the Release and upload target/lark-notice.hpi after the tag is pushed.
PUSH
fi
