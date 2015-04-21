
function queryServer(data, handler) {
	$.getJSON(serverIP, JSON.stringify(data), handler);
}

function constructMessage(type, CID) {
	var data = {};
	data['type'] = type;
	if (type != 'login') {
		data['CID'] = CID;
	}
	return data;
}
