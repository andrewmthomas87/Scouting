package team1619.scouting.server.utils;

/**
 * Created by tolkin on 2/15/2015.
 */
public class SCException extends RuntimeException
{
    public SCException( String description )
    {
        super( description );
    }
    
    public SCException( Exception ex )
    {
        super( ex );
    }
}
