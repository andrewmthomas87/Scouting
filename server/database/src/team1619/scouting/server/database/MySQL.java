package team1619.scouting.server.database;

import team1619.scouting.server.main.SCMatch;
import team1619.scouting.server.utils.SCLogger;
import team1619.scouting.server.utils.SCProperties;

import java.io.PrintWriter;
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
                    // eventType: D = disabled, E = enabled (after disabled), F = fell over, C = comments, R = rake bin (A), S = rake bin (Teleop), M = moved in auto
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

    public void close()
    {
        try
        {
            fConnection.close();
        }
        catch ( SQLException ex )
        {
            // swallow exception
        }
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

    public void generateReport( PrintWriter out, String eventCode, int matchNumber )
            throws SQLException, IllegalArgumentException
    {
        // get a cursor for each team in the match
        PreparedStatement teamList =
                fConnection.prepareStatement( "select redTeam1, redTeam2, redTeam3, blueTeam1, blueTeam2, blueTeam3 from eventMatches where matchNumber=? and eventCode=?" );

        teamList.setInt( 1, matchNumber );
        teamList.setString( 2, eventCode );

        ResultSet teamListSet = teamList.executeQuery();

        int[] teams = new int[ 6 ];

        if ( teamListSet.next() )
        {
            // should be one row
            for ( int i = 0; i < 6; i++ )
            {
                teams[ i ] = teamListSet.getInt( i + 1 );
            }
        }
        else
        {
            // no match number
            teamListSet.close();
            throw new IllegalArgumentException( "No such match number: " + matchNumber );
        }

        PreparedStatement isCoop =
                fConnection.prepareStatement( "select avg(coop) from " +
                        "(select count(*) coop from contributions " +
                        "where teamNumber=? and eventCode=? and object='Y' and mode='A' group by matchNumber) c" );

        PreparedStatement isRakinate =
                fConnection.prepareStatement( "select avg(rake), eventType from " +
                        " (select count(*) rake, eventType from robotEvents " +
                        "where teamNumber=? and eventCode=? and eventType in ('R', 'S') group by matchNumber, eventType) r" +
                                                      " group by eventType");

        PreparedStatement stackedObjects =
                fConnection.prepareStatement( "select avg(totes) from " +
                                                      "(select count(*) totes from contributions "+
                                            "where teamNumber=? and eventCode=? and object=? group by matchNumber) t" );

        // for each team, get report data and write to file
        out.format(  "Scouting report for match %d\n", matchNumber );
        out.println( "----------------------------" );

        for ( int team : teams )
        {
            // questions:
            // does team participate in coopertition (can they stack yellow totes on step?)
            // does team rakinate (do they pull bins in auto or teleop mode?)
            // how many totes are scored from chute?
            // how many totes are scored from floor?
            // how many bins are scored?

            out.format( "Team %d\n", team );
            out.println();

            isCoop.setInt( 1, team );
            isCoop.setString( 2, eventCode );

            ResultSet isCoopSet = isCoop.executeQuery();

            if ( isCoopSet.next() )
            {
                // we have a row
                double avgCoopTotes = isCoopSet.getDouble( 1 );
                if ( isCoopSet.wasNull() )
                {
                    out.println( "No data on coopertition totes.");
                }
                else
                {
                    if ( avgCoopTotes > 0 )
                    {
                        // this team stacks coop totes
                        out.format( "Stacks coopertition totes.  Average number stacked per match: %.1f\n", avgCoopTotes );
                    }
                    else
                    {
                        out.println( "Team does NOT stack coopertition totes." );
                    }
                }
            }
            else
            {
                out.println( "No data on coopertition totes." );
            }
            isCoopSet.close();

            // find rakinate data
            isRakinate.setInt( 1, team );
            isRakinate.setString( 2, eventCode );

            ResultSet isRakinateSet = isRakinate.executeQuery();

            boolean autoRakeOut = false;
            boolean teleopRakeOut = false;

            while ( isRakinateSet.next() )
            {
                // we have a row
                double avgRake = isRakinateSet.getDouble( 1 );
                boolean rakeNull = isRakinateSet.wasNull();
                String rakeTime = isRakinateSet.getString( 2 );

                if ( "r".equalsIgnoreCase( rakeTime ) )
                {
                    if ( rakeNull )
                    {
                        out.println( "Team does not rake in AUTO mode." );
                    }
                    else
                    {
                        // this is in auto mode
                        if ( avgRake > 0 )
                        {
                            out.format( "Team rakes an average of %.1f bins in AUTO mode.\n", avgRake );
                        }
                        else
                        {
                            out.println( "Team does NOT rake in AUTO mode." );
                        }
                    }
                    autoRakeOut = true;
                }
                else if ( "s".equalsIgnoreCase( rakeTime ) )
                {
                    // this is teleop mode
                    if ( rakeNull )
                    {
                        out.println( "Team does not rake in TELEOP mode.");
                    }
                    else
                    {
                        if ( avgRake > 0 )
                        {
                            out.format( "Team rakes an average of %.1f bins in TELEOP mode.\n", avgRake );
                        }
                        else
                        {
                            out.println( "Team does NOT rake in TELEOP mode." );
                        }
                    }
                    teleopRakeOut = true;
                }
            }

            isRakinateSet.close();

            if ( !autoRakeOut )
            {
                out.println( "Team does NOT rake in AUTO mode." );
            }
            if ( !teleopRakeOut )
            {
                out.println( "Team does NOT rake in TELEOP mode." );
            }

            // compute number of totes gotten from chute
            stackedObjects.setInt( 1, team );
            stackedObjects.setString( 2, eventCode );
            stackedObjects.setString( 3, "H" );

            ResultSet chuteTotesSet = stackedObjects.executeQuery();
            if ( chuteTotesSet.next() )
            {
                double avgTote = chuteTotesSet.getDouble( 1 );
                if ( avgTote > 0 )
                {
                    out.format( "Team loads an average of %.1f totes from CHUTE.\n", avgTote );
                }
                else
                {
                    out.println( "Team does NOT load totes from CHUTE." );
                }
            }
            else
            {
                out.println( "Team does NOT load totes from CHUTE." );
            }
            chuteTotesSet.close();

            // compute number of totes gotten from floor
            stackedObjects.setString( 3, "F" );

            ResultSet floorTotesSet = stackedObjects.executeQuery();
            if ( floorTotesSet.next() )
            {
                double avgTote = floorTotesSet.getDouble( 1 );
                if ( avgTote > 0 )
                {
                    out.format( "Team loads an average of %.1f totes from FLOOR.\n", avgTote );
                }
                else
                {
                    out.println( "Team does NOT load totes from FLOOR." );
                }
            }
            else
            {
                out.println( "Team does NOT load totes from FLOOR." );
            }
            floorTotesSet.close();

            // compute number of bins scored
            stackedObjects.setString( 3, "B" );

            ResultSet binSet = stackedObjects.executeQuery();
            if ( binSet.next() )
            {
                double avgTote = binSet.getDouble( 1 );
                if ( avgTote > 0 )
                {
                    out.format( "Team scores an average of %.1f bins.\n", avgTote );
                }
                else
                {
                    out.println( "Team does NOT use bins." );
                }
            }
            else
            {
                out.println( "Team does NOT use bins." );
            }
            binSet.close();

            out.println("====================\n");
        }
    }
}