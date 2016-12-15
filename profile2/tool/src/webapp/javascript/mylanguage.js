(function () {

	profile.renderTemplate('language', {locales: profile.locales, currentLocaleDisplay: profile.currentLocaleDisplay}, 'profile-language-form');

    $(document).ready(function () {
        document.getElementById('profile-' + profile.currentLanguage).setAttribute('selected', true);
    });

	document.getElementById("profile-language-form").onsubmit = function (e) {

        e.preventDefault();

        var f = e.target,
            formData = new FormData(f),
            xhr = new XMLHttpRequest();

        xhr.open("POST", f.action);
        xhr.onreadystatechange = function () {

            if (xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
                var success = $('#profile-language-update-success');
                success.show();
                window.setTimeout(function () {

                    success.fadeOut();
                    window.location = profile.languageTabUrl;
                }, 1000);
            }
        };
        xhr.send(formData);
    };

}) ();
