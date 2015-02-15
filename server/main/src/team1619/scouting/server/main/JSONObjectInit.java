package team1619.scouting.server.main;

/**
 * Created by avimoskoff on 2/15/15.
 */
public class JSONObjectInit {

    static String scout1;
    static String scout2;
    static String scout3;
    static String scout4;
    static String scout5;
    static String scout6;

    public static void main(String[] args) {



        JSONObject JSONO1 = new JSONObject();
        JSONObject JSONO2 = new JSONObject();
        JSONObject JSONO3 = new JSONObject();
        JSONObject JSONO4 = new JSONObject();
        JSONObject JSONO5 = new JSONObject();
        JSONObject JSONO6 = new JSONObject();

        JSONO1.setCID(1);
        JSONO2.setCID(2);
        JSONO3.setCID(3);
        JSONO4.setCID(4);
        JSONO5.setCID(5);
        JSONO6.setCID(6);

        JSONO1.setScoutName(scout1);
        JSONO2.setScoutName(scout2);
        JSONO3.setScoutName(scout3);
        JSONO4.setScoutName(scout4);
        JSONO5.setScoutName(scout5);
        JSONO6.setScoutName(scout6);


    }
}
