(function ($) {

    var snapPoll = $('#snap-poll');
    var sendButton = $('#snap-poll-send');
    var commentBox = $('#snap-poll-comment-box');
    var comment = $('#snap-poll-comment');

    var reset = function () {

        snapPoll.fadeOut();
        comment.hide();
        $('.snap-poll-option').removeClass('poll-option-selected');
        sendButton.prop('disabled', true);
        commentBox.val('');
    };

    var sendIt = function() {
        var response = parseInt($('.snap-poll-option.poll-option-selected').data('option'),10);
        var reason = "";
        if (response<4) {
            reason = commentBox.val();
        }
        if (!reason || reason == "") {reason = "-";}
        var tool = snapPoll.data('tool');
        var context = snapPoll.data('context');
        var siteId = portal.siteId;

        var url = '/direct/snap-poll/submitResponse';
        var data = { response: response, siteId: siteId, tool: tool, context: context, reason: reason };

        $.ajax({url: url, cache: false, method: 'POST', data: data})
            .done(function (data, textStatus, jqXHR) {
            }).fail(function (jqXHR, textStatus, errorThrown) {
            });

        reset();
    };

    $('.snap-poll-option').click(function (e) {

        $(this).addClass('poll-option-selected').siblings().removeClass('poll-option-selected');
        sendButton.prop('disabled', false);
        if (parseInt($(this).data("option"),10)<4) {
            comment.show();
        } else {
            comment.hide();
            commentBox.val('');
            sendIt();
        }
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
        sendIt();
    });
}) ($PBJQ);
