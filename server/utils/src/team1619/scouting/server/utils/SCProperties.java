package team1619.scouting.server.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages the properties for the server.
 * 
 * Created by tolkin on 2/15/2015.
 */
public class SCProperties implements SCSubsystem
{
    private Properties fProperties;
    
    private static final String PROPERTY_FILE_NAME = "scout.properties";
    
    private static SCProperties sProperties;
    
    @Override
    public void startup() throws InstantiationException
    {
        sProperties = new SCProperties();
    }
    
    @Override
    public void shutdown()
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
