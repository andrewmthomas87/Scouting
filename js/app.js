
var currentView = 0;

$(document).ready(function() {
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
		var objects = '';
		$(this).find('div').each(function() {
			objects += $(this).attr('class') + ',';
		});
		event.originalEvent.dataTransfer.setData('objects', objects);
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
		event.stopPropagation();
		event.originalEvent.preventDefault();
		var objects = event.originalEvent.dataTransfer.getData('objects').split(',');
		var stack = $('<div></div>');
		for (i = 0; i < objects.length - 1; i++) {
			stack.append('<div class="' + objects[i] + '"></div>');
		}
		var stackContainer = $('<div class="stack" draggable="true"></div>');
		stackContainer.append(stack);
		var spacer = $('<div class="spacer"></div>');
		$(this).after(spacer);
		$(this).after(stackContainer);
		$('div.selection').remove();
		$(this).removeClass('active');
	});

	// STACK EVENTS

	$('div#main section').delegate('div.stack', 'dragstart', function(event) {
		var objects = '';
		$(this).find('div div').each(function() {
			objects += $(this).attr('class') + ',';
		});
		event.originalEvent.dataTransfer.setData('objects', objects);
		event.originalEvent.dataTransfer.setDragImage($(this).find('>div')[0], $(this).width() / 2, $(this).find('>div').height() / 2);
		$(this).addClass('selection');
		$(this).next('div.spacer').addClass('selection');
		$('div.selection').hide(125);
	});
	$('div#main section').delegate('div.stack', 'dragend', function(event) {
		console.log(event);
		if (event.originalEvent.dataTransfer.dropEffect == 'none') {
			$('div.selection').show(125);
			$('div.selection').removeClass('selection');
		}
	});
	$('div#main section').delegate('div.stack', 'dragenter', function(event) {
		event.originalEvent.preventDefault();
		$(this).addClass('active');
	});
	$('div#main section').delegate('div.stack', 'dragover', function(event) {
		event.originalEvent.preventDefault();
	});
	$('div#main section').delegate('div.stack', 'dragleave', function(event) {
		event.originalEvent.preventDefault();
		$(this).removeClass('active');
	});
	$('div#main section').delegate('div.stack', 'drop', function(event) {
		event.stopPropagation();
		event.originalEvent.preventDefault();
		var objects = event.originalEvent.dataTransfer.getData('objects').split(',');
		var stack = $(this).find('>div');
		for (i = 0; i < objects.length - 1; i++) {
			stack.prepend('<div class="' + objects[objects.length - 2 - i] + '"></div>');
		}
		$('div.selection').remove();
		$(this).removeClass('active');
	});

});
