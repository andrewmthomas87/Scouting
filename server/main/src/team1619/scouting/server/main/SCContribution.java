package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/16/15.
 */

public class SCContribution extends SCMessage
{

    public SCContribution()
    {

    }

    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        int SID = message.getInteger( "SID" );
        if ( SID < 0 )
        {
            SID = conn.getNextSID();
        }
        int time = message.getInteger( "time" );
        String objects = message.getString( "objects" );
        String[] objectArray = objects.split( "," );
        String mode = message.getString( "mode" );
        int teamNumber = message.getInteger( "teamNumber" );
        int matchNumber = message.getInteger( "matchNumber" );

        for ( String object : objectArray )
        {
            if ( !object.isEmpty() )
            {
                conn.addContribution( teamNumber, matchNumber, mode, object, SID, time );
            }
        }

        SCJSON outboundMessage = new SCJSON();
        outboundMessage.put( "type", "contribution" );
        outboundMessage.put( "SID", SID );
        outboundMessage.put( "time", time );
        outboundMessage.put( "objects", objects );
        outboundMessage.put( "mode", mode );
        outboundMessage.put( "teamNumber", teamNumber );
        outboundMessage.put( "matchNumber", matchNumber );

        SCMatch.MatchTeamData clientTeam = SCMatch.getNextTeam( getClientID() );

        // add alliance color based on the client

        outboundMessage.put( "alliance", clientTeam.getAlliance() );

        for ( SCClientQueue q : SCOutbound.getClientQueues() )
        {
            SCMatch.MatchTeamData myTeamData = SCMatch.getNextTeam( q.getClientId() );
            if ( myTeamData == null || clientTeam.getAlliance().equals( myTeamData.getAlliance() ) )
            {
                q.writeToClient( outboundMessage );
            }
        }
    }
}