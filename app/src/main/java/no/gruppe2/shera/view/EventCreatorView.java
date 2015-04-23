package no.gruppe2.shera.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.firebase.client.Firebase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Event;
import no.gruppe2.shera.helpers.HelpMethods;
import no.gruppe2.shera.helpers.ImageAdapter;
import no.gruppe2.shera.helpers.Validator;
import no.gruppe2.shera.service.DBHandler;
import no.gruppe2.shera.service.SqlLiteDBHandler;

public class EventCreatorView extends ActionBarActivity {

    EditText nameInput, descriptionInput, participantsInput;
    TextView timeView, dateView, errorView;
    AutoCompleteTextView addressInput;
    static Button pickDateIn, pickTimeIn, findPhotos;
    CheckBox adultCheck;
    Spinner catSpinner;
    private ImageView eventPhotoView;

    private HelpMethods help;
    private DBHandler db;
    private Firebase ref;

    private long userID;
    private double lat, lng;

    DialogFragment dateFragment;
    DialogFragment timeFragment;

    Event event;

    SqlLiteDBHandler sqldb;

    private ArrayAdapter<String> adapter;
    private ArrayList<Address> list;
    private String[] array;

    public static Calendar cal;
    private static final String DATE_FORMAT = "dd-MM-yyyy", TIME_FORMAT = "kk:mm";
    private static final int TRESHOLD = 3, DEFAULTLONGLAT = 200, LISTOFLOCATIONSIZE = 5;

    GraphObject graphObject;
    private ArrayList<String> myFriendsList;
    private ArrayList<String> myPhotoSourceList, tempMyPhotoSourceList;
    private ArrayList<Bitmap> myPhotoList;
    private String afterPhotos, beforePhotos, sourceToObject;
    private boolean isMorePhotos, isOver18, after, newList, gridViewLoadOnce, stopLoadingData, flag,
            newImageSelected;
    private GridView gridView;
    private ProgressDialog progress;
    private Session session;
    private long last;

