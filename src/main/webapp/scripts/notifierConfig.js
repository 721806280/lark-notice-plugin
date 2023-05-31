(function ($) {
    $(function () {
        rawShowHide($('input[name="_.raw"]').is(":checked"));
        $(document).on('change', '.notifier-config-raw input[name="_.raw"]', function (event) {
            rawShowHide(event.target.checked);
        })
    })

    function rawShowHide(checked) {
        if (checked) {
            $('.raw-content').css('display', '')
            $('.none-raw-content').css('display', 'none')
        } else {
            $('.raw-content').css('display', 'none')
            $('.none-raw-content').css('display', '')
        }
    }
})(jQuery)