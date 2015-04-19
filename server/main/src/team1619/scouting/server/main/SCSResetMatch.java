package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 3/7/15.
 */
public class SCSResetMatch extends SCAMessage
{
    public SCSResetMatch()
    {

    }

    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException
    {
        conn.deleteMatch( message.getInteger( "matchNumber" ) );

        SCAMatch.closeMatch();

        SCJSON outMessage = new SCJSON();

        outMessage.put( "type", "status" );
        outMessage.put( "status", "matchReset" );

        for ( SCAClientQueue q : SCAOutbound.getClientQueues() )
        {
            q.writeToClient( outMessage );
        }

    }
}
