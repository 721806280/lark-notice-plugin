(function () {
    var MODE_SELECTOR = '.lark-notifier-mode-input';
    var ROOT_SELECTOR = '[name="notifierConfigs"]';
    var RAW_VALUE_SELECTOR = '.lark-notifier-raw-value';
    var MESSAGE_SELECTOR = 'textarea[name="_.message"]';

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

    function resolveCustomPayloadKind(root) {
        var messageField = root.querySelector(MESSAGE_SELECTOR);
        return messageField && parseJsonObject(messageField.value) ? 'json' : 'custom';
    }

    function updateDisplay(root, mode) {
        if (!root) {
            return;
        }

        var rawContent = root.querySelector('.lark-notifier-message-fields');
        var defaultContent = root.querySelector('.lark-notifier-default-fields');
        var commonFields = root.querySelector('.lark-notifier-common-fields');
        var defaultHelp = root.querySelector('.lark-notifier-mode-help-default');
        var customHelp = root.querySelector('.lark-notifier-mode-help-custom');
        var messageCustomHelp = root.querySelector('.lark-notifier-message-help-custom');
        var messageJsonHelp = root.querySelector('.lark-notifier-message-help-json');
        var rawValue = root.querySelector(RAW_VALUE_SELECTOR);
        var customPayloadKind = resolveCustomPayloadKind(root);
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
        return input.closest(ROOT_SELECTOR) || input.closest('.jenkins-form-item') || document;
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
            roots = Array.prototype.slice.call(document.querySelectorAll(ROOT_SELECTOR));
        } else if (root.matches && root.matches(ROOT_SELECTOR)) {
            roots = [root];
        } else {
            roots = Array.prototype.slice.call(root.querySelectorAll(ROOT_SELECTOR));
        }

        roots.forEach(function (scopeRoot) {
            applyPlaceholders(scopeRoot);
            var mode = inferMode(scopeRoot);
            var modeInput = scopeRoot.querySelector(MODE_SELECTOR + '[value="' + mode + '"]');
            if (modeInput) {
                modeInput.checked = true;
            }
            updateDisplay(scopeRoot, mode);
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        refreshAll(document);

        document.addEventListener('change', function (event) {
            if (!event.target.matches(MODE_SELECTOR)) {
                return;
            }
            updateDisplay(findScopeRoot(event.target), event.target.value);
        });

        document.addEventListener('input', function (event) {
            if (!event.target.matches(MESSAGE_SELECTOR)) {
                return;
            }
            var root = findScopeRoot(event.target);
            var modeInput = root.querySelector(MODE_SELECTOR + ':checked');
            var mode = modeInput ? modeInput.value : inferMode(root);
            updateDisplay(root, mode);
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
                    if (node.matches && node.matches(ROOT_SELECTOR)) {
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
