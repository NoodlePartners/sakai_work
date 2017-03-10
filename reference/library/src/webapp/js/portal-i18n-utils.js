(function ($) {

    portal.i18n = portal.i18n || {};
    portal.i18n.translations = portal.i18n.translations || {};

    portal.i18n.loadProperties = function (name, path, debug) {

        if (!path.match(/\/$/)) path += '/';

        $.i18n.properties({
            name: name,
            path: path,
            mode: 'both',
            async: true,
            debug: debug ? true:false,
            language: portal.locale,
            callback: function () {

                $.extend(portal.i18n.translations, $.i18n.map);
                if (debug) {
                    console.log('Updated translations: ');
                    console.log(portal.i18n.translations);
                }
            }
        });
    };

    portal.i18n.translate = function (key, options) {

        var ret = portal.i18n.translations[key];

        if (!ret) {
            console.log('key ' + key + ' not found. Returning key ...');
            return key;
        }

        if (options != undefined) {
             for (var prop in options) {
                 ret = ret.replace('{'+prop+'}', options[prop]);
             }
        }
        return ret;
    };

	Handlebars.registerHelper('translate', function (key, options) {
        return new Handlebars.SafeString(portal.i18n.translate(key, options.hash));
    });
}) ($PBJQ);
