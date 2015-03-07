package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCLogger;
import team1619.scouting.server.utils.SCProperties;

/**
 * This class has the scouting server main method.
 * 
 * This will:
 * 1. Start the various subsystems.
 * 2. Open the port on which we are listening for connections.
 * 3. Start listening for connections.
 * 4. Assign a thread to handle an inbound request.
 * 5. Process the request.
 * 
 * This process only shuts down if it receives a shutdown message.
 * 
 * Created by tolkin on 2/15/2015.
 */
public class SCServer
{
    /**
     * Starts the scouting server.  The server will look in the local directory
     * for scout.properties for the properties file.
     * 
     * @param args command-line arguments
     * <username for scout.properties>: this is a username that is prepended to scout.properties such
     *             as avi.scout.properties
     * --initialize : this will create the tables in the database
     * --clean-db: deletes old database tables before initializing
     * --event-code: the code for this event
     */
    public static void main( String[] args )
    {

        if ( args.length == 0 )
        {
            System.out.println( "Missing required argument: username for scout properties" );
            System.exit(0);
        }

        String username = args[ 0 ];
        String propertiesFileName = String.format( "%s.scout.properties", username );
        SCProperties.setPropertyFileName( propertiesFileName );

        startupSubsystems();

        try
        {
            if ( args.length > 0 )
            {
                for ( String arg : args )
                {
                    if ( "--initialize".equals( arg ) )
                    {
                        MySQL db = MySQL.connect();
                        db.initialize();
                        db.close();
                    }
                    else if ( arg.startsWith( "--eventCode=" ) )
                    {
                        String eventCode = arg.substring( arg.indexOf( "=" ) + 1 );

                        // this will override a property given in the file

                        SCProperties.setProperty( "event.code", eventCode );
                    }
                    else if ( "--clean-db".equals( arg ) )
                    {
                        // drops tables and then initializes
                        MySQL db = MySQL.connect();
                        db.deleteTables();
                        db.initialize();
                        db.close();
                        System.exit( 0 );
                    }
                }

            }

            // start the main listener on its own thread
            Thread listener = new SCListener( SCThreadPool.getPool() );
            listener.start();

            listener.join();
        }
        catch ( Throwable t )
        {
            SCLogger.getLogger().info( "Exiting server." );
            SCLogger.getLogger().error( "Exception: %s", t.getMessage() );
        }

        shutdownSubsystems();
    }

    /**
     * Starts the subsystems 
     */
    private static void startupSubsystems()
    {
        try
        {
            SCProperties.startup();
            SCLogger.startup();

            // after this, logger can be used
            SCThreadPool.startup();
        }
        catch ( Throwable t )
        {
            System.out.println( "Exception while starting subsystems. " );
            t.printStackTrace();
        }
    }

    /**
     * Shutdown all the subsystems
     */
    private static void shutdownSubsystems()
    {
        SCProperties.shutdown();
        
        // logger should be last
        SCLogger.shutdown();
    }
}
