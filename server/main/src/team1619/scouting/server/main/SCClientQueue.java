package team1619.scouting.server.main;

import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCLogger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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

    private static SimpleDateFormat sDateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz" );

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
        SCLogger.getLogger().debug( "Flushing queue to client %d", getClientId() );

        if ( !fQueue.isEmpty() )
        {
            writeJSONToClient( out, fQueue, getClientId() );

            fQueue.clear();
        }
    }

    public static void writeJSONToClient( OutputStream out, Queue<SCJSON> messageQueue, int clientID ) throws IOException
    {
        String serializedMessages = serializeMessage( messageQueue );

        SCLogger.getLogger().debug( "Writing message to client %d: %s", clientID, serializedMessages );

        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( out ) );

        writeHTTPResponseHeaders( writer, serializedMessages.length() );

        writer.write( serializedMessages );

        writer.flush();
    }

    /**
     * Serializes the queue of JSON messages back to the client.
     *
     * @return the string version of the JSON objects
     */
    public static String serializeMessage( Queue<SCJSON> queue )
    {
        StringBuilder buf = new StringBuilder();

        addArray( buf, queue );

        return buf.toString();
    }

    private static void addArray( StringBuilder buf, Collection<SCJSON> elements )
    {
        buf.append( "[" );

        boolean first = true;

        for ( SCJSON element : elements )
        {
            if ( !first )
            {
                buf.append( ", " );
            }
            else
            {
                first = false;
            }

            addObject( buf, element );
        }

        buf.append( "]" );
    }

    @SuppressWarnings( "unchecked" )
    private static void addObject( StringBuilder buf, SCJSON map )
    {
        buf.append( "{" );

        boolean first = true;

        for ( Map.Entry<String, Object> entry : map.entrySet() )
        {
            if ( !first )
            {
                buf.append( ", " );
            }
            else
            {
                first = false;
            }

            buf.append('"').append( entry.getKey() ).append( "\" : " );

            Object val = entry.getValue();

            if ( val instanceof SCJSON )
            {
                addObject( buf, (SCJSON) val );
            }
            else if ( val instanceof Collection )
            {
                addArray( buf, (Collection<SCJSON>)val );
            }
            else if ( val instanceof Number )
            {
                buf.append( val.toString() );
            }
            else
            {
                buf.append( '"' ).append( val.toString() ).append( '"' );
            }
        }
        buf.append( "}" );
    }

    /**
     * Outputs the HTTP headers (minimal).
     *
     * @param out the output stream
     * @param contentLength the number of octets in the JSON output being written in the body
     *
     * @throws IOException problem writing
     */
    private static void writeHTTPResponseHeaders( BufferedWriter out, int contentLength ) throws IOException
    {
        out.write( "HTTP/1.1 200 OK\n" );

        String date = sDateFormat.format( new Date() );
        out.write( "Date: " + date + "\n" );

        out.write( "Content-Type: application/json; charset=UTF-8\n" );
        out.write( "Content-Length: " + contentLength + "\n" );
        out.write( "Access-Control-Allow-Origin: *\n");

        // need to write a blank line to separate headers from body
        out.write( "\n" );
    }
}
