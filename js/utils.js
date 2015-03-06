
var serverIP = 'http://172.20.10.4:8002';

var CID;

function queryServer(data, handler) {
	if (CID) {
		data.CID = CID;
	}
	console.log(data);
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
