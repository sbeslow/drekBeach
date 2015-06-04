package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.Logger;
import scoreboard.EcoliMeasurement;

public class BeachRanking {

    private static final Logger.ALogger logger = Logger.of(BeachRanking.class);

    public Integer rank;
    public String beachName;
    public String beachUrl;
    public double poopScore;
    public String currentStatus;
    public int hoursRecorded;

    public List<EcoliMeasurement> ecoliMeasurements;

    public BeachRanking(Beach beach) {
        this.beachName = beach.name;
        this.beachUrl = beach.urlCode;
        
        buildPoopReadings(beach);
    }
    
    private void buildPoopReadings(Beach beach) {
        poopScore = 0;
        
        int minsNoRestrict = 0;
        int minsAdvisory = 0;
        int minsSwimBan = 0;
        
        Map<LocalDate, EcoliMeasurement> ecoliMap = new HashMap<>();
        BeachSnapshot lastSnapshot = null;

        for (BeachSnapshot thisSnapshot : beach.snapshots) {

            if (thisSnapshot.resultDate == null) {
                try {
                    String dateStr = thisSnapshot.resultCollected.replace("sample collected on ", "");
                    dateStr = dateStr.replace("<small><em>", "");
                    dateStr = dateStr.replace("</em></small>", "");
                    DateTimeFormatter fmt = DateTimeFormat.forPattern("MMM dd, yyyy");
                    thisSnapshot.resultDate = LocalDate.parse(dateStr, fmt);
                    thisSnapshot.update();
                }
                catch (Exception e) {
                    logger.error("Unable to fill result date for snapshot: " + thisSnapshot.id);
                    continue;
                }
            }

        	EcoliMeasurement ecoliMeas = ecoliMap.get(thisSnapshot.scrapeTime.toLocalDate());
        	if (ecoliMeas == null) {
        		ecoliMeas = new EcoliMeasurement(thisSnapshot.scrapeTime.toLocalDate());
        	}
        	ecoliMeas.setPrediction(thisSnapshot.forecastForToday);
        	ecoliMap.put(thisSnapshot.scrapeTime.toLocalDate(), ecoliMeas);

        	ecoliMeas = ecoliMap.get(thisSnapshot.resultDate);
        	if (ecoliMeas == null) {
        		ecoliMeas = new EcoliMeasurement(thisSnapshot.resultDate);
        	}
        	ecoliMeas.setReading(thisSnapshot.mostRecentResult);
        	ecoliMap.put(thisSnapshot.scrapeTime.toLocalDate(), ecoliMeas);
       	

            // If this is the first pass or if this is a new day.  Reset lastSnapshot and move to the next one
            if ((lastSnapshot == null) || (!lastSnapshot.scrapeTime.toLocalDate().equals(thisSnapshot.scrapeTime.toLocalDate()))) {
                lastSnapshot = thisSnapshot;
                continue;
            }

            long msPassed = thisSnapshot.scrapeTime.getMillis() - lastSnapshot.scrapeTime.getMillis();
            int minsPassed = (int) (msPassed / 60000);

            switch (thisSnapshot.swimStatus) {
                case BeachSnapshot.NO_RESTRICTIONS:
                    minsNoRestrict += minsPassed;
                    break;
                case BeachSnapshot.SWIM_ADVISORY:
                    minsAdvisory += minsPassed;
                    break;
                case BeachSnapshot.SWIM_BAN:
                    minsSwimBan += minsPassed;
                    break;
            }
        }
        
        ecoliMeasurements = new ArrayList<>(ecoliMap.values());
        poopScore = (minsAdvisory/60) + (2 * (minsSwimBan/60));
        currentStatus = beach.snapshots.get(beach.snapshots.size() - 1).swimStatus;
        hoursRecorded = minsAdvisory + minsSwimBan + minsNoRestrict;
    	
    }
}
