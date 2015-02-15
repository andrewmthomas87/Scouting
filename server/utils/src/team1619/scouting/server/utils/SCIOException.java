package team1619.scouting.server.utils;

/**
 * Wrapper for java.io.IOExceptions that we need to deal with.
 * *
 * Created by tolkin on 2/15/2015.
 */
public class SCIOException extends SCException
{
    public SCIOException( Exception ex )
    {
        super( ex );
    }
}
