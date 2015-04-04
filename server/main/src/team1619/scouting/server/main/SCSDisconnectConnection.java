package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCProperties;

import java.sql.SQLException;

/**
 * Sent by supervisor to forcibly remove a client queue
 */
public class SCSDisconnectConnection extends SCMessage
{
    public SCSDisconnectConnection()
    {
    }

    public void processMessage( MySQL conn, SCJSON message )
            throws SQLException
    {
        SCClientQueue disconnected = SCOutbound.removeClientQueue( message.getInteger( "disconnectCID" ) );

        if ( disconnected != null )
        {
            if ( SCMatch.isMatchActive() )
            {
                // delete from database the scouting information
                conn.disconnectClient( SCProperties.getProperty( "event.code" ),
                                       SCMatch.getMatchNumber(),
                                       SCMatch.getTeamNumber( disconnected.getClientId() ) );

                SCMatch.unassignTeam( disconnected.getClientId() );
            }

            SCJSON clientResponse = new SCJSON();
            clientResponse.put( "type", "status" );
            clientResponse.put( "status", "disconnected" );
            disconnected.writeToClient( clientResponse );
        }
        SCJSON response = new SCJSON();

        response.put( "type", "status" );
        response.put( "status", "ok" );
        response.put( "MID", message.getInteger( "MID" ) );

        SCOutbound.getClientQueue( getClientID() ).writeToClient( response );
    }
}
