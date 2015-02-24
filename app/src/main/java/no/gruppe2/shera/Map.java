package no.gruppe2.shera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.facebook.Session;

public class Map extends ActionBarActivity {

    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setSession();
    }

    public void setSession(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Session.setActiveSession((Session) extras.getSerializable("fb_session"));
        }
        session = Session.getActiveSession();
        if(!session.isOpened()){
            finish();
        }
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
        }
        else if(id == R.id.new_event_option){
            Intent i = new Intent(this, EventCreator.class);
            startActivity(i);
        }
        else if(id == R.id.events_option){
            Intent i = new Intent(this, Events.class);
            startActivity(i);
        }
        else if(id == R.id.event_option){
            Intent i = new Intent(this, Event.class);
            startActivity(i);
        }
        else if(id == R.id.logout_option){
            session.close();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
