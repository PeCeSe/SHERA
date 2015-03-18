package no.gruppe2.shera.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Event;
import no.gruppe2.shera.fragments.NavigationDrawerFragment;
import no.gruppe2.shera.service.SqlLiteDBHandler;

public class MapView extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment navigationDrawerFragment;
    // Stores the last screen title.
    private CharSequence title;

    Session session;
    private GoogleMap map;
    Marker marker;

    private Firebase ref, event;
    private LinkedList<Event> list = new LinkedList<>();
    private ArrayList<Long> arrayList;
    private HashMap<String, Object> hash = new HashMap<>();
    private HashMap<String, Event> markerMap;
    private Calendar cal;
    private Event eo;
    private String userID;
    private int ZOOMLEVEL = 14;

    private SqlLiteDBHandler sqldb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_map);

        setSession();
        findUserID(session);

        sqldb = new SqlLiteDBHandler(this);

        setNewMarkerListener();

        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);

        map.getUiSettings().setZoomControlsEnabled(true);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        markerMap = new HashMap<>();

        ref = new Firebase(getResources().getString(R.string.firebase_root));
        event = ref.child(getResources().getString(R.string.firebase_events));

        arrayList = new ArrayList<>();
        readEventsFromFirebase();
        setInfoWindowListener();

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        centerMapOnMyLocation();
    }

    private void readEventsFromFirebase() {
        event.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                hash = new HashMap<>();
                hash = (HashMap<String, Object>) dataSnapshot.getValue();
                String time = hash.get("calendar").toString();
                cal = new GregorianCalendar();
                cal.setTimeInMillis(Long.parseLong(time));
                if (hash.get("participantsList") == null)
                    arrayList = null;
                else
                    arrayList = (ArrayList<Long>) hash.get("participantsList");

                eo = new Event(hash.get("eventID").toString(),
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
                        arrayList);

                list.add(eo);
                addPin(eo);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void addPin(Event eo) {
        Calendar today = Calendar.getInstance();
        if (today.before(eo.getCalendar())) {
            Marker m = map.addMarker(new MarkerOptions()
                    .position(new LatLng(eo.getLatitude(), eo.getLongitude()))
                    .title(eo.getName())
                    .snippet(eo.getDescription()));
            markerMap.put(m.getId(), eo);
        }

    }

    private void centerMapOnMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
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
        // Update the main content by replacing fragments/activities
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position) {

            case 0: {
                Intent i = new Intent(this, EventCreatorView.class);
                startActivity(i);
                break;
            }
            case 1: {

                Intent intent = new Intent(this, EventsView.class);

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

                intent.putParcelableArrayListExtra(getResources().getString(R.string.intent_parcelable_key), eoList);
                startActivity(intent);
                break;
            }
            case 2: {
                break;
            }
            case 3: {
                session.close();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            /*
            * Only show items in the action bar relevant to this screen
            * if the drawer is not showing. Otherwise, let the drawer
            * decide what to show in the action bar.
            */
            getMenuInflater().inflate(R.menu.menu_map, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    // A fragment containing the map view.
    public static class MapsFragment extends Fragment {
        // The fragment argument representing the section number for this fragment.
        private static final String ARG_SECTION_NUMBER = "section_number";

        // Returns a new instance of this fragment for the given section number.
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
            View rootView = inflater.inflate(R.layout.fragment_map, container, false);
            return rootView;
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

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPrefs = this.getSharedPreferences(
                getResources().getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        sharedPrefs.edit().putString("userID", userID).apply();
    }
}
