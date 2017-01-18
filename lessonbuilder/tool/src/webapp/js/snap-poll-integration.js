(function ($) {

    if (sakai && sakai.showSnapPoll) {
        if (sakai.snapPollTimeoutId) {
            window.clearTimeout(sakai.snapPollTimeoutId);
        }

        // Set up youtube iframe API, see:
        // http://www.htmlgoodies.com/beyond/video/respond-to-embedded-youtube-video-events.html
        // https://developers.google.com/youtube/iframe_api_reference
		// http://stackoverflow.com/questions/10259216/youtube-api-css-control-with-more-than-one-player
        // This bit is taken from the Google docs...
        var tag = document.createElement('script');
        tag.src = "https://www.youtube.com/iframe_api";
        var firstScriptTag = document.getElementsByTagName('script')[0];
        firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

        jQuery(tag).load(function(event) {
            sakai.page_players = {};
            var setIt = function(event, value) {
                sakai.page_players[event.target.a.id] = value;
            }
            var onReady = function(event) {
                setIt(event, YT.PlayerState.UNSTARTED);
            };
            var onStateChange = function(event) {
                setIt(event, event.data);
            };
            jQuery("iframe.playerwidth").each(function(index){
                var theUrl = this.src;
                // Only do this stuff for iframes embedding youtube vids.
                if(theUrl.indexOf("youtube.com") >= 0) {
                    // First, we need to add something to the queryString
                    // to enable the JS API
                    var queryString = "enablejsapi=1";
                    var sep = "?";
                    if(theUrl.indexOf("?") >= 0) {
                        sep = "&";
                    }
                    if(theUrl.indexOf(queryString) < 0) {
                        theUrl += sep + queryString;
                    }
                    // Switch the src url
                    this.src = theUrl;
                    // and we must wait for it to load.
                    $(this).load(function(event) {
                        new YT.Player(this, {
                            'events':{
                                'onReady': onReady,
                                'onStateChange': onStateChange
                            }
                        });
                    });
                }
            });
        });
        
        sakai.snapPollTimeoutId = setTimeout(function () {

                var currentPageId = document.getElementById('current-pageid').innerHTML;
                sakai.showSnapPoll('lessons', currentPageId);
            }, portal.snapPollTimeout);
    }
}) (jQuery);
