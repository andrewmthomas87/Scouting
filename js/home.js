
var matchNumber, teamNumber;

var drag = 0;

var matchStarted;

var autonomous = true;
var autonomousTimeout;

var date;
var startTime;

function resize() {
	$('div#loading').css('top', 'calc(' + ($(window).height() / 2) + 'px - 1em)');
	$('form#login').css('top', 'calc(' + ($(window).height() / 2) + 'px - 4.375em)');
	$('form#readyForNextMatch').css('top', 'calc(' + ($(window).height() / 2) + 'px - 1.875em)');
	$('form#ready').css('top', 'calc(' + ($(window).height() / 2) + 'px - 5.3125em)');
	$('form#comments').css('top', 'calc(' + ($(window).height() / 2) + 'px - 4.375em)');
}

function reset() {
	$('div.stack, div.spacer').not('.initial').fadeOut('fast', function() {
		$(this).remove();
	});
	$('section#local div div div').fadeOut('fast', function() {
		$(this).remove();
	});
	$('a#trash').removeClass('active');
	$('a#trash').fadeOut('fast');
	matchNumber = null;
	teamNumber = null;
	autonomous = true;
	$('a#teleop-ended').fadeOut('fast');
	$('body, a#autonomous-ended').removeClass('blinking');
	clearTimeout(autonomousTimeout);
	$('a#autonomous-ended').fadeIn('fast');
	$('a#robot-state').removeClass('enabled');
	$('a#robot-state').addClass('disabled');
	$('a#robot-orientation').removeClass('fell-over');
	$('a#rake').html(0);
	$('div#palette>div').fadeOut('fast');
	$('div#palette div.F, div#palette div.H').attr('teamNumber', '');
	$('span#matchNumberDisplay span').html('');
	$('span#teamNumberDisplay span').html('');
	$('form#comments input').val('');
	$('div#overlay, form#readyForNextMatch').fadeIn('fast');
}

$(window).resize(resize);

