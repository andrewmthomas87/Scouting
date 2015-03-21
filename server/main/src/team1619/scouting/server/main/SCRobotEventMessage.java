package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCProperties;

import java.sql.SQLException;

/**
 * Created by tolkin on 3/7/2015.
 */
public class SCRobotEventMessage extends SCMessage
{
    public SCRobotEventMessage()
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
                message.getInteger( "rakeNumber" ),
                message.getInteger( "matchTime" ),
                message.getString( "comments" ) );

        SCJSON response = new SCJSON();

        response.put( "type", "status" );
        response.put( "status", "ok" );

        SCOutbound.getClientQueue( getClientID() ).writeToClient( response );
    }
}
