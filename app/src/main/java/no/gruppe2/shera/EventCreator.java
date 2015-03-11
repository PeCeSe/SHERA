package no.gruppe2.shera;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class EventCreator extends ActionBarActivity {

    EditText nameInput, descriptionInput, participantsInput;
    TextView timeView, dateView, errorView;
    AutoCompleteTextView addressInput;
    static Button pickDateIn, pickTimeIn;
    CheckBox adultCheck;
    Spinner catSpinner;

    private DBHandler db;
    private Firebase ref;

    private long userID;
    private double lat, lng;

    DialogFragment dateFragment;
    DialogFragment timeFragment;

    EventObject eventObject;

    private ArrayAdapter<String> adapter;
    private ArrayList<Address> list;
    private String[] array;

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
        addressInput = (AutoCompleteTextView) findViewById(R.id.addressInputField);
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

        if (fromMap()) {
            getAddress(lat, lng);
        } else if (incomingEvent()) {
            //setFields();
        } else {

        }

        addressInput.setThreshold(3);
        addressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 3) {
                    list = getLocationFromAddress(s.toString());
                    Address loc;
                    if (list != null) {
                        array = new String[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            loc = list.get(i);
                            String in = "";
                            if (loc.getAddressLine(0) != null) {
                                in += loc.getAddressLine(0).toString();
                            }
                            if (loc.getAddressLine(1) != null) {
                                in += " " + loc.getAddressLine(1);
                            }
                            if (loc.getAddressLine(2) != null) {
                                in += " " + loc.getAddressLine(2);
                            }
                            array[i] = in;
                            Log.d("ARRAY::", array[i]);
                        }
                        adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, array);
                        addressInput.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        addressInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = addressInput.getText().toString();
                Address out = null;
                for (int i = 0; i < array.length; i++) {
                    if (array[i].equals(s))
                        out = list.get(i);
                }
                lat = out.getLatitude();
                lng = out.getLongitude();

                Log.d("LATITUDE::", out.getLatitude() + "");
                Log.d("LONGITUDE::", out.getLongitude() + "");
            }
        });

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

    public boolean incomingEvent() {
        Intent i = getIntent();
        //  Bundle extras = i.getExtras();
        if (i != null) {
            eventObject = (EventObject) i.getParcelableExtra("EventObject");
            if (eventObject == null) {
                return false;
            }
            return true;
        }
        return false;
    }


    private void setFields() {
        nameInput.setText(eventObject.getName());
        descriptionInput.setText(eventObject.getDescription());
        addressInput.setText(eventObject.getAddress());
        participantsInput.setText(eventObject.getMaxParticipants() + "");
        pickDateIn.setText(String.format("%02d", eventObject.getCalendar().get(Calendar.DAY_OF_MONTH)) + "-"
                + (String.format("%02d", eventObject.getCalendar().get(Calendar.MONTH) + 1)) + "-"
                + eventObject.getCalendar().get(Calendar.YEAR));
        pickTimeIn.setText(eventObject.getCalendar().get(Calendar.HOUR_OF_DAY) + ":" + eventObject.getCalendar().get(Calendar.MINUTE));
        Log.d("tid", eventObject.getCalendar().toString());
        if (eventObject.isAdult()) {
            adultCheck.setChecked(true);
        }
        catSpinner.setSelection(eventObject.getCategory() - 1);
    }

    private boolean fromMap() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lat = extras.getDouble("Lat", 200);
            lng = extras.getDouble("Long", 200);
            if (lat == 200 || lng == 200) {
                return false;
            }
            return true;
        }
        return false;
    }

    private List<Address> getAddress(double lat, double lng) {
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this);
            if (lat != 0 || lng != 0) {
                addresses = geocoder.getFromLocation(lat,
                        lng, 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getAddressLine(1);
                String country = addresses.get(0).getAddressLine(2);
                String s = address + " " + city;
                addressInput.setText(s);
                return addresses;
            } else {
                Toast.makeText(this, "latitude and longitude are null",
                        Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    private void showDatePickerDialog(View v) {
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
                            userID = Long.parseLong(user.getId());
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);
        }
    }

    private void saveEvent() {
        if (eventObject != null) {
            if (validateInput()) {
                updateEventObject();
                updateObjectInDatabase();
                finish();
            } else
                return;
        } else {
            if (validateInput()) {
                createEventObject();
                writeObjectToDatabase();
                showToast(getResources().getString(R.string.event_created));
                finish();
            } else
                return;
        }
    }

    private boolean validateInput() {
        if (nameInput.getText().length() < 1) {
            writeErrorMessage(getResources().getString(R.string.name_error));
            return false;
        } else if (descriptionInput.getText().length() < 1) {
            writeErrorMessage(getResources().getString(R.string.description_error));
            return false;
        } else if (addressInput.getText().length() < 1) {
            writeErrorMessage(getResources().getString(R.string.address_error));
            return false;
        } else if (participantsInput.getText().toString().length() < 1) {
            writeErrorMessage(getResources().getString(R.string.participants_error));
            return false;
        } else if (Integer.parseInt(participantsInput.getText().toString()) < 1) {
            writeErrorMessage(getResources().getString(R.string.participants_num_error));
            return false;
        }
        if (eventObject != null) {
            if (Integer.parseInt(participantsInput.getText().toString()) < eventObject.getNumParticipants()) {
                writeErrorMessage(getResources().getString(R.string.max_less_than_num_error) + "(" +
                        eventObject.getNumParticipants() + ")");
            }
        }
        Calendar c = Calendar.getInstance();
        if (!cal.after(c)) {
            writeErrorMessage(getResources().getString(R.string.calendar_error));
            return false;
        }
        return true;

    }

    private void writeErrorMessage(String s) {
        errorView = (TextView) findViewById(R.id.error_display_message);
        errorView.setVisibility(View.VISIBLE);
        errorView.setText(s);
    }

    private void createEventObject() {
        eventObject = new EventObject(
                userID,
                nameInput.getText().toString(),
                descriptionInput.getText().toString(),
                addressInput.getText().toString(),
                lat, lng,
                Integer.parseInt(participantsInput.getText().toString()),
                (catSpinner.getSelectedItemPosition() + 1),
                cal,
                adultCheck.isChecked());
    }

    private void updateEventObject() {
        eventObject.setName(nameInput.getText().toString());
        eventObject.setDescription(descriptionInput.getText().toString());
        eventObject.setAddress(addressInput.getText().toString());

        /*Need to make changes so we can construct an Address-object,
        then call to getLocationFromAddress(address)*/

        eventObject.setMaxParticipants(Integer.parseInt(participantsInput.getText().toString()));
        eventObject.setCategory(catSpinner.getSelectedItemPosition() + 1);
        if (cal != null) {
            eventObject.setCalendar(cal);
        }
        eventObject.setAdult(adultCheck.isChecked());

    }

    public ArrayList<Address> getLocationFromAddress(String strAddress) {
        List<Address> result;
        ArrayList<Address> list = new ArrayList<>();
        Geocoder coder = new Geocoder(this);
        try {
            result = coder.getFromLocationName(strAddress, 5);
            if (result == null)
                return null;
            else if (result.size() == 0)
                return null;
            else {
                for (int i = 0; i < result.size(); i++) {
                    list = (ArrayList) result;
                }
            }
        } catch (Exception e) {
            Log.d("EXC::", e.toString());
        }

        return list;
    }

    private void writeObjectToDatabase() {
        try {
            db.pushToDB(eventObject, ref);
        } catch (Exception e) {
            Log.d("createEventObject()", "Failed to send object to database: " + e);
        }
    }

    private void updateObjectInDatabase() {
        try {
            db.updateEventDB(eventObject);
        } catch (Exception e) {
            Log.d("createEventObject()", "Failed to update object in database: " + e);
        }
    }

    private void showToast(String s) {
        Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void showTimePickerDialog(View v) {
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