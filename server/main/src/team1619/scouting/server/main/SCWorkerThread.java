package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCLogger;
import team1619.scouting.server.utils.SCProperties;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * A worker thread processes an inbound connection.
 * * 
 * Created by tolkin on 2/15/2015.
 */
public class SCWorkerThread extends Thread
{
    private final Object fWaitFlag;
    
    private Socket fInboundSocket;
    private SCListener fListener;
    private SCThreadPool fPool;
    private Connection conn;
    
    public SCWorkerThread()
    {
        // all workers are daemons
        setDaemon( true );
        
        fWaitFlag = new Object();
    }

    /**
     * This just waits until there is work to do. 
     */
    public void run()
    {
        try {
            MySQL.connect(conn);
        } catch(SQLException e) {
            SCLogger.getLogger().warning("failed to connect to database");
        }
        while ( true )
        {
            synchronized ( fWaitFlag )
            {
                try
                {
                    fWaitFlag.wait();
                }
                catch ( InterruptedException ex )
                {
                    SCLogger.getLogger().warning( "Thread was interrupted and will now exit." );
                }
            }
            executeWork(conn);
        }
        try {
            MySQL.close(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Assigns the inbound request to this thread so it can read the message and process it.
     *
     * @param request the socket with the inbound request
     * @param listener the listener object
     */
    public void assignWork( Socket request, SCListener listener, SCThreadPool pool )
    {
        fInboundSocket = request;
        fListener      = listener;
        fPool          = pool;
        
        SCLogger.getLogger().debug( "Assigning work to thread %s", getName() );
        
        synchronized ( fWaitFlag )
        {
            // ok, we have work to do, so let this thread now run
            fWaitFlag.notifyAll();
        }
    }

    /**
     * Executes the work of this thread.
     */
    private void executeWork(Connection conn)
    {
        SCLogger.getLogger().debug( "Starting work on thread %s", getName() );
        
        try
        {
            // reads the input channel
            InputStream requestStream = fInboundSocket.getInputStream();
            
            // for now, just write the request to the console
            BufferedReader in = new BufferedReader( new InputStreamReader( requestStream ) );
            
            System.out.println( "Writing input to screen" );
            
            int bodyLength = 0;
            
            String line = in.readLine();
            while ( line != null && !line.isEmpty() )
            {
                // read from the input until we get the Content-Length header so that
                // we know the size of the JSON payload

                if ( line.startsWith( "Content-Length" ) )
                {
                    bodyLength = Integer.parseInt( line.substring( line.indexOf( ':' ) + 2 ) );
                }

                line = in.readLine();
            }
            
            // we should be pointing at the body at this point
            if ( bodyLength > 0 )
            {
                char[] buf = new char[ bodyLength ];
                in.read( buf );
                
                SCJSON json = SCJSON.parse( new String( buf ) );
                
                // now, based on the "type" field in the object, we create a specific
                // instance of the message and then execute that message.
            }
            else
            {
                SCLogger.getLogger().warning( "The request had no body." );
            }
                
            requestStream.close();
            
            fInboundSocket.close();
        }
        catch ( Throwable t )
        {
            SCLogger.getLogger().error( "Exception while executing work: %s", t.getMessage() );
            SCLogger.getLogger().printStackTrace( t );
        }
        
        // when the work is done, signal the pool to reclaim this thread
        fPool.signalDone( this );
    }
}
