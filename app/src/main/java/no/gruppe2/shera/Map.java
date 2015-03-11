package no.gruppe2.shera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.Session;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map extends ActionBarActivity {

    Session session;
    private GoogleMap map;
    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setSession();

        setNewMarkerListener();

        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
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

    private void createMarker(double lat, double lng) {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(getResources().getString(R.string.pin_new_event)).draggable(true)
                .snippet(getResources().getString(R.string.pin_creation_info))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

        marker.showInfoWindow();
        map.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Intent i = new Intent(getBaseContext(), EventCreator.class);
                        i.putExtra("Lat", marker.getPosition().latitude);
                        i.putExtra("Long", marker.getPosition().longitude);
                        startActivity(i);
                        return false;
                    }
                }
        );
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
}
