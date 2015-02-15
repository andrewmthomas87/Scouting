package team1619.scouting.server.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * This class is a general purpose logger.
 * * 
 * Created by tolkin on 2/15/2015.
 */
public class SCLogger
{
    private enum SCLogLevels
    {
        INFO, ERROR, WARNING, DEBUG
    };
    
    // the parameter in the log name will be replaced with the date and time the
    // log file was created
    private static final String LOG_NAME = "scout_log_%s.txt";
    
    // the file that holds the log
    private final FileWriter fLog;
    
    private final SimpleDateFormat fLogTimeFormat;
    
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

    public static void startup() throws InstantiationException
    {
        try
        {
            sLogger = new SCLogger();
        }
        catch ( IOException ex )
        {
            throw new InstantiationException( ex.getMessage() );
        }
    }
    
    public static void shutdown()
    {
        sLogger.close();
    }

    /**
     * Constructs a logger by creating the logger file.
     * 
     * @throws InstantiationException if directory invalid or unable to create
     * @throws IOException if unable to create the log file
     */
    private SCLogger() throws InstantiationException, IOException
    {
        String directory = SCProperties.getProperty( "log.directory" );
        File dir = new File( directory );

        fLogTimeFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

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

    /**
     * Writes a message at the info level to the log.
     * 
     * @param msg the message to write
     * @param args the arguments to the message
     *  
     * @throws SCIOException if there is a problem writing to the log
     */
    public void info( String msg, Object ... args ) throws SCIOException
    {
        logWrite( SCLogLevels.INFO.toString(), msg, args );
    }

    /**
     * Writes a message at the warning level to the log.
     *
     * @param msg the message to write
     * @param args the arguments to the message
     *
     * @throws SCIOException if there is a problem writing to the log
     */
    public void warning( String msg, Object ... args ) throws SCIOException
    {
        logWrite( SCLogLevels.WARNING.toString(), msg, args );
    }

    /**
     * Writes a message at the error level to the log.
     *
     * @param msg the message to write
     * @param args the arguments to the message
     *
     * @throws SCIOException if there is a problem writing to the log
     */
    public void error( String msg, Object ... args ) throws SCIOException
    {
        logWrite( SCLogLevels.ERROR.toString(), msg, args );
    }

    /**
     * Writes a message at the debug level to the log.
     *
     * @param msg the message to write
     * @param args the arguments to the message
     *
     * @throws SCIOException if there is a problem writing to the log
     */
    public void debug( String msg, Object ... args ) throws SCIOException
    {
        logWrite( SCLogLevels.DEBUG.toString(), msg, args );
    }

    /**
     * Prints a stack dump to the logger.
     *
     * @param t the exception
     */
    public void printStackTrace( Throwable t )
    {
        PrintWriter out = new PrintWriter( fLog );
        t.printStackTrace( out );
    }

    private void logWrite( String level, String msg, Object ... args ) throws SCIOException
    {
        try
        {
            String formattedString = String.format( "%s [%s]: %s\n", fLogTimeFormat.format( new Date() ), level, msg );
            
            fLog.write( String.format( formattedString, args ) );
        }
        catch ( IOException ex )
        {
            throw new SCIOException( ex );
        }
    }
}
