package edu.stanford.trumanc.sleepjournal;

import android.util.Log;

/**
 * Created by truman on 2/24/15.
 */
public enum Alertness {
    VAL1("Wide awake absolutely at peak alertness and energy. \"I am alert and alive!\"", 1),
    VAL2("May be tired, slightly tired. \"I am more or less alert.\"", 2),
    VAL3("Unambiguously tired. \"I am tired right now.\"", 3),
    VAL4("Very tired, but not sleepy/drowsy. \"I am sooo tired but am definitely awake.\"", 4),
    VAL5("Unambiguously sleepy/drowsy, making a conscious effort to stay awake.\"I just can’t keep my eyes open.\" ", 5),
    
    VAL6("Asleep.", Integer.MAX_VALUE); // This val should never be used in calculations


    private String description;
    private int value;

    Alertness(String desc, int val) {
        description = desc;
        value = val;
    }

    String getDescription() {
        return description;
    }

    int getValue() {
        if (this == VAL6) {
            Log.e("alertness", "Incorrectly asked VAL6 for integer value");
        }
        return value;
    }
}
