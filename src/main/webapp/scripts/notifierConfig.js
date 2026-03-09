(function () {
    var RAW_CHECKBOX_SELECTOR = '.notifier-config-raw input[name="_.raw"]';

    function updateDisplay(root, isRaw) {
        if (!root) {
            return;
        }

        var rawContent = root.querySelector('.raw-content');
        var noneRawContent = root.querySelector('.none-raw-content');
        if (rawContent) {
            rawContent.style.display = isRaw ? '' : 'none';
        }
        if (noneRawContent) {
            noneRawContent.style.display = isRaw ? 'none' : '';
        }
    }

    function findScopeRoot(input) {
        return input.closest('[name="notifierConfigs"]') || input.closest('.jenkins-form-item') || document;
    }

    function refreshCheckbox(checkbox) {
        updateDisplay(findScopeRoot(checkbox), checkbox.checked);
    }

    function refreshAll(root) {
        var checkboxes = (root || document).querySelectorAll(RAW_CHECKBOX_SELECTOR);
        checkboxes.forEach(function (checkbox) {
            refreshCheckbox(checkbox);
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        refreshAll(document);

        document.addEventListener('change', function (event) {
            if (!event.target.matches(RAW_CHECKBOX_SELECTOR)) {
                return;
            }
            refreshCheckbox(event.target);
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
                    if (node.matches && node.matches(RAW_CHECKBOX_SELECTOR)) {
                        refreshCheckbox(node);
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
