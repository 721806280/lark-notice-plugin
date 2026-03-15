function validateRobotConfig(_this) {
    var robot = _this.closest('.robot-config-container');
    var validateMsg = robot.querySelector('.robot-config-validate-msg');
    var requestFailedMessage = _this.getAttribute('data-validate-request-failed-message') || 'Validation request failed.';
    validateMsg.textContent = '';
    validateMsg.className = 'robot-config-validate-msg';
    _this.disabled = true;

    var checkUrl = _this.getAttribute('data-validate-button-descriptor-url') + '/' + _this.getAttribute('data-validate-button-method');
    LarkNoticeRequest.postForm(checkUrl, getParams(robot)).then(function (payload) {
        var parsed = parseValidationResponse(payload.text);
        var ok = parsed.ok === null ? payload.ok : (payload.ok && parsed.ok);
        var message = parsed.message;
        if (!message) {
            message = payload.ok
                ? (_this.getAttribute('data-validate-generic-success-message') || 'Validation passed.')
                : requestFailedMessage;
        }
        renderValidationResult(validateMsg, message, ok);
    }).catch(function (error) {
        console.error(error);
        renderValidationResult(validateMsg, requestFailedMessage, false);
    }).then(function () {
        _this.disabled = false;
    });
}

function parseValidationResponse(responseText) {
    var emptyResult = {ok: null, message: ''};
    if (!responseText || responseText.trim().length === 0) {
        return emptyResult;
    }

    var payload = LarkNoticeRequest.parseJsonObject(responseText);
    if (!payload) {
        var text = responseText.trim();
        return {
            ok: /^error\s*[:：]/i.test(text) ? false : null,
            message: /^[{\[]/.test(text) ? '' : text
        };
    }
    var data = payload && typeof payload.data === 'object' && payload.data !== null ? payload.data : payload;
    var ok = typeof data.ok === 'boolean' ? data.ok : null;

    var message = '';
    if (typeof data.message === 'string' && data.message.length > 0) {
        message = data.message;
    } else if (typeof payload.message === 'string' && payload.message.length > 0) {
        message = payload.message;
    }
    return {ok: ok, message: message};
}

function renderValidationResult(container, message, isSuccess) {
    LarkNoticeUi.renderAlertMessage(container, 'robot-config-validate-msg', message, isSuccess);
}

function copyRobotId(button) {
    var robot = button.closest('.robot-config-container');
    if (!robot) {
        return;
    }
    var idInput = robot.querySelector('input[name="id"]');
    var robotId = idInput ? idInput.value : '';
    if (!robotId) {
        flashCopyButtonLabel(button, button.getAttribute('data-copy-failure-label') || 'Copy failed');
        return;
    }
    writeTextToClipboard(robotId).then(function () {
        flashCopyButtonLabel(button, button.getAttribute('data-copy-success-label') || 'Copied');
    }).catch(function (error) {
        console.error(error);
        flashCopyButtonLabel(button, button.getAttribute('data-copy-failure-label') || 'Copy failed');
    });
}

function flashCopyButtonLabel(button, label) {
    var defaultLabel = button.getAttribute('data-copy-default-label') || button.textContent;
    if (button.dataset.copyLabelTimerId) {
        window.clearTimeout(Number(button.dataset.copyLabelTimerId));
    }
    button.textContent = label;
    button.disabled = true;
    button.dataset.copyLabelTimerId = String(window.setTimeout(function () {
        button.textContent = defaultLabel;
        button.disabled = false;
        delete button.dataset.copyLabelTimerId;
    }, 1200));
}

function writeTextToClipboard(text) {
    if (navigator.clipboard && typeof navigator.clipboard.writeText === 'function') {
        return navigator.clipboard.writeText(text);
    }
    return new Promise(function (resolve, reject) {
        var helper = document.createElement('textarea');
        helper.value = text;
        helper.setAttribute('readonly', 'readonly');
        helper.style.position = 'fixed';
        helper.style.opacity = '0';
        document.body.appendChild(helper);
        helper.select();
        try {
            if (document.execCommand('copy')) {
                resolve();
            } else {
                reject(new Error('Copy command was rejected.'));
            }
        } catch (error) {
            reject(error);
        } finally {
            document.body.removeChild(helper);
        }
    });
}

/**
 * Builds the request payload used to validate a robot configuration.
 * @param {HTMLElement} robot - Robot configuration container element.
 * @returns {URLSearchParams} Request parameters for the validation endpoint.
 */
function getParams(robot) {
    // Read the shared proxy settings from the global proxy section.
    var proxy = document.getElementById('proxyConfigContainer');
    var proxyConfig = {
        enabled: false,
        type: 'DIRECT',
        host: '',
        port: ''
    };
    if (proxy) {
        var enabledInput = proxy.querySelector('input[name="enabled"], input[name="_.enabled"]');
        var typeInput = proxy.querySelector('select[name="type"], select[name="_.type"]');
        var hostInput = proxy.querySelector('input[name="host"], input[name="_.host"]');
        var portInput = proxy.querySelector('input[name="port"], input[name="_.port"]');
        proxyConfig.enabled = enabledInput ? enabledInput.checked : false;
        proxyConfig.type = typeInput ? typeInput.value : 'DIRECT';
        proxyConfig.host = hostInput ? hostInput.value : '';
        proxyConfig.port = portInput ? portInput.value : '';
    }

    // Collect all security policy entries configured for the current robot.
    var securityConfigs = Array.from(robot.querySelectorAll('.security-config-container'))
        .map(function (el) {
            // Extract the policy type and its current value.
            var type = el.querySelector('input[name="type"]').value;
            var valueElement = el.querySelector('input[name="value"]');
            var value = valueElement.type === 'checkbox' ? valueElement.checked : valueElement.value;
            var desc = el.querySelector('input[name="desc"]').value;
            return {type: type, value: value, desc: desc};
        });

    // Build the form-encoded payload expected by Jenkins.
    var params = new URLSearchParams();

    // Append the current robot form values.
    params.append('id', robot.querySelector('input[name="id"]').value);
    params.append('name', robot.querySelector('input[name="name"]').value);
    params.append('webhook', robot.querySelector('input[name="webhook"]').value);
    params.append('proxy', JSON.stringify(proxyConfig));                // Serialized proxy configuration.
    params.append('securityConfigs', JSON.stringify(securityConfigs));  // Serialized security policy configuration.

    // Return the payload for the validation request.
    return params;
}

/**
 * Applies the proxy detail visibility based on the enable checkbox.
 * @param {HTMLElement} configRoot - Proxy configuration container element.
 */
function applyProxyDetailsVisibility(configRoot) {
    if (!configRoot) {
        return;
    }
    var enabledInput = configRoot.querySelector('input[name="enabled"], input[name="_.enabled"]');
    var details = configRoot.querySelector('.lark-proxy-details');
    if (!enabledInput || !details) {
        return;
    }
    details.hidden = !enabledInput.checked;
}

/**
 * Wires the proxy detail toggle behavior to the enable checkbox.
 * @param {HTMLElement} configRoot - Proxy configuration container element.
 */
function bindProxyDetailsVisibility(configRoot) {
    if (!configRoot || configRoot.dataset.proxyVisibilityBound === 'true') {
        return;
    }
    configRoot.dataset.proxyVisibilityBound = 'true';
    var enabledInput = configRoot.querySelector('input[name="enabled"], input[name="_.enabled"]');
    if (!enabledInput) {
        return;
    }
    applyProxyDetailsVisibility(configRoot);
    enabledInput.addEventListener('change', function () {
        applyProxyDetailsVisibility(configRoot);
    });
}

/**
 * Applies the retry detail visibility based on the enable checkbox.
 * @param {HTMLElement} configRoot - Retry configuration container element.
 */
function applyRetryDetailsVisibility(configRoot) {
    if (!configRoot) {
        return;
    }
    var enabledInput = configRoot.querySelector('input[name="_.enabled"]');
    var details = configRoot.querySelector('.lark-retry-details');
    if (!enabledInput || !details) {
        return;
    }
    details.hidden = !enabledInput.checked;
}

/**
 * Wires the retry detail toggle behavior to the enable checkbox.
 * @param {HTMLElement} configRoot - Retry configuration container element.
 */
function bindRetryDetailsVisibility(configRoot) {
    if (!configRoot || configRoot.dataset.retryVisibilityBound === 'true') {
        return;
    }
    configRoot.dataset.retryVisibilityBound = 'true';
    var enabledInput = configRoot.querySelector('input[name="_.enabled"]');
    if (!enabledInput) {
        return;
    }
    applyRetryDetailsVisibility(configRoot);
    enabledInput.addEventListener('change', function () {
        applyRetryDetailsVisibility(configRoot);
    });
}
