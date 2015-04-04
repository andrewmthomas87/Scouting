package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCLogger;
import team1619.scouting.server.utils.SCProperties;

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
        String[] objectArray;
        if ( objects.startsWith( "K" ) )
        {
            objectArray = conn.getStackObjectsFromSID( Integer.parseInt( objects.substring( 1 ) ) );
        }
        else if ( objects.startsWith( "X" ) )
        {
            objectArray = conn.getStackObjectsFromSID( Integer.parseInt( objects.substring( 1 ) ) );
        }
        else
        {
            objectArray = objects.split( "," );
        }
        String mode = message.getString( "mode" );
        int teamNumber = message.getInteger( "teamNumber" );
        int matchNumber = message.getInteger( "matchNumber" );

        for ( String object : objectArray )
        {
            if ( !object.isEmpty() )
            {
                conn.addContribution( SCProperties.getProperty( "event.code" ), teamNumber, matchNumber, mode, object, SID, time );
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

        // add alliance color based on the client

        String contributorTeamAlliance = SCMatch.getTeamAlliance( getClientID() );
        outboundMessage.put( "alliance", contributorTeamAlliance );

        int numMessagesSent = 0;

        for ( SCClientQueue q : SCOutbound.getClientQueues() )
        {
            SCMatch.MatchTeamData myTeamData = SCMatch.getAssociatedTeamData( q.getClientId() );

            if ( myTeamData == null || contributorTeamAlliance.equals( myTeamData.getAlliance() ) )
            {
                SCLogger.getLogger().debug( "Writing contribution message from client %d to client %d", getClientID(), q.getClientId() );
                q.writeToClient( outboundMessage );
                numMessagesSent++;
            }
        }

        SCLogger.getLogger().debug( "Contribution message generated %d responses.", numMessagesSent );
    }
}