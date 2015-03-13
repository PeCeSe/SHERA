package no.gruppe2.shera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.Session;
import com.firebase.client.Firebase;

import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

public class Event extends ActionBarActivity {
    TextView titleView, descriptionView, participantsView, dateView, timeView, addressView;
    MenuItem editButton, joinButton, unjoinButton;
    private EventObject eo;
    private String userID;
    private SqlLiteDBHandler sqldb;

    private DBHandler db;
    private Firebase ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Session session = Session.openActiveSessionFromCache(this);

        sqldb = new SqlLiteDBHandler(this);

        db = new DBHandler();
        Firebase.setAndroidContext(this);
        ref = new Firebase("https://shera.firebaseio.com/");

        Intent i = getIntent();
        eo = i.getParcelableExtra("EventObject");
        userID = i.getStringExtra("userID");

        titleView = (TextView) findViewById(R.id.titleView);
        descriptionView = (TextView) findViewById(R.id.descriptionView);
        participantsView = (TextView) findViewById(R.id.participantsView);
        dateView = (TextView) findViewById(R.id.dateView);
        timeView = (TextView) findViewById(R.id.timeView);
        addressView = (TextView) findViewById(R.id.addressView);

        titleView.setText(eo.getName());
        descriptionView.setText(eo.getDescription());
        participantsView.setText(eo.getNumParticipants() + "/" + eo.getMaxParticipants());
        dateView.setText(String.format("%02d", eo.getCalendar().get(Calendar.DAY_OF_MONTH)) + "-"
                + (String.format("%02d", eo.getCalendar().get(Calendar.MONTH) + 1)) + "-"
                + eo.getCalendar().get(Calendar.YEAR));
        timeView.setText(String.format("%02d", eo.getCalendar().get(Calendar.HOUR_OF_DAY)) + ":"
                + String.format("%02d", eo.getCalendar().get(Calendar.MINUTE)));
        addressView.setText(eo.getAddress());
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
            Intent i = new Intent(this, EventCreator.class);
            i.putExtra("EventObject", eo);
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
