package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/16/15.
 */

public class SCContribution extends SCMessage {

    private int fSID;
    private String fObjects;
    private String fMode;
    private int fTime;
    private boolean fAutonomous;
    private int fTeamNumber;
    private int fMatchNumber;

    public SCContribution() {

    }

    void processMessage( MySQL conn, SCJSON message ) throws SQLException
    {
        conn.addContribution( fTeamNumber, fMatchNumber, fMode, fObjects, fSID, fTime );
        fSID = (int) message.get("SID");
        fTime = (int) message.get("time");
        String mode = (String) message.get("mode");
        fMode = mode.substring(0, 0);
        fObjects = mode.substring(1);
        fAutonomous = (boolean) message.get("autonomous");
        fTeamNumber = (int) message.get("teamNumber");
        fMatchNumber = (int) message.get("matchNumber");
        SCClientQueue clientQueue1 = SCOutbound.getClientQueue( 1 );
        SCClientQueue clientQueue2 = SCOutbound.getClientQueue(2);
        SCClientQueue clientQueue3 = SCOutbound.getClientQueue(3);
        SCClientQueue clientQueue4 = SCOutbound.getClientQueue(4);
        SCClientQueue clientQueue5 = SCOutbound.getClientQueue(5);
        SCClientQueue clientQueue6 = SCOutbound.getClientQueue(6);

        SCJSON outboundMessage = new SCJSON();
        outboundMessage.put( "type", "contribution" );
        outboundMessage.put( "SID", fSID );
        outboundMessage.put( "time", fTime);
        outboundMessage.put( "mode", mode );
        outboundMessage.put( "autonomous", fAutonomous);
        outboundMessage.put( "teamNumber", fTeamNumber);
        outboundMessage.put( "matchNumber", fMatchNumber );

        clientQueue1.writeToClient( outboundMessage );
        clientQueue2.writeToClient( outboundMessage );
        clientQueue3.writeToClient( outboundMessage );
        clientQueue4.writeToClient( outboundMessage );
        clientQueue5.writeToClient( outboundMessage );
        clientQueue6.writeToClient( outboundMessage );
    }
}