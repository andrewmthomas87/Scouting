package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCProperties;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/19/15.
 */
public class SCCGetTeam extends SCAMessage
{

    public SCCGetTeam()
    {
    }

    @Override
    void processMessage( MySQL conn, SCJSON message ) throws SQLException
    {
        SCJSON outboundMessage = new SCJSON();

        SCAClientQueue clientQueue = SCAOutbound.getClientQueue( getClientID() );

        SCAMatch.MatchTeamData matchData = SCAMatch.getNextTeam( getClientID() );

        if ( matchData == null )
        {
            outboundMessage.put( "type", "status" );
            outboundMessage.put( "status", "noTeamAvailable" );
            outboundMessage.put( "MID", message.getInteger( "MID" ) );
        }
        else
        {
            outboundMessage.put( "type", "assignedTeam" );
            outboundMessage.put( "matchNumber", SCAMatch.getMatchNumber() );
            outboundMessage.put( "teamNumber", matchData.getTeamNumber() );
            outboundMessage.put( "alliance", matchData.getAlliance() );

            // assign scout to this match
            conn.assignScoutToTeam( SCProperties.getProperty( "event.code" ),
                    outboundMessage.getInteger( "matchNumber" ),
                    outboundMessage.getInteger( "teamNumber" ),
                    clientQueue.getScoutName() );
        }

        clientQueue.clearQueue();

        clientQueue.writeToClient( outboundMessage );
    }
}