package no.gruppe2.shera.service;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;

import no.gruppe2.shera.dto.Event;

/**
 * Created by chris.forberg on 25.02.2015.
 */

public class DBHandler {

    private Firebase ref, event, id;
    public LinkedList<Event> list = new LinkedList<>();
    private Event eo;
    private HashMap<String, Object> map;
    private Calendar cal;
    public long numChildren;

    public void pushToDB(Event e, Firebase r) {
        ref = r;
        event = ref.child("Events");
        eo = e;
        id = event.push();
        String s = id.toString();
        eo.setEventID(s);
        id.setValue(eo);
    }

    public void updateEventDB(Event e) {
        eo = e;
        id = new Firebase(eo.getEventID());
        id.setValue(eo);
    }

    public long getNumChildren(Firebase r) {
        ref = r;
        event = ref.child("Events");
        event.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                numChildren = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        return numChildren;
    }

    public LinkedList<Event> getFromDB(Firebase r) {
        ref = r;
        event = ref.child("Events");
        event.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                map = new HashMap<>();
                map = (HashMap<String, Object>) dataSnapshot.getValue();
                String time = map.get("calendar").toString();
                cal = new GregorianCalendar();
                cal.setTimeInMillis(Long.parseLong(time));
                ArrayList<Long> listetest = new ArrayList<Long>();

                eo = new Event(map.get("eventID").toString(),
                        Long.parseLong(map.get("userID").toString()),
                        map.get("name").toString(),
                        map.get("description").toString(),
                        map.get("address").toString(),
                        Double.parseDouble(map.get("latitude").toString()),
                        Double.parseDouble(map.get("longitude").toString()),
                        Integer.parseInt(map.get("maxParticipants").toString()),
                        Integer.parseInt(map.get("numParticipants").toString()),
                        Integer.parseInt(map.get("category").toString()),
                        cal,
                        Boolean.parseBoolean(map.get("adult").toString()),
                        listetest);
                list.add(eo);
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
        return list;
    }
}