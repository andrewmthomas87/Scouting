package team1619.scouting.server.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages the properties for the server.
 * 
 * Created by tolkin on 2/15/2015.
 */
public class SCProperties
{
    private Properties fProperties;
    
    private static String PROPERTY_FILE_NAME;

    public static void setPropertyFileName( String name )
    {
        PROPERTY_FILE_NAME = name;
    }
    
    private static SCProperties sProperties;
    
    public static void startup() throws InstantiationException
    {
        sProperties = new SCProperties();
    }
    
    public static void shutdown()
    {
        // do nothing
    }

    /**
     * Gets a property from the property file.
     * 
     * @param key the property key to lookup
     * 
     * @return the value of the key
     * 
     * @throws SCPropertyNotFoundException if the property was not found
     */
    public static String getProperty( String key ) throws SCPropertyNotFoundException
    {
        String value = sProperties.fProperties.getProperty( key );
        
        if ( value == null )
        {
            throw new SCPropertyNotFoundException( String.format( "Property '%s' was not found.", key ) );
        }
        
        return value;
    }

    /**
     * Sets a property.  Usually used when we want to override a default.
     *
     * @param key the key of the property
     * @param value the value of the property
     */
    public static void setProperty( String key, String value )
    {
        sProperties.fProperties.setProperty( key, value );
    }
    
    private SCProperties() throws InstantiationException
    {
        fProperties = new Properties();
        
        try
        {
            fProperties.load( new FileReader( PROPERTY_FILE_NAME ) );
        }
        catch ( IOException ex )
        {
            System.out.println( "unable to open properties file." );
            throw new InstantiationException( "unable to open properties file" );
        }
    }
}
