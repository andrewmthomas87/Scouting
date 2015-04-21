
var connectionRefreshInterval = 1000;

var connections = {};

var matchNumber;

var outboundMessages = [];

var matchStarted = false;

function reset() {
	matchNumber = null;
	matchStarted = false;
	$('section#matchManager').find('div#buttons, div#field, div#step').fadeOut('fast', function() {
		$('div#field div div').remove();
		$('div#step').find('div.bin').removeClass('raked');
		$('div#step').find('div#cooperitition').attr('number', '0');
		getMatch();
	});
}

$(document).ready(function() {
	var data = constructMessage('login');
	data['scoutName'] = 'Supervisor';
	queryServer(data, handleServerResponses);

	$('a#edit').click(function() {
		if ($(this).hasClass('save')) {
			var invalid = false;
			$(this).parent().find('input.inline').each(function() {
				invalid = invalid || $(this).val() == '';
			});
			if (!invalid) {
				var data = constructMessage('setMatch', CID);
				$(this).parent().find('input').each(function() {
					data[$(this).attr('id')] = parseInt($(this).val());
				});
				data['MID'] = outboundMessages.length;
				queryServer(data, handleServerResponses);
				outboundMessages.push('setMatch');
			}
		}
		else {
			$('section#matchManager').find('div input.inline').prop('disabled', false);
			$(this).addClass('save');
			$(this).parent().find('input.inline').eq(0).focus();
			$(this).find('span').fadeOut('fast', function() {
				$(this).html('Save');
				$(this).fadeIn('fast');
			});
		}
	});

	$('a#startMatch').click(function() {
		var data = constructMessage('startMatch', CID);
		queryServer(data, handleServerResponses);
	});

	$('a#reset').click(function() {
		var data = constructMessage('resetMatch', CID);
		data['matchNumber'] = matchNumber;
		queryServer(data, handleServerResponses);
	});

	$('a#end').click(function() {
		var data = constructMessage('endMatch', CID);
		queryServer(data, handleServerResponses);
	});

	$('section#connectionsManager').delegate('div div.remove', 'click', function() {
		var disconnectCID = parseInt($(this).parent().attr('cid'));
		var data = constructMessage('disconnectConnection', CID);
		data['disconnectCID'] = disconnectCID;
		data['MID'] = outboundMessages.length;
		queryServer(data, handleServerResponses);
		outboundMessages.push(disconnectCID);
	});

});

function getMatch() {
	var data = constructMessage('getMatch', CID);
	queryServer(data, handleServerResponses);
	if (!($('a#edit').hasClass('save') || matchStarted)) {
		setTimeout(getMatch, 1000);
	}
}

function getConnections() {
	var data = constructMessage('getConnections', CID);
	queryServer(data, handleServerResponses);
	setTimeout(getConnections, connectionRefreshInterval);
}

