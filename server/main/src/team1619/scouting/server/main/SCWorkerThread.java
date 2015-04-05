package team1619.scouting.server.main;

import team1619.scouting.server.database.MySQL;
import team1619.scouting.server.utils.SCJSON;
import team1619.scouting.server.utils.SCJSONSyntaxException;
import team1619.scouting.server.utils.SCLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * A worker thread processes an inbound connection.
 */
public class SCWorkerThread extends Thread
{

    private static final String ignoredMessages = "wazUp, waitForMatchStart, getConnections, getMatch,";
    //private static final String ignoredMessages = "";
    /**
     * Message mapping table (type to message object)
     */
    private static Map<String, Class<? extends SCMessage>> sMessageTable;

    static
    {
        sMessageTable = new HashMap<>();
        sMessageTable.put( "waitForMatchStart", SCCWaitForMatchStart.class );
        sMessageTable.put( "getTeam", SCCGetTeam.class );
        sMessageTable.put( "login", SCULogin.class );
        sMessageTable.put( "contribution", SCContribution.class );
        sMessageTable.put( "setMatch", SCSSetMatch.class );
        sMessageTable.put( "startMatch", SCSStartMatch.class );
        sMessageTable.put( "disconnectConnection", SCSDisconnectConnection.class );
        sMessageTable.put( "getConnections", SCSGetConnections.class );
        sMessageTable.put( "getMatch", SCSGetMatch.class );
        sMessageTable.put( "resetMatch", SCResetMatchMessage.class );
        sMessageTable.put( "wazUp", SCWazUp.class );
        sMessageTable.put( "robotEvent", SCRobotEventMessage.class );
        sMessageTable.put( "matchEnded", SCMatchEnded.class );
        sMessageTable.put( "matchReset", SCResetMatchMessage.class );
    }

    private final Object fWaitFlag;

    private Socket fInboundSocket;
    private SCListener fListener;
    private SCThreadPool fPool;
    private MySQL fdbConnection;

    public SCWorkerThread()
    {
        // all workers are daemons
        setDaemon( true );

        fWaitFlag = new Object();
    }

    /**
     * This just waits until there is work to do.
     */
    @Override
    public void run()
    {
        try
        {
            fdbConnection = MySQL.connect();

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
                executeWork( fdbConnection );
            }
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
        }
        finally
        {
            fdbConnection.close();
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
        fListener = listener;
        fPool = pool;

        SCLogger.getLogger().debug( "Assigning work to thread %s", getName() );

        synchronized ( fWaitFlag )
        {
            // ok, we have work to do, so let this thread run now
            fWaitFlag.notifyAll();
        }
    }

    /**
     * Executes the work of this thread.
     */
    private void executeWork( MySQL conn )
    {
        SCLogger.getLogger().debug( "Starting work on thread %s", getName() );

        try
        {
            // reads the input channel
            InputStream requestStream = fInboundSocket.getInputStream();

            // for now, just write the request to the console
            BufferedReader in = new BufferedReader( new InputStreamReader( requestStream ) );

            String line = in.readLine();
            String httpHeader = line;

            while ( line != null && !line.isEmpty() )
            {
                // read from the input until we get the Content-Length header so that
                // we know the size of the JSON payload

                //System.out.println( line );

                line = in.readLine();
            }

            if ( httpHeader == null )
            {
                SCLogger.getLogger().error( "[%s] missing HTTP header", fInboundSocket.getInetAddress().toString() );
                System.out.format( "[%s] missing HTTP header\n", fInboundSocket.getInetAddress().toString() );
            }
            else
            {
                String decodedJSON = "";
                try
                {
                    String[] httpParts = httpHeader.split( " " );
                    String encodedJSON = httpParts[ 1 ].substring( 2 );
                    decodedJSON = URLDecoder.decode( encodedJSON, "UTF-8" );

                    // we should be pointing at the body at this point

                    SCJSON json = SCJSON.parse( decodedJSON );

                    // now, based on the "type" field in the object, we create a specific
                    // instance of the message and then execute that message.
                    String messageType = (String) json.get( "type" );

                    if ( !( ignoredMessages.contains( messageType + "," ) ) )
                    {
                        String inetAddress = fInboundSocket.getInetAddress().toString();

                        System.out.printf( "[%s] Received message type %s\n", inetAddress, messageType );
                        SCLogger.getLogger().debug( "[%s] Received message type '%s'", inetAddress, messageType );
                    }

                    if ( "shutdown".equals( messageType ) )
                    {
                        fListener.setExit();
                    }
                    else
                    {
                        Class<? extends SCMessage> processorClass = sMessageTable.get( messageType );

                        if ( processorClass == null )
                        {
                            SCLogger.getLogger().error( "Unknown message type: %s", messageType );
                            throw new IllegalArgumentException( "message type " + messageType );
                        }

                        SCMessage messageProcessor = processorClass.newInstance();

                        // set the client id for every message
                        messageProcessor.setClientID( (Integer) json.get( "CID" ) );

                        boolean normalResponse = true;

                        try
                        {
                            messageProcessor.processMessage( conn, json );
                        }
                        catch ( IllegalArgumentException ex )
                        {
                            SCLogger.getLogger().warning( ( "Client not logged in response" ) );
                            // this means no client queue, so send back not logged in directly on socket
                            Queue<SCJSON> logoutMessage = new LinkedList<>();
                            SCJSON msg = new SCJSON();
                            msg.put( "type", "status" );
                            msg.put( "status", "disconnected" );
                            logoutMessage.add( msg );
                            SCClientQueue.writeJSONToClient( fInboundSocket.getOutputStream(), logoutMessage, 0 );
                            normalResponse = false;
                        }

                        if ( normalResponse )
                        {
                            // flush this client's queue to client
                            SCClientQueue clientQueue = SCOutbound.getClientQueue( messageProcessor.getClientID() );

                            clientQueue.flushQueueToClient( fInboundSocket.getOutputStream() );
                        }
                    }
                }
                catch ( SCJSONSyntaxException ex )
                {
                    SCLogger.getLogger().debug( "Syntax error on input: %s", decodedJSON );
                }
            }

            // System.out.println( "=== Finished processing inbound message ===" );

            in.close();

            requestStream.close();

            fInboundSocket.close();
        }
        catch ( Throwable t )
        {
            SCLogger.getLogger().error( "Exception while executing work: %s", t.getMessage() );
            SCLogger.getLogger().printStackTrace( t );
            t.printStackTrace();
        }

        // when the work is done, signal the pool to reclaim this thread
        fPool.signalDone( this );
    }
}
