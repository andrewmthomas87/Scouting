package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 3/17/15.
 */
public class SCBinRakeMessage extends SCMessage
{
    public SCBinRakeMessage()
    {

    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {

    }
}
