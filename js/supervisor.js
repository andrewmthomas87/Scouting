
connect('Supervisor', handleServerResponses);

var currentView = 0;

var matchStarted = false;

var matchNumber;

function resize() {
	$('form#confirmMatchReset').css('top', 'calc(' + ($(window).height() / 2) + 'px - 3.8125em)');
}

$(window).resize(resize);

$(document).ready(function() {
	resize();

	$('section#setUpMatch input').prop('disabled', true);

	$('nav a#match').click(function() {
		if (currentView != 1 && currentView != -1) {
			$('section:visible').fadeOut('fast');
			var data = {};
			data.type = 'getNextMatch';
			queryServer(data, handleSupervisorServerResponses);
			currentView = -1;
		}
	});

	$('nav a#devices').click(function() {
		if (currentView != 2 && currentView != -1) {
			$('section#manageDevices div').remove();
			var data = {};
			data.type = 'getClients';
			queryServer(data, handleSupervisorServerResponses);
			currentView = -1;
		}
	});

	$('section#setUpMatch a#edit').click(function() {
		$('section#setUpMatch a#edit, section#setUpMatch a#continue').fadeOut('fast', function() {
			$('section#setUpMatch a#update').fadeIn('fast', function() {
				$('section#setUpMatch div input').prop('disabled', false);
			});
		});
	});

	$('section#setUpMatch a#update').click(function() {
		var data = {};
		data.type = 'setMatchData';
		data.matchNumber = parseInt($('section#setUpMatch input#matchNumber').val());
		$('section#setUpMatch div input').each(function() {
			data[$(this).attr('id')] = parseInt($(this).val());
		});
		queryServer(data, handleServerResponses);
		setTimeout(function() {
			$('section#setUpMatch input').prop('disabled', true);
			$('section#setUpMatch a#update').fadeOut('fast', function() {
				$('section#setUpMatch a#edit, section#setUpMatch a#continue').fadeIn('fast');
				var data = {};
				data.type = 'getNextMatch';
				queryServer(data, handleSupervisorServerResponses);
			});
		}, 500);
	});

	$('section#setUpMatch a#continue').click(function() {
		currentView = 3;
		$('section#setUpMatch').fadeOut('fast', function() {
			$('section#startMatch').fadeIn('fast');
		});
	});

	$('section#manageDevices').delegate('div a', 'click', function() {
		var data = {};
		data.type = 'disconnectClient';
		data.disconnectCID = parseInt($(this).parent().attr('CID'));
		queryServer(data, handleServerResponses);
		$(this).parent().fadeOut('fast', function() {
			$(this).remove();
		});
	});

	$('section#manageDevices>a').click(function() {
		$('section#manageDevices div').remove();
		var data = {};
		data.type = 'getClients';
		queryServer(data, handleSupervisorServerResponses);
		currentView = -1;
	});

	$('section#startMatch a').click(function() {
		var data = {};
		data.type = 'matchStarted';
		queryServer(data, handleSupervisorServerResponses);
		currentView = -1;
	});

	$('a#matchEnded').click(function() {
		$(this).fadeOut('fast');
		$('div#red, div#blue').fadeOut('fast');
		var data = {};
		data.type = 'matchEnded';
		queryServer(data, handleSupervisorServerResponses);
	});

	$('a#matchReset').click(function() {
		$('div#overlay, form#confirmMatchReset').fadeIn('fast');
	});

	$('a#cancel').click(function() {
		$('div#overlay, form#confirmMatchReset').fadeOut('fast');
	});

	$('a#reset').click(function() {
		var data = {};
		data.type = 'matchReset';
		data.matchNumber = matchNumber;
		queryServer(data, handleSupervisorServerResponses);
	});

});

function wazUp() {
	var data = {};
	data.type = 'wazUp';
	queryServer(data, handleSupervisorServerResponses);
}

