package team1619.scouting.server.utils;

import java.io.IOException;

/**
 * Created by tolkin on 2/15/2015.
 */
public interface SCSubsystem
{
    void startup() throws InstantiationException;
    
    void shutdown();
}
