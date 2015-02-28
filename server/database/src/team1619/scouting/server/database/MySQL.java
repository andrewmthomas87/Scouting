package team1619.scouting.server.database;

import team1619.scouting.server.utils.SCProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL
{

    private Connection fConnection;

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

    protected void deleteTables() throws SQLException {
        Statement stmt = fConnection.createStatement();

        for (String kill : killTables) {
            stmt.execute(kill);
        }
        stmt.close();
    }

    public static MySQL connect() throws SQLException
    {
        MySQL connection = new MySQL();

        connection.establishConnection();

        return connection;
    }

    private void establishConnection() throws SQLException
    {
        String dbURL      = SCProperties.getProperty( "db.url" );
        String dbUser     = SCProperties.getProperty( "db.user" );
        String dbPassword = SCProperties.getProperty( "db.password" );

        fConnection = DriverManager.getConnection("jdbc:mysql:" + dbURL, dbUser, dbPassword );
    }

    public void close() throws SQLException {
        fConnection.close();
    }

    protected void initialize() throws SQLException {
        Statement stmt = fConnection.createStatement();
        for (String table : tables) {
            stmt.execute(table);
        }
        stmt.close();
    }

    public void addContribution(int teamNumber, int matchNumber, String mode, String object, int SID, int matchTime) throws SQLException {
        PreparedStatement stmt = fConnection
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

    public void addRobotEvent(int teamNumber, int matchNumber, String eventType, int matchTime, String comments) throws SQLException {
        PreparedStatement stmt = fConnection.prepareStatement("insert into robotEvents (teamNumber, matchNumber, eventType, eventTime, comments) values (?,?,?,?,?)");
        stmt.setInt(1, teamNumber);
        stmt.setInt(2, matchNumber);
        stmt.setString(3, eventType);
        stmt.setInt(4, matchTime);
        stmt.setString(5, comments);
        stmt.executeUpdate();
        stmt.close();
    }


    public int checkSID(int matchNumber, int SID) throws SQLException {
        Statement stmt = fConnection.createStatement();
        ResultSet resultSet = stmt.executeQuery("select SID from stacks where matchNumber=" + matchNumber + " && SID=" + SID);
        ResultSet nextSID = stmt.executeQuery("select max(SID) as maxSID from stacks");
        boolean availableSID = resultSet.wasNull(); //if input SID is not in the database, return true
        if (!availableSID) {                        //if it is in the database, do this
            if (nextSID.next()) {
                int newSID = nextSID.getInt("maxSID");
                SID = newSID++;
            }
        }
        return SID;
    }
}