    private AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creator);

        db = new DBHandler(this);
        Firebase.setAndroidContext(this);
        ref = new Firebase(getResources().getString(R.string.firebase_root));
        help = new HelpMethods();
        sqldb = new SqlLiteDBHandler(this);
        myFriendsList = new ArrayList<>();
        myPhotoSourceList = new ArrayList<>();
        tempMyPhotoSourceList = new ArrayList<>();
        myPhotoList = new ArrayList<>();
        afterPhotos = "";
        beforePhotos = "";
        sourceToObject = "NOTSET";
        isMorePhotos = true;
        stopLoadingData = false;
        newImageSelected = false;
        last = 0;

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
        findPhotos = (Button) findViewById(R.id.findPhotos);
        eventPhotoView = (ImageView) findViewById(R.id.event_photo_view);

        cal = Calendar.getInstance();

        session = Session.getActiveSession();
        findUserID(session);
        after = true;
        if (myPhotoSourceList == null)
            newList = true;
        else
            newList = false;
        gridViewLoadOnce = false;

        if (fromMap()) {
            addressInput.setText(getAddress(lat, lng));
        } else if (incomingEvent()) {
            cal = event.getCalendar();
            setFields();
        } else {

        }

        addressInput.setThreshold(TRESHOLD);
        setAddressFieldListener();
        setAutoCompleteClickListener();

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

        findPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newList) {
                    myPhotoList = new ArrayList<>();
                    new DownloadImages(myPhotoSourceList).execute();
                    newList = false;
                } else {
                    setGridView();
                }
            }
        });
        SharedPreferences prefs = getSharedPreferences(getResources()
                .getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        isOver18 = prefs.getBoolean(getResources()
                .getString(R.string.shared_preferences_is_over_18), false);
        if (!isOver18) {
            adultCheck.setChecked(false);
            adultCheck.setVisibility(View.GONE);
        }
    }

    public void setGridView() {
        gridViewLoadOnce = true;
        newList = false;
        gridView = new GridView(this);

        if (myPhotoList.size() <= 0)
            findPhotosList(session);

        gridView.setAdapter(new ImageAdapter(this, myPhotoList, true));
        gridView.setNumColumns(4);
        gridView.setPadding(3, 3, 3, 3);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(gridView);
        builder.setTitle(R.string.choose_picture);

        alert = builder.show();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sourceToObject = myPhotoSourceList.get(position);
                newImageSelected = true;
                findPhotos.setText(getResources().getString(R.string.change_photo));
                new DownloadImages(sourceToObject).execute();
                alert.dismiss();
            }
        });
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 2)
                    flag = true;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((lastInScreen == totalItemCount) && isMorePhotos && !stopLoadingData &&
                        (System.currentTimeMillis() - last > 10000) && flag) {
                    stopLoadingData = true;
                    flag = false;
                    findPhotosList(session);
                }
            }
        });
    }

    private void findPhotosList(Session session) {
        Bundle params = new Bundle();
        params.putString("after", afterPhotos);

        if (isMorePhotos) {
            //myPhotoSourceList = new ArrayList<>();
            tempMyPhotoSourceList = new ArrayList<>();
            new Request(session, "/me/photos", params, HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            FacebookRequestError error = response.getError();
                            if (error != null && response != null) {
                                Log.e("ERROR::", error.toString());
                            } else {
                                graphObject = response.getGraphObject();
                            }

                            JSONArray dataArray = (JSONArray) graphObject.getProperty("data");
                            JSONObject pages = (JSONObject) graphObject.getProperty("paging");
                            JSONObject cursorObject = null;

                            if (pages != null) {
                                try {
                                    cursorObject = (JSONObject) pages.get("cursors");
                                    afterPhotos = cursorObject.getString("after");
                                    beforePhotos = cursorObject.getString("before");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (!pages.has("next")) {
                                isMorePhotos = false;
                                stopLoadingData = true;
                            }


                            if (dataArray.length() > 0) {
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject json = dataArray.optJSONObject(i);
                                    myPhotoSourceList.add(findPhotoSource(json));
                                    tempMyPhotoSourceList.add(findPhotoSource(json));
                                }
                            }
                            stopLoadingData = false;
                            new DownloadImages(tempMyPhotoSourceList).execute();
                        }
                    }).executeAsync();
        }
    }

    private String findPhotoSource(JSONObject json) {
        String id = "";
        try {
            id = json.getString("source");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    public boolean incomingEvent() {
        Intent i = getIntent();
        if (i != null) {
            event = (Event) i.getParcelableExtra(getResources()
                    .getString(R.string.intent_parcelable_key));
            if (event == null) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void setAutoCompleteClickListener() {
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
            }
        });
    }

    private void setAddressFieldListener() {
        addressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > TRESHOLD) {
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
                        adapter = new ArrayAdapter<String>(getBaseContext(),
                                android.R.layout.simple_spinner_dropdown_item, array);
                        addressInput.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setFields() {
        nameInput.setText(event.getName());
        descriptionInput.setText(event.getDescription());
        addressInput.setText(event.getAddress());
        participantsInput.setText(event.getMaxParticipants() + "");
        pickDateIn.setText(help.leadingZeroesDate(event.getCalendar()));
        pickTimeIn.setText(help.leadingZeroesTime(event.getCalendar()));
        if (event.isAdult()) {
            adultCheck.setChecked(true);
        }
        catSpinner.setSelection(event.getCategory() - 1);
        if (!event.getPhotoSource().equals("NOTSET")) {
            new DownloadImages(event.getPhotoSource()).execute();
        }
    }

    private boolean fromMap() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lat = extras.getDouble("Lat", DEFAULTLONGLAT);
            lng = extras.getDouble("Long", DEFAULTLONGLAT);
            if (lat == DEFAULTLONGLAT || lng == DEFAULTLONGLAT) {
                return false;
            }
            return true;
        }
        return false;
    }

    private String getAddress(double lat, double lng) {
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this);
            if (lat != 0 || lng != 0) {
                addresses = geocoder.getFromLocation(lat,
                        lng, 1);
                String address = "";
                if (addresses.get(0).getAddressLine(0) != null)
                    address += addresses.get(0).getAddressLine(0);
                if (addresses.get(0).getAddressLine(1) != null)
                    address += " " + addresses.get(0).getAddressLine(1);
                if (addresses.get(0).getAddressLine(2) != null)
                    address += " " + addresses.get(0).getAddressLine(2);
                return address;
            } else {
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

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.saveEvent) {
            saveEvent();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("Event", event);
        setResult(Activity.RESULT_OK, data);
        super.onBackPressed();
    }

    private void showDatePickerDialog(View v) {
        dateFragment = new DatePickerFragment();
        dateFragment.show(getFragmentManager(), "datePicker");

    }

    private void findUserID(final Session session) {
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
        if (event != null) {
            if (validateInput()) {
                updateEventObject();
                updateObjectInDatabase();
                showToast(getResources().getString(R.string.event_saved));
                onBackPressed();
                //finish();
            } else
                return;
        } else {
            if (validateInput()) {
                createEventObject();
                if (!sourceToObject.equals("NOTSET"))
                    event.setPhotoSource(sourceToObject);
                writeObjectToDatabase();
                sqldb.eventCreated(event.getEventID());
                showToast(getResources().getString(R.string.event_saved));
                finish();
            } else
                return;
        }
    }

    private boolean validateInput() {
        Validator validator = new Validator();
        if (validator.isEmpty(nameInput.getText().toString())) {
            writeErrorMessage(getResources().getString(R.string.name_error));
            return false;
        } else if (validator.isEmpty(descriptionInput.getText().toString())) {
            writeErrorMessage(getResources().getString(R.string.description_error));
            return false;
        } else if (validator.isEmpty(addressInput.getText().toString())) {
            writeErrorMessage(getResources().getString(R.string.address_error));
            return false;
        } else if (validator.isEmpty(participantsInput.getText().toString().toString())) {
            writeErrorMessage(getResources().getString(R.string.participants_error));
            return false;
        } else if (validator.isEmpty(participantsInput.getText().toString())) {
            if (validator.isEmpty(Integer.parseInt(participantsInput.getText().toString()))) {
                writeErrorMessage(getResources().getString(R.string.participants_num_error));
                return false;
            }
        }
        if (event != null) {
            if (!validator.isMaxLargerThanNum(Integer.parseInt(participantsInput.getText().toString()), event.getNumParticipants())) {
                writeErrorMessage(getResources().getString(R.string.max_less_than_num_error) + "(" +
                        event.getNumParticipants() + ")");
                return false;
            }
        }
        if (!validator.isDateInFuture(cal)) {
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
        event = new Event(
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
        event.setName(nameInput.getText().toString());
        event.setDescription(descriptionInput.getText().toString());
        event.setAddress(addressInput.getText().toString());
        event.setMaxParticipants(Integer.parseInt(participantsInput.getText().toString()));
        event.setCategory(catSpinner.getSelectedItemPosition() + 1);
        if (cal != null) {
            event.setCalendar(cal);
        }
        event.setAdult(adultCheck.isChecked());
        if (newImageSelected)
            event.setPhotoSource(sourceToObject);
    }

    public ArrayList<Address> getLocationFromAddress(String strAddress) {
        List<Address> result;
        ArrayList list = new ArrayList<>();
        Geocoder coder = new Geocoder(this);
        try {
            result = coder.getFromLocationName(strAddress, LISTOFLOCATIONSIZE);
            if (result == null)
                return null;
            else if (result.isEmpty())
                return null;
            else {
                for (int i = 0; i < result.size(); i++) {
                    list = (ArrayList) result;
                }
            }
        } catch (Exception e) {
            showToast(getResources().getString(R.string.location_error));
        }

        return list;
    }

    private void writeObjectToDatabase() {
        try {
            db.pushToDB(event, ref);
        } catch (Exception e) {
            showToast(getResources().getString(R.string.write_to_db_error));
        }
    }

    private void updateObjectInDatabase() {
        try {
            db.updateEventDB(event);
        } catch (Exception e) {
            showToast(getResources().getString(R.string.write_to_db_error));
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

    private class DownloadImages extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        Bitmap view;
        ArrayList<String> strings = new ArrayList<>();
        String source;

        public DownloadImages(List<String> list) {
            strings = (ArrayList) list;
        }

        public DownloadImages(String src) {
            source = src;
        }

        @Override
        protected void onPreExecute() {
            if (!strings.isEmpty()) {
                progress = new ProgressDialog(EventCreatorView.this);
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setTitle(getResources().getString(R.string.loading));
                progress.setMessage(getResources().getString(R.string.download_from_facebook));
                progress.setCancelable(true);
                progress.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.abort_photo_download), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (myPhotoList.size() < 25)
                            alert.dismiss();
                    }
                });
                progress.setIndeterminate(false);
                progress.setMax(strings.size());
                progress.setProgress(0);
                progress.show();
            }
        }

        protected Bitmap doInBackground(String... params) {
            Bitmap icon = null;
            if (!strings.isEmpty()) {
                bmImage = new ImageView(getBaseContext());
                for (int i = 0; i < strings.size(); i++) {
                    String url = strings.get(i);
                    icon = null;
                    try {
                        InputStream in = new java.net.URL(url).openStream();
                        icon = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(in), 115, 115, true);
                        view = icon;
                        myPhotoList.add(view);
                        onProgressUpdate(i);
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                String url = source;
                icon = null;
                try {
                    InputStream in = new java.net.URL(url).openStream();
                    Bitmap iconTemp;
                    icon = Bitmap.createScaledBitmap(iconTemp = BitmapFactory.decodeStream(in), iconTemp.getWidth() / 2, iconTemp.getHeight() / 2, true);
                    view = icon;
                } catch (Exception e) {
                    Log.e("Error", e + "");
                    e.printStackTrace();
                }
            }
            return icon;
        }

        protected void onProgressUpdate(Integer... values) {
            progress.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (!strings.isEmpty()) {
                if (gridViewLoadOnce) {
                    int currentPosition = gridView.getFirstVisiblePosition();
                    gridView.setAdapter(new ImageAdapter(EventCreatorView.this, myPhotoList, true));
                    gridView.setSelection(currentPosition + 4);
                } else
                    setGridView();
                bmImage.setImageBitmap(result);
                progress.dismiss();
                newList = false;
            } else {
                eventPhotoView.setImageBitmap(result);
            }
            last = System.currentTimeMillis();
        }
    }
}