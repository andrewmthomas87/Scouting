
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
	$('div#palette>div').on('dragstart', paletteDivDrag);
	addSpacerEvents($('div.spacer'));
});

function paletteDivDrag(event) {
	var objects = '';
	$(this).find('div').each(function() {
		objects += $(this).attr('class') + ',';
	});
	event.originalEvent.dataTransfer.setData('type', objects);
}

function droppableEnter(event) {
	event.originalEvent.preventDefault();
	$(this).addClass('active');
}

function droppableOver(event) {
	event.originalEvent.preventDefault();
}

function droppableLeave(event) {
	event.originalEvent.preventDefault();
	$(this).removeClass('active');
}

function spacerStart(event) {
	event.originalEvent.preventDefault;
}

function spacerDrop(event) {
	event.originalEvent.preventDefault();
	var objects = event.originalEvent.dataTransfer.getData('type').split(',');
	var stack = $('<div></div>');
	for (i = 0; i < objects.length; i++) {
		stack.append('<div class="' + objects[i] + '"></div>');
	}
	var stackContainer = $('<div class="stack"></div>');
	stackContainer.append(stack);
	var spacer = $('<div class="spacer"></div>');
	addSpacerEvents(spacer);
	$(this).after(spacer);
	$(this).after(stackContainer);
	$(this).removeClass('active');
}


function addSpacerEvents(spacer) {
	spacer.on('dragstart', spacerStart);
	spacer.on('dragenter', droppableEnter);
	spacer.on('dragover', droppableOver);
	spacer.on('dragleave', droppableLeave);
	spacer.on('drop', spacerDrop);
}
