package edu.stanford.trumanc.sleepjournal;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by truman on 2/24/15.
 */
public class DayRecord implements Serializable {
    // Serializable so we can write/read this object directly from a file.

    public DayRecord(int year, int month, int day) {
        // Every other piece of information is input by the user, but actual date must be set now
        date = new GregorianCalendar(year, month, day);
    }

    public Calendar date; // Only day/month/year fields

    // These next 4 fields all have month/day/year/hour/minute
    // The day may be the same as the one above, or the day before it

    // These will all be recorded in Pacific time for consistency
    public Calendar timeInBed;
    public Calendar timeAsleep;
    public Calendar timeAwake;
    public Calendar timeOutOfBed;

    public double hoursAwakeDuringNight;

    public double hoursAwakeInBed() {
        // TODO:
        // Calc from the 4 Calendars above.
        // error check values
        return Double.NaN;
    }

    public double timeAsleepAtNight() {
        // TODO: Calculate from the 4 Calendars above
        return Double.NaN;
    }

    public void setHoursToFallAsleep(double hours) {
        // TODO: use hours to calculate timeAsleep
    }

    public void setHoursToGetUp(double hours) {
        // TODO: use hours to calculate timeAwake
    }

    public double hoursNapping;

    public void addNap(Calendar startTime, Calendar endTime) {
        // TODO: add time to nap field, add entry to factors
    }

    public double totalHoursAsleep() {
        return timeAsleepAtNight() + hoursNapping;
    }

    Alertness alertnessArray[]; // index 0 is midnight to 2AM, index 11 is 10PM to midnight

    public static int indexFromTime(Calendar time) {
        // TODO: Calculate index from Calendar.
        // error check that date is correct?
        return 0;
    }

    public static Calendar timeFromIndex(int index) {
        // TODO: Calculate time using date and index
        return null;
    }

    public Calendar optimumAlertness;
    private int overallFeel;

    public void setOverallFeel(int val) {
        if (val < 1) {
            Log.e("overallFeel", "Incorrectly called setOverallFeel with val: " + val
                   + " Using 0 instead.");
            overallFeel = 0;
        }
        if (val > 10) {
            Log.e("overallFeel", "Incorrectly called setOverallFeel with val: " + val
                  + " Using 10 instead.");
            overallFeel = 10;
        }
    }

    public int getOverallFeel() {
        return overallFeel;
    }

    ArrayList<Factor> factors;

    public void addFactor(String tag, Calendar time) {
        factors.add(new Factor(tag, time));
    }

    public void removeFactor(int index) { // Is this the best way to remove?
        if (index < 0 || index >= factors.size()) {
            Log.e("factors", "Called removeFactor with index: " + index + " on list of size: " + factors.size());
        } else {
            factors.remove(index);
        }
    }

    public int numDreams;
    public boolean nightmareFlag;
    public boolean lucidFlag;
    public boolean recurringFlag;


    public boolean isIncomplete() {
        // TODO: returns true if any of the objects/times/alertness values are null
        return true;
    }
}
