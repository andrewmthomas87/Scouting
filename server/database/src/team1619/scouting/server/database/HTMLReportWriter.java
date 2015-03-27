package team1619.scouting.server.database;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tolkin on 3/25/2015.
 */
public class HTMLReportWriter
{
    private static final String sProlog =
            "<html>\n" +
            "  <head>\n" +
            "    <style>\n" +
            "      table {\n" +
            "      font-size: 110%;\n" +
            "      }\n" +
            "      table, th, td {\n" +
            "      border: 1px solid black;\n" +
            "      }\n" +
            "      th, td {\n" +
            "      width: 100px;\n" +
            "      }\n" +
            "      .redAlliance {\n" +
            "      color: red;\n" +
            "      font-weight: bold;\n" +
            "      text-align: center;\n" +
            "      }\n" +
            "      .blueAlliance {\n" +
            "      color: blue;\n" +
            "      font-weight: bold;\n" +
            "      text-align: center;\n" +
            "      }\n" +
            "      .noData {\n" +
            "      background-color: gray;\n" +
                    "   -webkit-print-color-adjust: exact;" +
            "      }\n" +
            "      .numberData {\n" +
            "      font-family: courier;\n" +
            "      font-weight: bold;\n" +
            "      text-align: center;\n" +
            "      }\n" +
            "    </style>\n" +
            "  </head>\n" +
            "  <body>\n";

    private static final String sEpilog =
            "      </tbody>\n" +
            "    </table>\n" +
            "    <p><i>\n" +
            "        Produced by Up-A-Creek Robotics (Team 1619) Advanced Scouting System\n" +
            "    </i></p>\n" +
            "  </body>\n" +
            "</html>\n";

    private static final String sHeader =
            "    <h1>Scouting Report for Match %d</h1>\n" +
            "    <p>\n" +
            "      <i>\n" +
            "        Report time: %s\n" +
            "      </i>\n" +
            "    </p>\n" +
                    "    <table>\n" +
                    "      <thead>\n" +
                    "        <tr>\n" +
                    "          <th>Team</th>\n" +
                    "          <th>Coopertition Totes</th>\n" +
                    "          <th>Auto Rake</th>\n" +
                    "          <th>Teleop Rake</th>\n" +
                    "          <th>Chute Totes</th>\n" +
                    "          <th>Floor Totes</th>\n" +
                    "          <th>Used Bins</th>\n" +
                    "        </tr>\n" +
                    "      </thead>\n" +
                    "      <tbody>\n";

    private static final String sTeamRow =
            "        <tr>\n" +
            "          <td class=\"%sAlliance\">%d</td>\n" +
            "          %s\n" +
            "          %s\n" +
            "          %s\n" +
            "          %s\n" +
            "          %s\n" +
            "          %s\n" +
            "        </tr>\n";

    private static final String sNumberData =
            "<td class=\"numberData\">%.1f</td>";

    private static final String sNoData =
            "<td class=\"noData\"></td>";

    private static final String sAllianceDivider =
            "        <tr><td colspan=\"7\" style=\"background-color:black\"></td></tr>\n";

    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat( "dd MMM yyyy, hh:mm a" );

    private final PrintWriter fOut;

    public HTMLReportWriter( PrintWriter out )
    {
        fOut = out;
    }

    public void writeProlog()
    {
        fOut.print( sProlog );
    }

    public void writeEpilog()
    {
        fOut.print( sEpilog );
    }

    public void writeHeader( int matchNumber )
    {
        fOut.format( sHeader, matchNumber, sDateFormat.format( new Date() ) );
    }

    public void writeDivider()
    {
        fOut.print( sAllianceDivider );
    }

    public void writeTeamData( int teamNumber,
                               boolean redAlliance,
                               double coopTotes,
                               double autoRake,
                               double teleopRake,
                               double chuteTotes,
                               double floorTotes,
                               double usedBins )
    {
        String coopTotesCell;
        String autoRakeCell;
        String teleopRakeCell;
        String chuteTotesCell;
        String floorTotesCell;
        String usedBinsCell;

        if ( coopTotes > 0 )
        {
            coopTotesCell = String.format( sNumberData, coopTotes );
        }
        else
        {
            coopTotesCell = sNoData;
        }

        if ( teleopRake > 0 )
        {
            autoRakeCell = String.format( sNumberData, autoRake );
        }
        else
        {
            autoRakeCell = sNoData;
        }

        if ( teleopRake > 0 )
        {
            teleopRakeCell = String.format( sNumberData, teleopRake );
        }
        else
        {
            teleopRakeCell = sNoData;
        }

        if ( chuteTotes > 0 )
        {
            chuteTotesCell = String.format( sNumberData, chuteTotes );
        }
        else
        {
            chuteTotesCell =  sNoData;
        }

        if ( floorTotes > 0 )
        {
            floorTotesCell = String.format( sNumberData, floorTotes );
        }
        else
        {
            floorTotesCell = sNoData;
        }

        if ( usedBins > 0 )
        {
            usedBinsCell = String.format( sNumberData, usedBins );
        }
        else
        {
            usedBinsCell = sNoData;
        }

        fOut.format( sTeamRow,
                     redAlliance ? "red" : "blue",
                     teamNumber,
                     coopTotesCell,
                     autoRakeCell,
                     teleopRakeCell,
                     chuteTotesCell,
                     floorTotesCell,
                     usedBinsCell );
    }
}
