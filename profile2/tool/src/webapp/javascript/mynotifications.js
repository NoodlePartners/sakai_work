(function () {

    var overrideOptions = profile.registrations[0].options;

    Handlebars.registerPartial('override', Handlebars.partials['override']);

    var overrideRE = /(.*)_override$/;

    var currentOverrides = {};

    // We need the list of site overrides before rendering
    Object.keys(profile.currentPreferences).forEach(function (tool) {

        var result = tool.match(overrideRE);
        if (result) {
            currentOverrides[result[1]] = profile.currentPreferences[tool];
        }
    });

    profile.escapeTool = function (tool) {
        return tool.replace(':', "\\:");
    };

    // Add the current overrides and the available options to each registration. Makes the
    // template simpler.
    profile.registrations.forEach(function (r) {

        r.overrides = currentOverrides[r.tool];
        if (r.overrides) {
            r.overrides.forEach(function (o) {
                o.tool = r.tool;
                o.options = overrideOptions;
            });
        }
    });

    // Render the main notifications template
    profile.renderTemplate('notifications', {
                registrations: profile.registrations,
                currentOverrides: currentOverrides,
                overrideOptions: overrideOptions }, 'profile-notifications-form');

    $(document).ready(function () {

        // Check the currently overridden sites in the qtip popup
        Object.keys(currentOverrides).forEach(function (tool) {

            currentOverrides[tool].forEach(function (override) {
                $('#profile-overridable-' + profile.escapeTool(tool) + '-' + override.siteId).prop('checked', true);
            });
        });

        var overrideUpdateHandler = function (e) {

            var tool = e.target.dataset.tool;
            var escapedTool = profile.escapeTool(tool);

            $('.profile-overriding-site-' + escapedTool).each(function (index, site) {

                var row = $('#profile-override-' + escapedTool + '-' + site.dataset.siteId);
                if (site.checked) {
                    if (row.length == 0) {
                        var template = Handlebars.templates['override'];
                        var newRow = template({
                                        tool: tool,
                                        siteId: site.dataset.siteId,
                                        siteTitle: site.dataset.siteTitle,
                                        options: overrideOptions});
                        $('#profile-notification-overrides-table-for-' + escapedTool).append(newRow);
                    }
                } else {
                    if (row.length == 1) {
                        row.remove();
                    }
                }
            });

            $('#profile-addsiteoverride-link-' + escapedTool).qtip('api').hide();
        };

        $('.profile-notifications-title-button').click(function (e) {
            $('#profile-notifications-block-' + profile.escapeTool(e.target.dataset.tool)).toggle();
        });

        $('.profile-addsiteoverride-link').qtip({
            content: {
                text: 'None'
            },
            show: {
                event: 'click'
            },
            hide: {
                event: 'click unfocus'
            },
            style: {
                classes: 'profile-qtip qtip-shadow'
            },
            events: {
                show: function (event, api) {

                    var tool = profile.escapeTool(event.originalEvent.target.dataset.tool);
                    api.set('content.text', $('#profile-sitepicker-for-' + tool));
                    $('#profile-sitepicker-donebutton-' + tool).click(overrideUpdateHandler);
                    $('#profile-sitepicker-cancelbutton-' + tool).click(function (e) { api.hide(); });
                }
            }
        });
    }); //document.ready

    Object.keys(profile.currentPreferences).forEach(function (tool) {

        var preference = profile.currentPreferences[tool];
        if (tool.indexOf('_override', tool.length - 9) == -1) {
            $('#' + profile.escapeTool(tool) + '-' + preference).prop('checked', true);
        } else {
            preference.forEach(function (p) {
                $('#' + profile.escapeTool(tool) + '-' + p.siteId + '-' + p.setting).attr('selected', 'true');
            });
        }
    });

    document.getElementById("profile-notifications-form").onsubmit = function (e) {

        e.preventDefault();

        var f = e.target,
            formData = new FormData(f),
            xhr = new XMLHttpRequest();

        xhr.open("POST", f.action);
        xhr.onreadystatechange = function () {

            if (xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
                var success = $('#profile-notifications-update-success');
                success.show();
                window.setTimeout(function () { success.fadeOut(); }, 1000);
            }
        };
        xhr.send(formData);
    };
}) ();