$(document).ready(function() {
	resize();
	$('div#overlay, form#login').fadeIn('fast');


	// Form events

	$('form').submit(function(event) {
		event.preventDefault();
		$(this).find('a').click();
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


	// Comments form events

	$('form#comments a').click(function() {
		var comments = $(this).parent().find('input').val();
		if (comments) {
			var data = {};
			data.type = 'robotEvent';
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			date = new Date();
			data.matchTime = date.getTime() - startTime;
			data.eventType = 'C';
			data.comments = comments;
			queryServer(data, handleServerResponses);
		}
		$(this).parent().fadeOut('fast');
		reset();
	});


	// Pallete div events

	$('div#palette>div').on('dragstart', function(event) {
		event.originalEvent.dataTransfer.setData('objects', $(this).find('div').attr('class'));
	});


	// Autonomous ended events

	$('a#autonomous-ended').click(function() {
		autonomous = false;
		$('body').removeClass('blinking');
		$(this).fadeOut('fast');
		if ($('a#robot-state').hasClass('disabled')) {
			$('a#robot-state').removeClass('disabled');
			$('a#robot-state').addClass('enabled');
			$('div#palette>div').fadeIn('fast');
		}
		setTimeout(function() {
			$('a#teleop-ended').fadeIn('fast');
		}, 2500);
	});


	// Teleop ended events

	$('a#teleop-ended').click(function() {
		matchStarted = false;
		$('div#overlay, form#comments').fadeIn('fast');
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
			data.eventType = autonomous && $('a#robot-state').hasClass('disabled') ? 'M' : 'E';
			$(this).removeClass('disabled');
			$(this).addClass('enabled');
			$('div#palette>div').fadeIn('fast');
		}
		queryServer(data, handleServerResponses);
	});


	// Robot orientation events

	$('a#robot-orientation').click(function() {
		if ($(this).hasClass('fell-over')) {
			$(this).removeClass('fell-over');
			$('div#palette>div').fadeIn('fast');
		}
		else {
			var data = {};
			data.type = 'robotEvent';
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			date = new Date();
			data.matchTime = date.getTime() - startTime;
			data.eventType = 'F';
			queryServer(data, handleServerResponses);
			$(this).addClass('fell-over');
			$('div#palette>div').fadeOut('fast');
		}
	});


	// Rake events

	$('a#rake').click(function() {
		var data = {};
		data.type = 'robotEvent';
		data.matchNumber = matchNumber;
		data.teamNumber = teamNumber;
		date = new Date();
		data.matchTime = date.getTime() - startTime;
		data.eventType = autonomous ? 'R' : 'S';
		queryServer(data, handleServerResponses);
		$(this).html(parseInt($(this).html()) + 1);
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
		if (objects.charAt(0) == 'l') {
			objects = objects.substr(1);
			$('section#local div.stack div div').remove();
		}
		if ((objects.charAt(0) != 'K' && objects.length > 1) || (objects.indexOf('L') < 0 && objects.indexOf('P') < 0 && objects.indexOf('K') < 0)) {
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
			$('div#main section div.stack.active').not(this).removeClass('active');
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
	$('div#main section#global').delegate('div.stack', 'drop', function(event) {
		event.originalEvent.preventDefault();
		var objects = event.originalEvent.dataTransfer.getData('objects');
		var SID = 0;
		var local = false;
		if (objects.charAt(0) == 'K') {
			SID = objects.substr(1);
			objects = '';
			$('div#' + SID + ' div div').each(function() {
				objects += $(this).attr('class') + ',';
			});
			objects = objects.substr(0, objects.length - 1);
		}
		else if (objects.charAt(0) == 'l') {
			local = true;
			objects = objects.substr(1);
		}
		var invalid = false;
		var hasBin = $(this).find('div div.B').length > 0;
		var alreadyBinned = objects.indexOf('B') > -1 && hasBin;
		var alreadyLittered = objects.indexOf('L') > -1 && $(this).find('div div.L').length > 0;
		var alreadyScored = objects.indexOf('P') > -1 && $(this).find('div div.P').length > 0;
		var invalidYellowTotePlacement = objects.indexOf('Y') > -1 && ($(this).find('div div.Y').length < 1 || $(this).find('div div.Y').length > 2);
		var invalidPlacementUponYellowToteStack = objects.indexOf('Y') < 0 && objects.indexOf('P') < 0 && $(this).find('div div.Y').length > 0;
		var invalidLitter = !(hasBin || objects.indexOf('B') > -1) && objects.indexOf('L') > -1;
		invalid = alreadyBinned || alreadyLittered || alreadyScored || invalidYellowTotePlacement || invalidPlacementUponYellowToteStack || invalidLitter;
		objects = objects.split(',');
		var totes = 0;
		for (i = 0; i < objects.length; i++) {
			totes += (objects[i].indexOf('F') > -1 || objects[i].indexOf('H') > -1) ? 1 : 0;
		}
		invalid = invalid || (totes + $(this).find('div div.F, div div.H').length > 6 && totes > 0);
		var yellowTotes = 0;
		for (i = 0; i < objects.length; i++) {
			yellowTotes += objects[i].indexOf('Y') > -1 ? 1 : 0;
		}
		invalid = invalid || (yellowTotes + $(this).find('div div.Y').length > 3 && yellowTotes > 0);
		objects = SID > 0 ? 'K' + SID : objects.join();
		if (!invalid) {
			event.stopPropagation();
			if (local) {
				$('section#local div.stack div div').remove();
			}
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
	$('div#main section#local').delegate('div.stack', 'drop', function(event) {
		event.originalEvent.preventDefault();
		var objects = event.originalEvent.dataTransfer.getData('objects');
		var SID = 0;
		var invalid = false;
		var isStack = objects.charAt(0) == 'K';
		var hasBin = $(this).find('div div.B').length > 0;
		var alreadyBinned = objects.indexOf('B') > -1 && hasBin;
		var alreadyLittered = objects.indexOf('L') > -1 && $(this).find('div div.L').length > 0;
		var invalidYellowTotePlacement = objects.indexOf('Y') > -1 && $(this).find('div div.Y').length > 2;
		var invalidPlacementUponYellowToteStack = objects.indexOf('Y') < 0 && $(this).find('div div.Y').length > 0;
		var invalidLitter = !(hasBin || objects.indexOf('B') > -1) && objects.indexOf('L') > -1;
		var scoringPlatform = objects.indexOf('P') > -1;
		invalid = isStack || alreadyBinned || alreadyLittered || invalidYellowTotePlacement || invalidPlacementUponYellowToteStack || invalidLitter || scoringPlatform;
		objects = objects.split(',');
		var totes = 0;
		for (i = 0; i < objects.length; i++) {
			totes += (objects[i].indexOf('F') > -1 || objects[i].indexOf('H') > -1) ? 1 : 0;
		}
		invalid = invalid || (totes + $(this).find('div div.F, div div.H').length > 6 && totes > 0);
		var yellowTotes = 0;
		for (i = 0; i < objects.length; i++) {
			yellowTotes += objects[i].indexOf('Y') > -1 ? 1 : 0;
		}
		invalid = invalid || (yellowTotes + $(this).find('div div.Y').length > 3 && yellowTotes > 0);
		objects = SID > 0 ? 'K' + SID : objects.join();
		if (!invalid) {
			event.stopPropagation();
			var data = {};
			data.type = 'contribution';
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			data.SID = -1;
			data.objects = objects;
			handleClientServerResponses([data]);
		}
		$(this).removeClass('active');
		drag = 0;
	});


	// Stack events

	$('div#main section#global').delegate('div.stack>div', 'dragstart', function(event) {
		var objects = '';
		objects += 'K' + $(this).parent().attr('id');
		event.originalEvent.dataTransfer.setData('objects', objects);
		$(this).parent().addClass('selection');
		$(this).parent().next('div.spacer').addClass('selection');
		$('div.selection').hide(125);
		$('a#trash').fadeIn(125);
	});
	$('div#main section#local').delegate('div.stack>div', 'dragstart', function(event) {
		var objects = 'l';
		$(this).find('div').each(function() {
			objects += $(this).attr('class') + ',';
		});
		objects = objects.substr(0, objects.length - 1);
		event.originalEvent.dataTransfer.setData('objects', objects);
		$(this).find('div').addClass('selection');
		$('div.selection').hide(125);
		$('a#trash').fadeIn(125);
	});
	$('div#main section#global').delegate('div.stack>div', 'dragend', function(event) {
		$('a#trash').removeClass('active');
		$('a#trash').fadeOut(125);
		$('div.selection').show(125);
		$('div.selection').removeClass('selection');
		$('div.active').removeClass('active');
	});
	$('div#main section#local').delegate('div.stack>div', 'dragend', function(event) {
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
		var objects = event.originalEvent.dataTransfer.getData('objects');
		if (objects.charAt(0) == 'K') {
			var data = {};
			data.type = 'contribution';
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			data.mode = autonomous ? 'A' : 'T';
			data.SID = parseInt(objects.substr(1));
			date = new Date();
			data.time = date.getTime() - startTime;
			data.objects = 'X' + objects.substr(1);
			queryServer(data, handleClientServerResponses);
		}
		else {
			$('div.selection').remove();
		}
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
	console.log(JSON.stringify(data));
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
					var offset = 0;
					if (message.matchTime) {
						offset = message.matchTime;
					}
					startTime = date.getTime() - offset;
					$('div#palette div.F, div#palette div.H').attr('teamNumber', teamNumber);
					$('span#matchNumberDisplay span').html(matchNumber);
					$('span#teamNumberDisplay span').html(teamNumber);
					$('div#overlay, div#loading').fadeOut('fast');
					setTimeout(wazUp, updateSpeed);
					autonomousTimeout = setTimeout(function() {
						if (autonomous) {
							$('body, a#autonomous-ended').addClass('blinking');
						}
					}, 15000 - Math.min(offset, 15000));
				}
				break;
			case 'status':
				if (message.status == 'noTeamAvailable') {
					$('div#loading').fadeOut('fast');
					$('form#readyForNextMatch').fadeIn('fast');
				}
				else if (message.status == 'waiting') {
					setTimeout(waitForMatchStart, updateSpeed);
				}
				else if (message.status == 'wazUp' && matchStarted) {
					setTimeout(wazUp, updateSpeed);
				}
				else if (message.status == 'matchReset') {
					reset();
				}
				handleServerResponses([message]);
				break;
			case 'contribution':
				if (message.matchNumber == matchNumber) {
					var SID = message.SID;
					var contributor = message.teamNumber;
					var objects = message.objects.split(',');
					if (objects[0].charAt(0) == 'X') {
						$('section div#' + SID).next('div.spacer').fadeOut('fast', function() {
							$(this).remove();
						});
						$('section div#' + SID).fadeOut('fast', function() {
							$(this).remove();
						});
					}
					else {
						var stackExists = $('section div#' + SID).length > 0;
						if (stackExists) {
							if (objects[0].charAt(0) == 'K') {
								var originSID = parseInt(objects[0].substr(1));
								var objects = [];
								$('div#' + originSID + ' div div').each(function() {
									objects.push($(this).attr('class'));
								});
								$('div#' + originSID).next('div.spacer').remove();
								$('div#' + originSID).remove();
							}
							else if (objects.length > 1) {
								$('div.selection').remove();
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
					}
				}
				break;
			default:
				handleServerResponses([message]);
		}
	});
}
