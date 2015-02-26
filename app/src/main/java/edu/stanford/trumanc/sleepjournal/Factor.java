package edu.stanford.trumanc.sleepjournal;

import java.util.Calendar;

/**
 * Created by truman on 2/24/15.
 */
public class Factor {
    /* Describes a Sleep Altering Factor.
     * Has a String name/descriptor, and a Calendar for the time it occurred
     */

    public String tag;
    public Calendar time; // Should have month/day/year/hour/minute

    public Factor(String name, Calendar t) {
        tag = name;
        time = t;
    }

    public String toString() {
        return tag + ": " + printableTime(time);
    }

    public static String printableTime(Calendar time) {
        return time.get(Calendar.HOUR) + ":" + time.get(Calendar.MINUTE) + " " + time.get(Calendar.AM_PM);
    }

}
