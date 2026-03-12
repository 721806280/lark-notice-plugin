Behaviour.specify('.lark-management-import-form', 'import-lark-config', 0, function (form) {
    if (form.dataset.importBound === 'true') {
        return;
    }
    form.dataset.importBound = 'true';

    var previewButton = form.querySelector('.lark-config-preview-btn');
    if (previewButton) {
        previewButton.addEventListener('click', function () {
            submitConfigRequest(form, 'preview');
        });
    }

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        submitConfigRequest(form, 'import');
    });
});

function submitConfigRequest(form, action) {
    var importResultContainer = form.querySelector('.lark-management-import-result');
    var previewContainer = form.querySelector('.lark-management-import-preview');
    var importButton = form.querySelector('.lark-config-import-btn');
    var previewButton = form.querySelector('.lark-config-preview-btn');
    var requestFailedMessage = action === 'preview'
        ? (previewButton.getAttribute('data-preview-request-failed-message') || 'Unable to preview import.')
        : (importButton.getAttribute('data-import-request-failed-message') || 'Unable to import configuration.');
    var actionUrl = action === 'preview'
        ? resolvePreviewActionUrl(form)
        : form.action;

    renderImportResult(importResultContainer, '', true);
    if (action === 'preview') {
        renderPreviewResult(previewContainer, '', null, true);
    }

    toggleImportButtons(importButton, previewButton, true);

    fetch(actionUrl, {
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
        var parsed = parseConfigResponse(payload.text);
        var ok = parsed.ok === null ? payload.ok : (payload.ok && parsed.ok);
        var message = parsed.message || requestFailedMessage;

        if (action === 'preview') {
            renderPreviewResult(previewContainer, message, parsed.data, ok);
            return;
        }

        renderImportResult(importResultContainer, message, ok);
        if (ok) {
            window.setTimeout(function () {
                window.location.reload();
            }, 800);
        }
    }).catch(function (error) {
        console.error(error);
        if (action === 'preview') {
            renderPreviewResult(previewContainer, requestFailedMessage, null, false);
        } else {
            renderImportResult(importResultContainer, requestFailedMessage, false);
        }
    }).then(function () {
        toggleImportButtons(importButton, previewButton, false);
    });
}

function resolvePreviewActionUrl(form) {
    return new URL('previewImport', form.action).toString();
}

function toggleImportButtons(importButton, previewButton, disabled) {
    if (importButton) {
        importButton.disabled = disabled;
    }
    if (previewButton) {
        previewButton.disabled = disabled;
    }
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

function parseConfigResponse(responseText) {
    var emptyResult = {ok: null, message: '', data: null};
    if (!responseText || responseText.trim().length === 0) {
        return emptyResult;
    }

    try {
        var payload = JSON.parse(responseText);
        return {
            ok: typeof payload.ok === 'boolean' ? payload.ok : null,
            message: typeof payload.message === 'string' ? payload.message : '',
            data: payload.data && typeof payload.data === 'object' ? payload.data : null
        };
    } catch (error) {
        return {
            ok: null,
            message: responseText.trim(),
            data: null
        };
    }
}

function renderPreviewResult(container, message, data, isSuccess) {
    if (!container) {
        return;
    }

    container.textContent = '';
    container.className = 'lark-management-import-preview';

    if (!message && !data) {
        return;
    }

    var alert = document.createElement('div');
    alert.className = 'jenkins-alert ' + (isSuccess ? 'jenkins-alert-success' : 'jenkins-alert-danger');
    alert.textContent = message;
    container.appendChild(alert);

    if (!isSuccess || !data) {
        return;
    }

    var title = document.createElement('div');
    title.className = 'lark-management-import-preview__title';
    title.textContent = container.getAttribute('data-preview-title') || 'Preview Summary';
    container.appendChild(title);

    var summary = document.createElement('dl');
    summary.className = 'lark-management-import-preview__summary';
    appendPreviewItem(summary, container.getAttribute('data-preview-current-label'), data.currentRobotCount);
    appendPreviewItem(summary, container.getAttribute('data-preview-imported-label'), data.importedRobotCount);
    appendPreviewItem(summary, container.getAttribute('data-preview-added-label'), data.addedRobotCount);
    appendPreviewItem(summary, container.getAttribute('data-preview-updated-label'), data.updatedRobotCount);
    appendPreviewItem(summary, container.getAttribute('data-preview-removed-label'), data.removedRobotCount);
    appendPreviewItem(summary, container.getAttribute('data-preview-retained-label'), data.retainedRobotCount);
    appendPreviewItem(
        summary,
        container.getAttribute('data-preview-global-label'),
        data.globalSettingsOverwritten
            ? (container.getAttribute('data-preview-global-yes-label') || 'Yes')
            : (container.getAttribute('data-preview-global-no-label') || 'No')
    );
    container.appendChild(summary);
}

function appendPreviewItem(summary, label, value) {
    var term = document.createElement('dt');
    term.textContent = label || '';

    var description = document.createElement('dd');
    description.textContent = value == null ? '' : String(value);

    summary.appendChild(term);
    summary.appendChild(description);
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
