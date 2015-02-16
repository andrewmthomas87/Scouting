package team1619.scouting.server.database;

import java.sql.*;

public class MySQL {

    protected static Connection conn;
    private static int newMatchNumber;

    private static String[] tables = new String[]{
            // eventType: D = disabled, E = enabled (after disabled), F = fell over, C = comments
            "create table robotEvents (teamNumber int, matchNumber int, eventType char(1), matchTime int, comments varchar(1024))",


            //not used for now:
            "create table stacks (matchNumber int, SID int, totalHeight int, bin tinyint(1), litter tinyint(1), auton tinyint(1), scoringTeam int, platformType char(1), knockedBy int, matchTime int)",

            
            /*
            mode: A = autonomous, T = teleop
            object: Y = yellow tote, F = floor tote, H = chute tote, B = bin, L = litter, S = step, P = platform
            */

            "create table contributions (teamNumber int, matchNumber int, mode char(1), object char(1), SID int, matchTime int)"};

    private static String[] killTables = new String[]{
            "drop table robotEvents", "drop table stacks", "drop table contributions"};

    protected static void deleteTables() throws SQLException {
        Statement stmt = conn.createStatement();
        for (String kill : killTables) {
            stmt.execute(kill);
        }
        stmt.close();
    }

    protected static void connect() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://localhost/scout",
                "avi", "avi");
    }

    protected static void close() throws SQLException {
        conn.close();
    }

    protected static void initialize() throws SQLException {
        Statement stmt = conn.createStatement();
        for (String table : tables) {
            stmt.execute(table);
        }
        stmt.close();
    }

    public static void addContribution(int teamNumber, int matchNumber, String mode, String object, int SID, int matchTime) throws SQLException {
        PreparedStatement stmt = conn
                .prepareStatement("insert into contributions (teamNumber, matchNumber, mode, object, SID, matchTime) values (?,?,?,?,?,?)");
        stmt.setInt(1, teamNumber);
        stmt.setInt(2, matchNumber);
        stmt.setString(3, mode);
        stmt.setString(4, object);
        stmt.setInt(5, SID);
        stmt.setInt(6, matchTime);
        stmt.executeUpdate();
        stmt.close();
    }

    public static void addRobotEvent(int teamNumber, int matchNumber, String eventType, int matchTime, String comments) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("insert into robotEvents (teamNumber, matchNumber, eventType, eventTime, comments) values (?,?,?,?,?)");
        stmt.setInt(1, teamNumber);
        stmt.setInt(2, matchNumber);
        stmt.setString(3, eventType);
        stmt.setInt(4, matchTime);
        stmt.setString(5, comments);
        stmt.executeUpdate();
        stmt.close();
    }


    public static int checkSID(int matchNumber, int SID) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet resultSet = stmt.executeQuery("select SID from stacks where matchNumber=" + matchNumber + " && SID=" + SID);
        ResultSet nextSID = stmt.executeQuery("select max(SID) as maxSID from stacks");
        boolean availableSID = resultSet.wasNull(); //if input SID is not in the database, return true
        if (!availableSID) {                        //if it is in the database, do this
            if (nextSID.next()) {
                int newSID = nextSID.getInt("maxSID");
                SID = newSID ++;
            }
        }
        return SID;
    }
}