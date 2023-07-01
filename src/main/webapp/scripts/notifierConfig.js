(function () {
    // 当页面内容加载完成时执行以下操作
    document.addEventListener('DOMContentLoaded', function () {
        // 获取复选框和原始内容元素以及非原始内容元素
        var checkbox = document.querySelector('.notifier-config-raw input[name="_.raw"]');
        var rawContent = document.querySelector('.raw-content');
        var noneRawContent = document.querySelector('.none-raw-content');

        // 首次进入页面执行一次更新显示样式
        updateDisplayStyle(checkbox.checked);

        // 监听复选框的改变事件
        checkbox.addEventListener('change', function (event) {
            updateDisplayStyle(event.target.checked);
        });

        // 定义函数用于更新显示样式
        function updateDisplayStyle(isChecked) {
            // 根据选中状态设置原始内容元素和非原始内容元素的显示样式
            rawContent.style.display = isChecked ? '' : 'none';
            noneRawContent.style.display = isChecked ? 'none' : '';
        }
    });
})();