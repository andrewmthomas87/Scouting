package team1619.scouting.server.main;

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
public class SCStartup
{
    /**
     * Starts the scouting server.  The server will look in the local directory
     * for scout.properties for the properties file.
     * 
     * @param args unused
     */
    public static void main( String[] args )
    {
        startupSubsystems();
        
        try
        {
            // start the main listener on its own thread
            Thread listener = new SCListener( SCThreadPool.getPool() );
            listener.start();

            listener.join();
        }
        catch ( Throwable t )
        {
            SCLogger.getLogger().info( "Exiting server." );
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
