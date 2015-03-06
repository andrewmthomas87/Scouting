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
            SCProperties.startup();
            MySQL db = MySQL.connect();
            String eventCode = args[0];
            String dataFile = args[1];
            BufferedReader file = new BufferedReader( new FileReader( dataFile ) );
            String line = file.readLine();
            while (line != null)
            {
                String[] splitLine = line.split( " " );
                int matchNumber = Integer.parseInt(splitLine[0]);
                int redTeam1 = Integer.parseInt( splitLine[1] );
                int redTeam2 = Integer.parseInt( splitLine[2] );
                int redTeam3 = Integer.parseInt( splitLine[3] );
                int blueTeam1 = Integer.parseInt( splitLine[4] );
                int blueTeam2 = Integer.parseInt( splitLine[5] );
                int blueTeam3 = Integer.parseInt( splitLine[6] );
                db.addMatchData( eventCode, matchNumber, redTeam1, redTeam2, redTeam3, blueTeam1, blueTeam2, blueTeam3);
                System.out.format( "added: %d %d %d %d %d %d %d \n", matchNumber, redTeam1, redTeam2, redTeam3, blueTeam1, blueTeam2, blueTeam3 );
                line = file.readLine();
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