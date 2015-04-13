package no.gruppe2.shera.view;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import no.gruppe2.shera.R;
import no.gruppe2.shera.fragments.EventListFragment;

//This class is going to contain a listfragment

public class EventsView extends ActionBarActivity {

    private EventListFragment eventListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (findViewById(R.id.fragment_container) != null) {
            eventListFragment = new EventListFragment();
            getFragmentManager().beginTransaction().add(R.id.fragment_container, eventListFragment).commit();
        }
    }
}
