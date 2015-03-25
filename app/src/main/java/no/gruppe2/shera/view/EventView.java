package no.gruppe2.shera.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.List;
import java.util.ListIterator;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Event;
import no.gruppe2.shera.helpers.HelpMethods;
import no.gruppe2.shera.service.DBHandler;
import no.gruppe2.shera.service.SqlLiteDBHandler;

public class EventView extends ActionBarActivity {
    TextView titleView, descriptionView, participantsView, dateView, timeView, addressView;
    MenuItem editButton, joinButton, unjoinButton;
    private Event eo;
    private String userID;
    private SqlLiteDBHandler sqldb;

    private DBHandler db;
    private Firebase ref;
    private HelpMethods help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        SharedPreferences prefs = this.getSharedPreferences(
                getResources().getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        help = new HelpMethods();
        userID = prefs.getString("userID", null);

        sqldb = new SqlLiteDBHandler(this);

        db = new DBHandler();
        Firebase.setAndroidContext(this);
        ref = new Firebase(getResources().getString(R.string.firebase_root));

        Intent i = getIntent();
        eo = i.getParcelableExtra(getResources().getString(R.string.intent_parcelable_key));

        titleView = (TextView) findViewById(R.id.titleView);
        descriptionView = (TextView) findViewById(R.id.descriptionView);
        participantsView = (TextView) findViewById(R.id.participantsView);
        dateView = (TextView) findViewById(R.id.dateView);
        timeView = (TextView) findViewById(R.id.timeView);
        addressView = (TextView) findViewById(R.id.addressView);

        titleView.setText(eo.getName());
        descriptionView.setText(eo.getDescription());
        participantsView.setText(eo.getNumParticipants() + "/" + eo.getMaxParticipants());
        dateView.setText(help.leadingZeroesDate(eo.getCalendar()));
        timeView.setText(help.leadingZeroesTime(eo.getCalendar()));
        addressView.setText(eo.getAddress());
        //Log.d("SOURCE::", eo.getPhotoSource());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event, menu);
        editButton = menu.findItem(R.id.change_button);
        joinButton = menu.findItem(R.id.join_button);
        unjoinButton = menu.findItem(R.id.unjoin_button);
        boolean found = false;
        if (userID != null) {
            if (userID.equals(eo.getUserID() + "")) {
                if (!editButton.isVisible()) {
                    editButton.setVisible(true);
                }
            } else {
                List<String> events = sqldb.getJoinedEvents();
                ListIterator<String> iterator = events.listIterator();
                while (iterator.hasNext()) {
                    String s = iterator.next();
                    if (s.equals(eo.getEventID())) {
                        unjoinButton.setVisible(true);
                        found = true;
                    }
                }
                if (!found) {
                    joinButton.setVisible(true);
                }
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_button) {
            Intent i = new Intent(this, EventCreatorView.class);
            i.putExtra(getResources().getString(R.string.intent_parcelable_key), eo);
            startActivity(i);
            return true;
        }
        if (id == R.id.join_button) {
            joinEvent();
            return true;
        }
        if (id == R.id.unjoin_button) {
            unjoinEvent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void joinEvent() {
        eo.addParticipantToList(Long.parseLong(userID));

        db.updateEventDB(eo);

        sqldb.eventJoined(eo.getEventID());

        joinButton.setVisible(false);
        unjoinButton.setVisible(true);
    }

    private void unjoinEvent() {
        if (eo.removePartisipantFromList(Long.parseLong(userID))) {
            db.updateEventDB(eo);
        }

        sqldb.deleteEventID(eo.getEventID());

        joinButton.setVisible(true);
        unjoinButton.setVisible(false);

    }
}