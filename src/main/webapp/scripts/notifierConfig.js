(function () {
    document.addEventListener('DOMContentLoaded', function () {
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

        var checkboxes = document.querySelectorAll('.notifier-config-raw input[name="_.raw"]');
        checkboxes.forEach(function (checkbox) {
            updateDisplay(findScopeRoot(checkbox), checkbox.checked);
        });

        document.addEventListener('change', function (event) {
            if (!event.target.matches('.notifier-config-raw input[name="_.raw"]')) {
                return;
            }
            updateDisplay(findScopeRoot(event.target), event.target.checked);
        });
    });
})();