function handleServerResponses(data) {
	data.forEach(function(message) {
		switch (message.type) {
			case 'login':
				CID = message.CID;
				getMatch();
				getConnections();
				break;
			case 'connections':
				message.connections.forEach(function(connection) {
					if (connection.CID != CID) {
						var identifier = connection.CID + connection.scoutName;
						if (connections[identifier]) {
							if (connections[identifier] != connection.teamNumber) {
								connections[identifier] = connection.teamNumber;
								var connectionDiv = $('section#connectionsManager').find('div[cid="' + connection.CID + '"]');
								connectionDiv.hide('fast', function() {
									var teamNumber = connections[$(this).attr('cid') + $(this).find('div span:first-child').html()];
									$(this).detach();
									if (teamNumber > -1) {
										$(this).find('div span:last-child').html(teamNumber);
										$('section#connectionsManager').prepend(this);
									}
									else {
										$(this).find('div span:last-child').html('');
										$('section#connectionsManager').append(this);
									}
									$(this).show('fast');
								});
							}
						}
						else {
							connections[identifier] = connection.teamNumber;
							var connectionDiv;
							if (connection.teamNumber > -1) {
								connectionDiv = $('<div style="display: none" cid="' + connection.CID + '"><div class="connection"><span>' + connection.scoutName + '</span><span>' + connection.teamNumber + '</span></div><div class="remove"></div></div>');
								$('section#connectionsManager').prepend(connectionDiv);
							}
							else {
								connectionDiv = $('<div style="display: none" cid="' + connection.CID + '"><div class="connection"><span>' + connection.scoutName + '</span><span></span></div><div class="remove"></div></div>');
								$('section#connectionsManager').append(connectionDiv);
							}
							connectionDiv.show('fast');
						}
					}
				});
				break;
			case 'matchData':
				if (!matchStarted) {
					if (!$('a#edit').hasClass('save')) {
						$('input#matchNumber').val(message.matchNumber);
						for (i = 1; i < 4; i++) {
							$('input#redTeam' + i).val(message['redTeam' + i]);
							$('input#blueTeam' + i).val(message['blueTeam' + i]);
						}
						$('section#matchManager').find('div#setUpMatch').fadeIn('fast');
					}
				}
				break;
			case 'matchStarted':
				matchStarted = true;
				matchNumber = message.matchNumber;
				$('section#matchManager').find('div#setUpMatch').fadeOut('fast');
				$('section#matchManager').find('div#buttons, div#field, div#step').fadeIn('fast');
				break;
			case 'contribution':
				if (message.matchNumber == matchNumber) {
					var SID = message.SID;
					if (message.objects == 'P') {
						var stack = $('div#' + SID);
						stack.hide();
						stack.next('div.spacer').remove();
						stack.detach();
						stack.addClass('scored');
						$('div#' + message.alliance).prepend(stack);
						stack.show();
					}
					else if (message.objects.charAt(0) == 'S') {
						$('div#' + SID).next('div.spacer').remove();
						$('div#' + SID).remove();
						var numberOfYellowTotes = parseInt(message.objects.substr(1));
						$('div#step').find('div#cooperitition').attr('number', parseInt($('div#step').find('div#cooperitition').attr('number')) + numberOfYellowTotes);
					}
					else {
						var contributor = message.teamNumber;
						var objects = message.objects.split(',');
						if (objects[0].charAt(0) == 'X') {
							$('div#' + SID + ', div#' + SID + '+div.spacer').remove();
						}
						else if (objects[0].charAt(0) == 'K' || SID > 0 || contributor == teamNumber) {
							var stackExists = $('div#' + SID).length > 0;
							if (stackExists) {
								if (objects[0].charAt(0) == 'K') {
									var originSID = parseInt(objects[0].substr(1));
									var objects = [];
									if (SID > 0) {
										$('div#' + originSID + ' div div').each(function() {
											objects.push($(this).attr('class'));
										});
										objects.reverse();
									}
									$('div#' + originSID).next('div.spacer').remove();
									$('div#' + originSID).remove();
								}
								var stack = $('div#' + SID + '>div');
								var objectType;
								var object;
								for (i = 0; i < objects.length; i++) {
									objectType = objects[i];
									object = '<div class="' + objectType + '" teamNumber="' + contributor + '"></div>';
									if (objectType == 'B' && stack.find('div.L').length > 0) {
										stack.find('div.L').after(object);
									}
									else if (stack.find('div.B').length == 0 || objectType == 'L') {
										stack.prepend(object);
									}
									else {
										stack.append('<div class="' + objects[objects.length - i - 1] + '" teamNumber="' + contributor + '"></div>');
									}
								}
							}
							else {
								var stackContainer = $('<div class="stack" id="' + SID + '"></div>');
								var stack = $('<div draggable="true"></div>');
								var objectDivs = '';
								for (i = 0; i < objects.length; i++) {
									objectDivs += '<div class="' + objects[objects.length - 1 - i] + '" teamNumber="' + contributor + '"></div>';
								}
								stack.append(objectDivs);
								stackContainer.append(stack);
								$('div#' + message.alliance).append(stackContainer);
								$('div#' + message.alliance).append('<div class="spacer"></div>');
							}
						}
					}
				}
				break;
			case 'robotEvent':
				switch (message.eventType) {
					case 'R':
					case 'S':
						var index = parseInt(message.comments);
						$('div#step').find('div.bin').eq(index - 1).addClass('raked');
						break;
					default:

				}
				break;
			case 'status':
				switch (message.status) {
					case 'noMatch':
						break;
					case 'disconnected':
						$('body').fadeOut('slow', function() {
							window.location = 'disconnected.html';
						});
						break;
					case 'matchReset':
					case 'matchEnded':
						reset();
						break;
					case 'ok':
						if (outboundMessages[message.MID] == 'setMatch') {
							$('section#matchManager').find('div input.inline').prop('disabled', true);
							$('a#edit').removeClass('save');
							$('a#edit').find('span').fadeOut('fast', function() {
								$(this).html('Edit');
								$(this).fadeIn('fast');
							});
							setTimeout(getMatch, 1000);
						}
						else {
							var connectionDiv = $('section#connectionsManager').find('div[cid="' + outboundMessages[message.MID] + '"]');
							delete connections[outboundMessages[message.MID] + connectionDiv.find('div span:first-child').html()];
							connectionDiv.hide('fast', function() {
								$(this).remove();
							});
						}
						outboundMessages.splice(message.MID, 1);
						break;
					default:

				}
				break;
			default:

		}
	});
}
