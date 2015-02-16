package team1619.scouting.server.main;

import team1619.scouting.server.utils.SCLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

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
            executeWork();
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
    private void executeWork()
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
                System.out.println( line );

                if ( line.startsWith( "Content-Length" ) )
                {
                    bodyLength = Integer.parseInt( line.substring( line.indexOf( ':' ) + 2 ) );
                }

                line = in.readLine();
            }
            
            if ( bodyLength > 0 )
            {
                char[] buf = new char[ bodyLength ];
                in.read( buf );
                System.out.println( buf );
            }
                
            requestStream.close();
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
