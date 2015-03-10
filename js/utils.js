
var updateSpeed = 125;

var serverIP = 'http://169.254.22.205:8002';

var CID;

function queryServer(data, handler) {
	if (CID) {
		data.CID = CID;
	}
	$.getJSON(serverIP, JSON.stringify(data), handler);
}

function handleServerResponses(data) {
	data.forEach(function(message) {
		switch (message.type) {
			case 'login':
				CID = message.CID
				break;
			case 'status':
				if (message.status == 'disconnected') {
					window.location = '/disconnected.html';
				}
				else if (message.status != 'ok' && message.status != 'waiting') {

				}
				break;
			default:
		}
	});
}

function connect(name, handler) {
	var data = {};
	data.type = 'login';
	data.scoutName = name;
	queryServer(data, handler);
}
