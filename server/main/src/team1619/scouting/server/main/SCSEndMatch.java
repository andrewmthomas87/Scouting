package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCProperties;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 3/7/15.
 */
public class SCSEndMatch extends SCAMessage
{

    public SCSEndMatch()
    {
    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        if ( SCAMatch.isMatchActive() )
        {
            int thisMatchNumber = SCAMatch.getMatchNumber();

            SCAMatch.closeMatch();

            SCJSON outMessage = new SCJSON();

            outMessage.put( "type", "status" );
            outMessage.put( "status", "matchEnded" );

            SCAOutbound.getClientQueue( getClientID() ).writeToClient( outMessage );

            // update the database for this match

            conn.setMatchPlayed( SCProperties.getProperty( "event.code" ), thisMatchNumber );
        }
        else
        {
            SCJSON response = new SCJSON();

            response.put( "type", "status" );
            response.put( "status", "no-current-match" );

            SCAOutbound.getClientQueue( getClientID() ).writeToClient( response );
        }
    }
}
