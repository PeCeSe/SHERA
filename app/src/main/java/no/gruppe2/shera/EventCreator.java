package no.gruppe2.shera;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class EventCreator extends ActionBarActivity {

    EditText nameInput;
    EditText descriptionInput;
    EditText addressInput;
    EditText participantsInput;
    TextView timeView;
    TextView dateView;
    Button pickDateIn;
    Button pickTimeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creator);

        nameInput = (EditText) findViewById(R.id.nameInputField);
        descriptionInput = (EditText) findViewById(R.id.descriptionInputField);
        addressInput = (EditText) findViewById(R.id.addressInputField);
        participantsInput = (EditText) findViewById(R.id.participantsInputField);
        pickDateIn = (Button) findViewById(R.id.pickDateButton);
        pickTimeIn = (Button) findViewById(R.id.pickTimeButton);
        timeView = (TextView) findViewById(R.id.timeText);
        dateView = (TextView) findViewById(R.id.dateText);


        pickDateIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {


            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_creator, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
