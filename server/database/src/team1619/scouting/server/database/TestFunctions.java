package team1619.scouting.server.database;

import java.sql.SQLException;

public class TestFunctions {
    public static void main(String[] args) {
        try {

            int teamNumber = 2;
            int matchNumber = 3;
            String mode = "b";
            String object = "c";
            int SID = 5;
            int matchTime = 6;
            String eventType = "a";

            String comments = "LOLZ";


            MySQL.connect();
//            team1619.scouting.server.database.MySQL.initialize();
//            team1619.scouting.server.database.MySQL.deleteTables();
//            team1619.scouting.server.database.MySQL.addContribution(teamNumber, matchNumber, mode, object, SID, matchTime);
//            team1619.scouting.server.database.MySQL.addRobotEvent(teamNumber, matchNumber, eventType, matchTime, comments);
            System.out.print(MySQL.checkSID(314, 1617));
            MySQL.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}