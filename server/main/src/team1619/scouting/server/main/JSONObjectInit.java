package team1619.scouting.server.main;

import java.sql.SQLException;

/**
 * Created by avimoskoff on 2/15/15.
 */

public class JSONObjectInit {

    static int MID;
    static int CID;
    static int SID;
    static int time;
    static int teamNumber;

    static boolean started = false;
    static boolean auton = true;

    static String objects;

    static String scoutName;

    static JSONObject client0 = new JSONObject();
    static JSONObject client1 = new JSONObject();
    static JSONObject client2 = new JSONObject();
    static JSONObject client3 = new JSONObject();
    static JSONObject client4 = new JSONObject();
    static JSONObject client5 = new JSONObject();

    public static void main(String[] args) {
        switch (MID) {
            case 0:
                client0.setScoutName(scoutName);
                client0.getCID();
                client1.setScoutName(scoutName);
                client1.getCID();
                client2.setScoutName(scoutName);
                client2.getCID();
                client3.setScoutName(scoutName);
                client3.getCID();
                client4.setScoutName(scoutName);
                client4.getCID();
                client5.setScoutName(scoutName);
                client5.getCID();
                break;

            case 1:
                switch (CID) {
                    case 0:
                        client0.getMatchNumber();
                        client0.setTeamNumber(teamNumber);
                        break;
                    case 1:
                        client1.getMatchNumber();
                        client1.setTeamNumber(teamNumber);
                        break;
                    case 2:
                        client2.getMatchNumber();
                        client2.setTeamNumber(teamNumber);
                        break;
                    case 3:
                        client3.getMatchNumber();
                        client3.setTeamNumber(teamNumber);
                        break;
                    case 4:
                        client4.getMatchNumber();
                        client4.setTeamNumber(teamNumber);
                        break;
                    case 5:
                        client5.getMatchNumber();
                        client5.setTeamNumber(teamNumber);
                        break;
                }
                break;
            case 2:
                started = true;
                break;
            case 3:
                switch(CID) {
                    case 0:
                        client0.setSID(SID);
                        client0.setObject(objects);
                        client0.setTime(time);
                        client0.setAuton(auton);
                        try {
                            client0.enterContribution();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        client1.setSID(SID);
                        client1.setObject(objects);
                        client1.setTime(time);
                        client1.setAuton(auton);
                        try {
                            client1.enterContribution();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;

                }
        }
    }
}