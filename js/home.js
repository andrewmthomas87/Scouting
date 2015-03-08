
var currentView = 0;

var matchNumber, teamNumber;

var drag = 0;

var matchStarted;

var autonomous = true;

var date;
var startTime;

function resize() {
	$('div#loading').css('top', 'calc(' + ($(window).height() / 2) + 'px - 1em)');
	$('form#login').css('top', 'calc(' + ($(window).height() / 2) + 'px - 4.375em)');
	$('form#readyForNextMatch').css('top', 'calc(' + ($(window).height() / 2) + 'px - 1.875em)');
	$('form#ready').css ('top', 'calc(' + ($(window).height() / 2) + 'px - 5.3125em)');
}

$(window).resize(resize);

$(document).ready(function() {
	resize();
	$('div#overlay, form#login').fadeIn('fast');


	// Nav events

	$('nav a#global-view').click(function() {
		if (currentView != 0) {
			$('nav a.active').removeClass('active');
			$(this).addClass('active');
			$('h1#header').fadeOut(125, function() {
				$(this).html('Global');
				$(this).fadeIn(125);
			});
			if (currentView == 2) {
				$('div section').css('width', '100%');
			}
			else {
				$('div section').css('left', '0');
			}
			currentView = 0;
		}
	});
	$('nav a#local-view').click(function() {
		if (currentView != 1) {
			$('h1#header').fadeOut(125, function() {
				$(this).html('Local');
				$(this).fadeIn(125);
			});
			$('nav a.active').removeClass('active');
			$(this).addClass('active');
			if (currentView == 2) {
				$('div section').css('width', '100%');
			}
			$('div section').css('left', '-100%');
			currentView = 1;
		}
	});
	$('nav a#split-view').click(function() {
		if (currentView != 2) {
			$('h1#header').fadeOut(125, function() {
				$(this).html('Split');
				$(this).fadeIn(125);
			});
			$('nav a.active').removeClass('active');
			$(this).addClass('active');
			if (currentView == 1) {
				$('div section').css('left', '0');
			}
			$('div section#global').css('width', '75%');
			$('div section#local').css('width', '25%');
			currentView = 2;
		}
	});


	// Login form events

	$('form#login a').click(function() {
		var scoutName = $(this).parent().find('input[type="text"]').val().trim();
		if (scoutName) {
			$(this).parent().fadeOut('fast');
			$('div#loading').fadeIn('fast');
			connect(scoutName, handleClientServerResponses);
		}
	});


	// Ready for next match form events

	$('form#readyForNextMatch a').click(function() {
		$(this).parent().fadeOut('fast');
		$('div#loading').fadeIn('fast');
		var data = {};
		data.type = 'prepare';
		queryServer(data, handleClientServerResponses);
	});


	// Ready form events

	$('form#ready a').click(function() {
		$(this).parent().fadeOut('fast');
		$('div#loading').fadeIn('fast');
		matchStarted = false;
		waitForMatchStart();
	});


	// Pallete div events

	$('div#palette>div').on('dragstart', function(event) {
		event.originalEvent.dataTransfer.setData('objects', $(this).find('div').attr('class'));
	});


	// Autonomous ended events

	$('a#autonomous-ended').click(function() {
		autonomous = false;
		$(this).fadeOut('fast');
	});


	// Robot-state events

	$('a#robot-state').click(function() {
		var data = {};
		data.type = 'robotEvent';
		data.matchNumber = matchNumber;
		data.teamNumber = teamNumber;
		date = new Date();
		data.matchTime = date.getTime() - startTime;
		if ($(this).hasClass('enabled')) {
			data.eventType = 'D';
			$(this).removeClass('enabled');
			$(this).addClass('disabled');
			$('div#palette>div').fadeOut('fast');
		}
		else {
			data.eventType = 'E';
			$(this).removeClass('disabled');
			$(this).addClass('enabled');
			$('div#palette>div').fadeIn('fast');
		}
		queryServer(data, handleServerResponses);
	});


	// Spacer events

	$('div#main section').delegate('div.spacer', 'dragenter', function(event) {
		event.originalEvent.preventDefault();
		$(this).addClass('active');
	});
	$('div#main section').delegate('div.spacer', 'dragover', function(event) {
		event.originalEvent.preventDefault();
	});
	$('div#main section').delegate('div.spacer', 'dragleave', function(event) {
		event.originalEvent.preventDefault();
		$(this).removeClass('active');
	});
	$('div#main section').delegate('div.spacer', 'drop', function(event) {
		event.originalEvent.preventDefault();
		var objects = event.originalEvent.dataTransfer.getData('objects');
		if (objects.indexOf('L') < 0 && objects.indexOf('P') < 0 && objects.indexOf('K') < 0) {
			event.stopPropagation();
			var data = {};
			data.type = 'contribution';
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			data.mode = autonomous ? 'A' : 'T';
			data.SID = -1;
			date = new Date();
			data.time = date.getTime() - startTime;
			data.objects = objects;
			queryServer(data, handleClientServerResponses);
		}
		$(this).removeClass('active');
		$('a#trash').removeClass('active');
		$('a#trash').fadeOut(125);
	});


	// Stack container events

	$('div#main section').delegate('div.stack', 'dragenter', function(event) {
		drag++;
		if (drag > 0) {
			$(this).addClass('active');
		}
		else {
			$(this).removeClass('active');
		}
		event.originalEvent.preventDefault();
	});
	$('div#main section').delegate('div.stack', 'dragover', function(event) {
		event.originalEvent.preventDefault();
	});
	$('div#main section').delegate('div.stack', 'dragleave', function(event) {
		drag--;
		if (drag > 0) {
			$(this).addClass('active');
		}
		else {
			$(this).removeClass('active');
		}
		event.originalEvent.preventDefault();
	});
	$('div#main section').delegate('div.stack', 'drop', function(event) {
		event.originalEvent.preventDefault();
		var objects = event.originalEvent.dataTransfer.getData('objects');
		var SID = 0;
		if (objects.charAt(0) == 'K') {
			SID = objects.substring(1);
			objects = '';
			$('div#' + SID + ' div div').each(function() {
				objects += $(this).attr('class') + ',';
			});
			objects = objects.substring(-1);
		}
		var invalid = false;
		var hasBin = $(this).find('div div.B').length > 0;
		var alreadyBinned = objects.indexOf('B') > -1 && hasBin;
		var alreadyLittered = objects.indexOf('L') > -1 && $(this).find('div div.L').length > 0;
		var alreadyScored = objects.indexOf('P') > -1 && $(this).find('div div.P').length > 0;
		var invalidYellowTotePlacement = objects.indexOf('Y') > -1 && ($(this).find('div div.Y').length < 1 || $(this).find('div div.Y').length > 2);
		var invalidPlacementUponYellowToteStack = objects.indexOf('Y') < 0 && $(this).find('div div.Y').length > 0;
		var invalidLitter = !hasBin && objects.indexOf('L') > -1;
		invalid = alreadyBinned || alreadyLittered || alreadyScored || invalidYellowTotePlacement || invalidPlacementUponYellowToteStack || invalidLitter;
		objects = objects.split(',');
		var totes = 0;
		for (i = 0; i < objects.length; i++) {
			totes += (objects[i].indexOf('F') > -1 || objects[i].indexOf('H') > -1) ? 1 : 0;
		}
		invalid = invalid || (totes + $(this).find('div div.F, div div.H').length > 6 && totes > 0);
		objects = SID > 0 ? 'K' + SID : objects.join();
		if (!invalid) {
			event.stopPropagation();
			var data = {};
			data.type = 'contribution';
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			data.mode = autonomous ? 'A' : 'T';
			data.SID = parseInt($(this).attr('id'));
			date = new Date();
			data.time = date.getTime() - startTime;
			data.objects = objects;
			queryServer(data, handleClientServerResponses);
		}
		$(this).removeClass('active');
		$('a#trash').removeClass('active');
		$('a#trash').fadeOut(125);
		drag = 0;
	});


	// Stack events

	$('div#main section').delegate('div.stack>div', 'dragstart', function(event) {
		var objects = '';
		objects += 'K' + $(this).parent().attr('id');
		event.originalEvent.dataTransfer.setData('objects', objects);
		$(this).parent().addClass('selection');
		$(this).parent().next('div.spacer').addClass('selection');
		$('div.selection').hide(125);
		$('a#trash').fadeIn(125);
	});
	$('div#main section').delegate('div.stack>div', 'dragend', function(event) {
		$('a#trash').removeClass('active');
		$('a#trash').fadeOut(125);
		$('div.selection').show(125);
		$('div.selection').removeClass('selection');
		$('div.active').removeClass('active');
	});
	$('div#main section').delegate('div.stack>div', 'dragenter', function(event) {
		drag++;
		event.originalEvent.preventDefault();
	});
	$('div#main section').delegate('div.stack>div', 'dragover', function(event) {
		event.originalEvent.preventDefault();
	});
	$('div#main section').delegate('div.stack>div', 'dragleave', function(event) {
		drag--;
		event.originalEvent.preventDefault();
	});


	// Trash events

	$('a#trash').on('dragenter', function(event) {
		event.originalEvent.preventDefault();
		$(this).addClass('active');
	});

	$('a#trash').on('dragover', function(event) {
		event.originalEvent.preventDefault();
	});

	$('a#trash').on('dragleave', function(event) {
		event.originalEvent.preventDefault();
		$(this).removeClass('active');
	});

	$('a#trash').on('drop', function(event) {
		event.stopPropagation();
		event.originalEvent.preventDefault();
		$('div.selection').remove();
		$(this).removeClass('active');
		$(this).fadeOut(125);
	});

});

