(function ($) {

    portal.connectionManager = portal.connectionManager || {};

    var connectionTemplate = Handlebars.templates['connection-manager-connection'];
    var searchResultTemplate = Handlebars.templates['connection-manager-searchresult'];

    portal.i18n.loadProperties('connection-manager', '/library/translations');
    portal.i18n.loadProperties('profile-popup', '/library/translations');

    var searchUserIdFilter = [];
    var pendingTotal = 0;
    var currentTotal = 0;
    var currentConnections = [];

    var CONNECTIONS = 'connections';
    var SEARCH_RESULTS = 'searchresults';

    var currentState = CONNECTIONS;

    var shown = 0;
    var pendingTabBaseHtml = '';

    $('#Mrphs-userNav__submenuitem--connections').click(function (event) {

        $('#connection-manager').modal({
            width: 320
        });

        var connectionsView = $('#connection-manager-connectionsview');
        var searchResultsView = $('#connection-manager-searchresultsview');
        var searchResultsCount = $('#connection-manager-searchresultsview-count');

        var currentTab = $('#connection-manager-navbar-current > span > a');
        var pendingTab = $('#connection-manager-navbar-pending > span > a');

        var updateSearchResultsCount = function (count) {

                if (count === 0) {
                    searchResultsCount.html(portal.i18n.translate('connection_manager_no_results'));
                } else {
                    var translateOptions
                        = {count: count, criteria: portal.connectionManager.searchCriteria};
                    var countMessage = (count > 1)
                        ? portal.i18n.translate('connection_manager_results_count', translateOptions)
                        : portal.i18n.translate('connection_manager_result_count', translateOptions);

                    searchResultsCount.html(countMessage);
                }
            };

        var showCurrentTab = function () {

                pendingConnectionsWrapper.hide();
                currentConnectionsWrapper.show();
                connectionsView.show();
                searchResultsView.hide();
                currentTab.parent().addClass('current');
                pendingTab.parent().removeClass('current');
            };

        var showPendingTab = function () {

                currentConnectionsWrapper.hide();
                pendingConnectionsWrapper.show();
                connectionsView.show();
                searchResultsView.hide();
                pendingTab.parent().addClass('current');
                currentTab.parent().removeClass('current');
            };

		var addPendingAndShowTab = function (friendId, displayName) {

				$('#connection-manager-connection-' + friendId).remove();
				$('#connection-manager-connectionsview-searchresult-' + friendId).remove();

				if (searchResults.children().length === 0) {
					searchResultsWrapper.hide();
				}

				var markup = connectionTemplate({displayName: displayName, uuid: friendId, connected: false, hideConnect: true, outgoing: true});
				if (pendingTotal == 0) {
                    pendingConnectionsDiv.show().html('');
                    noPendingConnectionsDiv.hide();
                }
				pendingConnectionsDiv.append(markup);
				pendingTotal += 1;
				updatePendingTabText();
				searchUserIdFilter.push(friendId);

                if (currentState === CONNECTIONS) {
                    showPendingTab();
                }
				$('#connection-manager-cancel-button-' + friendId).click(cancelHandler);
			};

        currentTab.click(function (e) { showCurrentTab(); });
        pendingTab.click(function (e) { showPendingTab(); });

        var searchResultsWrapper = $('#connection-manager-connectionsview-searchresults-wrapper');
        var searchResults = $('#connection-manager-connectionsview-searchresults');
        var moreSearchResults = $('#connection-manager-searchresultsview-results');

        var currentConnectionsDiv = $('#connection-manager-current-connections');
        var currentConnectionsWrapper = $('#connection-manager-current-connections-wrapper');
        var noCurrentConnectionsDiv = $('#connection-manager-no-current-connections-wrapper');
        var pendingConnectionsDiv = $('#connection-manager-pending-connections');
        var pendingConnectionsWrapper = $('#connection-manager-pending-connections-wrapper');
        var noPendingConnectionsDiv = $('#connection-manager-no-pending-connections-wrapper');
        var searchBox = $('#connection-manager-connectionsview-searchbox');
        var moreSearchBox = $('#connection-manager-searchresultsview-searchbox');

        if (shown == 0) {
            pendingTabBaseHtml = pendingTab.html();
            shown += 1;
        }

        var connectHandler = function () {

				var friendId = this.dataset.userId;
				var displayName = this.dataset.displayName;
				if (confirm(portal.i18n.translate('connection_manager_connect_confirm', {displayName: displayName}))) {
					$.ajax('/direct/profile/' + portal.user.id + '/requestFriend?friendId=' + friendId
							, {cache: false})
						.done(function (data) {

							addPendingAndShowTab(friendId, displayName);
						})
						.fail(function (jqXHR, textStatus, errorThrown) {
							console.log('ERROR: failed to request connection to \'' + displayName + '\'. errorThrown: ' + errorThrown);
						});
				}
			};

        var removeHandler = function () {

				var friendId = this.dataset.userId;
				var displayName = this.dataset.displayName;
				if (confirm(portal.i18n.translate('connection_manager_remove_confirm', {displayName: displayName}))) {
					$.ajax('/direct/profile/' + portal.user.id + '/removeFriend?friendId=' + friendId, {cache: false})
						.done(function (data) {

							$('#connection-manager-connection-' + friendId).remove();
							currentTotal -= 1;
							if (currentTotal == 0) {
                                noCurrentConnectionsDiv.show();
							}
							var index = searchUserIdFilter.indexOf(friendId);
							searchUserIdFilter.splice(index, 1);
						})
						.fail(function (jqXHR, textStatus, errorThrown) {
							console.log('ERROR: failed to remove \'' + displayName + '\'. errorThrown: ' + errorThrown);
						});
				}
        };

        var cancelHandler = function () {

                var friendId = this.dataset.userId;
                var displayName = this.dataset.displayName;
                if (confirm(portal.i18n.translate('connection_manager_cancel_confirm', {displayName: displayName}))) {
                    $.ajax('/direct/profile/' + friendId + '/ignoreFriendRequest?friendId=' + portal.user.id, {cache: false})
                        .done(function (data) {

                            $('#connection-manager-connection-' + friendId).remove();
                            pendingTotal -= 1;
                            updatePendingTabText();
                            var index = searchUserIdFilter.indexOf(friendId);
                            searchUserIdFilter.splice(index, 1);
                        })
                        .fail(function (jqXHR, textStatus, errorThrown) {
                            console.log('ERROR: failed to ignore request from \'' + displayName + '\'. errorThrown: ' + errorThrown);
                        });
                }
            };

        var updatePendingTabText = function () {

                if (pendingTotal === 0) {
                    pendingTab.html(pendingTabBaseHtml);
                    pendingConnectionsDiv.html('').hide();
                    noPendingConnectionsDiv.show();
                } else {
                    pendingTab.html(pendingTabBaseHtml + ' (' + pendingTotal + ')');
                }
            };

        var search = function (criteria, showFullConnections) {

                var container = (showFullConnections) ? moreSearchResults : searchResults;

                if (criteria.length < 4) {
                    container.html('');
                    if (!showFullConnections) {
                        searchResultsWrapper.hide();
                    } else {
                        updateSearchResultsCount(0);
                    }
                    return;
                }

                portal.connectionManager.searchCriteria = criteria;

                var template = (showFullConnections) ? connectionTemplate : searchResultTemplate;

                $.ajax('/direct/portal/connectionsearch.json?query=' + criteria, {cache: false})
                    .done(function (results) {

                        container.html('');

                        if (results.length === 0) {
                            if (!showFullConnections) {
                                searchResultsWrapper.hide();
                            }
                            return;
                        }

                        if (!showFullConnections) {
                            searchResultsWrapper.show();
                        }

                        var markup = '';
                        portal.connectionManager.lastSearchResults
                            = results.filter(function (r) { return searchUserIdFilter.indexOf(r.uuid) == -1; });

                        if (showFullConnections) {
                            portal.connectionManager.lastSearchResults.forEach(function (result, i) {
                                    markup += template(result);
                                });
                        } else {
                            portal.connectionManager.lastSearchResults.slice(0, 5).forEach(function (result, i) {
                                    markup += template(result);
                                });
                        }

                        if (showFullConnections) {
                            updateSearchResultsCount(portal.connectionManager.lastSearchResults.length);
                        }

                        container.html(markup);

                        if (container.children().length > 0) {
                            container.show();
                        }

                        $(document).ready(function () {

                            if (!showFullConnections) {
                                profile.attachPopups($('.profile-popup'), {connect: addPendingAndShowTab});
                            } else {
                                $('.connection-manager-connect-button').click(connectHandler);
                            }

                            $('#connection-manager-connectionsview-searchresults-more').click(function (e) {

                                searchResults.html('');
                                searchResultsWrapper.hide();
                                connectionsView.hide();
                                searchResultsView.show();
                                currentState = SEARCH_RESULTS;
                                moreSearchBox.val(portal.connectionManager.searchCriteria);
                                searchBox.val('');
                                $(document).ready(function () {

                                    updateSearchResultsCount(portal.connectionManager.lastSearchResults.length);

                                    var markup = '';
                                    portal.connectionManager.lastSearchResults.forEach(function (result, i) {
                                        result.facebookSet = result.socialNetworkingInfo.facebookUrl;
                                        result.twitterSet = result.socialNetworkingInfo.twitterUrl;
                                        markup += connectionTemplate(result);
                                    });
                                    moreSearchResults.html(markup);
                                    $(document).ready(function () {
                                        $('.connection-manager-connect-button').click(connectHandler);
                                    });
                                });
                            });

                            $('#connection-manager-backtoconnections-link').click(function (e) {

                                currentState = CONNECTIONS;
                                searchResultsView.hide();
                                connectionsView.show();
                                searchResultsWrapper.hide();
                                searchResults.html('');
                                searchBox.val('');
                            });
                        }); // document.ready
                    }); // ajax call
            }; // search

        // Load up the current connections
        $.ajax('/direct/profile/' + portal.user.id + '/connections.json', {cache: false})
            .done(function (data) {

                currentTotal = data.length;

                // Reset the search filter
                searchUserIdFilter = [portal.user.id];

                currentConnectionsDiv.html('');

                currentConnections = data;
                if (currentConnections.length == 0) {
                    noCurrentConnectionsDiv.show();
                } else {
                    noCurrentConnectionsDiv.hide();
                }

                data.forEach(function (connection) {

                    connection.facebookSet = connection.socialNetworkingInfo.facebookUrl;
                    connection.twitterSet = connection.socialNetworkingInfo.twitterUrl;
                    connection.current = true;
                    connection.connected = true;
                    connection.incoming = false;
                    connection.hideConnect = true;
                    searchUserIdFilter.push(connection.uuid);
                    currentConnectionsDiv.append(connectionTemplate(connection));
                });

                $(document).ready(function () {
                    $('.connection-manager-remove-button').click(removeHandler);
                });
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.log('ERROR: failed to get current connections. errorThrown: ' + errorThrown);
            });

        var pendingConnectionsCallback = function (connections) {

                if (connections.length == 0) {
                    noPendingConnectionsDiv.show();
                    pendingConnectionsDiv.hide();
                } else {
                    noPendingConnectionsDiv.hide();
                    pendingConnectionsDiv.show().html('');
                }

                connections.forEach(function (connection) {

                    connection.hideConnect = true;
                    searchUserIdFilter.push(connection.uuid);
                    connection.pending = true;
                    pendingConnectionsDiv.append(connectionTemplate(connection));
                });

                pendingTotal = connections.length;

                if (connections.length > 0) {
                    // Update the pending tab
                    pendingTab.html(pendingTabBaseHtml + ' (' + connections.length + ')');
                } else {
                    pendingTab.html(pendingTabBaseHtml);
                }

                $(document).ready(function () {

                    $('.connection-manager-accept-button').click(function (e) {

                        var friendId = this.dataset.userId;
                        var displayName = this.dataset.displayName;
                        if (confirm(portal.i18n.translate('connection_manager_accept_confirm', {displayName: displayName}))) {
                            $.ajax('/direct/profile/' + portal.user.id + '/confirmFriendRequest?friendId=' + friendId
                                    , {cache: false})
                                .done(function (data) {

                                    $('#connection-manager-connection-' + friendId).remove();
                                    if (currentTotal == 0) {
                                        currentConnectionsDiv.html('');
                                    }
                                    var markup = connectionTemplate({displayName: displayName, uuid: friendId, connected: true, hideConnect: true});
                                    currentConnectionsDiv.append(markup);
                                    currentTotal += 1;
                                    $('#connection-manager-remove-button-' + friendId).click(removeHandler);
                                    pendingTotal -= 1;
                                    noCurrentConnectionsDiv.hide();
                                    updatePendingTabText();
                                    showCurrentTab();
                                })
                                .fail(function (jqXHR, textStatus, errorThrown) {
                                    console.log('ERROR: failed to confirm request from \'' + displayName + '\'. errorThrown: ' + errorThrown);
                                });
                        }
                    });
                    $('.connection-manager-ignore-button').click(function (e) {

                        var friendId = this.dataset.userId;
                        var displayName = this.dataset.displayName;
                        if (confirm(portal.i18n.translate('connection_manager_ignore_confirm', {displayName: displayName}))) {
                            $.ajax('/direct/profile/' + portal.user.id + '/ignoreFriendRequest?friendId=' + friendId, {cache: false})
                                .done(function (data) {

                                    $('#connection-manager-connection-' + friendId).remove();
                                    pendingTotal -= 1;
                                    updatePendingTabText();
                                    var index = searchUserIdFilter.indexOf(friendId);
                                    searchUserIdFilter.splice(index, 1);
                                })
                                .fail(function (jqXHR, textStatus, errorThrown) {
                                    console.log('ERROR: failed to ignore request from \'' + displayName + '\'. errorThrown: ' + errorThrown);
                                });
                        }
                    });

                    $('.connection-manager-cancel-button').click(cancelHandler);
                }); // document.ready
            }; // pendingConnectionsCallback

        // Load up the pending connections
        $.ajax('/direct/profile/' + portal.user.id + '/incomingConnectionRequests.json', {cache: false})
            .done(function (data) {

                data.forEach(function (connection) { connection.incoming = true; });

                $.ajax('/direct/profile/' + portal.user.id + '/outgoingConnectionRequests.json', {cache: false})
                    .done(function (outgoing) {

                        outgoing.forEach(function (connection) {

                            connection.outgoing = true;
                            data.push(connection);
                        });
                        pendingConnectionsCallback(data);
                    })
                    .fail(function (jqXHR, textStatus, errorThrown) {
                        console.log('Failed to get outgoing requests. errorThrown: ' + errorThrown);
                    });
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.log('Failed to get incoming requests. errorThrown: ' + errorThrown);
            });

        searchBox.keyup(function (e) { search(this.value, false); });
        moreSearchBox.keyup(function (e) { search(this.value, true); });
    }); // #Mrphs-userNav__submenuitem--connections.click
}) ($PBJQ);
