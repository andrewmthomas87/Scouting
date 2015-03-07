
connect('Supervisor');

var currentView = 0;

$(document).ready(function() {

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
				$('section#setUpMatch a#edit, section#setUpMatch a#start').fadeIn('fast');
				var data = {};
				data.type = 'getNextMatch';
				queryServer(data, handleSupervisorServerResponses);
			});
		}, 500);
	});

	$('section#setUpMatch a#continue').click(function() {

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

});

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
			default:
				handleServerResponses([message]);
		}
	});
}
