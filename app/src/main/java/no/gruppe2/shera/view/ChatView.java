package no.gruppe2.shera.view;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Chat;
import no.gruppe2.shera.dto.Event;
import no.gruppe2.shera.helpers.ChatArrayAdapter;
import no.gruppe2.shera.helpers.Validator;
import no.gruppe2.shera.service.DBHandler;

/*
This class retrieves the incoming events chat-objects from the Firebase database, and displays
them in a TextView. It also allows the user to enter new messages, and stores these as chat-objects
in Firebase, before displaying them on the screen.
 */

public class ChatView extends ActionBarActivity {
    private EditText input;
    private Event eo;
    private DBHandler db;
    private Chat message;
    private Calendar cal;
    private long userID;
    private Session session;
    private String userName;
    private Firebase ref;
    private Query queryRef;
    private HashMap<String, Object> map;
    private Validator validator;
    private ListView listView;
    private ChatArrayAdapter chatArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);


        Intent i = getIntent();
        eo = i.getParcelableExtra(getResources().getString(R.string.intent_parcelable_key));

        Firebase.setAndroidContext(this);
        db = new DBHandler(this);
        ref = new Firebase(getResources().getString(R.string.firebase_root));
        Firebase chatRef = ref.child("Chat");
        queryRef = chatRef.orderByChild("eventID").equalTo(eo.getEventID());
        readChatMessage();

        session = Session.getActiveSession();
        findUserFirstName(session);
        findUserID(session);

        map = new HashMap<>();
        validator = new Validator();

        input = (EditText) findViewById(R.id.chat_input);
        Button send = (Button) findViewById(R.id.chat_send_button);
        listView = (ListView) findViewById(R.id.chatListView);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.single_chat_bubble);
        listView.setAdapter(chatArrayAdapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                cal = Calendar.getInstance();
                session = Session.getActiveSession();

                if (!validator.isEmpty(input.getText().toString())) {
                    message = new Chat(cal, userID,
                            userName, input.getText().toString(),
                            eo.getEventID());
                    db.pushChatMessageToDB(message, ref);
                }
                input.setText("");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private boolean sendChatMessage() {
        chatArrayAdapter.add(message, eo.getUserID(), userID);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void readChatMessage() {

        queryRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                map = (HashMap<String, Object>) dataSnapshot.getValue();
                String time = map.get("dateTime").toString();

                cal = new GregorianCalendar();
                cal.setTimeInMillis(Long.parseLong(time));

                message = new Chat(cal, Long.parseLong(map.get("userID").toString()),
                        map.get("userName").toString(),
                        map.get("message").toString(), map.get("eventID").toString());

                sendChatMessage();
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
    }

    private void findUserID(final Session session) {

        if (session != null && session.isOpened()) {

            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {

                    if (session == Session.getActiveSession()) {
                        if (user != null) {
                            userID = Long.parseLong(user.getId());
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);
        }
    }

    private void findUserFirstName(final Session session) {

        if (session != null && session.isOpened()) {

            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {

                    if (session == Session.getActiveSession()) {
                        if (user != null) {
                            userName = user.getFirstName();
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);
        }
    }


}


