package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 3/3/15.
 */
public class SCSetNextMatch extends SCMessage {


    @Override
    void processMessage(MySQL conn, SCJSON message) throws SQLException {
        SCMatch.newMatch(
                (Integer) message.get("matchNumber"),
                (Integer) message.get("redTeam1"),
                (Integer) message.get("redTeam2"),
                (Integer) message.get("redTeam3"),
                (Integer) message.get("blueTeam1"),
                (Integer) message.get("blueTeam2"),
                (Integer) message.get("blueTeam3"));
    }
}
