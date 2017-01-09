(function ($) {

    var snapPoll = $('#snap-poll');
    var sendButton = $('#snap-poll-send');
    var commentBox = $('#snap-poll-comment-box');

    var reset = function () {

            snapPoll.fadeOut();
            $('.snap-poll-option').removeClass('poll-option-selected');
            sendButton.prop('disabled', true);
            commentBox.val('');
        };

    $('.snap-poll-option').click(function (e) {

        $(this).addClass('poll-option-selected').siblings().removeClass('poll-option-selected');
        sendButton.prop('disabled', false);
    });

    $('#snap-poll-ignore').click(function (e) {

        var siteId = portal.siteId;
        var tool = snapPoll.data('tool');
        var context = snapPoll.data('context');
        var url = '/direct/snap-poll/ignore?siteId=' + siteId + '&tool=' + tool + '&context=' + context;

        $.ajax({url: url, cache: false})
            .done(function (data, textStatus, jqXHR) {
            }).fail(function (jqXHR, textStatus, errorThrown) {
            });

        reset();
    });

    $('#snap-poll-send').click(function (e) {

        var response = $('.snap-poll-option.poll-option-selected').data('option');
        var tool = snapPoll.data('tool');
        var context = snapPoll.data('context');
        var reason = commentBox.val();
        var siteId = portal.siteId;

        var url = '/direct/snap-poll/submitResponse';
        var data = { response: response, siteId: siteId, tool: tool, context: context, reason: reason };

        $.ajax({url: url, cache: false, method: 'POST', data: data})
            .done(function (data, textStatus, jqXHR) {
            }).fail(function (jqXHR, textStatus, errorThrown) {
            });

        reset();
    });
}) ($PBJQ);
