package no.gruppe2.shera;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.firebase.client.Firebase;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class EventCreator extends ActionBarActivity {

    EditText nameInput, descriptionInput, addressInput, participantsInput;
    TextView timeView, dateView;
    static Button pickDateIn, pickTimeIn;
    CheckBox adultCheck;
    Spinner catSpinner;

    private DBHandler db;
    private Firebase ref;

    private String userID;

    DialogFragment dateFragment;
    DialogFragment timeFragment;

    EventObject eventObject;

    public static Calendar cal;
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String TIME_FORMAT = "kk:mm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creator);
        db=new DBHandler();
        Firebase.setAndroidContext(this);
        ref=new Firebase("https://shera.firebaseio.com/");

        nameInput = (EditText) findViewById(R.id.nameInputField);
        descriptionInput = (EditText) findViewById(R.id.descriptionInputField);
        addressInput = (EditText) findViewById(R.id.addressInputField);
        participantsInput = (EditText) findViewById(R.id.participantsInputField);
        pickDateIn = (Button) findViewById(R.id.pickDateButton);
        pickTimeIn = (Button) findViewById(R.id.pickTimeButton);
        timeView = (TextView) findViewById(R.id.timeText);
        dateView = (TextView) findViewById(R.id.dateText);
        adultCheck = (CheckBox) findViewById(R.id.adultCheck);
        catSpinner = (Spinner) findViewById(R.id.cat_spinner);

        cal = Calendar.getInstance();

        final Session session = Session.getActiveSession();
        findUserID(session);


        pickDateIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showDatePickerDialog(arg0);
            }
        });
        pickTimeIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showTimePickerDialog(arg0);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_creator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.saveEvent) {
            saveEvent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void showDatePickerDialog(View v) {
        dateFragment = new DatePickerFragment();
        dateFragment.show(getFragmentManager(), "datePicker");

    }

    private void findUserID(final Session session){
        if (session != null && session.isOpened()) {
            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (session == Session.getActiveSession()) {
                        if (user != null) {
                            userID = user.getId();
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);
        }
    }

    public void saveEvent() {
        if (validateInput()) {
            createEventObject();
            writeObjectToDatabase();
            showToast(getResources().getString(R.string.event_created));
            finish();
        }
    }

    public boolean validateInput() {
        if (nameInput.getText().length() < 1) {
            writeErrorMessage("STRING");
        } else if (descriptionInput.getText().length() < 1) {
            writeErrorMessage("STRING");
        } else if (addressInput.getText().length() < 1) {
            writeErrorMessage("STRING");
        } else if (participantsInput.getText().toString().length() < 1) {
            writeErrorMessage("STRING");
        }
        return false;
    }

    private void writeErrorMessage(String s) {

    }

    public void createEventObject(){
        eventObject = new EventObject(userID, nameInput.getText().toString(), descriptionInput.getText().toString(),
                addressInput.getText().toString(), Integer.parseInt(participantsInput.getText().toString()),
                (catSpinner.getSelectedItemPosition()+1), cal, adultCheck.isChecked());
    }

    public void writeObjectToDatabase() {
        try {
            db.pushToDB(eventObject, ref);
        } catch (Exception e) {
            Log.d("createEventObject()", "Failed to send object to database: " + e);
        }
    }

    public void showToast(String s) {
        Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
    }

    public void showTimePickerDialog(View v) {
        timeFragment = new TimePickerFragment();
        timeFragment.show(getFragmentManager(), "timePicker");
    }

    private static void updateDateButtonText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String dateForButton = dateFormat.format(cal.getTime());
        pickDateIn.setText(dateForButton);
    }
    private static void updateTimeButtonText() {
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
        String timeForButton = timeFormat.format(cal.getTime());
        pickTimeIn.setText(timeForButton);
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            updateDateButtonText();
        }
    }

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);
            updateTimeButtonText();
        }
    }

}
