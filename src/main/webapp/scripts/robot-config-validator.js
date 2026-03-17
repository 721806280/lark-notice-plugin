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

var ROBOT_PROVIDER_DEFAULTS = {
    FEISHU: {
        protocolType: 'LARK_COMPATIBLE',
        endpointMode: 'BASE_URL_AND_TOKEN',
        baseUrl: 'https://open.feishu.cn',
        prefixPath: '/open-apis/bot/v2/hook/'
    },
    LARK: {
        protocolType: 'LARK_COMPATIBLE',
        endpointMode: 'BASE_URL_AND_TOKEN',
        baseUrl: 'https://open.larksuite.com',
        prefixPath: '/open-apis/bot/v2/hook/'
    },
    DING_TALK: {
        protocolType: 'DING_TALK',
        endpointMode: 'FULL_WEBHOOK',
        webhookPrefix: 'https://oapi.dingtalk.com/robot/send?access_token='
    }
};

function initRobotEndpointConfig(container) {
    if (!container || container.dataset.endpointConfigBound === 'true') {
        return;
    }
    container.dataset.endpointConfigBound = 'true';

    seedRobotEndpointUiState(container);

    var providerSelect = container.querySelector('.robot-provider-select');
    if (providerSelect) {
        providerSelect.addEventListener('change', function () {
            applyRobotEndpointConfig(container);
        });
    }

    var customToggle = container.querySelector('.robot-custom-webhook-toggle');
    if (customToggle) {
        customToggle.addEventListener('change', function () {
            applyRobotEndpointConfig(container);
        });
    }

    var endpointInput = container.querySelector('.robot-endpoint-input');
    if (endpointInput) {
        endpointInput.addEventListener('input', function () {
            applyRobotEndpointConfig(container);
        });
    }

    applyRobotEndpointConfig(container);
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
    params.append('protocolType', getRobotConfigValue(robot, '.robot-protocol-type-input'));
    params.append('endpointMode', getRobotConfigValue(robot, '.robot-endpoint-mode-input'));
    params.append('webhook', getRobotConfigValue(robot, '.robot-webhook-input'));
    params.append('baseUrl', getRobotConfigValue(robot, '.robot-base-url-input'));
    params.append('webhookToken', getRobotConfigValue(robot, '.robot-webhook-token-input'));
    params.append('proxy', JSON.stringify(proxyConfig));                // Serialized proxy configuration.
    params.append('securityConfigs', JSON.stringify(securityConfigs));  // Serialized security policy configuration.

    // Return the payload for the validation request.
    return params;
}

function getRobotConfigValue(robot, selector) {
    var element = robot.querySelector(selector);
    return element ? element.value : '';
}

function getRobotCheckedValue(robot, selector) {
    var element = robot.querySelector(selector + ':checked');
    return element ? element.value : '';
}

function applyRobotEndpointConfig(robot) {
    if (!robot) {
        return;
    }
    var workspace = robot.querySelector('.robot-endpoint-workspace');
    if (!workspace) {
        return;
    }
    var state = collectRobotEndpointState(robot, workspace);
    syncHiddenEndpointFields(robot, workspace, state);
    renderRobotEndpointEditor(robot, workspace, state);
}

function seedRobotEndpointUiState(robot) {
    if (!robot || robot.dataset.endpointUiSeeded === 'true') {
        return;
    }
    robot.dataset.endpointUiSeeded = 'true';

    var inferred = inferRobotEndpointState(robot);
    setRobotProvider(robot, inferred.provider);

    var customToggle = robot.querySelector('.robot-custom-webhook-toggle');
    if (customToggle) {
        customToggle.checked = inferred.custom;
    }

    var endpointInput = robot.querySelector('.robot-endpoint-input');
    if (endpointInput) {
        endpointInput.value = inferred.value;
    }
}

