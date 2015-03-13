package no.gruppe2.shera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

public class Map extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;
    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;

    Session session;
    private GoogleMap map;
    Marker marker;

    private Firebase ref, event;
    private DBHandler db = new DBHandler();
    private LinkedList<EventObject> list = new LinkedList<>();
    private ArrayList<Long> arrayList;
    private HashMap<String, Object> hash = new HashMap<>();
    private HashMap<String, EventObject> markerMap;
    private Calendar cal;
    public long numChildren;
    private EventObject eo;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_map);

        setSession();
        findUserID(session);

        setNewMarkerListener();

        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);

        map.getUiSettings().setZoomControlsEnabled(true);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        markerMap = new HashMap<>();

        ref = new Firebase("https://shera.firebaseio.com/");
        event = ref.child("Events");

        arrayList = new ArrayList<>();

        event.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                hash = new HashMap<>();
                hash = (HashMap<String, Object>) dataSnapshot.getValue();
                String time = hash.get("calendar").toString();
                cal = new GregorianCalendar();
                cal.setTimeInMillis(Long.parseLong(time));
                //Log.d("LIST::",""+hash.get("participantsList").toString());
                if (hash.get("participantsList") == null)
                    arrayList = null;
                else
                    arrayList = (ArrayList<Long>) hash.get("participantsList");

                eo = new EventObject(hash.get("eventID").toString(),
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

                Log.d("LIST::", eo.getParticipantsList() + "");

                Marker m = map.addMarker(new MarkerOptions()
                        .position(new LatLng(eo.getLatitude(), eo.getLongitude()))
                        .title(eo.getName())
                        .snippet(eo.getDescription()));
                markerMap.put(m.getId(), eo);


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

        setInfoWindowListener();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
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
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position) {

            case 0: {
                Intent i = new Intent(this, EventCreator.class);
                startActivity(i);
                break;
            }
            case 1: {
                Intent i = new Intent(this, Events.class);
                startActivity(i);
                break;
            }
            case 2: {
                //  Intent i = new Intent(this, Settings.class);
                // startActivity(i);
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
                mTitle = getString(R.string.create_event_string);
                break;
            case 1:
                mTitle = getString(R.string.title_activity_events);
                break;
            case 2:
                mTitle = getString(R.string.action_settings);
                break;
            case 3:
                mTitle = getString(R.string.logout);
                break;
            case 4:
                mTitle = getString(R.string.title_activity_map);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_map, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
/*       int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.logout_option){
            session.close();
            finish();
        }*/
        return super.onOptionsItemSelected(item);
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

    // A placeholder fragment containing a map view.
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
            ((Map) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void setInfoWindowListener() {
        map.setOnInfoWindowClickListener(
                new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        if (markerMap.containsKey(marker.getId())) {
                            EventObject eventObject = markerMap.get(marker.getId());
                            Intent i = new Intent(getBaseContext(), Event.class);
                            i.putExtra("EventObject", eventObject);
                            i.putExtra("userID", userID);
                            startActivity(i);
                        } else {
                            marker.remove();
                            Intent i = new Intent(getBaseContext(), EventCreator.class);
                            i.putExtra("Lat", marker.getPosition().latitude);
                            i.putExtra("Long", marker.getPosition().longitude);
                            startActivity(i);
                        }
                    }
                }
        );
    }
}
