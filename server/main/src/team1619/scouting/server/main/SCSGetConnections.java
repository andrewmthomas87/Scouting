package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Message from supervisor to request all currently logged in clients.
 */
public class SCSGetConnections extends SCAMessage
{
    public SCSGetConnections()
    {
    }

    public void processMessage( MySQL conn, SCJSON message )
        throws SQLException
    {
        SCJSON response = new SCJSON();

        response.put( "type", "connections" );

        List<SCJSON> clientList = new LinkedList<>();

        for ( SCAClientQueue queue : SCAOutbound.getClientQueues() )
        {
            SCJSON client = new SCJSON();

            client.put( "CID", queue.getClientId() );
            client.put( "scoutName", queue.getScoutName() );
            client.put( "teamNumber", SCAMatch.getTeamNumber( queue.getClientId() ) );
            client.put( "alliance" , SCAMatch.getTeamAlliance( queue.getClientId() ));
            clientList.add( client );
        }

        response.put( "connections", clientList );

        SCAOutbound.getClientQueue( getClientID() ).writeToClient( response );
    }
}
