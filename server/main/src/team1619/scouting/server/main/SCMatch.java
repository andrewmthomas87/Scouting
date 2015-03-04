package team1619.scouting.server.main;

/**
 * Created by avimoskoff on 3/3/15.
 */
public class SCMatch {
    private static SCMatch sCurrentMatch;
    int fMatchNumber;
    int[] fTeams;
    int fNextTeamIndex;

    public static void newMatch(int matchNumber, int redTeam1, int redTeam2, int redTeam3, int blueTeam1, int blueTeam2, int blueTeam3) {
        sCurrentMatch = new SCMatch(matchNumber, redTeam1, redTeam2, redTeam3, blueTeam1, blueTeam2, blueTeam3);
    }

    private SCMatch(int matchNumber, int redTeam1, int redTeam2, int redTeam3, int blueTeam1, int blueTeam2, int blueTeam3) {
        fMatchNumber = matchNumber;
        fTeams = new int[6];
        fTeams[0] = redTeam1;
        fTeams[1] = redTeam2;
        fTeams[2] = redTeam3;
        fTeams[3] = blueTeam1;
        fTeams[4] = blueTeam2;
        fTeams[5] = blueTeam3;
        fNextTeamIndex = 0;
    }

    public static int getNextTeam() {
        return sCurrentMatch.fTeams[sCurrentMatch.fNextTeamIndex++];
    }

    public static int getMatchNumber() {
        return sCurrentMatch.fMatchNumber;
    }
}
