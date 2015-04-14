package no.gruppe2.shera.view;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.LinkedList;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Chat;
import no.gruppe2.shera.dto.Event;
import no.gruppe2.shera.helpers.HelpMethods;
import no.gruppe2.shera.service.DBHandler;

/**
 * Created by chris.forberg on 08.04.2015.
 */
public class ChatView extends ActionBarActivity {
    private TextView view;
    private EditText input;
    private Button send;
    private Event eo;
    private ActionBar actionBar;
    private Intent i;
    private DBHandler db;
    private Chat message;
    private Calendar cal;
    private long userID;
    private Session session;
    private String userName;
    private Firebase ref, chatRef;
    private Query queryRef;
    private HashMap<String, Object> map;
    private LinkedList<Chat> chatList;
    private HelpMethods help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);
        Firebase.setAndroidContext(this);
        i = getIntent();
        eo = i.getParcelableExtra(getResources().getString(R.string.intent_parcelable_key));
        db = new DBHandler();
        ref = new Firebase(getResources().getString(R.string.firebase_root));
        chatRef = ref.child("Chat");
        queryRef = chatRef.orderByChild("eventID").equalTo(eo.getEventID());
        readChatMessage();
        session = Session.getActiveSession();
        findUserFirstName(session);
        findUserID(session);

        map = new HashMap<>();
        chatList = new LinkedList<>();
        help = new HelpMethods();
        view = (TextView) findViewById(R.id.chat_view);
        input = (EditText) findViewById(R.id.chat_input);
        send = (Button) findViewById(R.id.chat_send_button);

        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                cal = new GregorianCalendar().getInstance();
                session = Session.getActiveSession();
                if (!input.getText().equals("")) {
                    message = new Chat(cal, userID,
                            userName, input.getText().toString(),
                            eo.getEventID());
                    db.pushChatMessageToDB(message, ref);
                } else {
                    //ERROR MESSAGE
                }

            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
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
                chatList.add(message);
                view.append(help.leadingZeroesDate(message.getDateTime()) + " " +
                        help.leadingZeroesTime(message.getDateTime()) + " " +
                        message.getUserName() + "\n" +
                        message.getMessage() + "\n");
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