function waitForMatchStart() {
	if (!matchStarted) {
		var data = {};
		data.type = 'ready';
		queryServer(data, handleClientServerResponses);
	}
}

function wazUp() {
	var data = {};
	data.type = 'wazUp';
	queryServer(data, handleClientServerResponses);
}

function handleClientServerResponses(data) {
	data.forEach(function(message) {
		switch (message.type) {
			case 'login':
				handleServerResponses([message]);
				$('div#loading').fadeOut('fast');
				$('form#readyForNextMatch').fadeIn('fast');
				break;
			case 'assignedTeam':
				matchNumber = message.matchNumber;
				teamNumber = message.teamNumber;
				$('div#loading').fadeOut('fast');
				$('form#ready span#matchNumber').html(matchNumber);
				$('form#ready span#alliance').attr('class', message.alliance);
				$('form#ready span#alliance').html(message.alliance + ' alliance');
				$('form#ready span#teamNumber').html(teamNumber);
				$('form#ready').fadeIn('fast');
				break;
			case 'matchStarted':
				if (message.matchNumber == matchNumber) {
					matchStarted = true;
					date = new Date();
					startTime = date.getTime();
					$('div#palette div.F, div#palette div.H').attr('teamNumber', teamNumber);
					$('div#overlay, div#loading').fadeOut('fast');
					setTimeout(wazUp, updateSpeed);
				}
				break;
			case 'status':
				if (message.status == 'no-match-available') {
					$('div#loading').fadeOut('fast');
					$('form#readyForNextMatch').fadeIn('fast');
				}
				else if (message.status == 'waiting') {
					setTimeout(waitForMatchStart, updateSpeed);
				}
				else if (message.status == 'wazUp') {
					setTimeout(wazUp, updateSpeed);
				}
				handleServerResponses([message]);
				break;
			case 'contribution':
				var SID = message.SID;
				var contributor = message.teamNumber;
				var objects = message.objects.split(',');
				var stackExists = $('section#global div#' + SID).length > 0;
				if (stackExists) {
					if (objects[0].charAt(0) == 'K') {
						var originSID = parseInt(objects[0].substring(1));
						var objects = [];
						$('div#' + originSID + ' div div').each(function() {
							objects.push($(this).attr('class'));
						});
						$('div#' + originSID).next('div.spacer').remove();
						$('div#' + originSID).remove();
					}
					var stack = $('div#' + SID + '>div');
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
					$('section#global').append(stackContainer);
					$('section#global').append('<div class="spacer"></div>');
					stack.find('div').fadeIn('fast');
				}
				break;
			default:
				handleServerResponses([message]);
		}
	});
}
