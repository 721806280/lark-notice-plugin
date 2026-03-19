Behaviour.specify('.robot-config-validate-btn', 'validate-robot-config', 0, function (element) {
    element.addEventListener('click', function () {
        validateRobotConfig(this);
    });
});

Behaviour.specify('.robot-config-copy-id-btn', 'copy-robot-id', 0, function (element) {
    element.addEventListener('click', function () {
        copyRobotId(this);
    });
});

Behaviour.specify('.robot-config-container', 'bind-robot-endpoint-config', 0, function (element) {
    initRobotEndpointConfig(element);
});

Behaviour.specify('.robot-locale-input', 'bind-robot-locale-config', 0, function (element) {
    element.addEventListener('change', function () {
        syncRobotLocaleValue(this.closest('.robot-config-container'));
    });
});

// Toggle retry details visibility when the enable checkbox changes.
Behaviour.specify('.lark-retry-config', 'toggle-retry-details', 0, function (element) {
    bindRetryDetailsVisibility(element);
});
