package no.gruppe2.shera.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.Toast;

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
import java.util.ListIterator;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Event;
import no.gruppe2.shera.helpers.HelpMethods;
import no.gruppe2.shera.helpers.ImageAdapter;
import no.gruppe2.shera.service.DBHandler;
import no.gruppe2.shera.service.SqlLiteDBHandler;

public class EventView extends ActionBarActivity {
    TextView titleView, descriptionView, participantsView, dateView, timeView, addressView;
    MenuItem editButton, joinButton, unjoinButton, deleteButton, chatButton;
    private Event eo;
    private String userID;
    private SqlLiteDBHandler sqldb;
    private ImageView eventImageView;

    private DBHandler db;
    private Firebase ref;
    private HelpMethods help;

    private ArrayList<String> myFriendsListName;
    private ArrayList<String> myFriendsPhotosToShow, namesToShow;
    private ArrayList<Bitmap> myPhotoList;
    private ArrayList<Long> myFriendsListID, participantsFromObject;
    private GraphObject graphObject;
    private GridView gridView;
    private Session session;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        SharedPreferences prefs = this.getSharedPreferences(
                getResources().getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        help = new HelpMethods();
        userID = prefs.getString("userID", null);

        sqldb = new SqlLiteDBHandler(this);

        db = new DBHandler();
        Firebase.setAndroidContext(this);
        ref = new Firebase(getResources().getString(R.string.firebase_root));

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

        if (!eo.getPhotoSource().equals("NOTSET"))
            new DownloadImages(eo.getPhotoSource()).execute();
        titleView.setText(eo.getName());
        descriptionView.setText(eo.getDescription());
        participantsView.setText(eo.getNumParticipants() + "/" + eo.getMaxParticipants());
        dateView.setText(help.leadingZeroesDate(eo.getCalendar()));
        timeView.setText(help.leadingZeroesTime(eo.getCalendar()));
        addressView.setText(eo.getAddress());

        myFriendsListID = new ArrayList<>();
        myFriendsListName = new ArrayList<>();
        myFriendsPhotosToShow = new ArrayList<>();
        myPhotoList = new ArrayList<>();
        namesToShow = new ArrayList<>();
        participantsFromObject = eo.getParticipantsList();
        session = Session.getActiveSession();
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
                ListIterator<String> iterator = events.listIterator();
                while (iterator.hasNext()) {
                    String s = iterator.next();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_button) {
            Intent i = new Intent(this, EventCreatorView.class);
            i.putExtra(getResources().getString(R.string.intent_parcelable_key), eo);
            startActivity(i);
            return true;
        }
        if(id == R.id.chat_button){
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

    public void setGridView() {
        gridView.setAdapter(new ImageAdapter(this, myPhotoList));
        gridView.setNumColumns(4);
        gridView.setPadding(3, 3, 3, 3);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showToast(namesToShow.get(position));
            }
        });

    }

    private void findFriendsList(Session session) {
        new Request(session, "/me/friends", null, HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        FacebookRequestError error = response.getError();
                        if (error != null && response != null) {
                            Log.e("ERROR::", error.toString());
                        } else {
                            graphObject = response.getGraphObject();
                        }
                        JSONArray dataArray = (JSONArray) graphObject.getProperty("data");

                        if (dataArray.length() > 0) {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject json = dataArray.optJSONObject(i);
                                myFriendsListName.add(findFriendsName(json));
                                myFriendsListID.add(findFriendsID(json));
                            }
                        }
                    }
                }
        ).executeAsync();

        //myFriendsList = new ArrayList<>();
        new Request(session, "/me/taggable_friends", null, HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        FacebookRequestError error = response.getError();
                        if (error != null && response != null) {
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
                                    if (name.equals(myFriendsListName.get(k))) {
                                        if (participantsFromObject.contains(myFriendsListID.get(k))) {
                                            try {
                                                JSONObject obj = (JSONObject) json.get("picture");
                                                JSONObject arr = (JSONObject) obj.get("data");
                                                String s = arr.getString("url");
                                                myFriendsPhotosToShow.add(s);
                                                namesToShow.add(name);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
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

        joinButton.setVisible(false);
        unjoinButton.setVisible(true);
    }

    private void unjoinEvent() {
        if (eo.removePartisipantFromList(Long.parseLong(userID))) {
            db.updateEventDB(eo);
        }

        sqldb.deleteEventID(eo.getEventID());

        joinButton.setVisible(true);
        unjoinButton.setVisible(false);

    }

    private void showToast(String s) {
        Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
    }

    private class DownloadImages extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        Bitmap view;
        ArrayList<String> strings = new ArrayList<>();
        String source;

        public DownloadImages(List<String> list) {
            strings = (ArrayList) list;
        }

        public DownloadImages(String src) {
            source = src;
        }

        @Override
        protected void onPreExecute() {
            if (!strings.isEmpty()) {
                progress = new ProgressDialog(EventView.this);
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setTitle(getResources().getString(R.string.loading));
                progress.setMessage(getResources().getString(R.string.download_friends_profile_pictures_facebook));
                progress.setCancelable(false);
                progress.setIndeterminate(false);
                progress.setMax(strings.size());
                progress.setProgress(0);
                progress.show();
            }
        }

        protected Bitmap doInBackground(String... params) {
            Bitmap icon = null;
            if (!strings.isEmpty()) {
                bmImage = new ImageView(getBaseContext());
                for (int i = 0; i < strings.size(); i++) {
                    String url = strings.get(i);
                    icon = null;
                    try {
                        InputStream in = new java.net.URL(url).openStream();
                        icon = BitmapFactory.decodeStream(in);
                        view = icon;
                        myPhotoList.add(view);
                        onProgressUpdate(i);
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
                    //icon = BitmapFactory.decodeStream(in);
                    Bitmap iconTemp;
                    icon = Bitmap.createScaledBitmap(iconTemp = BitmapFactory.decodeStream(in), iconTemp.getWidth() / 2, iconTemp.getHeight() / 2, true);
                    view = icon;
                    myPhotoList.add(view);
                } catch (Exception e) {
                    Log.e("Error", e + "");
                    e.printStackTrace();
                }
            }
            return icon;
        }

        protected void onProgressUpdate(Integer... values) {
            progress.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (!strings.isEmpty()) {
                setGridView();
                progress.dismiss();
            } else {
                eventImageView.setImageBitmap(result);
                //eventImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            //bmImage.setImageBitmap(result);
        }
    }
}