(function (window) {
    if (window.LarkNoticeRequest) {
        return;
    }

    function buildFormHeaders() {
        var headers = {
            "Content-Type": "application/x-www-form-urlencoded"
        };
        if (typeof crumb === 'object' && crumb) {
            headers[crumb.fieldName] = crumb.value;
        }
        return headers;
    }

    function readTextResponse(response) {
        return response.text().then(function (responseText) {
            return {
                ok: response.ok,
                text: responseText
            };
        });
    }

    function postForm(url, body) {
        return fetch(url, {
            method: 'POST',
            headers: buildFormHeaders(),
            body: body,
            credentials: 'include'
        }).then(readTextResponse);
    }

    function parseJsonObject(responseText) {
        if (!responseText || responseText.trim().length === 0) {
            return null;
        }
        try {
            return JSON.parse(responseText);
        } catch (error) {
            return null;
        }
    }

    window.LarkNoticeRequest = {
        buildFormHeaders: buildFormHeaders,
        parseJsonObject: parseJsonObject,
        postForm: postForm,
        readTextResponse: readTextResponse
    };
}(window));
