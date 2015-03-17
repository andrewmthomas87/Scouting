package team1619.scouting.server.main;

import team1619.scouting.server.utils.SCLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by avimoskoff on 3/3/15.
 */
public class SCMatch
{
    public static class MatchTeamData
    {
        private final int fTeamNumber;
        private final String fAlliance;

        public MatchTeamData(int teamNumber, String alliance)
        {
            fTeamNumber = teamNumber;
            fAlliance = alliance;
        }

        public int getTeamNumber()
        {
            return fTeamNumber;
        }

        public String getAlliance()
        {
            return fAlliance;
        }
    }

    private static SCMatch sCurrentMatch;

    private int fMatchNumber;

    private Map<Integer, MatchTeamData> fTeamsMap;

    private List<MatchTeamData> fTeams;

    public static void setCurrentMatch(int matchNumber, int redTeam1, int redTeam2, int redTeam3, int blueTeam1, int blueTeam2, int blueTeam3)
    {
        sCurrentMatch = new SCMatch( matchNumber, redTeam1, redTeam2, redTeam3, blueTeam1, blueTeam2, blueTeam3 );
    }

    private SCMatch(int matchNumber, int redTeam1, int redTeam2, int redTeam3, int blueTeam1, int blueTeam2, int blueTeam3)
    {
        fMatchNumber = matchNumber;

        fTeams = new ArrayList<>( 6 );

        fTeams.add( new MatchTeamData( redTeam1, "red" ) );
        fTeams.add( new MatchTeamData( redTeam2, "red" ) );
        fTeams.add( new MatchTeamData( redTeam3, "red" ) );
        fTeams.add( new MatchTeamData( blueTeam1, "blue" ) );
        fTeams.add( new MatchTeamData( blueTeam2, "blue" ) );
        fTeams.add( new MatchTeamData( blueTeam3, "blue" ) );

        // initialize empty client to team map
        fTeamsMap = new HashMap<>();
    }

    public static MatchTeamData getNextTeam(Integer clientId)
    {
        if ( sCurrentMatch == null) {
            SCLogger.getLogger().warning( "client %d trying to get team before match setup", clientId );
            return null;
        }
        return sCurrentMatch.lookupTeamData( clientId );
    }

    private MatchTeamData lookupTeamData(Integer clientId)
    {
        MatchTeamData teamData = fTeamsMap.get( clientId );

        if ( teamData == null )
        {
            if ( fTeams.isEmpty() )
            {
                SCLogger.getLogger().error( "Client trying to get a team when all were assigned" );
                return null;
            }

            // assign this client to the next team data
            teamData = fTeams.remove( 0 );

            SCLogger.getLogger().info( "Assigning team %d to scout %d", teamData.getTeamNumber(), clientId );

            fTeamsMap.put( clientId, teamData );
        }

        return teamData;
    }

    public static MatchTeamData getAssociatedTeamData( Integer clientId )
    {
        return sCurrentMatch.fTeamsMap.get( clientId );
    }

    public static int getMatchNumber()
    {
        return sCurrentMatch.fMatchNumber;
    }

    /**
     * This call only works before any clients have been assigned.
     *
     * @return the list of teams in this match
     */
    public static List<MatchTeamData> getAllTeamData()
    {
        return sCurrentMatch.fTeams;
    }

    public static int getTeamNumber(Integer clientID)
    {
        if ( sCurrentMatch == null )
        {
            SCLogger.getLogger().info( "Trying to get team number when no current match" );

            // called before first match has been set up
            return -1;
        }

        MatchTeamData data = sCurrentMatch.fTeamsMap.get( clientID );

        if ( data == null )
        {
            SCLogger.getLogger().info( "Client %d does not have match data when getting team number", clientID );

            return -1;
        }
        else
        {
            return data.getTeamNumber();
        }
    }

    public static String getTeamAlliance(Integer clientID)
    {
        if ( sCurrentMatch == null )
        {
            // called before first match has been set up
            return "";
        }

        MatchTeamData data = sCurrentMatch.fTeamsMap.get( clientID );

        if ( data == null )
        {
            return "";
        }
        else
        {
            return data.getAlliance();
        }
    }

    public static void closeMatch()
    {
        sCurrentMatch = null;
    }

    public static boolean isMatchActive()
    {
        return sCurrentMatch != null;
    }
}
