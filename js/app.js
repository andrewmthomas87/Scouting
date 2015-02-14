
var currentView = 0;
var palette = false;

$(document).ready(function() {
	$('nav a#global-view').click(function() {
		if (currentView != 0) {
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
			if (currentView == 2) {
				$('div section').css('width', '100%');
			}
			$('div section').css('left', '-100%');
			currentView = 1;
		}
	});
	$('nav a#split-view').click(function() {
		if (currentView != 2) {
			if (currentView == 1) {
				$('div section').css('left', '0');
			}
			$('div section').css('width', '50%');
			currentView = 2;
		}
	});
	$('a#menu-toggle').click(function() {
		palette = !palette;
		if (palette) {
			$('body>div').css('left', '-25%');
		}
		else {
			$('body>div').css('left', '0');
		}
	});
});
