package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCLogger;

import java.sql.SQLException;

/**
 * Created by a on 3/4/15.
 */
public class SCMatchStartedMessage extends SCMessage
{
    public SCMatchStartedMessage()
    {
    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        if ( SCMatch.isMatchActive() )
        {
            SCJSON startMessage = new SCJSON();

            startMessage.put( "type", "matchStarted" );
            startMessage.put( "matchNumber", SCMatch.getMatchNumber() );

            for ( SCClientQueue queue : SCOutbound.getClientQueues() )
            {
                SCLogger.getLogger().debug( "Writing match started message to client %d", queue.getClientId() );

                queue.writeToClient( startMessage );
            }
        }
        else
        {
            SCJSON response = new SCJSON();

            response.put( "type", "status" );
            response.put( "status", "no-current-match" );
            response.put( "description", "trying to start a match before setting it up" );

            SCOutbound.getClientQueue( getClientID() ).writeToClient( response );
        }

    }

}
