Behaviour.specify('.lark-management-import-form', 'import-lark-config', 0, function (form) {
    if (form.dataset.importBound === 'true') {
        return;
    }
    form.dataset.importBound = 'true';

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        submitConfigImport(form);
    });
});

function submitConfigImport(form) {
    var resultContainer = form.querySelector('.lark-management-import-result');
    var submitButton = form.querySelector('.lark-config-import-btn');
    var requestFailedMessage = submitButton.getAttribute('data-import-request-failed-message') || 'Unable to import configuration.';
    renderImportResult(resultContainer, '', true);
    submitButton.disabled = true;

    fetch(form.action, {
        method: 'POST',
        headers: buildImportHeaders(),
        body: new URLSearchParams(new FormData(form)),
        credentials: 'include'
    }).then(function (response) {
        return response.text().then(function (responseText) {
            return {
                ok: response.ok,
                text: responseText
            };
        });
    }).then(function (payload) {
        var parsed = parseImportResponse(payload.text);
        var ok = parsed.ok === null ? payload.ok : (payload.ok && parsed.ok);
        var message = parsed.message || requestFailedMessage;
        renderImportResult(resultContainer, message, ok);
        if (ok) {
            window.setTimeout(function () {
                window.location.reload();
            }, 800);
        }
    }).catch(function (error) {
        console.error(error);
        renderImportResult(resultContainer, requestFailedMessage, false);
    }).then(function () {
        submitButton.disabled = false;
    });
}

function buildImportHeaders() {
    var headers = {
        "Content-Type": "application/x-www-form-urlencoded"
    };
    if (typeof crumb === 'object' && crumb) {
        headers[crumb.fieldName] = crumb.value;
    }
    return headers;
}

function parseImportResponse(responseText) {
    var emptyResult = {ok: null, message: ''};
    if (!responseText || responseText.trim().length === 0) {
        return emptyResult;
    }

    try {
        var payload = JSON.parse(responseText);
        return {
            ok: typeof payload.ok === 'boolean' ? payload.ok : null,
            message: typeof payload.message === 'string' ? payload.message : ''
        };
    } catch (error) {
        return {
            ok: null,
            message: responseText.trim()
        };
    }
}

function renderImportResult(container, message, isSuccess) {
    if (!container) {
        return;
    }
    container.textContent = message;
    container.className = 'lark-management-import-result';
    if (!message) {
        return;
    }
    container.classList.add('jenkins-alert', isSuccess ? 'jenkins-alert-success' : 'jenkins-alert-danger');
    Behaviour.applySubtree(container);
}
