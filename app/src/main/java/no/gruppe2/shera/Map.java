package no.gruppe2.shera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;

public class Map extends ActionBarActivity {

    Session session;
    private GoogleMap map;
    Marker marker;

    private Firebase ref, event;
    private DBHandler db = new DBHandler();
    private LinkedList<EventObject> list = new LinkedList<>();
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
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        markerMap = new HashMap<>();

        ref = new Firebase("https://shera.firebaseio.com/");
        event = ref.child("Events");
        event.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                hash = new HashMap<>();
                hash = (HashMap<String, Object>) dataSnapshot.getValue();
                String time = hash.get("calendar").toString();
                cal = new GregorianCalendar();
                cal.setTimeInMillis(Long.parseLong(time));

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
                        Boolean.parseBoolean(hash.get("adult").toString()));
                list.add(eo);

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

    }

    private void setNewMarkerListener() {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                createMarker(latLng.latitude, latLng.longitude);
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

    private void createMarker(double lat, double lng) {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(getResources().getString(R.string.pin_new_event)).draggable(true)
                .snippet(getResources().getString(R.string.pin_creation_info))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

        marker.showInfoWindow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
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
        } else if (id == R.id.new_event_option) {
            Intent i = new Intent(this, EventCreator.class);
            startActivity(i);
        } else if (id == R.id.events_option) {
            Intent i = new Intent(this, Events.class);
            startActivity(i);
        } else if (id == R.id.event_option) {
            Intent i = new Intent(this, Event.class);
            startActivity(i);
        } else if (id == R.id.logout_option) {
            session.close();
            finish();
        }

        return super.onOptionsItemSelected(item);
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
