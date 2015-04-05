package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/19/15.
 */
public class SCCWaitForMatchStart extends SCMessage {

    public SCCWaitForMatchStart() {
    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        SCJSON response = new SCJSON();

        if ( SCMatch.hasMatchStarted() )
        {
            // just send a match started message instead

            response.put( "type", "matchStarted" );
            response.put( "matchNumber", SCMatch.getMatchNumber() );
            response.put( "matchTime", SCMatch.getMatchDuration() );
        }
        else
        {
            response.put( "type", "status" );
            response.put( "status", "waiting" );
        }

        SCOutbound.getClientQueue( getClientID() ).writeToClient( response );
    }

}
