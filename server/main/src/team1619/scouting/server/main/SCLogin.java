package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Login message:
 * scoutName: name of the scout
 *
 * This message assigns a new client id
 */

public class SCLogin extends SCMessage
{
    public SCLogin()
    {
    }

    @Override
    void processMessage( MySQL connection, SCJSON message ) throws SQLException
    {
        // assign a client id and create a queue for this client
        SCClientQueue clientQueue = SCOutbound.setupClient();

        clientQueue.setScoutName( (String)message.get( "scoutName" ) );

        setClientID( clientQueue.getClientId() );

        // json object that holds the assigned client id
        SCJSON outboundMessage = new SCJSON();
        outboundMessage.put( "type", "login" );
        outboundMessage.put( "CID", getClientID() );

        clientQueue.writeToClient( outboundMessage );
    }
}