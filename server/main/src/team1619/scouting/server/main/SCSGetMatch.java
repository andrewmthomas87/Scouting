package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCProperties;

import java.sql.SQLException;
import java.util.List;

/**
 * This message is sent by the supervisor to get the data about the next match
 * that will be played.
 */
public class SCSGetMatch extends SCMessage
{
    public SCSGetMatch()
    {
    }

    public void processMessage( MySQL conn, SCJSON message ) throws SQLException
    {
        // query the database to get the next match data

        int matchNumber;
        if ( !SCMatch.isMatchActive() )
        {
            matchNumber = conn.setupNextMatch( SCProperties.getProperty( "event.code" ) );
        }
        else {
            matchNumber = SCMatch.getMatchNumber();
        }

        SCJSON response = new SCJSON();

        if ( matchNumber < 0 )
        {
            // no match

            response.put( "type", "status" );
            response.put( "status", "noMatch" );
            response.put( "description", "There is no next match available for this event" );
            response.put( "MID", message.getInteger( "MID" ) );
        }
        else
        {
            // we have a next match.  send back all the data

            response.put( "type", "matchData" );

            response.put( "matchNumber", matchNumber );

            List<SCMatch.MatchTeamData> teamData = SCMatch.getAllTeamData();

            int redNo = 1;
            int blueNo = 1;

            for ( SCMatch.MatchTeamData data : teamData )
            {
                if ( "red".equals( data.getAlliance() ) )
                {
                    response.put( "redTeam" + redNo, data.getTeamNumber() );
                    redNo++;
                }
                else
                {
                    response.put( "blueTeam" + blueNo, data.getTeamNumber() );
                    blueNo++;
                }
            }
        }

        SCOutbound.getClientQueue( getClientID() ).writeToClient( response );
    }
}
