package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCProperties;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 3/7/15.
 */
public class SCMatchEnded extends SCMessage
{

    public SCMatchEnded()
    {
    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        SCMatch.closeMatch();

        SCClientQueue clientQueue = SCOutbound.setupClient();
        SCJSON outMessage = new SCJSON();

        outMessage.put( "type:", "status" );
        outMessage.put( "status", "ok");

        clientQueue.writeToClient( outMessage );

        // update the database for this match

        conn.setMatchPlayed( SCProperties.getProperty( "event.code" ), SCMatch.getMatchNumber() );
    }
}
