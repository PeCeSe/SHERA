package no.gruppe2.shera;

import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by chris.forberg on 25.02.2015.
 */

public class DBHandler {

    private Firebase ref, event;
    private Gson gson = new Gson();
    public LinkedList<EventObject> list = new LinkedList<>();
    private EventObject eo;
    private HashMap<String, Object> map;

    public void pushToDB(Firebase r) {
        ref = r;
        event = ref.child("Events");
        Calendar cal = new GregorianCalendar();

        EventObject eo = new EventObject("123", "JALL", "JÅSS",
                "JÅSSEVEIEN1", 123,
                1, cal, true);
        Firebase id = event.push();
        //Log.d("ID", "" + id);
        id.setValue(eo);
    }

    public EventObject convertToObject(String obj) {
        return gson.fromJson(obj, EventObject.class);
    }

    public String convertToJson(EventObject eo) {
        return gson.toJson(eo);
    }

    public void getFromDB(Firebase r) {
        ref = r;
        event = ref.child("Events");
        event.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                map = new HashMap<>();
                map = (HashMap<String, Object>) dataSnapshot.getValue();
                String time = map.get("calendar").toString();
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(Long.parseLong(time));

                eo = new EventObject(map.get("userID").toString(),
                        map.get("name").toString(),
                        map.get("description").toString(),
                        map.get("address").toString(),
                        Integer.parseInt(map.get("maxParticipants").toString()),
                        Integer.parseInt(map.get("numParticipants").toString()),
                        Integer.parseInt(map.get("category").toString()),
                        cal,
                        Boolean.parseBoolean(map.get("adult").toString()));
                list.add(eo);
                Log.d("ID:", eo.getUserID() + "");
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