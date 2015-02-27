package edu.stanford.trumanc.sleepjournal;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class FirstTimeInitializationActivity extends ActionBarActivity implements DateSetListener {
    private Calendar startDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_initialization);

        // Set the final editText to submit also
        EditText editText = (EditText) findViewById(R.id.studentEmailInputText);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSubmit(v);
                    handled = true;
                }
                return handled;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first_time_initialization, menu);
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

    public void onSubmit(View view) {
        Log.i("test", "in onSubmit");
        if (startDate == null) {
            Toast.makeText(this, "Must select a start date above!", Toast.LENGTH_LONG).show();
        } else if (verifyInput()) {
            int year = getYear();
            int month = getMonth();
            int day = getDay();

            // Save the start date in persistent memory
            SharedPreferences settings = getSharedPreferences(WelcomeActivity.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(WelcomeActivity.START_DATE_YEAR, year);
            editor.putInt(WelcomeActivity.START_DATE_MONTH, month);
            editor.putInt(WelcomeActivity.START_DATE_DAY, day);

            editor.putString(WelcomeActivity.STUDENT_NAME, getName());
            editor.putString(WelcomeActivity.STUDENT_SUNET_ID, getIdNumber());
            editor.putString(WelcomeActivity.STUDENT_EMAIL, getStudentEmail());
            editor.putString(WelcomeActivity.TA_EMAIL, getTaEmail());

            editor.putInt(WelcomeActivity.NUM_DAYS_COLLECTING, getNumDays());
            editor.putBoolean(WelcomeActivity.IS_AWAKE, true);
            editor.putBoolean(WelcomeActivity.START_DATE_SET, true);

            editor.commit();

            // send the startDate back
            Intent returnDateIntent = new Intent();
            returnDateIntent.putExtra(WelcomeActivity.START_DATE_EXTRA,
                    new GregorianCalendar(year, month, day));
            setResult(RESULT_OK, returnDateIntent);
            finish();
        }
    }

    private boolean verifyInput() {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(getTaEmail()).matches()) {
            Toast.makeText(this, "TA Email address isn't valid.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(getStudentEmail()).matches()) {
            Toast.makeText(this, "Student Email address isn't valid.", Toast.LENGTH_SHORT).show();
            return false;
        }
        String suNetID = getIdNumber();
        if (suNetID.length() != 8) {
            Toast.makeText(this, "SUNetID should be 8 characters long.", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Integer.parseInt(suNetID);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "SUNetID is not a valid number.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private int getYear() {
        return startDate.get(Calendar.YEAR);
    }

    private int getMonth()  {
        return startDate.get(Calendar.MONTH);
    }

    private int getDay()  {
        return startDate.get(Calendar.DAY_OF_MONTH);
    }

    private String getName() {
        TextView nameInput = (TextView) findViewById(R.id.nameInputText);
        return nameInput.getText().toString();
    }

    private String getIdNumber() {
        TextView idInput = (TextView) findViewById(R.id.sunetIdInputText);
        return idInput.getText().toString();
    }

    private String getTaEmail() {
        TextView emailInput = (TextView) findViewById(R.id.taEmailInputText);
        return emailInput.getText().toString();
    }

    private String getStudentEmail() {
        TextView emailInput = (TextView) findViewById(R.id.studentEmailInputText);
        return emailInput.getText().toString();
    }

    private int getNumDays() {
        TextView numDaysInput = (TextView) findViewById(R.id.numDaysInput);
        try {
            return Integer.parseInt(numDaysInput.getText().toString());
        } catch (NumberFormatException e) {
            return 49; // By default this lasts for 7 weeks
        }
    }


    @Override
    public void onDateSet(int year, int month, int day) {
        startDate = new GregorianCalendar(year, month, day);

        TextView datePreview  = (TextView) findViewById(R.id.startDateText);
        datePreview.setText(WelcomeActivity.DATE_FORMAT.format(startDate.getTime()));
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
