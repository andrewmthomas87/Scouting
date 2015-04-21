
var matchNumber, teamNumber, alliance;

var matchTime;

var matchStarted = false;

var autonomous = true;

var outboundMessages = [];

var waitForMatchStartInterval, getUpdatesInterval, autonomousTimeout;

var drag = 0;

var disabled = false;

function reset() {
	clearInterval(getUpdatesInterval);
	clearTimeout(autonomousTimeout);
	matchNumber = teamNumber = alliance = matchTime = waitForMatchStartInterval = getUpdatesInterval = autonomousTimeout = null;
	matchStarted = false;
	autonomous = true;
	drag = 0;
	disabled = false;
	$('section#forms').show('fast');
	$('form#getTeam').show('fast', function() {
		$('section#main').hide();
		$('a#moved').show();
		$('a#gameState').addClass('autonomous').html('End Autonomous');
		$('a#state').addClass('enabled').html('Enabled');
		$('a#orientation').addClass('upright').html('Upright');
		$('div#step').find('div.bin').removeClass('raked');
		$('div#step').find('div#cooperitition').attr('number', '0');
	});
	$('div#local div div div').remove();
	$('div#global>div:not(.initial)').remove();
}

$(document).ready(function() {
	$('body').css('height', $(window).height());
	setTimeout(function() {
		$('div#loading').find('span:first-child').show('fast', function() {
			setTimeout(function() {
				$('div#loading').find('span:last-child').show('fast', function() {
					setTimeout(function() {
						$('div#loading').find('span:last-child').hide('fast', function() {
							$('div#loading').find('span:first-child').hide('fast', function() {
								$('form#login').show('fast');
							});
						});
					}, 1000);
				});
			}, 250);
		});
	}, 250);

	$('form').submit(function(event) {
		event.preventDefault();
		$(this).find('a').click();
	});

	$('form#login a').click(function() {
		var scoutName = $(this).parent().find('input').val();
		if (scoutName) {
			var data = constructMessage('login');
			data['scoutName'] = scoutName;
			queryServer(data, handleServerResponses);
		}
	});

	$('form#getTeam a').click(function() {
		var data = constructMessage('getTeam', CID);
		data['MID'] = outboundMessages.length;
		queryServer(data, handleServerResponses);
		outboundMessages.push('getTeam');
	});

	$('form#comments a').click(function() {
		var comments = $(this).parent().find('input').val();
		if (comments) {
			var data = constructMessage('robotEvent', CID);
			data['matchNumber'] = matchNumber;
			data['teamNumber'] = teamNumber;
			data['eventType'] = 'C';
			data['matchTime'] = 0;
			data['comments'] = comments;
			data['MID'] = outboundMessages.length;
			queryServer(data, handleServerResponses);
			outboundMessages.push('comments');
		}
		else {
			$(this).parent().hide('fast', function() {
				reset();
			});
		}
	});

	$('a#moved').click(function() {
		var data = constructMessage('robotEvent', CID);
		data['matchNumber'] = matchNumber;
		data['teamNumber'] = teamNumber;
		data['eventType'] = 'M';
		var date = new Date();
		data['matchTime'] = date.getTime() - matchTime;
		data['MID'] = outboundMessages.length;
		queryServer(data, handleServerResponses);
		outboundMessages.push('moved');
	});

	$('a#gameState').click(function() {
		if (autonomous) {
			clearTimeout(autonomousTimeout);
			$('a#moved').fadeOut('fast');
			$(this).fadeOut('fast', function() {
				$(this).removeClass('blinking');
				$(this).html('End Teleop');
				setTimeout(function() {
					$('a#gameState').fadeIn('fast');
				}, 2500);
			});
			autonomous = false;
		}
		else {
			$('section#forms').fadeIn('fast', function() {
				$('section#main').hide();
				$('form#comments').show('fast');
			});
		}
	});

	$('a#state').click(function() {
		var data = constructMessage('robotEvent', CID);
		data['matchNumber'] = matchNumber;
		data['teamNumber'] = teamNumber;
		data['eventType'] = $(this).hasClass('enabled') ? 'E' : 'D';
		var date = new Date();
		data['matchTime'] = date.getTime() - matchTime;
		data['MID'] = outboundMessages.length;
		queryServer(data, handleServerResponses);
		outboundMessages.push('state');
	});

	$('a#orientation').click(function() {
		var data = constructMessage('robotEvent', CID);
		data['matchNumber'] = matchNumber;
		data['teamNumber'] = teamNumber;
		data['eventType'] = $(this).hasClass('upright') ? 'U' : 'F';
		var date = new Date();
		data['matchTime'] = date.getTime() - matchTime;
		data['MID'] = outboundMessages.length;
		queryServer(data, handleServerResponses);
		outboundMessages.push('orientation');
	});

	$('div#palette div div').on('dragstart', function(event) {
		event.originalEvent.dataTransfer.setData('objects', $(this).attr('class'));
	});

	$('section#main').delegate('div.spacer', 'dragenter', function(event) {
		event.originalEvent.preventDefault();
		$(this).addClass('active');
	});

	$('section#main').delegate('div.spacer', 'dragover', function(event) {
		event.originalEvent.preventDefault();
	});

	$('section#main').delegate('div.spacer', 'dragleave', function(event) {
		event.originalEvent.preventDefault();
		$(this).removeClass('active');
	});

	$('section#main').delegate('div.spacer', 'drop', function(event) {
		var objects = event.originalEvent.dataTransfer.getData('objects');
		if (!(objects.length == 1 && objects.indexOf('L') > -1) && objects.indexOf('K') < 0) {
			event.originalEvent.preventDefault();
			var data = constructMessage('contribution', CID);
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			data.mode = autonomous ? 'A' : 'T';
			data.SID = -1;
			var date = new Date();
			data.time = date.getTime() - matchTime;
			data.objects = objects;
			data.global = 0;
			queryServer(data, handleServerResponses);
		}
		$(this).removeClass('active');
	});

	$('section#main').delegate('div.stack', 'dragenter', function(event) {
		drag++;
		if (drag > 0) {
			$('div.stack.active').not(this).removeClass('active');
			$(this).addClass('active');
		}
		else {
			$(this).removeClass('active');
		}
		event.originalEvent.preventDefault();
	});

	$('section#main').delegate('div.stack', 'dragover', function(event) {
		event.originalEvent.preventDefault();
	});
	$('section#main').delegate('div.stack', 'dragleave', function(event) {
		drag--;
		if (drag > 0) {
			$(this).addClass('active');
		}
		else {
			$(this).removeClass('active');
		}
		event.originalEvent.preventDefault();
	});

	$('div#global').delegate('div.stack:not(.initial)', 'drop', function(event) {
		var objects = event.originalEvent.dataTransfer.getData('objects');
		var SID = 0;
		var scored = false;
		if (objects.charAt(0) == 'K') {
			SID = objects.substr(1);
			scored = $('div#' + SID).hasClass('scored');
			objects = '';
			$('div#' + SID + ' div div').each(function() {
				objects += $(this).attr('class') + ',';
			});
			objects = objects.substr(0, objects.length - 1);
		}
		var invalid = false;
		var hasBin = $(this).find('div div.B').length > 0;
		var alreadyBinned = objects.indexOf('B') > -1 && hasBin;
		var alreadyLittered = objects.indexOf('L') > -1 && $(this).find('div div.L').length > 0;
		var invalidYellowTotePlacement = objects.indexOf('Y') > -1 && ($(this).find('div div.Y').length < 1 || $(this).find('div div.Y').length > 2);
		var invalidPlacementUponYellowToteStack = $(this).find('div div.Y').length > 0 && (objects.indexOf('L') > -1 || objects.indexOf('B') > -1 || objects.indexOf('F') > -1 || objects.indexOf('H') > -1);
		var invalidLitter = !(hasBin || objects.indexOf('B') > -1) && objects.indexOf('L') > -1;
		invalid = scored || alreadyBinned || alreadyLittered || invalidYellowTotePlacement || invalidPlacementUponYellowToteStack || invalidLitter;
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
			event.originalEvent.preventDefault();
			var data = constructMessage('contribution', CID);
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			data.mode = autonomous ? 'A' : 'T';
			data.SID = parseInt($(this).attr('id'));
			var date = new Date();
			data.time = date.getTime() - matchTime;
			data.objects = objects;
			data.global = 0;
			queryServer(data, handleServerResponses);
		}
		$(this).removeClass('active');
		drag = 0;
		$('a#trash').fadeOut('fast');
	});

	$('div#global div.stack.initial').on('drop', function(event) {
		var objects = event.originalEvent.dataTransfer.getData('objects');
		if (objects.charAt(0) == 'K') {
			event.originalEvent.preventDefault();
			var SID = parseInt(objects.substr(1));
			var data = constructMessage('contribution', CID);
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			data.mode = autonomous ? 'A' : 'T';
			data.SID = SID;
			var date = new Date();
			data.time = date.getTime() - matchTime;
			data.objects = 'P';
			data.global = 0;
			queryServer(data, handleServerResponses);
		}
		$(this).removeClass('active');
		drag = 0;
		$('a#trash').fadeOut('fast');
	});

	$('div#local').delegate('div.stack', 'drop', function(event) {
		var objects = event.originalEvent.dataTransfer.getData('objects');
		var SID = 0;
		var scored = false;
		if (objects.charAt(0) == 'K') {
			SID = objects.substr(1);
			scored = $('div#' + SID).hasClass('scored');
			objects = '';
			$('div#' + SID + ' div div').each(function() {
				objects += $(this).attr('class') + ',';
			});
			objects = objects.substr(0, objects.length - 1);
		}
		var invalid = false;
		var hasBin = $(this).find('div div.B').length > 0;
		var alreadyBinned = objects.indexOf('B') > -1 && hasBin;
		var alreadyLittered = objects.indexOf('L') > -1 && $(this).find('div div.L').length > 0;
		var invalidPlacementUponYellowToteStack = $(this).find('div div.Y').length > 0 && (objects.indexOf('L') > -1 || objects.indexOf('B') > -1 || objects.indexOf('F') > -1 || objects.indexOf('H') > -1);
		var invalidLitter = !(hasBin || objects.indexOf('B') > -1) && objects.indexOf('L') > -1;
		invalid = scored || alreadyBinned || alreadyLittered || invalidPlacementUponYellowToteStack || invalidLitter;
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
		if (!invalid) {
			event.originalEvent.preventDefault();
			var data = constructMessage('contribution', CID);
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			data.mode = autonomous ? 'A' : 'T';
			data.SID = 0;
			var date = new Date();
			data.time = date.getTime() - matchTime;
			data.global = 0;
			if (SID > 0) {
				data.objects = 'K' + SID;
				queryServer(data, handleServerResponses);
			}
			else {
				data.objects = objects.join();
				handleServerResponses([data]);
			}
		}
		$(this).removeClass('active');
		drag = 0;
		$('a#trash').fadeOut('fast');
	});

	$('div#global').delegate('div.stack>div', 'dragstart', function(event) {
		var objects = 'K' + $(this).parent().attr('id');
		event.originalEvent.dataTransfer.setData('objects', objects);
		$(this).parent().hide(1);
		$(this).parent().next('div.spacer').hide();
		$('a#trash').fadeIn('fast');
	});

	$('div#local').delegate('div.stack>div', 'dragstart', function(event) {
		var objects = '';
		$(this).find('div').each(function() {
			objects = $(this).attr('class') + ',' + objects;
		});
		objects = objects.substr(0, objects.length - 1);
		event.originalEvent.dataTransfer.setData('objects', objects);
		$(this).parent().hide(1);
		$('a#trash').fadeIn('fast');
	});

	$('section#main').delegate('div.stack>div', 'dragend', function(event) {
		var parent = $(this).parent();
		parent.removeClass('active');
		setTimeout(function() {
			parent.show();
			parent.next('div.spacer').show();
		}, 250, parent);
		$('a#trash').fadeOut('fast');
	});

	$('section#main').delegate('div.stack>div', 'dragenter', function(event) {
		drag++;
		event.originalEvent.preventDefault();
	});

	$('section#main').delegate('div.stack>div', 'dragover', function(event) {
		event.originalEvent.preventDefault();
	});

	$('section#main').delegate('div.stack>div', 'dragleave', function(event) {
		drag--;
		event.originalEvent.preventDefault();
	});

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
		event.originalEvent.preventDefault();
		var objects = event.originalEvent.dataTransfer.getData('objects');
		if (objects.charAt(0) == 'K') {
			var data = constructMessage('contribution', CID);
			data.matchNumber = matchNumber;
			data.teamNumber = teamNumber;
			data.mode = autonomous ? 'A' : 'T';
			data.SID = parseInt(objects.substr(1));
			var date = new Date();
			data.time = date.getTime() - matchTime;
			data.objects = 'X';
			data.global = 0;
			queryServer(data, handleServerResponses);
		}
		else {
			$('div#local div div div').remove();
		}
		$(this).removeClass('active');
		$(this).fadeOut('fast');
	});

	$('div#step').find('div.bin').click(function() {
		if (!$(this).hasClass('raked')) {
			var data = constructMessage('robotEvent', CID);
			data['matchNumber'] = matchNumber;
			data['teamNumber'] = teamNumber;
			data['eventType'] = autonomous ? 'R' : 'S';
			var date = new Date();
			data['matchTime'] = date - matchTime;
			data['comments'] = $(this).attr('id').substr(1);
			queryServer(data, handleServerResponses);
		}
	});

	$('div#step').find('div#cooperitition').on('dragenter', function(event) {
		event.originalEvent.preventDefault();
	});

	$('div#step').find('div#cooperitition').on('dragover', function(event) {
		event.originalEvent.preventDefault();
	});

	$('div#step').find('div#cooperitition').on('dragleave', function(event) {
		event.originalEvent.preventDefault();
	});

	$('div#step').find('div#cooperitition').on('drop', function(event) {
		var objects = event.originalEvent.dataTransfer.getData('objects');
		if (objects.charAt(0) == 'K') {
			var SID = objects.substr(1);
			objects = '';
			$('div#' + SID + ' div div').each(function() {
				objects += $(this).attr('class');
			});
			if (objects.indexOf('Y') > -1 && parseInt($(this).attr('number')) < 4) {
				var data = constructMessage('contribution', CID);
				data.matchNumber = matchNumber;
				data.teamNumber = teamNumber;
				data.mode = autonomous ? 'A' : 'T';
				data.SID = parseInt(SID);
				var date = new Date();
				data.time = date.getTime() - matchTime;
				data.objects = 'S' + objects.length;
				data.global = 1;
				queryServer(data, handleServerResponses);
			}
		}
		$('a#trash').fadeOut('fast');
	});

});

