package edu.stanford.trumanc.sleepjournal;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class WelcomeActivity extends Activity implements DateSetListener {
    /**
     * This Activity serves a few purposes as the entry point of the app. First, it welcomes the
     * user, and asks the user to input what day data collection will start. This information is
     * saved in persistent data and is only required once (but nothing else can be reached or used
     * in the app until this has been completed.
     *
     * If the user has already entered the start date, but it hasn't happened yet, it will tell the
     * user to come back later.
     *
     * If the start date has occurred (or is the following day) this activity will display a button
     * to go to the current day's page, or a calendar to pick a different day to edit.
     *
     * The days will be displayed differently depending on whether or not they are:
     * INVALID: (before the start date, or after the end date 7 weeks after that)
     * INCOMPLETE: (A past day that is missing some data)
     * CURRENT: (the present day)
     * FUTURE: (days in the future that don't have data yet)
     *
     * @param savedInstanceState
     */

    public static final String PREFS_NAME = "startDatePrefs";
    public static final String START_DATE_SET = "startDateSet";
    public static final String START_DATE_YEAR = "startDateYear";
    public static final String START_DATE_MONTH = "startDateMonth";
    public static final String START_DATE_DAY = "startDateDay";

    public static Calendar startDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // See if the start date has been entered
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean initialized = settings.getBoolean(START_DATE_SET, false);
        if (!initialized) {
            promptUserForStartDate();
        } else {
            int year = settings.getInt(START_DATE_YEAR, -1);
            int month = settings.getInt(START_DATE_MONTH, -1);
            int day = settings.getInt(START_DATE_DAY, -1);
            if (year == -1 || month == -1 || day == -1) {
                throw new AssertionError("Invalid startDay/startMonth values: " + day + ", " + month);
            }
            Log.i("date", "Start date remembered: " + day + "/" + month + "/" + year);

            startDate = new GregorianCalendar(year, month, day);
            onStartDateSet();
        }
    }

    public void onStartDateSet() {
        Log.i("dataEntry", "Moved past init to data entry.");

        View pickStartLayout = findViewById(R.id.pickStartDateLayout);
        pickStartLayout.setVisibility(View.GONE);

        View insertDataLayout = findViewById(R.id.selectDataDateLayout);
        insertDataLayout.setVisibility(View.VISIBLE);
    }


    private void promptUserForStartDate() {
        View pickStartLayout = findViewById(R.id.pickStartDateLayout);
        pickStartLayout.setVisibility(View.VISIBLE);

        View insertDataLayout = findViewById(R.id.selectDataDateLayout);
        insertDataLayout.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_day_input, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /* This is the listener interface to interact with the DatePickerFragment.
     * Save the date in persistent memory, then move on to data entry.
     */
    @Override
    public void onDateSet(int year, int month, int day) {
        // Save the start date in persistent memory
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(START_DATE_YEAR, year);
        editor.putInt(START_DATE_MONTH, month);
        editor.putInt(START_DATE_DAY, day);
        editor.putBoolean(START_DATE_SET, true);
        editor.commit();
        Log.i("activity", "SUCCESS");

        // Edit the views to allow data entry
        onStartDateSet();
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private DateSetListener listener;

        static DatePickerFragment newInstance(DateSetListener listener) {
            DatePickerFragment pickerFragment = new DatePickerFragment();
            pickerFragment.setOnDateSetListener(listener);

            return pickerFragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
           return  new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Log.i("date", "" + month + "/" + day + "/" + year);
            listener.onDateSet(year, month, day);
        }

        public void setOnDateSetListener(DateSetListener lis) {
            listener = lis;
        }
    }

    public void onPickDateClick(View view) {
        DialogFragment newFragment = DatePickerFragment.newInstance(this);
        newFragment.show(getFragmentManager(), "datePicker");
    }

}
