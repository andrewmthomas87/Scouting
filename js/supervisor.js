
var serverIP = 'http://127.0.0.1:8002';

$(document).ready(function() {

	$('form').submit(function(event) {
		event.originalEvent.preventDefault();
		var data = {};
		data['type'] = 'setNextMatch';
		$(this).find('input[type="text"]').each(function() {
			data[$(this).attr('name')] = $(this).val();
		});
		$.getJSON(
			serverIP,
			JSON.stringify(data),
			function(data) {
				console.log(data);
			}
		);
	});

});
