package team1619.scouting.server.main;

import team1619.scouting.server.utils.SCLogger;
import team1619.scouting.server.utils.SCProperties;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is the main listener thread.  It will open the TCP socket and listen for
 * inbound HTTP requests.  It will then assign a worker thread to process it.
 * 
 * Created by tolkin on 2/15/2015.
 */
public class SCListener extends Thread
{
    private int fPort;
    private SCThreadPool fPool;
    private Boolean fExit;
    
    public SCListener( SCThreadPool pool )
    {
        fPort = Integer.parseInt( SCProperties.getProperty( "network.listener.port" ) );
        fPool = pool;
        fExit = false;
    }
    
    @Override
    public void run()
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket( fPort );

            while ( !fExit )
            {
                SCLogger.getLogger().debug( "Waiting for next inbound..." );
                
                Socket inbound = serverSocket.accept();
                
                SCLogger.getLogger().debug( "Getting inbound request from %s", inbound.getInetAddress() );
                
                fPool.assignThread( inbound, this );
            }
            
            serverSocket.close();
        }
        catch ( Throwable t )
        {
            SCLogger.getLogger().error( "Problem listing for requests: %s", t.getMessage() );
            SCLogger.getLogger().printStackTrace( t );
        }
    }

    /**
     * Sets the exit flag on this listener so that it will exit.  
     */
    public void setExit()
    {
        fExit = true;
    }
}
