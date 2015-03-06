package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Message from supervisor to request all currently logged in clients.
 */
public class SCGetClientsMessage extends SCMessage
{
    public SCGetClientsMessage()
    {
    }

    public void processMessage( MySQL conn, SCJSON message )
        throws SQLException
    {
        SCJSON response = new SCJSON();

        response.put( "type", "connectedClients" );

        List<SCJSON> clientList = new LinkedList<>();

        for ( SCClientQueue queue : SCOutbound.getClientQueues() )
        {
            SCJSON client = new SCJSON();

            client.put( "CID", queue.getClientId() );
            client.put( "scoutName", queue.getScoutName() );
            client.put( "teamNumber", SCMatch.getTeamNumber( queue.getClientId() ) );

            clientList.add( client );
        }

        response.put( "clients", clientList );

        SCOutbound.getClientQueue( getClientID() ).writeToClient( response );
    }
}
