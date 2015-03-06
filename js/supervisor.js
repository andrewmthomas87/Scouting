
connect('Supervisor');

$(document).ready(function() {

	setTimeout(function() {
		var data = {};
		data.type = 'getNextMatch';
		queryServer(data, handleSupervisorServerResponses);
	}, 1000);
	$('section#setUpMatch input').prop('disabled', true);

	$('section#setUpMatch a#edit').click(function() {
		$('section#setUpMatch a#edit, section#setUpMatch a#start').fadeOut('fast', function() {
			$('section#setUpMatch a#update').fadeIn('fast', function() {
				$('section#setUpMatch input').prop('disabled', false);
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
				$('section#setUpMatch').fadeIn('slow');
				break;
			default:
				handleServerResponses([message]);
		}
	});
}
