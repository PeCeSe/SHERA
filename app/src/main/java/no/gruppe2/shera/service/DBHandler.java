package no.gruppe2.shera.service;

import android.content.Context;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Chat;
import no.gruppe2.shera.dto.Event;

/**
 * Created by chris.forberg on 25.02.2015 10:09.
 */
public class DBHandler {

    private Firebase id, chatRef;
    private Context c;

    public DBHandler(Context context) {
        c = context;
    }

    public void pushToDB(Event e, Firebase r) {
        Firebase event = r.child("Events");
        id = event.push();
        String s = id.toString();
        e.setEventID(s);
        id.setValue(e);
    }

    public void pushChatMessageToDB(Chat c, Firebase r){
        Firebase chat = r.child("Chat");
        Firebase chatID = chat.push();
        chatID.setValue(c);
    }

    public void updateEventDB(Event e) {
        id = new Firebase(e.getEventID());
        id.setValue(e);
    }

    public void removeEvent(Event e) {
        id = new Firebase(e.getEventID());
        id.removeValue();
        deleteChat(e);
    }

    public void deleteChat(Event e) {
        Firebase ref = new Firebase(c.getResources().getString(R.string.firebase_root));
        chatRef = ref.child("Chat");
        Query queryRef = chatRef.orderByChild("eventID").equalTo(e.getEventID());

        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    id = chatRef.child(dataSnapshot.getKey());
                    id.removeValue();
                }
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
}