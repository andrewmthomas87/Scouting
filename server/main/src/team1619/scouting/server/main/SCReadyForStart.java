package team1619.scouting.server.main;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/19/15.
 */
public class SCReadyForStart extends SCMessage {

    private boolean fStarted;

    public SCReadyForStart(boolean started) {
        fStarted = started;
    }

    @Override
    void processMessage(Connection conn) throws SQLException {

    }
}
