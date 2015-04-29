package no.gruppe2.shera.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.firebase.client.Firebase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Event;
import no.gruppe2.shera.helpers.HelpMethods;
import no.gruppe2.shera.helpers.ImageAdapter;
import no.gruppe2.shera.service.DBHandler;
import no.gruppe2.shera.service.SqlLiteDBHandler;

/*
This class is meant to show the event that is clicked. The class receives an Intent which contains
an event-object. The class uses this object to show the info about the event to the user.
It also retrieve the UserID from sharedPreferences that is sat there in the MapView-class where it
is retrieved from the Facebook-session. The UserID is used to determine the relation to the Event;
if it was created by the user, and therefor sets it editable and deletable, if the user is
participating you get access to the chat of the event and the opportunity to leave the event
or if the user is eligible to join the event.
 */
public class EventView extends ActionBarActivity {
    TextView titleView, descriptionView, participantsView, dateView, timeView, addressView;
    MenuItem editButton, joinButton, unjoinButton, deleteButton, chatButton;
    private Event eo;
    private String userID;
    private SqlLiteDBHandler sqldb;
    private ImageView eventImageView;

    private DBHandler db;
    private HelpMethods help;

    private ArrayList<String> myFriendsListName;
    private ArrayList<String> myFriendsPhotosToShow, namesToShow;
    private ArrayList<Bitmap> myPhotoList;
    private ArrayList<Long> myFriendsListID;
    private ArrayList participantsFromObject;
    private GraphObject graphObject;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        SharedPreferences prefs = this.getSharedPreferences(
                getResources().getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        help = new HelpMethods();
        userID = prefs.getString("userID", null);

        sqldb = new SqlLiteDBHandler(this);
        db = new DBHandler(this);
        Firebase.setAndroidContext(this);

        Intent i = getIntent();
        eo = i.getParcelableExtra(getResources().getString(R.string.intent_parcelable_key));

        eventImageView = (ImageView) findViewById(R.id.event_picture);
        titleView = (TextView) findViewById(R.id.titleView);
        descriptionView = (TextView) findViewById(R.id.descriptionView);
        participantsView = (TextView) findViewById(R.id.participantsView);
        dateView = (TextView) findViewById(R.id.dateView);
        timeView = (TextView) findViewById(R.id.timeView);
        addressView = (TextView) findViewById(R.id.addressView);
        gridView = (GridView) findViewById(R.id.profile_photos);

        setFields(eo);

        myFriendsListID = new ArrayList<>();
        myFriendsListName = new ArrayList<>();
        myFriendsPhotosToShow = new ArrayList<>();
        myPhotoList = new ArrayList<>();
        namesToShow = new ArrayList<>();
        participantsFromObject = eo.getParticipantsList();
        Session session = Session.getActiveSession();
        findFriendsList(session);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event, menu);
        editButton = menu.findItem(R.id.change_button);
        joinButton = menu.findItem(R.id.join_button);
        unjoinButton = menu.findItem(R.id.unjoin_button);
        deleteButton = menu.findItem(R.id.delete_button);
        chatButton = menu.findItem(R.id.chat_button);
        boolean found = false;
        if (userID != null) {
            if (userID.equals(eo.getUserID() + "")) {
                if (!editButton.isVisible()) {
                    editButton.setVisible(true);
                    deleteButton.setVisible(true);
                    chatButton.setVisible(true);
                }
            } else {
                List<String> events = sqldb.getJoinedEvents();
                for (String s : events) {
                    if (s.equals(eo.getEventID())) {
                        unjoinButton.setVisible(true);
                        chatButton.setVisible(true);
                        found = true;
                    }
                }
                if (!found) {
                    joinButton.setVisible(true);
                }
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.change_button) {
            Intent i = new Intent(this, EventCreatorView.class);
            i.putExtra(getResources().getString(R.string.intent_parcelable_key), eo);
            startActivityForResult(i, 1);
            return true;
        }
        if (id == R.id.chat_button) {
            Intent i = new Intent(this, ChatView.class);
            i.putExtra(getResources().getString(R.string.intent_parcelable_key), eo);
            startActivity(i);
            return true;
        }
        if (id == R.id.join_button) {
            joinEvent();
            chatButton.setVisible(true);
            return true;
        }
        if (id == R.id.unjoin_button) {
            unjoinEvent();
            chatButton.setVisible(false);
            return true;
        }
        if (id == R.id.delete_button) {
            DialogInterface.OnClickListener dialogClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //no is clicked
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //yes is clicked
                                    db.removeEvent(eo);
                                    sqldb.deleteEventID(eo.getEventID());
                                    finish();
                                    break;
                            }
                        }
                    };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.are_you_sure))
                    .setPositiveButton(getResources().getString(R.string.no), dialogClickListener)
                    .setNegativeButton(getResources().getString(R.string.yes), dialogClickListener).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            eo = data.getParcelableExtra("Event");
            setFields(eo);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setFields(Event eo) {
        if (!eo.getPhotoSource().equals("NOTSET"))
            new DownloadImages(eo.getPhotoSource()).execute();
        titleView.setText(eo.getName());
        descriptionView.setText(eo.getDescription());
        participantsView.setText(eo.getNumParticipants() + "/" + eo.getMaxParticipants());
        dateView.setText(help.leadingZeroesDate(eo.getCalendar()));
        timeView.setText(help.leadingZeroesTime(eo.getCalendar()));
        addressView.setText(eo.getAddress());
    }

    public void setGridView() {
        gridView.setAdapter(new ImageAdapter(this, myPhotoList, false));
        gridView.setNumColumns(4);
        gridView.setPadding(3, 3, 3, 3);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                help.createToast(namesToShow.get(position), getBaseContext());
            }
        });

    }

    private void findFriendsList(Session session) {
        new Request(session, "/me/friends", null, HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        FacebookRequestError error = response.getError();
                        if (error != null) {
                            Log.e("ERROR::", error.toString());
                        } else {
                            graphObject = response.getGraphObject();
                        }
                        JSONArray dataArray = (JSONArray) graphObject.getProperty("data");

                        if (dataArray.length() > 0) {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject json = dataArray.optJSONObject(i);
                                long ID = findFriendsID(json);
                                String name = findFriendsName(json);
                                myFriendsListName.add(name);
                                myFriendsListID.add(ID);
                            }
                        }
                    }
                }
        ).executeAsync();

        new Request(session, "/me/taggable_friends", null, HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        FacebookRequestError error = response.getError();
                        if (error != null) {
                            Log.e("ERROR::", error.toString());
                        } else {
                            graphObject = response.getGraphObject();
                        }
                        JSONArray dataArray = (JSONArray) graphObject.getProperty("data");

                        if (dataArray.length() > 0) {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject json = dataArray.optJSONObject(i);
                                String name = findFriendsName(json);

                                for (int k = 0; k < myFriendsListName.size(); k++) {
                                    if (name.equals(myFriendsListName.get(k)) &&
                                            (participantsFromObject.contains(myFriendsListID.get(k)))) {
                                        try {
                                            JSONObject obj = (JSONObject) json.get("picture");
                                            JSONObject arr = (JSONObject) obj.get("data");
                                            String s = arr.getString("url");
                                            myFriendsPhotosToShow.add(s);
                                            namesToShow.add(name);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else if (name.equals(myFriendsListName.get(k)) && eo.getUserID() == myFriendsListID.get(k)) {
                                        try {
                                            JSONObject obj = (JSONObject) json.get("picture");
                                            JSONObject arr = (JSONObject) obj.get("data");
                                            String s = arr.getString("url");
                                            if (myFriendsPhotosToShow.size() == 0) {
                                                myFriendsPhotosToShow.add(s);
                                                namesToShow.add("HOST: " + name);
                                            } else {
                                                String tempURL = myFriendsPhotosToShow.get(0);
                                                String tempNAME = namesToShow.get(0);
                                                myFriendsPhotosToShow.set(0, s);
                                                namesToShow.set(0, "HOST: " + name);
                                                myFriendsPhotosToShow.add(tempURL);
                                                namesToShow.add(tempNAME);
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                        if (!myFriendsPhotosToShow.isEmpty())
                            new DownloadImages(myFriendsPhotosToShow).execute();
                    }
                }
        ).executeAsync();
    }

    private String findFriendsName(JSONObject json) {
        String name = "";
        try {
            name = json.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    private long findFriendsID(JSONObject json) {
        long id = 0;
        try {
            id = Long.parseLong(json.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    private void joinEvent() {
        eo.addParticipantToList(Long.parseLong(userID));
        db.updateEventDB(eo);
        sqldb.eventJoined(eo.getEventID());
        setFields(eo);
        joinButton.setVisible(false);
        unjoinButton.setVisible(true);
    }

    private void unjoinEvent() {
        if (eo.removePartisipantFromList(Long.parseLong(userID))) {
            db.updateEventDB(eo);
        }
        sqldb.deleteEventID(eo.getEventID());
        setFields(eo);
        joinButton.setVisible(true);
        unjoinButton.setVisible(false);

    }

    private class DownloadImages extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        Bitmap view;
        ArrayList strings = new ArrayList<>();
        String source;

        public DownloadImages(List<String> list) {
            strings = (ArrayList) list;
        }

        public DownloadImages(String src) {
            source = src;
        }

        @Override
        protected void onPreExecute() {
        }

        protected Bitmap doInBackground(String... params) {
            Bitmap icon = null;
            if (!strings.isEmpty()) {
                bmImage = new ImageView(getBaseContext());
                for (int i = 0; i < strings.size(); i++) {
                    String url = (String) strings.get(i);
                    icon = null;
                    try {
                        InputStream in = new java.net.URL(url).openStream();
                        icon = BitmapFactory.decodeStream(in);
                        view = icon;
                        myPhotoList.add(view);
                    } catch (Exception e) {
                        Log.e("Error", e + "");
                        e.printStackTrace();
                    }
                }
            } else {
                String url = source;
                icon = null;
                try {
                    InputStream in = new java.net.URL(url).openStream();
                    Bitmap iconTemp;
                    icon = Bitmap.createScaledBitmap(iconTemp = BitmapFactory.decodeStream(in), iconTemp.getWidth() / 2, iconTemp.getHeight() / 2, true);
                    view = icon;
                } catch (Exception e) {
                    Log.e("Error", e + "");
                    e.printStackTrace();
                }
            }
            return icon;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (!strings.isEmpty()) {
                setGridView();
            } else {
                eventImageView.setImageBitmap(result);
            }
        }
    }
}