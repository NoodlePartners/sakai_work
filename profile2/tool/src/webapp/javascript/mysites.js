(function () {

    var render = function (terms, currentPrefs) {

            profile.renderTemplate('sites', {terms: terms}, 'profile-sites-form');

            $(document).ready(function () {

                $('.profile-site-checkbox').click(function (e) {

                    if (this.checked) {
                        $('#profile-sitestab-site-' + this.dataset.siteId).addClass('profile-sitestab-site-checked');
                        if ($('.profile-site-checkbox-term-' + this.dataset.term + ':not(:checked)').length == 0) {
                            $('#profile-term-checkbox-' + this.dataset.term).prop('checked', true);
                        }
                    } else {
                        $('#profile-sitestab-site-' + this.dataset.siteId).removeClass('profile-sitestab-site-checked');
                        $('#profile-term-checkbox-' + this.dataset.term).prop('checked', false);
                    }
                });

                $('.profile-term-checkbox').click(function (e) {

                    if (this.checked) {
                        $('.profile-site-checkbox-term-' + this.dataset.term).prop('checked', true);
                        $('.profile-sitestab-site-term-' + this.dataset.term).addClass('profile-sitestab-site-checked');
                    } else {
                        $('.profile-site-checkbox-term-' + this.dataset.term).prop('checked', false);
                        $('.profile-sitestab-site-term-' + this.dataset.term).removeClass('profile-sitestab-site-checked');
                    }
                });

                if (currentPrefs.exclude) {
                    if (Array.isArray(currentPrefs.exclude)) {
                        currentPrefs.exclude.forEach(function (siteId) {
                            $('#profile-site-checkbox-' + siteId).click();
                        });
                    } else {
                        $('#profile-site-' + currentPrefs.exclude).click();
                    }
                }

                if (currentPrefs['tab:label']) {
                    $('#profile-tab-label-' + currentPrefs['tab:label']).prop('checked', true);
                }

                document.getElementById("profile-sites-form").onsubmit = function (e) {

                    e.preventDefault();

                    var f = e.target,
                        formData = new FormData(f),
                        xhr = new XMLHttpRequest();

                    xhr.open("POST", f.action);
                    xhr.onreadystatechange = function () {

                        if (xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
                            var success = $('#profile-sites-update-success');
                            success.show();
                            window.setTimeout(function () { success.fadeOut(); }, 1000);
                        }
                    };
                    xhr.send(formData);
                };
            });
        }; // render

    var coursesWithNoTerm = profile.i18n['sites.courseswithnoterm'];
    var other = profile.i18n['sites.other'];

    $.get('/direct/site.json', {cache: false})
        .done(function (sitesData) {

            $.get('/direct/userPrefs/key/' + portal.user.id + '/sakai:portal:sitenav.json', {cache: false})
                .done(function (prefsData) {

                    var unsortedSites = sitesData['site_collection'];

                    var termsByName = {};
                    unsortedSites.forEach(function (site) {

                        if (site.props && site.props.term) {
                            if (!termsByName[site.props.term]) termsByName[site.props.term] = {name: site.props.term, sites: []};
                            termsByName[site.props.term].sites.push(site);
                        } else {
                            if (site.type === 'course') {
                                if (!termsByName[coursesWithNoTerm]) termsByName[coursesWithNoTerm] = {name: coursesWithNoTerm, sites: []};
                                termsByName[coursesWithNoTerm].sites.push(site);
                            } else if (site.type === null) {
                                if (!termsByName[other]) termsByName[other] = {name: other, sites: []};
                                termsByName[other].sites.push(site);
                            } else {
                                var type = site.type.toUpperCase();
                                if (!termsByName[type]) termsByName[type] = {name: type, sites: []};
                                termsByName[type].sites.push(site);
                            }
                        }
                    });

                    var terms = Object.keys(termsByName).map(function (termName) {

                            termsByName[termName].safeName = termName.replace(/\s/g, '');
                            return termsByName[termName]
                        });
                    render(terms, prefsData.data);
                })
                .fail(function () {
                    console.log("FAILED TO GET USER PREFS");
                });
        })
        .fail(function () {
            console.log("FAILED TO GET USER SITES");
        });

}) ();
