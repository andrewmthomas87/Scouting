package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCLogger;
import team1619.scouting.server.utils.SCProperties;

import java.sql.SQLException;

/**
 * This message is sent by the supervisor to setup match data.
 */
public class SCSSetMatch extends SCAMessage
{
    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        String eventCode = SCProperties.getProperty( "event.code" );

        Integer matchNumber = message.getInteger( "matchNumber" );

        Integer redTeam1 = message.getInteger( "redTeam1" );
        Integer redTeam2 = message.getInteger( "redTeam2" );
        Integer redTeam3 = message.getInteger( "redTeam3" );

        Integer blueTeam1 = message.getInteger( "blueTeam1" );
        Integer blueTeam2 = message.getInteger( "blueTeam2" );
        Integer blueTeam3 = message.getInteger( "blueTeam3" );

        if (    eventCode == null || matchNumber == null ||
                redTeam1 == null  || redTeam2 == null  || redTeam3 == null ||
                blueTeam1 == null || blueTeam2 == null || blueTeam3 == null )
        {
            // write back error message
            SCLogger.getLogger().error( "missing a required parameter in setMatchData message" );

            SCJSON error = new SCJSON();

            error.put( "type", "status" );
            error.put( "status", "badMessage" );
            error.put( "description", "missing required values in setMatchData message" );

            SCAOutbound.getClientQueue( getClientID() ).writeToClient( error );
        }
        else
        {
            // update the database
            conn.addMatchData( eventCode, matchNumber, redTeam1, redTeam2, redTeam3, blueTeam1, blueTeam2, blueTeam3 );

            SCAMatch.closeMatch();

            // write back a happy response
            SCJSON response = new SCJSON();

            response.put( "type", "status" );
            response.put( "status", "ok" );
            response.put( "MID", message.getInteger( "MID" ) );

            SCAOutbound.getClientQueue( getClientID() ).writeToClient( response );

        }
    }
}
