package team1619.scouting.server.main;

import team1619.scouting.server.utils.SCJSON;

/**
 * Created by avimoskoff on 2/16/15.
 */

public class SCContribution extends SCMessage {

    private int fSID;
    private String fObjects;
    private String fMode;
    private int fTime;
    private boolean fAtonomous;
    private int fTeamNumber;
    private int fMatchNumber;

    public SCContribution() {

    }

    void init(SCJSON data) {
        fSID = data.get("SID");
    }

    @Override
    void processMessage() {

    }
}
