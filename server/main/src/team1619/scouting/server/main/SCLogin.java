package team1619.scouting.server.main;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/19/15.
 */

public class SCLogin extends SCMessage {

    private String fScoutName;
    private int fClientID;

    void setScoutName(String scoutName) {
        fScoutName = scoutName;
    }

    @Override
    void processMessage(Connection conn) throws SQLException {
    }
}