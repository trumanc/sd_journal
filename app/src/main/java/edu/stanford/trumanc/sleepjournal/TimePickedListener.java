package edu.stanford.trumanc.sleepjournal;

import java.util.Calendar;

/**
 * Created by truman on 2/26/15.
 */
public interface TimePickedListener {
    public void onTimePicked(TimeRecord which, int hour, int minute, Calendar date);
}
