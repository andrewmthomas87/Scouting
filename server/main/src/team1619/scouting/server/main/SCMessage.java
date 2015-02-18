package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;

import java.sql.*;

/**
 * Created by avimoskoff on 2/16/15.
 */

abstract class SCMessage {
    private int fClientID;
    void setClientID(int clientID) {
        fClientID = clientID;
    }
    int getClientID() {
        return fClientID;
    }
    abstract void processMessage(Connection conn) throws SQLException;
}