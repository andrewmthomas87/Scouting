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
        fObjects = message.getString("objects");
        fMode = (boolean) message.get("autonomous") ? "A" : "T";
        fTeamNumber = message.getInteger("teamNumber");
        fMatchNumber = message.getInteger("matchNumber");
        conn.addContribution( fTeamNumber, fMatchNumber, fMode, fObjects, fSID, fTime );

        SCJSON outboundMessage = new SCJSON();
        outboundMessage.put( "type", "contribution" );
        outboundMessage.put( "SID", fSID );
        outboundMessage.put( "time", fTime);
        outboundMessage.put( "autonomous", fMode);
        outboundMessage.put( "teamNumber", fTeamNumber);
        outboundMessage.put( "matchNumber", fMatchNumber );
        
        for (SCClientQueue q: SCOutbound.getClientQueues()) {
            q.writeToClient( outboundMessage );
        }
    }
}