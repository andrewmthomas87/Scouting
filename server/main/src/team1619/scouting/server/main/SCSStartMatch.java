package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCProperties;

import java.sql.SQLException;

/**
 * Created by a on 3/4/15.
 */
public class SCSStartMatch extends SCAMessage
{
    //int[] reportMatchNumbers = MySQL.getReportMatchNumbers( SCProperties.getProperty( "event.code" ) );

    public SCSStartMatch()
    {
    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        if ( SCAMatch.isMatchActive() )
        {

            // indicate that match has started (including start time)
            SCAMatch.matchHasStarted();

            SCJSON startMessage = new SCJSON();

            startMessage.put( "type", "matchStarted" );
            startMessage.put( "matchNumber", SCAMatch.getMatchNumber() );

           // for ( int i = 0; i < reportMatchNumbers.length; i++ ) {

            //}

           // SCAOutbound.getClientQueue( getClientID() ).writeToClient( startMessage );
        }
        else
        {
            SCJSON response = new SCJSON();

            response.put( "type", "status" );
            response.put( "status", "noCurrentMatch" );
            response.put( "description", "trying to start a match before setting it up" );

            SCAOutbound.getClientQueue( getClientID() ).writeToClient( response );
        }

    }

}
