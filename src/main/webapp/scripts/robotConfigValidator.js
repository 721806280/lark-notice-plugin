async function validateRobotConfig(_this) {
    var robot = _this.closest('.robot-config-container');
    var msg = robot.querySelector('.robot-config-validate-msg');
    msg.innerHTML = '';

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

        var message = await response.text();
        if (response.ok) {
            msg.innerHTML = message;
            layoutUpdateCallback.call();
        } else {
            var id = 'valerr' + iota++; // 这里的 iota 是一个全局计数器，你需要在其他地方定义并初始化它。
            msg.innerHTML = '<a href="" onclick="document.getElementById(\'' + id + '\').style.display=\'block\';return false">ERROR</a><div id="' + id + '" style="display:none"><pre>' + message + '</pre></div>';
        }
        Behaviour.applySubtree(msg);
    } catch (error) {
        console.error(error);
    }
}

/**
 * 获取机器人的请求参数
 * @param {HTMLElement} robot - 机器人元素
 * @returns {URLSearchParams} - 请求参数
 */
function getParams(robot) {
    // 获取代理信息
    var proxy = document.getElementById('proxyConfigContainer');
    var proxyConfig = {
        type: proxy.querySelector('select[name="type"]').value, // 获取代理类型
        host: proxy.querySelector('input[name="host"]').value, // 获取代理主机地址
        port: proxy.querySelector('input[name="port"]').value, // 获取代理端口号
    };

    // 获取安全策略配置
    var securityConfigs = Array.from(robot.querySelectorAll('.security-config-container'))
        .map(function (el) {
            // 获取策略类型以及值
            var type = el.querySelector('input[name="type"]').value;
            var valueElement = el.querySelector('input[name="value"]');
            var value = valueElement.type === 'checkbox' ? valueElement.checked : valueElement.value;
            var desc = el.querySelector('input[name="desc"]').value;
            return {type: type, value: value, desc: desc};
        });

    // 创建一个空数组来存储请求参数
    var params = new URLSearchParams();

    // 添加请求参数
    params.append('id', robot.querySelector('input[name="id"]').value);
    params.append('name', robot.querySelector('input[name="name"]').value);
    params.append('webhook', robot.querySelector('input[name="webhook"]').value);
    params.append('proxy', JSON.stringify(proxyConfig));                // 代理配置
    params.append('securityConfigs', JSON.stringify(securityConfigs));  // 安全配置

    // 返回表单请求参数
    return params;
}
