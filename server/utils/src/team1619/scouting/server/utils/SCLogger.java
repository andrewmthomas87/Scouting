package team1619.scouting.server.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * This class is a general purpose logger.
 * * 
 * Created by tolkin on 2/15/2015.
 */
public class SCLogger implements SCSubsystem
{
    // the parameter in the log name will be replaced with the date and time the
    // log file was created
    private static final String LOG_NAME = "scout_log_%s.txt";
    
    // the file that holds the log
    private final FileWriter fLog;
    
    // the singleton logger object
    private static SCLogger sLogger;

    /**
     * Gets the singleton logger object.
     * 
     * Precondition: logger must have been initialized before 
     *  
     * @return the logger object.
     * 
     * @throws InstantiationError the logger was never initialized
     */
    public static SCLogger getLogger()
            throws InstantiationError
    {
        if ( sLogger == null )
        {
            throw new InstantiationError( "logger was never initialized" );
        }
        return sLogger;
    }

    @Override
    public void startup() throws InstantiationException
    {
        try
        {
            sLogger = new SCLogger( SCProperties.getProperty( "log.directory" ) );
        }
        catch ( IOException ex )
        {
            throw new InstantiationException( ex.getMessage() );
        }
    }
    
    @Override
    public void shutdown()
    {
        sLogger.close();
    }

    /**
     * Constructs a logger by creating the logger file.
     * 
     * @param directory the directory in which the log files will be created
     *                
     * @throws InstantiationException if directory invalid or unable to create
     * @throws IOException if unable to create the log file
     */
    private SCLogger( String directory )
            throws InstantiationException, IOException
    {
        File dir = new File( directory );
        
        if ( !dir.exists() )
        {
            // directory doesn't exist
            // prompt user to create it
            System.out.format( "Directory '%s' does not exist, create it? (y/n) ", directory );

            Scanner in = new Scanner( System.in );
            
            String answer = in.nextLine();
            
            if ( answer.startsWith( "y" ) || answer.startsWith( "Y" ) )
            {
                // create the directory
                if ( !dir.mkdir() )
                {
                    System.out.println( "Unable to create the logging directory." );
                    throw new InstantiationException( "unable to create logging directory" );
                }
            }
            
        }
            
        if ( !dir.isDirectory() )
        {
            // make sure the path is a directory
            System.out.format( "The path '%s' does not name a directory.\n", directory );
            throw new InstantiationException( "unable to open logging directory" );
        }
        
        // finally, create the logging file
        // get the timestamp as a string
        SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd-HHmm" );
        String timestamp = format.format( new Date() );
        
        fLog = new FileWriter( new File( dir, String.format( LOG_NAME, timestamp ) ) );
    }

    /**
     * Closes the log file.
     */
    private void close()
    {
        try
        {
            fLog.close();
        }
        catch ( IOException ex )
        {
            // ignore exception on close
        }
    }
}
