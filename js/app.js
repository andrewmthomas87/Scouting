
var serverIP = 'localhost:';

var CID, matchNumber, teamNumber;

var currentView = 0;

var drag = 0;

function resize() {
	$('div#loading').css('top', 'calc(' + ($(window).height() / 2) + 'px - 1em)');
	$('form#login').css('top', 'calc(' + ($(window).height() / 2) + 'px - 4.375em)');
	$('form#readyForNextMatch').css('top', 'calc(' + ($(window).height() / 2) + 'px - 1.875em)');
	$('form#ready').css ('top', 'calc(' + ($(window).height() / 2) + 'px - 5.3125em)');
}

$(window).resize(resize);

$(document).ready(function() {
	resize();
	$('div#overlay, form#login').fadeIn('slow');

	// Login form events

	$('form#login').submit(function(event) {
		event.originalEvent.preventDefault();
		var scoutName = $(this).find('input[type="text"]').val().trim();
		if (scoutName) {
			$(this).fadeOut('slow');
			$('div#loading').fadeIn('slow');
			$.post(serverIP, {
				'MID': 0,
				'scoutName': scoutName
			}, function(result) {
				var result = '{"CID":1}';
				var data = JSON.parse(result);
				CID = data.CID;
				$('div#loading').fadeOut('slow');
				$('form#readyForNextMatch').fadeIn('slow');
			});
		}
	});

	$('form#readyForNextMatch').submit(function(event) {
		event.originalEvent.preventDefault();
		$(this).fadeOut('slow');
		$('div#loading').fadeIn('slow');
		$.post(serverIP, {
			'MID': 1,
			'CID': CID
		}, function(result) {
			var result = '{"matchNumber":5,"teamNumber":1619}';
			var data = JSON.parse(result);
			matchNumber = data.matchNumber;
			teamNumber = data.teamNumber;
			$('div#loading').fadeOut('slow');
			$('form#ready span#matchNumber').html(matchNumber);
			$('form#ready span#teamNumber').html(teamNumber);
			$('form#ready').fadeIn('slow');
		});
	});

	$('form#ready').submit(function(event) {
		event.originalEvent.preventDefault();
		$(this).fadeOut('slow');
		$('div#loading').fadeIn('slow');
		$.post(serverIP, {
			'MID': 2,
			'CID': CID,
			'matchNumber': matchNumber
		}, function(result) {
			var result = '{"started":true}';
			var data = JSON.parse(result);
			if (data.started) {
				$('div#palette div.tote, div#palette div.chute-tote').attr('teamNumber', teamNumber);
				$('div#overlay, div#loading').fadeOut('slow');
			}
		});
	});


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
			$('div section').css('width', '50%');
			currentView = 2;
		}
	});


	// PALETTE DIV EVENTS

	$('div#palette>div').on('dragstart', function(event) {
		event.originalEvent.dataTransfer.setData('objects', $(this).find('div').attr('class') + ($(this).find('div').hasClass('tote') || $(this).find('div').hasClass('chute-tote') ? (':' + $(this).find('div').attr('teamNumber')) : '') + ',');
	});


	// SPACER EVENTS

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
		if (objects.indexOf('litter,') < 0 && objects.indexOf('scoring-platform') < 0) {
			event.stopPropagation();
			objects = objects.split(',');
			var stack = $('<div draggable="true"></div>');
			for (i = 0; i < objects.length - 1; i++) {
				if (objects[i].indexOf('tote') > -1) {
					objects[i] = objects[i].split(':');
					stack.append('<div class="' + objects[i][0] + '" teamNumber="' + objects[i][1] + '"></div>');
				}
				else {
					stack.append('<div class="' + objects[i] + '"></div>');
				}
			}
			var stackContainer = $('<div class="stack"></div>');
			stackContainer.append(stack);
			var spacer = $('<div class="spacer"></div>');
			$(this).after(spacer);
			$(this).after(stackContainer);
			$('div.selection').remove();
		}
		$(this).removeClass('active');
		$('a#trash').removeClass('active');
		$('a#trash').fadeOut(125);
	});


	// STACK EVENTS

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
		var invalid = false;
		var hasBin = $(this).find('div div.bin').length > 0;
		var alreadyBinned = objects.indexOf('bin') > -1 && hasBin;
		var alreadyLittered = objects.indexOf('litter') > -1 && $(this).find('div div.litter, div div.litter-binned').length > 0;
		var alreadyScored = objects.indexOf('scoring-platform') > -1 && $(this).find('div div.scoring-platform').length > 0;
		var invalidYellowTotePlacement = objects.indexOf('yellow-tote') > -1 && ($(this).find('div div.yellow-tote').length < 1 || $(this).find('div div.yellow-tote').length > 2);
		var invalidPlacementUponYellowToteStack = objects.indexOf('yellow-tote') < 0 && $(this).find('div div.yellow-tote').length > 0;
		invalid = alreadyBinned || alreadyLittered || alreadyScored || invalidYellowTotePlacement || invalidPlacementUponYellowToteStack;
		if (objects.indexOf('litter,') > -1) {
			if ($(this).find('div div.bin').length > 0) {
				objects = objects.replace('litter', 'litter-binned');
			}
			else {
				invalid = true;
			}
		}
		objects = objects.split(',');
		var totes = 0;
		for (i = 0; i < objects.length - 1; i++) {
			totes += objects[i].indexOf('tote') > -1 ? 1 : 0;
		}
		invalid = invalid || totes + $(this).find('div div.tote, div div.chute-tote').length > 6;
		if (!invalid) {
			var stack = $(this).find('>div');
			var object;
			for (i = 0; i < objects.length - 1; i++) {
				if (objects[objects.length - 2 - i].indexOf('tote') > -1) {
					var tote = objects[objects.length - 2 - i].split(':');
					object = '<div class="' + tote[0] + '" teamNumber="' + tote[1] + '"></div>';
				}
				else {
					object = '<div class="' + objects[objects.length - 2 - i] + '"></div>';
				}
				if (object.indexOf('scoring-platform') > -1) {
					stack.append(object);
				}
				else if (!hasBin || object.indexOf('litter') > -1) {
					stack.prepend(object);
				}
				else if (stack.find('div').length > 1) {
					var tote = objects[i].split(':');
					stack.find('div').not('.scoring-platform').last().after('<div class="' + tote[0] + '" teamNumber="' + tote[1] + '"></div>');
				}
				else {
					stack.append(object);
				}
			}
			$('div.selection').remove();
			event.stopPropagation();
		}
		$(this).removeClass('active');
		$('a#trash').removeClass('active');
		$('a#trash').fadeOut(125);
		drag = 0;
	});


	// Stack container events

	$('div#main section').delegate('div.stack>div', 'dragstart', function(event) {
		var objects = '';
		$(this).find('div').each(function() {
			if ($(this).hasClass('tote') || $(this).hasClass('chute-tote')) {
				objects += 'tote:' + $(this).attr('teamNumber') + ',';
			}
			else {
				objects += $(this).attr('class') + ',';
			}
		});
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
