package team1619.scouting.server.main;

import team1619.scouting.server.utils.SCJSON;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * This is the outbound message queue for a client.
 */
public class SCClientQueue
{
    private int fClientId;
    private String fScoutName;

    private Queue<SCJSON> fQueue;

    public SCClientQueue( int clientId )
    {
        fClientId = clientId;

        fQueue = new LinkedList<>();
    }

    /**
     * Sets the name of the scout for this client.
     *
     * @param scoutName the name of the scout
     */
    public void setScoutName( String scoutName )
    {
        fScoutName = scoutName;
    }

    /**
     * Gets the name of the scouter for this client.
     *
     * @return the name of the client
     */
    public String getScoutName()
    {
        return fScoutName;
    }

    /**
     * Gets the client id associated with this queue.
     *
     * @return the client id for this queue
     */
    public int getClientId()
    {
        return fClientId;
    }

    /**
     * Adds a message to the outbound queue.
     *
     * @param message the message to be sent back to the client as a JSON map
     */
    public synchronized void writeToClient( SCJSON message )
    {
        fQueue.add( message );
    }

    /**
     * Serializes each JSON message into a string and wraps all the
     * messages into a JSON array of such message.
     *
     * @param out the output stream
     */
    public synchronized void flushQueueToClient( OutputStream out ) throws IOException
    {
        if ( !fQueue.isEmpty() )
        {
            String serializedMessages = serializeMessage();

            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( out ) );

            writer.write( serializedMessages );

            fQueue.clear();
        }
    }

    /**
     * Serializes the queue of JSON messages back to the client.
     *
     * @return the string version of the JSON objects
     */
    private String serializeMessage()
    {
        StringBuilder buf = new StringBuilder();

        addArray( buf, fQueue );

        return buf.toString();
    }

    private void addArray( StringBuilder buf, Collection<SCJSON> elements )
    {
        buf.append( "[" );

        boolean first = true;

        for ( SCJSON element : elements )
        {
            addObject( buf, element );

            if ( !first )
            {
                buf.append( ", " );
            }
            else
            {
                first = false;
            }
        }

        buf.append( "]" );
    }

    @SuppressWarnings( "unchecked" )
    private void addObject( StringBuilder buf, SCJSON map )
    {
        buf.append( "{" );

        boolean first = true;

        for ( Map.Entry<String, Object> entry : map.entrySet() )
        {
            buf.append( entry.getKey() ).append( " : " );

            Object val = entry.getValue();

            if ( val instanceof SCJSON )
            {
                addObject( buf, (SCJSON) val );
            }
            else if ( val instanceof Collection )
            {
                addArray( buf, (Collection<SCJSON>)val );
            }
            else
            {
                buf.append( val.toString() );
            }

            if ( !first )
            {
                buf.append( ", " );
            }
            else
            {
                first = false;
            }
        }
        buf.append( "}" );
    }
}
