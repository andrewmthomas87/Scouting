package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by a on 3/4/15.
 */
public class SCSStartMatch extends SCMessage
{
    public SCSStartMatch()
    {
    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        if ( SCMatch.isMatchActive() )
        {
            // indicate that match has started (including start time)
            SCMatch.matchHasStarted();

            SCJSON startMessage = new SCJSON();

            startMessage.put( "type", "matchStarted" );
            startMessage.put( "matchNumber", SCMatch.getMatchNumber() );

            SCOutbound.getClientQueue( getClientID() ).writeToClient( startMessage );
        }
        else
        {
            SCJSON response = new SCJSON();

            response.put( "type", "status" );
            response.put( "status", "noCurrentMatch" );
            response.put( "description", "trying to start a match before setting it up" );

            SCOutbound.getClientQueue( getClientID() ).writeToClient( response );
        }

    }

}
