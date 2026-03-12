Behaviour.specify('.lark-management-import-form', 'import-lark-config', 0, function (form) {
    if (form.dataset.importBound === 'true') {
        return;
    }
    form.dataset.importBound = 'true';
    form.dataset.previewReady = 'false';

    var previewButton = form.querySelector('.lark-config-preview-btn');
    if (previewButton) {
        previewButton.addEventListener('click', function () {
            submitConfigRequest(form, 'preview');
        });
    }

    Array.prototype.forEach.call(form.querySelectorAll('textarea[name="payload"], input[name="mode"]'), function (element) {
        element.addEventListener('input', function () {
            resetPreviewState(form);
        });
        element.addEventListener('change', function () {
            syncModeOptionState(form);
            resetPreviewState(form);
        });
    });

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        if (form.dataset.previewReady !== 'true') {
            return;
        }
        submitConfigRequest(form, 'import');
    });

    resetPreviewState(form);
    syncModeOptionState(form);
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
        form.dataset.previewReady = 'false';
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
            form.dataset.previewReady = ok && parsed.data ? 'true' : 'false';
            renderPreviewResult(previewContainer, message, parsed.data, ok);
            syncImportButtonState(form);
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
            form.dataset.previewReady = 'false';
            syncImportButtonState(form);
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

function resetPreviewState(form) {
    form.dataset.previewReady = 'false';
    syncImportButtonState(form);
    syncModeOptionState(form);
    renderImportResult(form.querySelector('.lark-management-import-result'), '', true);
    renderPreviewEmptyState(form.querySelector('.lark-management-import-preview'));
}

function syncModeOptionState(form) {
    Array.prototype.forEach.call(form.querySelectorAll('.lark-management-mode-option'), function (option) {
        var input = option.querySelector('input[name="mode"]');
        option.classList.toggle('is-selected', !!input && input.checked);
    });
}

function syncImportButtonState(form) {
    var importButton = form.querySelector('.lark-config-import-btn');
    if (importButton) {
        importButton.disabled = form.dataset.previewReady !== 'true';
    }
}

function toggleImportButtons(importButton, previewButton, disabled) {
    if (previewButton) {
        previewButton.disabled = disabled;
    }
    if (importButton) {
        if (disabled) {
            importButton.disabled = true;
        } else {
            importButton.disabled = importButton.form.dataset.previewReady !== 'true';
        }
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
        renderPreviewEmptyState(container);
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

    var mode = document.createElement('div');
    mode.className = 'lark-management-import-preview__mode';
    mode.textContent = resolvePreviewModeLabel(container, data.mode);
    container.appendChild(mode);

    var summary = document.createElement('div');
    summary.className = 'lark-management-import-preview__summary';
    appendPreviewMetric(summary, container.getAttribute('data-preview-current-label'), data.currentRobotCount);
    appendPreviewMetric(summary, container.getAttribute('data-preview-imported-label'), data.importedRobotCount);
    appendPreviewMetric(summary, container.getAttribute('data-preview-added-label'), data.addedRobotCount);
    appendPreviewMetric(summary, container.getAttribute('data-preview-updated-label'), data.updatedRobotCount);
    appendPreviewMetric(summary, container.getAttribute('data-preview-removed-label'), data.removedRobotCount);
    appendPreviewMetric(summary, container.getAttribute('data-preview-retained-label'), data.retainedRobotCount);
    appendPreviewMetric(
        summary,
        container.getAttribute('data-preview-global-label'),
        data.globalSettingsOverwritten
            ? (container.getAttribute('data-preview-global-yes-label') || 'Yes')
            : (container.getAttribute('data-preview-global-no-label') || 'No')
    );
    container.appendChild(summary);
}

function renderPreviewEmptyState(container) {
    if (!container) {
        return;
    }
    container.textContent = '';
    container.className = 'lark-management-import-preview';

    var empty = document.createElement('div');
    empty.className = 'lark-management-import-preview__empty';
    empty.textContent = container.getAttribute('data-preview-empty-message') || '';
    container.appendChild(empty);
}

function resolvePreviewModeLabel(container, mode) {
    if (mode === 'merge') {
        return container.getAttribute('data-preview-mode-merge-label') || 'Merge';
    }
    return container.getAttribute('data-preview-mode-replace-label') || 'Replace';
}

function appendPreviewMetric(summary, label, value) {
    var metric = document.createElement('div');
    metric.className = 'lark-management-import-preview__metric';

    var metricLabel = document.createElement('div');
    metricLabel.className = 'lark-management-import-preview__metric-label';
    metricLabel.textContent = label || '';

    var metricValue = document.createElement('div');
    metricValue.className = 'lark-management-import-preview__metric-value';
    metricValue.textContent = value == null ? '' : String(value);

    metric.appendChild(metricLabel);
    metric.appendChild(metricValue);
    summary.appendChild(metric);
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
