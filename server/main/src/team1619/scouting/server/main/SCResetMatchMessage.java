package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 3/7/15.
 */
public class SCResetMatchMessage extends SCMessage
{
    public SCResetMatchMessage() {

    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        conn.deleteMatch(message.getInteger( "matchNumber" ));

        SCClientQueue clientQueue = SCOutbound.setupClient();
        SCJSON outMessage = new SCJSON();

        outMessage.put( "type:", "status" );
        outMessage.put( "status", "ok");

        clientQueue.writeToClient( outMessage );

    }
}
