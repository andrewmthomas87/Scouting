package team1619.scouting.server.main;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/19/15.
 */
public class SCPrepareNextMatch extends SCMessage {

    private int fMatchNumber;
    private int fTeamNumber;

    public int getMatchNumber() {
        return fMatchNumber;
    }

    public void setMatchNumber(int matchNumber) {
        this.fMatchNumber = matchNumber;
    }

    public int getTeamNumber() {
        return fTeamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.fTeamNumber = teamNumber;
    }

    public SCPrepareNextMatch() {

    }

    @Override
    void processMessage(Connection conn) throws SQLException {

    }

}
