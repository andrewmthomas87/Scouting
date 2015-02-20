package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/16/15.
 */

public class SCContribution extends SCMessage {

    private int fSID;
    private String fObjects;
    private String fMode;
    private int fTime;
    private boolean fAtonomous;
    private int fTeamNumber;
    private int fMatchNumber;

    public SCContribution() {

    }

    void init(SCJSON data) {
        fSID = (int) data.get("SID");
        fTime = (int) data.get("time");
        String mode = (String) data.get("mode");
        fMode = mode.substring(0, 0);
        fObjects = mode.substring(1);
        fAtonomous = (boolean) data.get("autonomous");
        fTeamNumber = (int) data.get("teamNumber");
        fMatchNumber = (int) data.get("matchNumber");
    }

    void processMessage(Connection conn) throws SQLException{
        MySQL.addContribution(conn, fTeamNumber, fMatchNumber, fMode, fObjects, fSID, fTime);
    }
}