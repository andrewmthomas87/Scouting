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
    }
}