package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/19/15.
 */
public class SCWazUp extends SCMessage{

    public SCWazUp() {
    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        SCJSON response = new SCJSON();

        response.put( "type", "status" );
        response.put( "status", "wazUp" );

        SCOutbound.getClientQueue( getClientID() ).writeToClient( response );
    }
}