function inferRobotEndpointState(robot) {
    var protocolType = getRobotConfigValue(robot, '.robot-protocol-type-input').trim();
    var endpointMode = getRobotConfigValue(robot, '.robot-endpoint-mode-input').trim();
    var webhook = getRobotConfigValue(robot, '.robot-webhook-input').trim();
    var baseUrl = normalizeBaseUrl(getRobotConfigValue(robot, '.robot-base-url-input'));
    var webhookToken = getRobotConfigValue(robot, '.robot-webhook-token-input').trim();
    var resolvedWebhook = resolveWebhookFromFields(protocolType, endpointMode, webhook, baseUrl, webhookToken);

    if (!protocolType && !endpointMode && !webhook && !baseUrl && !webhookToken) {
        return {provider: 'FEISHU', custom: false, value: ''};
    }
    if (protocolType !== 'DING_TALK' && !resolvedWebhook && !baseUrl && !webhookToken) {
        return {provider: 'FEISHU', custom: false, value: ''};
    }

    if (protocolType === 'DING_TALK') {
        var dingTalkToken = extractDingTalkToken(webhook);
        return {
            provider: 'DING_TALK',
            custom: !dingTalkToken,
            value: dingTalkToken || webhook
        };
    }

    var extractedBaseUrl = baseUrl || extractLarkCompatibleBaseUrl(webhook);
    var extractedToken = webhookToken || extractLarkCompatibleToken(webhook);

    if (extractedBaseUrl === ROBOT_PROVIDER_DEFAULTS.FEISHU.baseUrl && extractedToken) {
        return {provider: 'FEISHU', custom: false, value: extractedToken};
    }
    if (extractedBaseUrl === ROBOT_PROVIDER_DEFAULTS.LARK.baseUrl && extractedToken) {
        return {provider: 'LARK', custom: false, value: extractedToken};
    }
    if (containsText(webhook, 'open.larksuite.com')) {
        return {provider: 'LARK', custom: true, value: resolvedWebhook || webhook};
    }
    return {
        provider: 'FEISHU',
        custom: true,
        value: resolvedWebhook || webhook
    };
}

function collectRobotEndpointState(robot, workspace) {
    var provider = getRobotConfigValue(robot, '.robot-provider-select') || 'FEISHU';
    var endpointInput = robot.querySelector('.robot-endpoint-input');
    var customToggle = robot.querySelector('.robot-custom-webhook-toggle');
    var rawValue = endpointInput ? endpointInput.value.trim() : '';
    var custom = !!(customToggle && customToggle.checked);

    return {
        provider: provider,
        custom: custom,
        value: rawValue,
        resolvedWebhook: buildResolvedWebhook(provider, custom, rawValue, workspace)
    };
}

function syncHiddenEndpointFields(robot, workspace, state) {
    var protocolInput = robot.querySelector('.robot-protocol-type-input');
    var endpointModeInput = robot.querySelector('.robot-endpoint-mode-input');
    var webhookInput = robot.querySelector('.robot-webhook-input');
    var baseUrlInput = robot.querySelector('.robot-base-url-input');
    var webhookTokenInput = robot.querySelector('.robot-webhook-token-input');
    if (!protocolInput || !endpointModeInput || !webhookInput || !baseUrlInput || !webhookTokenInput) {
        return;
    }

    if (state.provider === 'DING_TALK') {
        protocolInput.value = ROBOT_PROVIDER_DEFAULTS.DING_TALK.protocolType;
        endpointModeInput.value = ROBOT_PROVIDER_DEFAULTS.DING_TALK.endpointMode;
        webhookInput.value = state.resolvedWebhook;
        baseUrlInput.value = '';
        webhookTokenInput.value = '';
        return;
    }

    protocolInput.value = ROBOT_PROVIDER_DEFAULTS.FEISHU.protocolType;
    if (state.custom) {
        endpointModeInput.value = 'FULL_WEBHOOK';
        webhookInput.value = state.resolvedWebhook;
        baseUrlInput.value = '';
        webhookTokenInput.value = '';
        return;
    }

    endpointModeInput.value = 'BASE_URL_AND_TOKEN';
    webhookInput.value = '';
    baseUrlInput.value = getProviderBaseUrl(state.provider, workspace);
    webhookTokenInput.value = state.value;
}

