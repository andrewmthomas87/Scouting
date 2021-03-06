package team1619.scouting.server.main;

import team1619.scouting.server.utils.SCLogger;
import team1619.scouting.server.utils.SCProperties;

import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This is a thread pool class.  It manages a number of threads that can handle inbound requests.
 * * 
 * Created by tolkin on 2/15/2015.
 */
public class SCAThreadPool
{
    private int fPoolSize;
    
    private final List<SCAWorkerThread> fAvailablePool;
    private final Set<SCAWorkerThread> fInUsePool;
    
    private static SCAThreadPool sPool;
    
    public static void startup()
    {
        sPool = new SCAThreadPool();
    }
    
    private SCAThreadPool()
    {
        fPoolSize = Integer.parseInt( SCProperties.getProperty( "network.threads.pool.size" ) );
        
        SCLogger.getLogger().debug( "Creating pool of size %d", fPoolSize );
        
        fAvailablePool = new LinkedList<>(); 
        fInUsePool     = new HashSet<>();
        
        for ( int i = 0; i < fPoolSize; i++ )
        {
            SCAWorkerThread worker = new SCAWorkerThread();
            fAvailablePool.add( worker );
            worker.start();
        }
    }
    
    public static SCAThreadPool getPool()
    {
        return sPool;
    }

    /**
     * Called when the listener wants to assign a thread to perform work.
     *
     * @param request the socket with the inbound request
     * @param listener the listener object (used when the message is to exit the server)
     */
    public void assignThread( Socket request, SCAListener listener )
    {
        // find an available thread
        while ( fAvailablePool.isEmpty() )
        {
            // we need to wait until a thread is available
            SCLogger.getLogger().warning( "No threads currently available." );

            synchronized ( fAvailablePool )
            {
                try
                {
                    fAvailablePool.wait();
                }
                catch ( InterruptedException ex )
                {
                    // ignore
                }
            }
        }

        // ok, we have at least one available thread
        SCAWorkerThread worker;
        synchronized ( fAvailablePool )
        {
            worker = fAvailablePool.remove( 0 );
            fInUsePool.add( worker );
        }
        
        worker.assignWork( request, listener, this );
    }

    /**
     * Called by a worker thread when it is done so that the thread can be placed back into
     * the pool.
     *  
     * @param worker the worker thread that is done
     */
    public void signalDone( SCAWorkerThread worker )
    {
        synchronized ( fAvailablePool )
        {
            fInUsePool.remove( worker );
            fAvailablePool.add( worker );
            fAvailablePool.notifyAll();
        }
    }
}
