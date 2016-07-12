(function ($) {

    if (sakai && sakai.showSnapPoll) {
        if (sakai.snapPollTimeoutId) {
            window.clearTimeout(sakai.snapPollTimeoutId);
        }

        sakai.snapPollTimeoutId = setTimeout(function () {

                var currentPageId = document.getElementById('current-pageid').innerHTML;
                sakai.showSnapPoll('lessons', currentPageId);
            }, portal.snapPollTimeout);
    }
}) (jQuery);