function handleServerResponses(data) {
	data.forEach(function(message) {
		switch (message.type) {
			case 'login':
				CID = message.CID;
				$('form#login').hide('fast', function() {
					setTimeout(function() {
						$('form#getTeam').show('fast');
					}, 500);
				});
				break;
			case 'assignedTeam':
				matchNumber = message.matchNumber;
				teamNumber = message.teamNumber;
				alliance = message.alliance;
				$('form#getTeam').hide('fast', function() {
					setTimeout(function() {
						$('span#matchNumber').find('span').html(matchNumber);
						$('span#teamNumber').find('span').html(teamNumber);
						$('span#alliance').find('span').addClass(alliance);
						$('span#alliance').find('span').html(alliance.charAt(0).toUpperCase() + alliance.slice(1) + ' Alliance');
						$('div#details').show('fast');
						$('div#palette div div').attr('teamNumber', teamNumber);
						waitForMatchStartInterval = setInterval(function() {
							var data = constructMessage('waitForMatchStart', CID);
							queryServer(data, handleServerResponses);
						}, updateSpeed);
					}, 500);
				});
				break;
			case 'matchStarted':
				matchStarted = true;
				var date = new Date();
				matchTime = date.getTime() - message.matchTime;
				clearInterval(waitForMatchStartInterval);
				$('section#main').show();
				$('div#details').hide('fast');
				$('section#forms').hide('fast');
				getUpdatesInterval = setInterval(function() {
					var data = constructMessage('getUpdates', CID);
					queryServer(data, handleServerResponses);
				}, updateSpeed);
				autonomousTimeout = setTimeout(function() {
					$('a#gameState').addClass('blinking');
				}, Math.max(15000 - message.matchTime, 0));
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
						$('div#global div.stack.initial').before(stack);
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
									if (SID > 0 || contributor == teamNumber) {
										$('div#' + originSID + ' div div').each(function() {
											objects.push($(this).attr('class'));
										});
										objects.reverse();
									}
									$('div#' + originSID).next('div.spacer').remove();
									$('div#' + originSID).remove();
								}
								else if (SID > 0 && contributor == teamNumber) {
									$('div#local div div div').remove();
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
								$('div#global').append(stackContainer);
								$('div#global').append('<div class="spacer"></div>');
								if (contributor == teamNumber) {
									$('div#local div div div').remove();
									$('div#global').animate({
										scrollLeft: stackContainer.position().left
									}, 500);
								}
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
				if (message.status == 'matchReset') {
					reset();
				}
				else if (message.status != 'waiting') {
					switch (outboundMessages[message.MID]) {
						case 'getTeam':
							$('form#getTeam').find('a').addClass('error');
							setTimeout(function() {
								$('form#getTeam').find('a').removeClass('error');
							}, 125);
							break;
						case 'moved':
							$('a#moved').fadeOut('fast');
							break;
						case 'state':
							$('a#state').toggleClass('enabled');
							disabled = !disabled;
							if (disabled) {
								$('a#state').html('Disabled');
								$('div#palette').addClass('hidden');
							}
							else {
								$('a#state').html('Enabled');
								$('div#palette').removeClass('hidden');
							}
							break;
						case 'orientation':
							$('a#orientation').toggleClass('upright');
							disabled = !disabled;
							if (disabled) {
								$('a#orientation').html('Fell over');
								$('div#palette').addClass('hidden');
							}
							else {
								$('a#orientation').html('Upright');
								$('div#palette').removeClass('hidden');
							}
							break;
						case 'comments':
							$('form#comments').hide('fast', reset);
							break;
						default:
							if (message.status == 'disconnected') {
								$('body').fadeOut('slow', function() {
									window.location = 'disconnected.html';
								});
							}
					}
				}
			default:
		}
		if (message.MID) {
			outboundMessages.splice(message.MID, 1);
		}
	});
}
