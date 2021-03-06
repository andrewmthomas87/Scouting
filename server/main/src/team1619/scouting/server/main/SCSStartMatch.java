package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by a on 3/4/15.
 */
public class SCSStartMatch extends SCAMessage
{

    public SCSStartMatch()
    {
    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        if ( SCAMatch.isMatchActive() )
        {

            SCAMatch.matchHasStarted();

            SCJSON startMessage = new SCJSON();

            startMessage.put( "type", "matchStarted" );
            startMessage.put( "matchNumber", SCAMatch.getMatchNumber() );


            SCAOutbound.getClientQueue( getClientID() ).writeToClient( startMessage );
        }
        else
        {
            SCJSON response = new SCJSON();

            response.put( "type", "status" );
            response.put( "status", "noCurrentMatch" );

            SCAOutbound.getClientQueue( getClientID() ).writeToClient( response );
        }

    }

}
