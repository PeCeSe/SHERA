package no.gruppe2.shera.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Event;
import no.gruppe2.shera.fragments.NavigationDrawerFragment;
import no.gruppe2.shera.service.SqlLiteDBHandler;

/*
This class displays a "Google-maps" instance for the user, where markers shows the location of
various events. The user can tap on the markers to display an info-window with the name and
description of the event, and tap the info-window to open EventView.class.
The user can also create a new event by long-clicking on the location on the map where he wants
the event to be. A new marker will then appear, which the user can drag around, or click the
info window to create a new event at that location.
The user can also create new events through the Navigation Drawer Menu on the left hand side.
The menu also allows the user to navigate to the EventsView class (or ChatsView class),
or to log out.
The Navigation Drawer also provides filter options. The user can choose to only show events within
a certain time or distance range, or to only show a given category.
The user can also choose to show or hide adult-events (events which may contain alcohol consumption)
if the user is over 18. If the user is under 18 the adult events will be hidden, and it will not be
an option to show them.
*/

public class MapView extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment navigationDrawerFragment;
    private CharSequence title;

    Session session;
    private GoogleMap map;
    Marker marker;
    private SeekBar timeSeekBar, radiusSeekBar;
    private CheckBox adultCheck;
    private Spinner categorySpinner;

    static LinkedList<Event> list;
    private ArrayList<Long> arrayList;
    private HashMap<String, Object> hash;
    private HashMap<String, Event> markerMap;
    private HashMap<String, Marker> markerEventMap;
    private Event eo;
    private String userID;
    private final static int ZOOMLEVEL = 14, THREE_WEEKS = 22, TEN_KILOMETRES = 11,
            DOUBLE_TAP_TIME = 2000;
    private int dateSeekBarProgress, radiusSeekBarProgress;
    private Query queryRef;

    private Circle circle;

    private SqlLiteDBHandler sqldb;

    private boolean isOver18;
    private long backButtonPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Firebase.setAndroidContext(this);
        isOver18 = false;

        list = new LinkedList<>();

        setSession();
        findUserID(session);

        sqldb = new SqlLiteDBHandler(this);

        setNewMarkerListener();

        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);

        map.getUiSettings().setZoomControlsEnabled(true);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        hash = new HashMap<>();
        markerMap = new HashMap<>();
        markerEventMap = new HashMap<>();

        Firebase ref = new Firebase(getResources().getString(R.string.firebase_root));
        Firebase event = ref.child(getResources().getString(R.string.firebase_events));
        queryRef = event.orderByChild("calendar");

        arrayList = new ArrayList<>();
        readEventsFromFirebase();
        setInfoWindowListener();

        dateSeekBarProgress = THREE_WEEKS;
        radiusSeekBarProgress = TEN_KILOMETRES;

        timeSeekBar = (SeekBar) findViewById(R.id.drawer_time_seekbar);
        radiusSeekBar = (SeekBar) findViewById(R.id.drawer_radius_seekbar);
        adultCheck = (CheckBox) findViewById(R.id.drawer_checkbox_adult);
        categorySpinner = (Spinner) findViewById(R.id.drawer_category_spinner);


        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        centerMapOnMyLocation();

        setListeners();
        SharedPreferences prefs = getSharedPreferences(getResources()
                .getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        isOver18 = prefs.getBoolean(getResources()
                .getString(R.string.shared_preferences_is_over_18), false);
        if (isOver18) {
            adultCheck.setChecked(true);
        } else {
            adultCheck.setChecked(false);
            adultCheck.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.menu_map, menu);

            restoreActionBar();

            MenuItem searchItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    Calendar maxDate = Calendar.getInstance();
                    long daysInMillis = TimeUnit.MILLISECONDS.convert(dateSeekBarProgress, TimeUnit.DAYS);
                    maxDate.setTimeInMillis(maxDate.getTimeInMillis() + daysInMillis);

                    if (s.length() > 0) {
                        for (Event e : list) {
                            if (!e.getName().toLowerCase().contains(s.toLowerCase()) &&
                                    !e.getAddress().toLowerCase().contains(s.toLowerCase()) &&
                                    !e.getDescription().toLowerCase().contains(s.toLowerCase())) {

                                if (markerEventMap.containsKey(e.getEventID())) {
                                    removePin(e);
                                }

                            } else if (e.getName().toLowerCase().contains(s.toLowerCase()) ||
                                    e.getAddress().toLowerCase().contains(s.toLowerCase()) ||
                                    e.getDescription().toLowerCase().contains(s.toLowerCase())) {

                                if (!markerEventMap.containsKey(e.getEventID())) {
                                    if (dateSeekBarProgress >= THREE_WEEKS || !(e.getCalendar().after(maxDate))) {
                                        if (((radiusSeekBarProgress < TEN_KILOMETRES) && isEventInsideCircle(e)) || circle == null) {
                                            if ((e.getCategory() == categorySpinner.getSelectedItemPosition()) || categorySpinner.getSelectedItemPosition() <= 0) {
                                                if ((e.isAdult() && adultCheck.isChecked()) || !e.isAdult()) {
                                                    addPin(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        updateMarkers();
                    }
                    return false;
                }
            });

            return true;
        }


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (backButtonPressed + DOUBLE_TAP_TIME > System.currentTimeMillis()) {
            Intent intent = new Intent(this, LogInView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, getResources().getString(R.string.double_tap_to_finish), Toast.LENGTH_SHORT).show();
        }
        backButtonPressed = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPrefs = this.getSharedPreferences(
                getResources().getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        sharedPrefs.edit().putString("userID", userID).apply();
    }

    private void readEventsFromFirebase() {
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                hash = new HashMap<>();
                hash = (HashMap<String, Object>) dataSnapshot.getValue();
                eo = createObject(hash);

                ArrayList<String> eventIDs = sqldb.getAllEvents();

                if ((eo.getNumParticipants() < eo.getMaxParticipants()) ||
                        ((eo.getNumParticipants() >= eo.getMaxParticipants()) &&
                                eventIDs.contains(eo.getEventID()))) {
                    if (isOver18)
                        list.add(eo);
                    else if (!eo.isAdult())
                        list.add(eo);

                    if (!adultCheck.isChecked() && !eo.isAdult())
                        addPin(eo);
                    else if (adultCheck.isChecked())
                        addPin(eo);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                hash = new HashMap<>();
                hash = (HashMap<String, Object>) dataSnapshot.getValue();
                eo = createObject(hash);

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getEventID().equals(eo.getEventID())) {
                        if (isOver18)
                            list.set(i, eo);
                        else if (!eo.isAdult())
                            list.set(i, eo);
                        break;
                    }
                }

                ArrayList<String> eventIDs = sqldb.getAllEvents();
                if ((eo.getNumParticipants() < eo.getMaxParticipants()) ||
                        ((eo.getNumParticipants() >= eo.getMaxParticipants()) &&
                                eventIDs.contains(eo.getEventID()))) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getEventID().equals(eo.getEventID())) {
                            list.set(i, eo);
                            Collections.sort(list, new CalendarCompare());
                            if (markerEventMap.containsKey(eo.getEventID()))
                                removePin(eo);
                            addPin(eo);
                            Marker m = markerEventMap.get(eo.getEventID());
                            m.showInfoWindow();
                        } else {
                            list.add(eo);
                            Collections.sort(list, new CalendarCompare());
                            if (markerEventMap.containsKey(eo.getEventID()))
                                removePin(eo);
                            addPin(eo);
                            Marker m = markerEventMap.get(eo.getEventID());
                            m.showInfoWindow();
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getEventID().equals(eo.getEventID())) {
                            list.remove(i);
                            if (markerEventMap.containsKey(eo.getEventID())) {
                                removePin(eo);
                            }
                        }
                    }
                }


                EventsView.newList(list);
                updateMarkers();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                hash = new HashMap<>();
                hash = (HashMap<String, Object>) dataSnapshot.getValue();
                eo = createObject(hash);

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getEventID().equals(eo.getEventID())) {
                        if (markerEventMap.containsKey(eo.getEventID()))
                            removePin(eo);
                        list.remove(i);
                        break;
                    }
                }
                EventsView.newList(list);

                ArrayList<String> eventIDs = sqldb.getAllEvents();
                if (eventIDs.contains(eo.getEventID())) {
                    sqldb.deleteEventID(eo.getEventID());
                    createToast(getBaseContext().getResources().getString(R.string.cancelled));
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void checkForCancelledEvents() {
        ArrayList<String> eventIDs = sqldb.getAllEvents();

        for (Event e : list) {
            if (eventIDs.contains(e.getEventID())) {
                eventIDs.remove(e.getEventID());
            }
        }

        if (!eventIDs.isEmpty()) {
            for (int i = 0; i < eventIDs.size(); i++) {
                sqldb.deleteEventID(eventIDs.get(i));
                createToast(getBaseContext().getResources().getString(R.string.cancelled));
            }
        }
    }

    private void createToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void setListeners() {

        adultCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateMarkers();
            }
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMarkers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        timeSeekBar.setMax(THREE_WEEKS);
        timeSeekBar.setProgress(THREE_WEEKS);

        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dateSeekBarProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateMarkers();
            }
        });

        radiusSeekBar.setMax(TEN_KILOMETRES);
        radiusSeekBar.setProgress(TEN_KILOMETRES);

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusSeekBarProgress = progress;

                if (radiusSeekBarProgress < TEN_KILOMETRES) {
                    if (circle != null) {
                        circle.setRadius(radiusSeekBarProgress * 500);
                    }
                } else {
                    if (circle != null) {
                        circle.remove();
                        circle = null;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                Location location = map.getMyLocation();

                if (location != null) {
                    if (circle == null) {
                        circle = map.addCircle(new CircleOptions()
                                .center(new LatLng(location.getLatitude(), location.getLongitude()))
                                .radius(radiusSeekBarProgress * 500)
                                .strokeColor(getBaseContext().getResources().getColor(R.color.radius_green))
                                .strokeWidth(2)
                                .fillColor(getBaseContext().getResources().getColor(R.color.transparent_green)));
                    }
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateMarkers();
            }
        });
    }

    private void updateMarkers() {
        Calendar maxDate = new GregorianCalendar();
        maxDate = Calendar.getInstance();
        long daysInMillis = TimeUnit.MILLISECONDS.convert(dateSeekBarProgress, TimeUnit.DAYS);
        maxDate.setTimeInMillis(maxDate.getTimeInMillis() + daysInMillis);
        if (categorySpinner.getSelectedItemPosition() > 0) {
            ListIterator<Event> iterator = list.listIterator();
            while (iterator.hasNext()) {
                Event event = iterator.next();
                if (dateSeekBarProgress >= THREE_WEEKS || !(event.getCalendar().after(maxDate))) {
                    if (((radiusSeekBarProgress < TEN_KILOMETRES) && isEventInsideCircle(event)) || circle == null) {
                        if (event.getCategory() == categorySpinner.getSelectedItemPosition()) {
                            if (event.isAdult() && adultCheck.isChecked()) {
                                if (!markerEventMap.containsKey(event.getEventID())) {
                                    addPin(event);
                                }
                            } else if (!event.isAdult()) {
                                if (!markerEventMap.containsKey(event.getEventID())) {
                                    addPin(event);
                                }
                            } else if (event.isAdult() && !adultCheck.isChecked()) {
                                if (markerEventMap.containsKey(event.getEventID())) {
                                    removePin(event);
                                }
                            }
                        } else {
                            if (markerEventMap.containsKey(event.getEventID())) {
                                removePin(event);
                            }
                        }
                    } else {
                        if (markerEventMap.containsKey(event.getEventID())) {
                            removePin(event);
                        }
                    }
                } else {
                    if (markerEventMap.containsKey(event.getEventID())) {
                        removePin(event);
                    }
                }
            }
        } else {
            ListIterator<Event> iterator = list.listIterator();
            while (iterator.hasNext()) {
                Event event = iterator.next();
                if (dateSeekBarProgress >= THREE_WEEKS || !(event.getCalendar().after(maxDate))) {
                    if (((radiusSeekBarProgress < TEN_KILOMETRES) && isEventInsideCircle(event)) || circle == null) {
                        if (event.isAdult() && adultCheck.isChecked()) {
                            if (!markerEventMap.containsKey(event.getEventID())) {
                                addPin(event);
                            }
                        } else if (!event.isAdult()) {
                            if (!markerEventMap.containsKey(event.getEventID())) {
                                addPin(event);
                            }
                        } else if (event.isAdult() && !adultCheck.isChecked()) {
                            if (markerEventMap.containsKey(event.getEventID())) {
                                removePin(event);
                            }
                        }
                    } else {
                        if (markerEventMap.containsKey(event.getEventID())) {
                            removePin(event);
                        }
                    }
                } else {
                    if (markerEventMap.containsKey(event.getEventID())) {
                        removePin(event);
                    }
                }
            }
        }
    }

    private boolean isEventInsideCircle(Event event) {
        float[] distance = new float[2];

        if (circle != null) {
            Location.distanceBetween(event.getLatitude(), event.getLongitude(),
                    circle.getCenter().latitude, circle.getCenter().longitude, distance);

            return distance[0] < circle.getRadius();
        }
        return true;
    }

    private void addPin(Event eo) {
        Calendar today = Calendar.getInstance();
        if (today.before(eo.getCalendar())) {
            Marker m = map.addMarker(new MarkerOptions()
                    .position(new LatLng(eo.getLatitude(), eo.getLongitude()))
                    .title(eo.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(getColor(eo)))
                    .snippet(eo.getDescription()));
            markerMap.put(m.getId(), eo);
            markerEventMap.put(eo.getEventID(), m);
        }
    }

    private float getColor(Event eo) {
        float hue = BitmapDescriptorFactory.HUE_RED;
        List<String> ownEvents = sqldb.getOwnEvents();

        if (ownEvents.contains(eo.getEventID())) {
            hue = BitmapDescriptorFactory.HUE_ORANGE;
            return hue;
        }

        ArrayList<String> joinedEvents = sqldb.getJoinedEvents();

        if (joinedEvents.contains(eo.getEventID())) {
            hue = BitmapDescriptorFactory.HUE_GREEN;
            return hue;
        }
        return hue;
    }

    private void removePin(Event eo) {
        Marker marker = markerEventMap.get(eo.getEventID());
        markerMap.remove(marker.getId());
        markerEventMap.remove(eo.getEventID());
        marker.remove();
    }

    private Event createObject(HashMap<String, Object> hash) {
        String time = hash.get("calendar").toString();
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(Long.parseLong(time));
        if (hash.get("participantsList") == null)
            arrayList = null;
        else
            arrayList = (ArrayList<Long>) hash.get("participantsList");

        return new Event(hash.get("eventID").toString(),
                Long.parseLong(hash.get("userID").toString()),
                hash.get("name").toString(),
                hash.get("description").toString(),
                hash.get("address").toString(),
                Double.parseDouble(hash.get("latitude").toString()),
                Double.parseDouble(hash.get("longitude").toString()),
                Integer.parseInt(hash.get("maxParticipants").toString()),
                Integer.parseInt(hash.get("numParticipants").toString()),
                Integer.parseInt(hash.get("category").toString()),
                cal,
                Boolean.parseBoolean(hash.get("adult").toString()),
                arrayList,
                hash.get("photoSource").toString());
    }

    private void centerMapOnMyLocation() {

        Location location = map.getMyLocation();

        if (location != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), ZOOMLEVEL));
        }
    }

    private void findUserID(final Session session) {
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

    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position) {
            case 0: {
                Intent i = new Intent(this, EventCreatorView.class);
                startActivity(i);
                break;
            }
            case 1: {
                Intent intent = new Intent(this, EventsView.class);
                checkForCancelledEvents();
                ArrayList<Event> eoList = new ArrayList<>();
                ListIterator<Event> itObject = list.listIterator();
                ArrayList<String> localEvents = sqldb.getAllEvents();

                while (itObject.hasNext()) {
                    Event event = itObject.next();

                    for (int i = 0; i < localEvents.size(); i++) {
                        if (event.getEventID().equals(localEvents.get((i)))) {
                            eoList.add(event);
                        }
                    }
                }

                ArrayList<Event> fullList = new ArrayList<>();
                for (Event e : list) {
                    fullList.add(e);
                }

                intent.putParcelableArrayListExtra(getResources().getString(R.string.intent_parcelable_key), eoList);
                intent.putParcelableArrayListExtra("Events", fullList);
                intent.putExtra("Chat", false);
                startActivity(intent);
                break;
            }
            case 2: {
                Intent intent = new Intent(this, EventsView.class);
                checkForCancelledEvents();
                ArrayList<Event> eoList = new ArrayList<>();
                ListIterator<Event> itObject = list.listIterator();
                ArrayList<String> localEvents = sqldb.getAllEvents();

                while (itObject.hasNext()) {
                    Event event = itObject.next();

                    for (int i = 0; i < localEvents.size(); i++) {
                        if (event.getEventID().equals(localEvents.get((i)))) {
                            eoList.add(event);
                        }
                    }
                }

                ArrayList<Event> fullList = new ArrayList<>();

                for (Event e : list) {
                    fullList.add(e);
                }

                intent.putParcelableArrayListExtra(getResources().getString(R.string.intent_parcelable_key), eoList);
                intent.putParcelableArrayListExtra("Events", fullList);
                intent.putExtra("Chat", true);
                startActivity(intent);
                break;
            }
            case 3: {
                session.close();
                session.closeAndClearTokenInformation();
                finish();
                break;
            }
            case 4: {
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, MapsFragment.newInstance(position))
                        .commit();
                break;
            }
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                title = getString(R.string.create_event_string);
                break;
            case 1:
                title = getString(R.string.title_activity_events);
                break;
            case 2:
                title = getString(R.string.action_settings);
                break;
            case 3:
                title = getString(R.string.logout);
                break;
            case 4:
                title = getString(R.string.title_activity_map);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }

    private void setNewMarkerListener() {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (marker == null) {
                    createMarker(latLng.latitude, latLng.longitude);
                } else {
                    marker.remove();
                    createMarker(latLng.latitude, latLng.longitude);
                }
            }
        });
    }

    private void setSession() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Session.setActiveSession((Session) extras.getSerializable("fb_session"));
        }
        session = Session.getActiveSession();
        if (!session.isOpened()) {
            finish();
        }
    }

    private void createMarker(double lat, double lng) {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(getResources().getString(R.string.pin_new_event)).draggable(true)
                .snippet(getResources().getString(R.string.pin_creation_info))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

        marker.showInfoWindow();
    }

    public static class MapsFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static MapsFragment newInstance(int sectionNumber) {
            MapsFragment fragment = new MapsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_map, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MapView) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void setInfoWindowListener() {
        map.setOnInfoWindowClickListener(
                new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        if (markerMap.containsKey(marker.getId())) {
                            Event event = markerMap.get(marker.getId());
                            Intent i = new Intent(getBaseContext(), EventView.class);
                            i.putExtra(getResources().getString(R.string.intent_parcelable_key), event);
                            startActivity(i);
                        } else {
                            marker.remove();
                            Intent i = new Intent(getBaseContext(), EventCreatorView.class);
                            i.putExtra("Lat", marker.getPosition().latitude);
                            i.putExtra("Long", marker.getPosition().longitude);
                            startActivity(i);
                        }
                    }
                }
        );
    }

    private class CalendarCompare implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1.getCalendar().getTimeInMillis() >= e2.getCalendar().getTimeInMillis()) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
