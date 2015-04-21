package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCLogger;
import team1619.scouting.server.utils.SCProperties;

import java.sql.SQLException;

/**
 * Created by tolkin on 3/7/2015.
 */
public class SCCRobotEvent extends SCAMessage
{
    public SCCRobotEvent()
    {
    }

    @Override
    void processMessage( MySQL conn, SCJSON message ) throws SQLException
    {
        conn.addRobotEvent(
                SCProperties.getProperty( "event.code" ),
                message.getInteger( "matchNumber" ),
                message.getInteger( "teamNumber" ),
                message.getString( "eventType" ),
                message.getInteger( "matchTime" ),
                message.getString( "comments" ) );

        SCJSON response = new SCJSON();

        if ( "RS".contains( message.getString( "eventType" ) ) )
        {
            response.put( "type", "robotEvent" );
            response.put( "eventType", "R" );
            response.put( "comments", message.getString( "comments" ) );

            for ( SCAClientQueue q : SCAOutbound.getClientQueues() )
            {
                q.writeToClient( response );
            }
        }
        else
        {
            response.put( "type", "status" );
            response.put( "status", "ok" );
            response.put( "MID", message.getInteger( "MID" ) );

            SCAOutbound.getClientQueue( getClientID() ).writeToClient( response );
        }
    }
}
