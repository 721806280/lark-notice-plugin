Behaviour.specify('.robot-config-validate-btn', 'validate-robot-config', 0, function (element) {
    element.addEventListener('click', function () {
        validateRobotConfig(this);
    });
});

// Toggle retry details visibility when the enable checkbox changes.
Behaviour.specify('.lark-retry-config', 'toggle-retry-details', 0, function (element) {
    bindRetryDetailsVisibility(element);
});
