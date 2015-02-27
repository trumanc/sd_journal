package edu.stanford.trumanc.sleepjournal;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;


public class WelcomeActivity extends Activity{
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
    public static final String NUM_DAYS_COLLECTING = "numDaysCollecting";
    public static final String IS_AWAKE = "isAwake";
    public static final String STUDENT_NAME = "studentName";
    public static final String STUDENT_SUNET_ID = "studentSunetId";
    public static final String STUDENT_EMAIL = "studentEmail";
    public static final String TA_EMAIL = "taEmail";

    public static final String START_DATE_EXTRA = "startDateExtra";
    public static final String CURRENT_DATE_EXTRA = "currentDateExtra";
    public static final String ALL_RECORDS_EXTRA = "allRecordsExtra";


    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM d, yyyy");
    public static final String DAY_RECORDS_FILE = "dayRecordsFile";

    private static final int INIT_REQ = 135; // unique id. Nothing special
    private static final int EDIT_DAY_REQ = 136; // unique id. Nothing special

    public Calendar startDate;
    public HashMap<Calendar, DayRecord> allDayRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("create", "in onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        allDayRecords = readRecordsFromFile();

        // See if the start date has been entered
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean initialized = settings.getBoolean(START_DATE_SET, false);
        if (!initialized) {
            Intent initIntent = new Intent(this, FirstTimeInitializationActivity.class);
            startActivityForResult(initIntent, INIT_REQ);

            //View pickStartLayout = findViewById(R.id.pickStartDateLayout);
            //pickStartLayout.setVisibility(View.VISIBLE);

        } else {
            Log.i("create","in data block");

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

    private boolean getIsAwake() {
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(IS_AWAKE, false);
    }

    private void setIsAwake(boolean isAwake) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(IS_AWAKE, isAwake);
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == INIT_REQ) {

            // Information has already been saved to prefs. Grab the startDate
            startDate = (Calendar) intent.getSerializableExtra("startDateExtra");
            Log.i("testing", "Get init result: " + startDate.toString());
            onStartDateSet();
        } else if (requestCode == EDIT_DAY_REQ) {
            // User may have edited the RecordsMap. Get that info!
            if (intent == null) {
                // User backed out instead of saving. Ignore
                return;
            }
            HashMap<Calendar, DayRecord> temp = (HashMap<Calendar, DayRecord>) intent.getSerializableExtra(ALL_RECORDS_EXTRA);
            Log.i("val2", "" + temp.get(getTodayCalendar()).alertnessArray[0].getValue());
            allDayRecords = temp;
            saveDataToFile();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        saveDataToFile();
    }

    private void saveDataToFile() {
        try {
            FileOutputStream fileOut = openFileOutput(DAY_RECORDS_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(allDayRecords);
            out.close();
            fileOut.close();
            Log.i("file", "Saved the day records");
        } catch(IOException e)  {
            e.printStackTrace();
            Log.e("file", "Could not write map file!");

        }
    }

    private HashMap<Calendar, DayRecord> readRecordsFromFile() {
        try {
            FileInputStream infile = openFileInput(DAY_RECORDS_FILE);
            ObjectInputStream in = new ObjectInputStream(infile);

            HashMap<Calendar, DayRecord> records = null;

            records = (HashMap<Calendar, DayRecord>) in.readObject();
            if (records == null) {
                records = new HashMap<Calendar, DayRecord>();
                saveDataToFile();
            }
            Log.i("testing", "number of days recorded: " + records.size());


            return records;

        } catch (IOException e) {
            e.printStackTrace();
            // use an empty map
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Log.i("files", "No in file");
        return new HashMap<Calendar, DayRecord>();

    }

    public void onStartDateSet() {
        Log.i("dataEntry", "Moved past init to data entry.");

        hideAll();

        Calendar today = Calendar.getInstance();
        Calendar tomorrow = new GregorianCalendar(today.get(Calendar.YEAR),
                                                  today.get(Calendar.MONTH),
                                                  today.get(Calendar.DAY_OF_MONTH) + 1);
        if (startDate.after(tomorrow)) {
            // Too soon! Tell user to come back later
            TextView comeBackLater = (TextView) findViewById(R.id.comeBackLater);
            comeBackLater.setText("Nothing to do yet! Come back the night before " +
                    DATE_FORMAT.format(startDate.getTime()) + " to record when you go to bed!");
            comeBackLater.setVisibility(View.VISIBLE);

        } else if (startDate.equals(tomorrow) && false) { // Let the user record going to bed, but nothing else
            Button button = (Button) findViewById(R.id.emailDataButton);
            button.setEnabled(false);

            View insertDataLayout = findViewById(R.id.selectDataDateLayout);
            insertDataLayout.setVisibility(View.VISIBLE);
        } else { // User will spend most time in this region. Time to collect data!

            View insertDataLayout = findViewById(R.id.selectDataDateLayout);
            insertDataLayout.setVisibility(View.VISIBLE);
        }
    }

    public void onEditTodayClick(View view) {
        Intent editIntent =  new Intent(this, EditDayActivity.class);
        editIntent.putExtra(CURRENT_DATE_EXTRA, getTodayCalendar());
        editIntent.putExtra(ALL_RECORDS_EXTRA, allDayRecords);
        editIntent.putExtra(START_DATE_EXTRA, startDate);

        startActivityForResult(editIntent, EDIT_DAY_REQ);

    }

    public Calendar getTodayCalendar() {
        Calendar now = Calendar.getInstance();
        return new GregorianCalendar(now.get(Calendar.YEAR),
                                     now.get(Calendar.MONTH),
                                     now.get(Calendar.DAY_OF_MONTH));
    }

    private void hideAll() {

        View insertDataLayout = findViewById(R.id.selectDataDateLayout);
        insertDataLayout.setVisibility(View.GONE);

        View comeBackLater = findViewById(R.id.comeBackLater);
        comeBackLater.setVisibility(View.GONE);
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

}
