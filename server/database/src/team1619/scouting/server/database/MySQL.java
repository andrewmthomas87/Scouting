package team1619.scouting.server.database;

import team1619.scouting.server.main.SCMatch;
import team1619.scouting.server.utils.SCLogger;
import team1619.scouting.server.utils.SCProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class MySQL
{
    private Connection fConnection;

    private static String[] tables = new String[]
            {
                    // eventType: D = disabled, E = enabled (after disabled), F = fell over, C = comments, R = rake bin (A), S = rake bin (Teleop)
                    "create table robotEvents (eventCode varchar(12), teamNumber int, matchNumber int, eventType char(1), matchTime int, comments varchar(1024))",


                    // not used for now:
                    "create table stacks (matchNumber int, SID int, totalHeight int, bin tinyint(1), litter tinyint(1), auton tinyint(1), scoringTeam int, platformType char(1), knockedBy int, matchTime int)",

            
                    /*
                    mode: A = autonomous, T = teleop
                    object: Y = yellow tote, F = floor tote, H = chute tote, B = bin, L = litter, S = step, P = platform,
                    K = stack (K<int>)
                    */

                    "create table contributions (eventCode varchar(12), teamNumber int, matchNumber int, mode char(1), object char(1), SID int, otherSID int, matchTime int)",

                    "create table eventMatches (eventCode varchar(12), matchNumber int, played boolean default false, redTeam1 int, redTeam2 int, redTeam3 int, blueTeam1 int, blueTeam2 int, blueTeam3 int)",

                    "create table matchScouts(eventCode varchar(12), matchNumber int, teamNumber int, scoutName varchar(64))"
            };

    private static String[] killTables = new String[]
            {
                    "drop table if exists robotEvents",
                    "drop table if exists stacks",
                    "drop table if exists contributions",
                    "drop table if exists eventMatches",
                    "drop table if exists matchScouts"
            };

    public void deleteTables() throws SQLException
    {
        Statement stmt = fConnection.createStatement();

        for ( String kill : killTables )
        {
            stmt.execute( kill );
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
        try
        {
            String dbURL = SCProperties.getProperty( "db.url" );
            String dbUser = SCProperties.getProperty( "db.user" );
            String dbPassword = SCProperties.getProperty( "db.password" );

            fConnection = DriverManager.getConnection( "jdbc:mysql:" + dbURL, dbUser, dbPassword );
        }
        catch ( SQLException ex )
        {
            SCLogger.getLogger().error( "Problem establishing connection: %s", ex.getMessage() );
            throw ex;
        }
    }

    public void close() throws SQLException
    {
        fConnection.close();
    }

    public void initialize() throws SQLException
    {
        Statement stmt = fConnection.createStatement();
        for ( String table : tables )
        {
            stmt.execute( table );
        }
        stmt.close();
    }

    public void addContribution( String eventCode, int teamNumber, int matchNumber, String mode, String object, int SID, int matchTime ) throws SQLException
    {
        Integer otherSID = null;

        if ( object.charAt( 0 ) == 'K' )
        {
            otherSID = Integer.valueOf( object.substring( 1 ) );
            object = "K";
        }
        PreparedStatement stmt = fConnection
                .prepareStatement( "insert into contributions (teamNumber, matchNumber, mode, object, SID, matchTime, otherSID, eventCode) " +
                                           "values (?,?,?,?,?,?,?,?)" );
        stmt.setInt( 1, teamNumber );
        stmt.setInt( 2, matchNumber );
        stmt.setString( 3, mode );
        stmt.setString( 4, object );
        stmt.setInt( 5, SID );
        stmt.setInt( 6, matchTime );
        if ( otherSID == null )
        {
            stmt.setNull( 7, Types.INTEGER );
        }
        else
        {
            stmt.setInt( 7, otherSID );
        }
        stmt.setString( 8, eventCode );

        stmt.executeUpdate();

        stmt.close();
    }

    public int getNextSID() throws SQLException
    {
        Statement stmt = fConnection.createStatement();
        ResultSet resultSet = stmt.executeQuery( "select UUID_SHORT()");
        resultSet.next();
        int SID = (int)(0x7fffffffL & resultSet.getLong( 1 ));
        stmt.close();
        return SID;
    }

    public void addMatchData( String eventCode, int matchNumber, int redTeam1, int redTeam2, int redTeam3,
                              int blueTeam1, int blueTeam2, int blueTeam3 )
            throws SQLException
    {
        // first see if we are inserting or updating
        PreparedStatement matchDataExistsStmt =
                fConnection.prepareStatement( "select matchNumber from eventMatches where matchNumber=? and eventCode=?" );

        matchDataExistsStmt.setInt( 1, matchNumber );
        matchDataExistsStmt.setString( 2, eventCode );

        ResultSet existsCheck = matchDataExistsStmt.executeQuery();

        if ( existsCheck.next() )
        {
            // match exists, so we need to do an update

            PreparedStatement stmt =
                    fConnection.prepareStatement( "update eventMatches set redTeam1=?, redTeam2=?, redTeam3=?, " +
                                                          "blueTeam1=?, blueTeam2=?, blueTeam3=? where matchNumber=? and eventCode=?" );

            stmt.setInt( 1, redTeam1 );
            stmt.setInt( 2, redTeam2 );
            stmt.setInt( 3, redTeam3 );
            stmt.setInt( 4, blueTeam1 );
            stmt.setInt( 5, blueTeam2 );
            stmt.setInt( 6, blueTeam3 );

            stmt.setInt( 7, matchNumber );
            stmt.setString( 8, eventCode );

            stmt.execute();

            stmt.close();
        }
        else
        {
            // match does not yet exist, so insert

            PreparedStatement stmt =
                    fConnection.prepareStatement( "insert into eventMatches(eventCode, matchNumber, " +
                                                          "played, redTeam1, redTeam2, redTeam3, blueTeam1, blueTeam2, blueTeam3) values (?,?,false,?,?,?,?,?,?)" );

            stmt.setString( 1, eventCode );
            stmt.setInt( 2, matchNumber );

            stmt.setInt( 3, redTeam1 );
            stmt.setInt( 4, redTeam2 );
            stmt.setInt( 5, redTeam3 );
            stmt.setInt( 6, blueTeam1 );
            stmt.setInt( 7, blueTeam2 );
            stmt.setInt( 8, blueTeam3 );

            stmt.execute();

            stmt.close();
        }

        matchDataExistsStmt.close();
    }

    /**
     * Gets the next match (by number) that has yet to be played.  We set up the SCMatch
     * object so that the next match data is available.
     *
     * @return the next match number or -1 if no next match is available
     */
    public int setupNextMatch( String eventCode ) throws SQLException
    {
        PreparedStatement nextMatchStmt =
                fConnection.prepareStatement( "select matchNumber, redTeam1, redTeam2, redTeam3," +
                                                      " blueTeam1, blueTeam2, blueTeam3 from eventMatches where eventCode=? and" +
                                                      " matchNumber=(select min(matchNumber) from eventMatches where eventCode=? and" +
                                                      " played=false)" );

        nextMatchStmt.setString( 1, eventCode );
        nextMatchStmt.setString( 2, eventCode );

        ResultSet matchDataSet = nextMatchStmt.executeQuery();

        int matchNumber;

        if ( matchDataSet.next() )
        {
            matchNumber = matchDataSet.getInt( 1 );
            int redTeam1 = matchDataSet.getInt( 2 );
            int redTeam2 = matchDataSet.getInt( 3 );
            int redTeam3 = matchDataSet.getInt( 4 );
            int blueTeam1 = matchDataSet.getInt( 5 );
            int blueTeam2 = matchDataSet.getInt( 6 );
            int blueTeam3 = matchDataSet.getInt( 7 );

            // set up the next match

            SCMatch.setCurrentMatch( matchNumber, redTeam1, redTeam2, redTeam3, blueTeam1, blueTeam2, blueTeam3 );
        }
        else
        {
            // no next match

            matchNumber = -1;
        }

        matchDataSet.close();
        nextMatchStmt.close();

        return matchNumber;
    }

    /**
     * Called when we get a match started event.  This will ensure that this match number is not
     * retrieved again.
     *
     * @param eventCode the event code
     * @param matchNumber the match number to mark as played
     *
     * @throws SQLException if an error occurs
     */
    public void setMatchPlayed(String eventCode, int matchNumber) throws SQLException
    {
        PreparedStatement matchStartedStmt =
                fConnection.prepareStatement( "update eventMatches set played=true where matchNumber=? and eventCode=? " );

        matchStartedStmt.setInt( 1, matchNumber );
        matchStartedStmt.setString( 2, eventCode );

        matchStartedStmt.execute();

        matchStartedStmt.close();
    }

    /**
     * Deletes intermediate results from the database.  Used when the match needed to be reset.
     *
     * @param matchNumber the match number being reset
     *
     * @throws SQLException if an error occurs
     */
    public void deleteMatch(int matchNumber) throws SQLException
    {
        PreparedStatement matchDeleteStmt = fConnection.prepareStatement( "delete from contributions where matchNumber = ?" );
        matchDeleteStmt.setInt( 1, matchNumber );

        matchDeleteStmt.execute();

        matchDeleteStmt.close();
    }

    public void addRobotEvent( String eventCode, int matchNumber, int teamNumber, String eventType, int matchTime, String comments ) throws SQLException
    {
        PreparedStatement stmt =
                fConnection.prepareStatement( "insert into robotEvents(eventCode, matchNumber, teamNumber, eventType, matchTime, comments) values (?,?,?,?,?,?)" );

        stmt.setString( 1, eventCode );
        stmt.setInt( 2, matchNumber );
        stmt.setInt( 3, teamNumber );
        stmt.setString( 4, eventType );
        stmt.setInt( 5, matchTime );

        if ( comments == null || comments.isEmpty() )
        {
            stmt.setNull( 6, Types.VARCHAR );
        }
        else
        {
            stmt.setString( 6, comments );
        }

        stmt.executeUpdate();

        stmt.close();
    }

    public void assignScoutToTeam( String eventCode, int matchNumber, int teamNumber, String scoutName ) throws SQLException
    {
        PreparedStatement stmt =
                fConnection.prepareStatement( "insert into matchScouts(eventCode, matchNumber, teamNumber, scoutName) values (?,?,?,?)" );

        stmt.setString( 1, eventCode );
        stmt.setInt( 2, matchNumber );
        stmt.setInt( 3, teamNumber );
        stmt.setString( 4, scoutName );

        stmt.executeUpdate();

        stmt.close();
    }

    public void disconnectClient( String eventCode, int matchNumber, int teamNumber ) throws SQLException
    {
        PreparedStatement stmt =
                fConnection.prepareStatement( "delete from matchScouts where eventCode=? and matchNumber=? and teamNumber=?" );

        stmt.setString( 1, eventCode );
        stmt.setInt( 2, matchNumber );
        stmt.setInt( 3, teamNumber );

        stmt.execute();

        stmt.close();
    }
}