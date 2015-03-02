package no.gruppe2.shera;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.gson.Gson;

import java.util.LinkedList;

/**
 * Created by chris.forberg on 25.02.2015.
 */

public class DBHandler{

    private Firebase ref, event;
    private Gson gson=new Gson();
    public LinkedList<EventObject> list = new LinkedList<>();

    public void pushToDB(EventObject eventInn, Firebase r){
        ref=r;
        event=ref.child("Events");
        event.push().setValue(convertToJson(eventInn));
    }
    public EventObject convertToObject(String obj){
        return gson.fromJson(obj,EventObject.class);
    }
    public String convertToJson(EventObject eo){
        return gson.toJson(eo);
    }

    public void getFromDB(Firebase r){
        ref=r;
        event=ref.child("Events");
        event.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String str = (String)dataSnapshot.getValue();
                list.add(convertToObject(str));
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