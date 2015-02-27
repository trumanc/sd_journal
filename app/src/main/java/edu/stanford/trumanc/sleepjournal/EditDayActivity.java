package edu.stanford.trumanc.sleepjournal;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;


public class EditDayActivity extends ActionBarActivity
        implements TimePickedListener, NumberPicker.OnValueChangeListener {

    private Calendar date;
    private Calendar startDate;
    private HashMap<Calendar, DayRecord> records;

    private DayRecord currDay;


    private ArrayList<String> alertnessRecords;
    private ArrayAdapter<String> alertnessAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_day);

        getSupportActionBar().setTitle("Edit the current day:");

        Intent inputIntent = getIntent();
        if (inputIntent == null) {
            throw new AssertionError("WHY IS THE INTENT NULL?");
        }
        date = (Calendar)inputIntent.getSerializableExtra(WelcomeActivity.CURRENT_DATE_EXTRA);
        records = (HashMap<Calendar, DayRecord>) inputIntent.getSerializableExtra(WelcomeActivity.ALL_RECORDS_EXTRA);
        startDate = (Calendar)inputIntent.getSerializableExtra(WelcomeActivity.START_DATE_EXTRA);

        if (date == null || records == null || startDate == null) {
            throw new AssertionError("Something was NULL!");
        }

        TextView currDate = (TextView) findViewById(R.id.currDateText);
        currDate.setText("Today is: " + WelcomeActivity.DATE_FORMAT.format(date.getTime()));

        if (startDate.after(date)) {
            // Only thing we can do today is go to sleep and record that for tomorrow
            Button wakeSleep = (Button) findViewById(R.id.wakeSleepButton);
            wakeSleep.setText("Record sleep time for tomorrow");
        } else { // assumes day is valid, and can have data entered
            displayCurrentDayData();
        }
    }

    private void displayCurrentDayData() {
        // Get the record for the present day:
        if (currDay == null){
            currDay = getRecord(date);
        }

        // Set the Wake/Sleep special button
        Button wakeSleepButton = (Button) findViewById(R.id.wakeSleepButton);

        if (currDay.timeInBed == null) {
            wakeSleepButton.setText("Going to Sleep");
        } else if (currDay.timeAwake == null) {
            wakeSleepButton.setText("Wake up");
        } else {
            // Clicking now will record sleep time for tomorrow
            wakeSleepButton.setText("Record sleep time for tomorrow");
        }

        // Set the recorded times (inBed, asleep, awake, outOfBed
        TextView timeDisplay = (TextView) findViewById(R.id.timeInBedText);
        timeDisplay.setText(getDisplayString(currDay.timeInBed));

        timeDisplay = (TextView) findViewById(R.id.timeAsleepText);
        timeDisplay.setText(getDisplayString(currDay.timeAsleep));

        timeDisplay = (TextView) findViewById(R.id.timeAwakeText);
        timeDisplay.setText(getDisplayString(currDay.timeAwake));

        timeDisplay = (TextView) findViewById(R.id.timeOutOfBedText);
        timeDisplay.setText(getDisplayString(currDay.timeOutOfBed));

        //Set alertness values in list
        if (alertnessRecords == null) {
            alertnessRecords = new ArrayList<String>();
        }
        alertnessRecords.clear();

        for (int i = 0; i < 12; i++) {
            alertnessRecords.add(indexToTimeRange(i) + ": " + currDay.alertnessArray[i].getValue());
        }

        if (alertnessAdapter == null) {
            alertnessAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, alertnessRecords);
        }

        ListView alertnessListView = (ListView) findViewById(R.id.alertnessListView);
        alertnessListView.setAdapter(alertnessAdapter);
        alertnessListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showAlertnessDialog(position);
            }
        });
        alertnessAdapter.notifyDataSetChanged();

    }

    private String indexToTimeRange(int index) {
        return indexToHour(index) + "-" + indexToHour(index+1);
    }

    private String indexToHour(int index) {
        int hour = (((index*2) + 11) % 12) + 1;
        // Does mod, but also makes index 0 -> 12AM and index 6 -> 12PM
        String amPm = index < 6 ? "AM" : "PM";

        return hour + amPm;
    }


    private String getDisplayString(Calendar time) {
        if (time == null) {
            return "Unknown";
        } else {
            int hour = time.get(Calendar.HOUR);
            int minute = time.get(Calendar.MINUTE);
            String amPm = time.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
            return "" + hour + ":" + minute + " " + amPm;
        }
    }


    public void onChangeTime(View view) {
        // Use view id to differentiate!
    }

    public void onChangeAlertness(View view) {

    }

    public void onWakeSleepClick(View view) {
        if (currDay == null) {
            currDay = getRecord(date);
        }

        if (currDay.timeInBed == null) {
            // Recording bed time for today (forgot to do so last night I guess)
            DialogFragment newFragment = TimePickerFragment.newInstance(TimeRecord.TIME_IN_BED,
                    date, this);
            newFragment.show(getFragmentManager(), "TESTING");
        } else if (currDay.timeAwake == null) {
            // Launch the WakingUp activity. Get a lot of info back:
            // fellAsleep, awokeAt, outofBed, awakeInNight, morningGrogginess, dreams, nightmare, lucid, recurring
        } else {
            // Going to sleep, and applying this time to tomorrow's record
            Log.i("testing", "Recording timeInBed for tomorrow's record");
            Calendar tomorrow = new GregorianCalendar(date.get(Calendar.YEAR),
                                                      date.get(Calendar.MONTH),
                                                      date.get(Calendar.DAY_OF_MONTH) + 1);
            DialogFragment newFragment = TimePickerFragment.newInstance(TimeRecord.TIME_IN_BED,
                                                                        tomorrow,
                                                                        this);
            newFragment.show(getFragmentManager(), "TESTING");
        }
    }

    @Override
    public void onTimePicked(TimeRecord which, int hour, int minute, Calendar date) {
        if (currDay == null) {
            currDay = getRecord(date);
        }
        if (which == TimeRecord.TIME_IN_BED) {

            Calendar time = new GregorianCalendar(date.get(Calendar.YEAR),
                                                  date.get(Calendar.MONTH),
                                                  date.get(Calendar.DAY_OF_MONTH) - (date.get(Calendar.AM_PM) == Calendar.PM ? 1 : 0),
                    // If the time recorded is PM, it is being recorded from the night before.
                                                  hour,
                                                  minute);
            currDay.timeInBed = time;
        } else if (which == TimeRecord.TIME_ASLEEP) {
            Calendar time = new GregorianCalendar(date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH),
                    date.get(Calendar.DAY_OF_MONTH) - (date.get(Calendar.AM_PM) == Calendar.PM ? 1 : 0),
                    // If the time recorded is PM, it is being recorded from the night before.
                    hour,
                    minute);
            currDay.timeAsleep = time;
        } else if (which == TimeRecord.TIME_AWAKE) {
            Calendar time = new GregorianCalendar(date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH),
                    date.get(Calendar.DAY_OF_MONTH),
                    hour,
                    minute);
            currDay.timeAwake = time;
        } else if (which == TimeRecord.TIME_OUT_OF_BED) {
            Calendar time = new GregorianCalendar(date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH),
                    date.get(Calendar.DAY_OF_MONTH),
                    hour,
                    minute);
            currDay.timeOutOfBed = time;
        }
        records.put(date, currDay);
        displayCurrentDayData(); // Update the views in case we changed something for today
    }

    private void showAlertnessDialog(final int index){
        if (currDay == null) {
            currDay = getRecord(date);
        }

        final Dialog d = new Dialog(EditDayActivity.this);
        currDialog = d;
        d.setTitle("Set Your Alertness: ");
        d.setContentView(R.layout.alertness_dialog);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(6);
        np.setMinValue(1);
        np.setValue(currDay.alertnessArray[index].getValue());
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        Button setButton = (Button) d.findViewById(R.id.submitAlertnessButton);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currDay.alertnessArray[index] = Alertness.values()[np.getValue()-1];
                d.dismiss();
                displayCurrentDayData();
            }
        });
        d.show();
    }

    private DayRecord getRecord(Calendar date) {
        if (records.containsKey(date)) {
            return records.get(date);
        } else {
            DayRecord currDay = new DayRecord(date.get(Calendar.YEAR),
                                              date.get(Calendar.MONTH),
                                              date.get(Calendar.DAY_OF_MONTH));
            records.put(date, currDay);
            return currDay;
        }
    }

    public void onSubmit(View view) {
        // Send new hashmap back

        Intent intent = new Intent();
        intent.putExtra(WelcomeActivity.ALL_RECORDS_EXTRA, records);
        Log.i("val", "" + records.get(date).alertnessArray[0].getValue());
        setResult(RESULT_OK, intent);
        finish();
    }

    private Dialog currDialog;

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        TextView desc = (TextView) currDialog.findViewById(R.id.description);

        // newVal is 1-indexed, values array is 0 indexed
        desc.setText(Alertness.values()[newVal -1].getDescription());
    }

    @Override
    public void onStop() {
        super.onStop();

        onSubmit(null);
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        private TimePickedListener listener;
        private TimeRecord whichTime;
        private Calendar date;

        public static TimePickerFragment newInstance(TimeRecord which, Calendar date, TimePickedListener listener) {
            TimePickerFragment tpf = new TimePickerFragment();
            tpf.listener = listener;
            tpf.whichTime = which;
            tpf.date = date;
            return tpf;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            listener.onTimePicked(whichTime, hourOfDay, minute, date);
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_day, menu);
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
