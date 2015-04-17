package no.gruppe2.shera.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import no.gruppe2.shera.R;


public class LogInView extends FragmentActivity {

    static UiLifecycleHelper uiHelper;
    private GraphObject graphObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_in);
        LoginButton authButton = (LoginButton) findViewById(R.id.fb_login_button);
        authButton.setReadPermissions(Arrays.asList("user_photos","user_friends"));
        
        if(getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
    }

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.d("PERMISSION::", session.getPermissions() + "");
            if (state.isOpened()) {
                logInFb(session);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            logInFb(session);
        }
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        uiHelper.onSaveInstanceState(savedState);
    }

    public void logInFb(Session session) {
        SharedPreferences prefs = getSharedPreferences(getResources()
                .getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        boolean b = prefs.getBoolean(getResources()
                .getString(R.string.shared_preferences_is_over_18), false);

        if (!b) {
            putAgeRangeShared();
        }
        Intent i = new Intent(this, MapView.class);
        i.putExtra("fb_session", session);
        startActivity(i);
    }

    public void putAgeRangeShared() {
         /* make the API call */
        Bundle bundle = new Bundle();
        bundle.putString("fields", "age_range");
        new Request(
                Session.getActiveSession(),
                "me",
                bundle,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        /* handle the result */
                        FacebookRequestError error = response.getError();
                        boolean isOver18 = false;
                        if (error != null && response != null) {
                            Log.e("ERROR::", error.toString());
                        } else {
                            graphObject = response.getGraphObject();
                            int min = 0, max = 0;

                            JSONObject dataObject = (JSONObject) graphObject.getProperty("age_range");
                            if (dataObject.has("min")) {
                                try {
                                    min = Integer.parseInt(dataObject.get("min").toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (dataObject.has("max")) {
                                try {
                                    max = Integer.parseInt(dataObject.get("max").toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (min >= 18)
                                isOver18 = true;

                            SharedPreferences sharedPrefs = getSharedPreferences(
                                    getResources().getString(R.string.shared_preferences_key),
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putBoolean(getResources()
                                    .getString(R.string.shared_preferences_is_over_18), isOver18);
                            editor.commit();
                        }

                    }
                }
        ).executeAsync();
    }
}
