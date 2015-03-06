package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Sent by supervisor to forcibly remove a client queue
 */
public class SCDisconnectClientMessage extends SCMessage
{
    public SCDisconnectClientMessage()
    {
    }

    public void processMessage( MySQL conn, SCJSON message )
        throws SQLException
    {
        SCOutbound.removeClientQueue( message.getInteger( "disconnectCID") );

        SCJSON response = new SCJSON();

        response.put( "type", "status" );
        response.put( "status", "ok" );

        SCOutbound.getClientQueue( getClientID() ).writeToClient( response );
    }
}
