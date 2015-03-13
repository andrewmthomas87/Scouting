package team1619.scouting.server.database;

import team1619.scouting.server.utils.SCProperties;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by avimoskoff on 3/5/15.
 */
public class SCInitMatchData
{
    public static void main(String[] args)
    {
        try
        {
            SCProperties.setPropertyFileName( String.format( "%s.scout.properties" , args[0]) );
            SCProperties.startup();
            MySQL db = MySQL.connect();
            String eventCode = args[1];
            String dataFile = args[2];
            BufferedReader file = new BufferedReader( new FileReader( dataFile ) );
            String line = file.readLine();
            int matchNumber = 1;
            while (line != null && !line.isEmpty())
            {
                String[] splitLine = line.split( " " );
                int redTeam1 = Integer.parseInt( splitLine[0] );
                int redTeam2 = Integer.parseInt( splitLine[1] );
                int redTeam3 = Integer.parseInt( splitLine[2] );
                int blueTeam1 = Integer.parseInt( splitLine[3] );
                int blueTeam2 = Integer.parseInt( splitLine[4] );
                int blueTeam3 = Integer.parseInt( splitLine[5] );
                db.addMatchData( eventCode, matchNumber, redTeam1, redTeam2, redTeam3, blueTeam1, blueTeam2, blueTeam3);
                System.out.format( "added: %d %d %d %d %d %d %d \n", matchNumber, redTeam1, redTeam2, redTeam3, blueTeam1, blueTeam2, blueTeam3 );
                line = file.readLine();
                matchNumber++;
            }
            db.close();
            file.close();
        }
        catch ( Exception exception )
        {
            exception.printStackTrace();
        }
    }
}
