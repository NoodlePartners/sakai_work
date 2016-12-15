(function () {

	profile.renderTemplate('timezone', {timezones: profile.timezones, currentTimezone: profile.currentTimezone}, 'profile-timezone-form');

    $(document).ready(function () {
        document.getElementById('profile-' + profile.currentTimezone).setAttribute('selected', true);
    });

	document.getElementById("profile-timezone-form").onsubmit = function (e) {

        e.preventDefault();

        var f = e.target,
            formData = new FormData(f),
            xhr = new XMLHttpRequest();

        xhr.open("POST", f.action);
        xhr.onreadystatechange = function () {

            if (xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
                profile.currentTimezone = formData.get('timezone');
                var newInstruction = profile.translate('instruction.timezone', profile.currentTimezone);
                $('#profile-timezone-instruction').html(newInstruction);
                var success = $('#profile-timezone-update-success');
                success.show();
                window.setTimeout(function () { success.fadeOut(); }, 1000);
            }
        };
        xhr.send(formData);
    };

}) ();
