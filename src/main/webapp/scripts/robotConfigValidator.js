async function validateRobotConfig(_this) {
    var robot = _this.closest('.robot-config-container');
    var validateMsg = robot.querySelector('.robot-config-validate-msg');
    var requestFailedMessage = _this.getAttribute('data-validate-request-failed-message') || 'Validation request failed.';
    validateMsg.textContent = '';
    validateMsg.className = 'robot-config-validate-msg';
    _this.disabled = true;

    try {
        var checkUrl = _this.getAttribute('data-validate-button-descriptor-url') + '/' + _this.getAttribute('data-validate-button-method');

        var response = await fetch(checkUrl, {
            method: 'POST',
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                [crumb.fieldName]: crumb.value
            },
            body: getParams(robot),
            credentials: 'include'
        });

        var responseText = await response.text();
        var parsed = parseValidationResponse(responseText);
        var ok = parsed.ok === null ? response.ok : (response.ok && parsed.ok);
        var message = parsed.message;
        if (!message) {
            message = response.ok ? (_this.getAttribute('data-validate-generic-success-message') || 'Validation passed.') : requestFailedMessage;
        }
        renderValidationResult(validateMsg, message, ok);
    } catch (error) {
        console.error(error);
        renderValidationResult(validateMsg, requestFailedMessage, false);
    } finally {
        _this.disabled = false;
    }
}

function parseValidationResponse(responseText) {
    var emptyResult = {ok: null, message: ''};
    if (!responseText || responseText.trim().length === 0) {
        return emptyResult;
    }

    var payload;
    try {
        payload = JSON.parse(responseText);
    } catch (e) {
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
    container.textContent = message;
    container.className = 'robot-config-validate-msg';
    container.classList.add('jenkins-alert', isSuccess ? 'jenkins-alert-success' : 'jenkins-alert-danger');
    Behaviour.applySubtree(container);
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
        type: 'DIRECT',
        host: '',
        port: ''
    };
    if (proxy) {
        var typeInput = proxy.querySelector('select[name="type"]');
        var hostInput = proxy.querySelector('input[name="host"]');
        var portInput = proxy.querySelector('input[name="port"]');
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
