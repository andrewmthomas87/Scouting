package team1619.scouting.server.main;

import team1619.scouting.server.utils.SCLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a queue for each
 */
public class SCOutbound
{
    private static Map<Integer, SCClientQueue> fQueueMap;

    static
    {
        fQueueMap = new HashMap<>();
    }

    private static int sNextClientId = 1;

    /**
     * When a client logs in, we create a new queue for that client and assign the
     * client a new client id.
     *
     * @return a new client queue for this client
     */
    public static SCClientQueue setupClient()
    {
        SCClientQueue newQueue = new SCClientQueue( sNextClientId++ );

        fQueueMap.put( newQueue.getClientId(), newQueue );

        return newQueue;
    }

    /**
     * Gets the queue associated with this client.  Probably an assert failure
     * if no queue exists for this client.
     *
     * @param clientId the id of the client to get
     *
     * @return the associated client queue
     */
    public static SCClientQueue getClientQueue( Integer clientId )
    {
        SCClientQueue clientQueue = fQueueMap.get( clientId );

        if ( clientQueue == null )
        {
            SCLogger.getLogger().error( "Missing queue for client id %d", clientId );
            throw new IllegalArgumentException( "no queue for client: " + clientId );
        }

        return clientQueue;
    }

    /**
     * Gets all the current client queues
     *
     * @return the client queues
     */
    public static Iterable<SCClientQueue> getClientQueues()
    {
        return fQueueMap.values();
    }

    /**
     * Removes the queue (and any unsent messages) for client.
     *
     * @param clientId the id of the client to remove
     */
    public static void removeClientQueue( Integer clientId )
    {
        SCLogger.getLogger().info( "Removing queue for client id: %d", clientId );

        fQueueMap.remove( clientId );
    }
}
