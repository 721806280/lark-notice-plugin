(function (window) {
    if (window.LarkNoticeUi) {
        return;
    }

    function resetContainerClass(container, baseClass) {
        if (!container) {
            return;
        }
        container.textContent = '';
        container.className = baseClass;
    }

    function renderAlertMessage(container, baseClass, message, isSuccess) {
        if (!container) {
            return;
        }
        resetContainerClass(container, baseClass);
        if (!message) {
            return;
        }
        container.classList.add('jenkins-alert', isSuccess ? 'jenkins-alert-success' : 'jenkins-alert-danger');
        container.textContent = message;
        Behaviour.applySubtree(container);
    }

    function createAlert(message, isSuccess) {
        var alert = document.createElement('div');
        alert.className = 'jenkins-alert ' + (isSuccess ? 'jenkins-alert-success' : 'jenkins-alert-danger');
        alert.textContent = message;
        return alert;
    }

    window.LarkNoticeUi = {
        createAlert: createAlert,
        renderAlertMessage: renderAlertMessage,
        resetContainerClass: resetContainerClass
    };
}(window));
