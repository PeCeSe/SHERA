package no.gruppe2.shera.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Event;
import no.gruppe2.shera.fragments.EventListFragment;
import no.gruppe2.shera.service.SqlLiteDBHandler;

//This class is going to contain a listfragment

public class EventsView extends ActionBarActivity {

    private EventListFragment eventListFragment;
    private SqlLiteDBHandler sqlLiteDBHandler;
    private static ArrayList<Event> events;
    private String userID;
    private SqlLiteDBHandler sqldb;
    private boolean first = true;
    private boolean chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        sqldb = new SqlLiteDBHandler(this);

        Intent intent = getIntent();

        chat = intent.getBooleanExtra("Chat", false);

        if (chat) {
            setTitle(getResources().getString(R.string.chat));
        }

        events = intent.getParcelableArrayListExtra("Events");

        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getResources().getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        userID = sharedPreferences.getString("userID", null);

        sqlLiteDBHandler = new SqlLiteDBHandler(this);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            eventListFragment = new EventListFragment();
            getFragmentManager().beginTransaction().add(R.id.fragment_container, eventListFragment).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.update_option) {
            updateList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void newList(LinkedList<Event> list) {
        events = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            events.add(list.get(i));
        }
    }

    public void updateList() {
        sqlLiteDBHandler.updateSqlLite(events, userID);

        Intent intent = new Intent(this, EventsView.class);

        ArrayList<Event> eoList = new ArrayList<>();

        ListIterator<Event> itObject = events.listIterator();

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
        ListIterator iterator = MapView.list.listIterator();
        while (iterator.hasNext()) {
            Event e = (Event) iterator.next();
            fullList.add(e);
        }

        intent.putParcelableArrayListExtra(getResources().getString(R.string.intent_parcelable_key), eoList);
        intent.putParcelableArrayListExtra("Events", fullList);
        intent.putExtra("Chat", chat);

        finish();
        startActivity(intent);
        //recreate();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (findViewById(R.id.fragment_container) != null) {
            eventListFragment = new EventListFragment();
            getFragmentManager().beginTransaction().add(R.id.fragment_container, eventListFragment).commit();
        }
        Log.d("RESUME::BEFORE", first + "");
        if (!first)
            updateList();
        first = false;
        Log.d("RESUME::AFTER", first + "");
    }
}
