package team1619.scouting.server.utils;

/**
 * Exception for JSON syntax errors found while parsing.
 * 
 * Created by tolkin on 2/15/2015.
 */
public class SCJSONSyntaxException extends SCException
{
    public SCJSONSyntaxException( String reason )
    {
        super ( reason );
    }
}
