package team1619.scouting.server.database;

import team1619.scouting.server.utils.SCProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * A match report is generated before each match and summarizes each team based
 * on the available data.
 *
 * A report is generated called matchnn.txt nn is the match number.
 */
public class MatchReport
{
    /**
     * Arguments to main:
     * [0] : name of user for properties file
     * [1] : match number to gen report for
     *
     * @param args the command line arguments
     */
    public static void main( String[] args )
    {
        MySQL db = null;
        try
        {
            if ( args.length != 2 )
            {
                System.out.println( "Command line argument count is incorrect.  Must have two arguments." );
                System.exit( 0 );
            }

            SCProperties.setPropertyFileName( String.format( "%s.scout.properties", args[ 0 ] ) );
            SCProperties.startup();
            db = MySQL.connect();

            int matchNumber = Integer.parseInt( args[ 1 ] );

            // open output file
            File reportDirectory = new File( SCProperties.getProperty( "report.directory" ) );

            if ( !reportDirectory.isDirectory() )
            {
                System.out.format( "Report directory %s does not exist.\n", SCProperties.getProperty( "event.directory" ) );
                System.exit( 0 );
            }

            String matchFileName = String.format( "match%d.html", matchNumber );

            PrintWriter out = new PrintWriter( new FileWriter( new File( reportDirectory, matchFileName ) ) );

            db.generateReport( out, SCProperties.getProperty( "event.code" ), matchNumber );

            db.close();

            out.close();

            System.out.format( "Report for match %d had been generated.\n", matchNumber );
        }
        catch (IllegalArgumentException ex )
        {
            System.out.println( ex.getMessage() );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }

        if ( db != null )
        {
            db.close();
        }
    }
}
