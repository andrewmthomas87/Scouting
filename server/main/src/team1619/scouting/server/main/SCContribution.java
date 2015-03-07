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
    private String[] fObjectArray;
    private String fMode;
    private int fTime;
    private int fTeamNumber;
    private int fMatchNumber;

    public SCContribution() {

    }

    void processMessage( MySQL conn, SCJSON message ) throws SQLException
    {
        fSID = message.getInteger("SID");
        if (fSID < 0) {
            fSID = conn.getNextSID();
        }
        fTime = message.getInteger("time");
        fObjects = message.getString( "objects" );
        fObjectArray = fObjects.split( "," );
        fMode = message.getString("mode");
        fTeamNumber = message.getInteger("teamNumber");
        fMatchNumber = message.getInteger("matchNumber");
        for(int i = 0; i < fObjectArray.length; i ++)
        {
            conn.addContribution( fTeamNumber, fMatchNumber, fMode, fObjectArray[i], fSID, fTime );
        }

        SCJSON outboundMessage = new SCJSON();
        outboundMessage.put( "type", "contribution" );
        outboundMessage.put( "SID", fSID );
        outboundMessage.put( "time", fTime);
        outboundMessage.put( "objects", fObjects);
        outboundMessage.put( "mode", fMode);
        outboundMessage.put( "teamNumber", fTeamNumber);
        outboundMessage.put( "matchNumber", fMatchNumber );
        
        for (SCClientQueue q: SCOutbound.getClientQueues()) {
            q.writeToClient( outboundMessage );
        }
    }
}