(function () {
    var NOTIFIER_MODE_SELECTOR = '.lark-notifier-mode-input';
    var NOTIFIER_ROOT_SELECTOR = '[name="notifierConfigs"]';
    var RAW_VALUE_SELECTOR = '.lark-notifier-raw-value';
    var CUSTOM_MESSAGE_SELECTOR = 'textarea[name="_.message"]';
    var LOAD_DEFAULT_TEMPLATE_SELECTOR = '.lark-notifier-load-default-btn';
    var TEMPLATE_STATUS_SELECTOR = '.lark-notifier-template-status';

    function setDisabled(container, disabled) {
        if (!container) {
            return;
        }

        container.querySelectorAll('input, textarea, select').forEach(function (field) {
            if (field.classList.contains('lark-notifier-mode-input') || field.classList.contains('lark-notifier-raw-value')) {
                return;
            }
            field.disabled = disabled;
        });
    }

    function parseJsonObject(text) {
        if (!text) {
            return false;
        }

        var trimmed = text.trim();
        if (trimmed.charAt(0) !== '{') {
            return false;
        }

        try {
            return typeof JSON.parse(trimmed) === 'object';
        } catch (ignored) {
            return false;
        }
    }

    function renderTemplateStatus(root, message, isSuccess) {
        var statusContainer = root.querySelector(TEMPLATE_STATUS_SELECTOR);
        if (!statusContainer) {
            return;
        }
        LarkNoticeUi.renderAlertMessage(statusContainer, 'lark-notifier-template-status', message, isSuccess);
    }

    function collectTemplatePayload(root) {
        var params = new URLSearchParams();
        var robotId = root.querySelector('input[name="robotId"]');
        var title = root.querySelector('input[name="_.title"]');
        var content = root.querySelector('textarea[name="_.content"]');

        params.set('robotId', robotId ? robotId.value : '');
        params.set('title', title ? title.value : '');
        params.set('content', content ? content.value : '');
        return params;
    }

    function loadDefaultTemplate(root, onSuccess) {
        var editor = root.querySelector('.lark-notifier-editor');
        var templateUrl = editor && editor.getAttribute('data-template-url');
        var requestFailedMessage = editor && editor.getAttribute('data-template-request-failed-message')
            || 'Unable to load the default template.';

        if (!templateUrl) {
            return Promise.resolve();
        }

        return LarkNoticeRequest.postForm(templateUrl, collectTemplatePayload(root).toString())
            .then(function (response) {
                var payload = LarkNoticeRequest.parseJsonObject(response.text) || {};
                if (!response.ok || payload.ok === false) {
                    renderTemplateStatus(root, payload.message || requestFailedMessage, false);
                    return;
                }
                renderTemplateStatus(root, '', true);
                if (typeof onSuccess === 'function') {
                    onSuccess(payload);
                }
            })
            .catch(function () {
                renderTemplateStatus(root, requestFailedMessage, false);
            });
    }

    function applyPlaceholder(root, selector, placeholder) {
        if (!placeholder) {
            return;
        }
        var field = root.querySelector(selector);
        if (field) {
            field.setAttribute('placeholder', placeholder);
        }
    }

    function applyPlaceholders(root) {
        if (!root) {
            return;
        }

        var commonFields = root.querySelector('.lark-notifier-common-fields');
        var defaultFields = root.querySelector('.lark-notifier-default-fields');
        var messageFields = root.querySelector('.lark-notifier-message-fields');

        if (commonFields) {
            applyPlaceholder(commonFields, 'input[name="_.title"]', commonFields.dataset.titlePlaceholder);
            applyPlaceholder(commonFields, 'textarea[name="_.atUserId"]', commonFields.dataset.mentionsPlaceholder);
        }
        if (defaultFields) {
            applyPlaceholder(defaultFields, 'textarea[name="_.content"]', defaultFields.dataset.contentPlaceholder);
        }
        if (messageFields) {
            applyPlaceholder(messageFields, 'textarea[name="_.message"]', messageFields.dataset.messagePlaceholder);
        }
    }

    function detectCustomPayloadKind(root) {
        var messageField = root.querySelector(CUSTOM_MESSAGE_SELECTOR);
        return messageField && parseJsonObject(messageField.value) ? 'json' : 'custom';
    }

    function updateDisplay(root, mode) {
        if (!root) {
            return;
        }

        var rawContent = root.querySelector('.lark-notifier-message-fields');
        var defaultContent = root.querySelector('.lark-notifier-default-fields');
        var commonFields = root.querySelector('.lark-notifier-common-fields');
        var customActions = root.querySelector('.lark-notifier-custom-actions');
        var defaultHelp = root.querySelector('.lark-notifier-mode-help-default');
        var customHelp = root.querySelector('.lark-notifier-mode-help-custom');
        var messageCustomHelp = root.querySelector('.lark-notifier-message-help-custom');
        var messageJsonHelp = root.querySelector('.lark-notifier-message-help-json');
        var rawValue = root.querySelector(RAW_VALUE_SELECTOR);
        var customPayloadKind = detectCustomPayloadKind(root);
        var isCustomJson = mode === 'custom' && customPayloadKind === 'json';

        if (rawContent) {
            rawContent.style.display = mode === 'custom' ? 'block' : 'none';
        }
        if (defaultContent) {
            defaultContent.style.display = mode === 'default' ? '' : 'none';
        }
        if (commonFields) {
            commonFields.style.display = '';
        }
        if (customActions) {
            customActions.style.display = mode === 'custom' ? 'flex' : 'none';
        }
        setDisabled(defaultContent, mode !== 'default');
        setDisabled(commonFields, isCustomJson);
        setDisabled(rawContent, mode !== 'custom');
        if (defaultHelp) {
            defaultHelp.style.display = mode === 'default' ? '' : 'none';
        }
        if (customHelp) {
            customHelp.style.display = mode === 'custom' ? '' : 'none';
        }
        if (messageCustomHelp) {
            messageCustomHelp.style.display = mode === 'custom' && !isCustomJson ? '' : 'none';
        }
        if (messageJsonHelp) {
            messageJsonHelp.style.display = isCustomJson ? '' : 'none';
        }
        if (rawValue) {
            rawValue.value = mode === 'default' ? 'false' : 'true';
        }
    }

    function findScopeRoot(input) {
        return input.closest(NOTIFIER_ROOT_SELECTOR) || input.closest('.jenkins-form-item') || document;
    }

    function inferMode(root) {
        var rawValue = root.querySelector(RAW_VALUE_SELECTOR);
        var isRaw = rawValue && rawValue.value === 'true';

        if (!isRaw) {
            return 'default';
        }
        return 'custom';
    }

    function refreshAll(root) {
        var roots = [];
        if (!root || root === document) {
            roots = Array.prototype.slice.call(document.querySelectorAll(NOTIFIER_ROOT_SELECTOR));
        } else if (root.matches && root.matches(NOTIFIER_ROOT_SELECTOR)) {
            roots = [root];
        } else {
            roots = Array.prototype.slice.call(root.querySelectorAll(NOTIFIER_ROOT_SELECTOR));
        }

        roots.forEach(function (scopeRoot) {
            applyPlaceholders(scopeRoot);
            var mode = inferMode(scopeRoot);
            var modeInput = scopeRoot.querySelector(NOTIFIER_MODE_SELECTOR + '[value="' + mode + '"]');
            if (modeInput) {
                modeInput.checked = true;
            }
            updateDisplay(scopeRoot, mode);
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        refreshAll(document);

        document.addEventListener('change', function (event) {
            if (!event.target.matches(NOTIFIER_MODE_SELECTOR)) {
                return;
            }
            var modeRoot = findScopeRoot(event.target);
            updateDisplay(modeRoot, event.target.value);
        });

        document.addEventListener('input', function (event) {
            if (!event.target.matches(CUSTOM_MESSAGE_SELECTOR)) {
                return;
            }
            var root = findScopeRoot(event.target);
            var modeInput = root.querySelector(NOTIFIER_MODE_SELECTOR + ':checked');
            var mode = modeInput ? modeInput.value : inferMode(root);
            updateDisplay(root, mode);
        });

        document.addEventListener('click', function (event) {
            if (!event.target.matches(LOAD_DEFAULT_TEMPLATE_SELECTOR)) {
                return;
            }
            var root = findScopeRoot(event.target);
            loadDefaultTemplate(root, function (payload) {
                var messageField = root.querySelector(CUSTOM_MESSAGE_SELECTOR);
                if (!messageField) {
                    return;
                }
                messageField.value = payload.defaultTemplate || '';
                var modeInput = root.querySelector(NOTIFIER_MODE_SELECTOR + ':checked');
                var mode = modeInput ? modeInput.value : inferMode(root);
                updateDisplay(root, mode);
            });
        });

        if (typeof MutationObserver === 'undefined') {
            return;
        }

        var observer = new MutationObserver(function (mutations) {
            mutations.forEach(function (mutation) {
                mutation.addedNodes.forEach(function (node) {
                    if (!node || node.nodeType !== Node.ELEMENT_NODE) {
                        return;
                    }
                    if (node.matches && node.matches(NOTIFIER_ROOT_SELECTOR)) {
                        refreshAll(node);
                        return;
                    }
                    refreshAll(node);
                });
            });
        });

        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    });
})();
