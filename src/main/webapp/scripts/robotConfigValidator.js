(function ($) {
    if (!$) {
        throw new Error('jQuery 插件加载失败将无法校验机器人配置，但不影响正常使用')
    }

    // 页面加载完成后绑定按钮点击事件
    $(function () {
        $('.robot-config-validate-btn').on('click', validateRobotConfig);
    })

    async function validateRobotConfig() {
        const $robot = $(this).closest('.robot-config-container');
        const $msg = $robot.find('.robot-config-validate-msg');

        $msg.empty();

        try {
            const {fieldName, value} = crumb;
            const checkUrl = `${$(this).data('validate-button-descriptor-url')}/${$(this).data('validate-button-method')}`;
            const parameters = getParameters($robot);

            const response = await fetch(checkUrl, {
                method: 'POST',
                headers: new Headers({
                    "Content-Type": "application/x-www-form-urlencoded",
                    [fieldName]: value,
                }),
                body: new URLSearchParams(parameters),
                credentials: 'include',
            });

            const message = await response.text()
            if (response.ok) {
                $msg.html(message);
                layoutUpdateCallback.call();
            } else {
                const id = `valerr${iota++}`;
                const errorMsg = `<a href="" onclick="document.getElementById('${id}').style.display='block';return false">ERROR</a><div id="${id}" style="display:none"><pre>${message}</pre></div>`;
                $msg.html(`${message}${$msg.html()}`);
            }
            Behaviour.applySubtree($msg[0]);
        } catch (error) {
            console.error(error);
        }
    }

    /**
     * 从机器人配置中获取请求参数。
     *
     * @param {jQuery} $robot - 机器人配置容器的 jQuery 对象。
     * @returns {Object} 包含所有请求参数的对象。
     */
    function getParameters($robot) {
        // 获取代理信息
        const $proxy = $('#proxyConfigContainer');
        const proxyConfig = {
            type: $proxy.find('select[name="type"]').val(), // 获取代理类型
            host: $proxy.find('input[name="host"]').val(), // 获取代理主机地址
            port: $proxy.find('input[name="port"]').val(), // 获取代理端口号
        };

        // 获取安全策略配置
        const securityPolicyConfigs = $robot.find('.security-config-container').map((_, el) => ({
            type: $(el).find('input[name="type"]').val(), // 获取策略类型
            value: $(el).find('input[name="value"]').val(), // 获取策略值
        })).get();

        // 临时解除 Array 类型的 toJSON 函数，以免序列化出错
        const toJSON = Array.prototype.toJSON;
        Array.prototype.toJSON = undefined

        // 返回所有请求参数的对象
        const result = {
            id: $robot.find('input[name="id"]').val(), // 获取机器人 ID
            name: $robot.find('input[name="name"]').val(), // 获取机器人名称
            webhook: $robot.find('input[name="webhook"]').val(), // 获取机器人 Webhook 地址
            securityPolicyConfigs: JSON.stringify(securityPolicyConfigs), // 将安全策略配置转换为 JSON 字符串
            proxy: JSON.stringify(proxyConfig), // 将代理信息转换为 JSON 字符串
        };

        // 恢复 toJSON 函数
        Array.prototype.toJSON = toJSON

        return result
    }
})(jQuery)