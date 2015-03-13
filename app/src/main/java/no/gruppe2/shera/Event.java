package no.gruppe2.shera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.Session;

import java.util.Calendar;

public class Event extends ActionBarActivity {
    TextView titleView, descriptionView, participantsView, dateView, timeView, addressView;
    MenuItem editButton;
    private EventObject eo;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Session session = Session.openActiveSessionFromCache(this);

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
        if (userID != null) {
            if (userID.equals(eo.getUserID() + "")) {
                if (!editButton.isVisible()) {
                    editButton.setVisible(false);
                }
            } else {
                editButton.setVisible(false);
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

        return super.onOptionsItemSelected(item);
    }
}
