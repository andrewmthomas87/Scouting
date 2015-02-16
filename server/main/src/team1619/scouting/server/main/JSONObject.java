package team1619.scouting.server.main;

import java.sql.SQLException;
import team1619.scouting.server.database.MySQL;

/**
 * Created by avimoskoff on 2/15/15.
 */

public class JSONObject {

    private int MID;
    private String scoutName;
    private int CID = -1;
    private int matchNumber;
    private int teamNumber;
    public int SID = 0;
    public String object;
    public String objectState;
    private int time;
    private boolean auton;

    protected JSONObject() {

    }

    protected void setMID(int MID) {
        this.MID = MID;
    }

    protected void setScoutName(String scoutName) {
        this.scoutName = scoutName;
    }

    protected int getCID() {
        for (int i = -1; i < 5; i++) {
            if (i == CID) {
                break;
            }
        }
        CID++;
        return CID;
    }

    protected int getMatchNumber() {
        matchNumber ++;
        return matchNumber % 6;
    }

    protected void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    protected int setSID(int SID) {
        if (SID == -1) {
            this.SID ++;
            return this.SID;
        }
        else {
            return SID;
        }
    }

    protected void setObject(String object) {
        objectState = object.substring(0, 1);
        this.object = object;
    }

    protected void setTime(int time) {
        this.time = time;
    }

    protected void setAuton(boolean auton) {
        this.auton = auton;
    }

    protected void enterContribution() throws SQLException {
        MySQL.addContribution(teamNumber, matchNumber, objectState, object, SID, time);
    }

    protected WazUpReturn wazUp() {
        return new WazUpReturn(SID, object, objectState);
    }
}

class WazUpReturn {
    public WazUpReturn(int SID, String object, String objectState) {
    }
}