function renderRobotEndpointEditor(robot, workspace, state) {
    var preview = robot.querySelector('.robot-webhook-preview');
    var label = robot.querySelector('.robot-endpoint-input-label');
    var input = robot.querySelector('.robot-endpoint-input');
    var hint = robot.querySelector('.robot-endpoint-input-hint');

    if (label) {
        label.textContent = state.custom ? workspace.dataset.webhookLabel : workspace.dataset.tokenLabel;
    }
    workspace.classList.toggle('is-custom-mode', state.custom);
    workspace.setAttribute('data-provider', state.provider);
    if (input) {
        input.placeholder = state.custom
            ? (workspace.dataset.webhookPlaceholder || '')
            : (workspace.dataset.tokenPlaceholder || '');
    }
    if (hint) {
        hint.textContent = resolveEndpointHint(workspace, state);
    }
    if (preview) {
        preview.textContent = state.resolvedWebhook || preview.getAttribute('data-empty-preview-message') || '';
    }
}

function resolveEndpointHint(workspace, state) {
    if (state.custom) {
        return workspace.dataset.customHint || '';
    }
    if (state.provider === 'LARK') {
        return workspace.dataset.tokenHintLark || '';
    }
    if (state.provider === 'DING_TALK') {
        return workspace.dataset.tokenHintDingTalk || '';
    }
    return workspace.dataset.tokenHintFeishu || '';
}

function buildResolvedWebhook(provider, custom, value, workspace) {
    if (!value) {
        return '';
    }
    if (custom) {
        return value;
    }
    if (provider === 'DING_TALK') {
        return (workspace.dataset.dingTalkWebhookPrefix || ROBOT_PROVIDER_DEFAULTS.DING_TALK.webhookPrefix) + value;
    }
    return getProviderBaseUrl(provider, workspace) + ROBOT_PROVIDER_DEFAULTS.FEISHU.prefixPath + value;
}

function getProviderBaseUrl(provider, workspace) {
    if (provider === 'LARK') {
        return normalizeBaseUrl(workspace.dataset.larkBaseUrl) || ROBOT_PROVIDER_DEFAULTS.LARK.baseUrl;
    }
    return normalizeBaseUrl(workspace.dataset.feishuBaseUrl) || ROBOT_PROVIDER_DEFAULTS.FEISHU.baseUrl;
}

function setRobotProvider(robot, provider) {
    var select = robot.querySelector('.robot-provider-select');
    if (select) {
        select.value = provider;
    }
}

function resolveWebhookFromFields(protocolType, endpointMode, webhook, baseUrl, webhookToken) {
    if (protocolType === 'DING_TALK') {
        return webhook;
    }
    if (endpointMode === 'BASE_URL_AND_TOKEN') {
        return baseUrl && webhookToken ? baseUrl + ROBOT_PROVIDER_DEFAULTS.FEISHU.prefixPath + webhookToken : webhook;
    }
    return webhook;
}

function extractLarkCompatibleBaseUrl(webhook) {
    var parsed = parseUrl(webhook);
    if (!parsed || !startsWithText(parsed.pathname, ROBOT_PROVIDER_DEFAULTS.FEISHU.prefixPath)) {
        return '';
    }
    return parsed.origin;
}

function extractLarkCompatibleToken(webhook) {
    var parsed = parseUrl(webhook);
    if (!parsed || !startsWithText(parsed.pathname, ROBOT_PROVIDER_DEFAULTS.FEISHU.prefixPath)) {
        return '';
    }
    return parsed.pathname.substring(ROBOT_PROVIDER_DEFAULTS.FEISHU.prefixPath.length);
}

function extractDingTalkToken(webhook) {
    var parsed = parseUrl(webhook);
    if (!parsed || parsed.pathname !== '/robot/send' || !containsText(parsed.hostname, 'dingtalk.com')) {
        return '';
    }
    return parsed.searchParams.get('access_token') || '';
}

function parseUrl(value) {
    if (!value) {
        return null;
    }
    try {
        return new URL(value);
    } catch (error) {
        return null;
    }
}

function normalizeBaseUrl(value) {
    return (value || '').trim().replace(/\/+$/, '');
}

function startsWithText(value, prefix) {
    return typeof value === 'string' && value.indexOf(prefix) === 0;
}

function containsText(value, token) {
    return typeof value === 'string' && value.indexOf(token) !== -1;
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
