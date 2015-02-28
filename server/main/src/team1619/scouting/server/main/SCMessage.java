package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/16/15.
 */

abstract class SCMessage
{
    private Integer fClientID;

    void setClientID( Integer clientID )
    {
        fClientID = clientID;
    }

    Integer getClientID()
    {
        return fClientID;
    }

    abstract void processMessage( MySQL conn, SCJSON message ) throws SQLException;
}