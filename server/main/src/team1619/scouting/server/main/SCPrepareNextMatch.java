package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/19/15.
 */
public class SCPrepareNextMatch extends SCMessage {

    public SCPrepareNextMatch() {

    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException {
        SCJSON outboundMessage = new SCJSON();
        SCClientQueue clientQueue = SCOutbound.getClientQueue(getClientID());
        outboundMessage.put("matchNumber", SCMatch.getMatchNumber());
        outboundMessage.put("teamNumber", SCMatch.getNextTeam());
        clientQueue.writeToClient(outboundMessage);
    }
}