function handleSupervisorServerResponses(data) {
	data.forEach(function(message) {
		switch (message.type) {
			case 'nextMatchData':
				$('section#setUpMatch input#matchNumber').val(message.matchNumber);
				$('section#setUpMatch input#redTeam1').val(message.redTeam1);
				$('section#setUpMatch input#redTeam2').val(message.redTeam2);
				$('section#setUpMatch input#redTeam3').val(message.redTeam3);
				$('section#setUpMatch input#blueTeam1').val(message.blueTeam1);
				$('section#setUpMatch input#blueTeam2').val(message.blueTeam2);
				$('section#setUpMatch input#blueTeam3').val(message.blueTeam3);
				$('section:visible').hide();
				$('section#setUpMatch').fadeIn('fast');
				matchNumber = message.matchNumber;
				currentView = 1;
				break;
			case 'connectedClients':
				var clientDiv;
				message.clients.forEach(function(client) {
					if (CID != client.CID) {
						clientDiv = $('<div></div>');
						clientDiv.attr('CID', client.CID);
						clientDiv.append('<span>' + client.scoutName + '</span>');
						if (client.teamNumber != -1) {
							clientDiv.append('<span>' + client.teamNumber + '</span>');
						}
						clientDiv.append('<a>&#x2573;</a>');
						$('section#manageDevices>a').before(clientDiv);
					}
				});
				$('section:visible').hide();
				$('section#manageDevices').fadeIn('fast');
				currentView = 2;
				break;
			case 'matchStarted':
				$('nav, section#startMatch').fadeOut('fast', function() {
					$('div#red, div#blue, a#matchEnded, a#matchReset').fadeIn('fast');
				});
				matchStarted = true;
				setTimeout(wazUp, updateSpeed);
				break;
			case 'status':
				if (message.status == 'wazUp' && matchStarted) {
					setTimeout(wazUp, updateSpeed);
				}
				else if (message.status == 'matchEnded') {
					$('div.stack, div.spacer').not('.initial').fadeOut('fast', function() {
						$(this).remove();
					});
					$('div#red, div#blue, a#matchEnded, a#matchReset').fadeOut('fast');
					$('nav').fadeIn('fast');
					matchStarted = false;
					matchNumber = null;
					currentView = 0;
					var data = {};
					data.type = 'getNextMatch';
					queryServer(data, handleSupervisorServerResponses);
				}
				else if (message.status == 'matchReset') {
					$('div#overlay, form#confirmMatchReset').fadeOut('fast');
					$('div.stack, div.spacer').not('.initial').fadeOut('fast', function() {
						$(this).remove();
					});
					$('div#red, div#blue, a#matchEnded, a#matchReset').fadeOut('fast');
					$('nav').fadeIn('fast');
					matchStarted = false;
					matchNumber = null;
					currentView = 0;
					var data = {};
					data.type = 'getNextMatch';
					queryServer(data, handleSupervisorServerResponses);
				}
				else {
					handleServerResponses([message]);
				}
				break;
			case 'contribution':
				var alliance = message.alliance;
				var SID = message.SID;
				var contributor = message.teamNumber;
				var objects = message.objects.split(',');
				var stackExists = $('div#' + alliance + ' div#' + SID).length > 0;
				if (stackExists) {
					if (objects[0].charAt(0) == 'K') {
						var originSID = parseInt(objects[0].substring(1));
						var objects = [];
						$('div#' + alliance + ' div#' + originSID + ' div div').each(function() {
							objects.push($(this).attr('class'));
						});
						$('div#' + alliance + ' div#' + originSID).next('div.spacer').remove();
						$('div#' + alliance + ' div#' + originSID).remove();
					}
					var stack = $('div#' + alliance + ' div#' + SID + '>div');
					var objectType;
					var object;
					for (i = 0; i < objects.length; i++) {
						objectType = objects[objects.length - 1 - i];
						object = '<div style="display: none" class="' + objectType + '" teamNumber="' + contributor + '"></div>';
						if (objectType == 'P') {
							stack.append(object);
						}
						else if (!stack.find('div.B').length > 0 || objectType == 'L') {
							stack.prepend(object);
						}
						else if (stack.find('div').length > 1) {
							stack.find('div').not('.P').last().after('<div class="' + objects[i] + '" teamNumber="' + contributor + '"></div>');
						}
						else {
							stack.append(object);
						}
					}
					stack.find('div').not(':visible').fadeIn('fast');
				}
				else {
					var stackContainer = $('<div class="stack" id="' + SID + '"></div>');
					var stack = $('<div draggable="true"></div>');
					var object;
					for (i = 0; i < objects.length; i++) {
						object = $('<div style="display: none" class="' + objects[i] + '" teamNumber="' + contributor + '"></div>');
						stack.append(object);
					}
					stackContainer.append(stack);
					$('div#' + alliance).append(stackContainer);
					$('div#' + alliance).append('<div class="spacer"></div>');
					stack.find('div').fadeIn('fast');
				}
				break;
			default:
				handleServerResponses([message]);
		}
	});
}
