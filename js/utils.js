
var serverIP = 'http://127.0.0.1:8002';

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
				if (message.status != 'ok') {
					alert(message.description);
				}
				break;
			default:
		}
	});
}

function connect(name) {
	var data = {};
	data.type = 'login';
	data.scoutName = 'Supervisor';
	queryServer(data, handleServerResponses);
}
