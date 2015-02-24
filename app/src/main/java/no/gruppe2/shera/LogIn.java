package no.gruppe2.shera;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LogIn extends FragmentActivity {

    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
    }

    private Session.StatusCallback statusCallback = new Session.StatusCallback(){
        @Override
        public void call(Session session, SessionState state, Exception exception){
            if(state.isOpened()) {
                logInFb(session);
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState){
        super.onSaveInstanceState(savedState);
        uiHelper.onSaveInstanceState(savedState);
    }

    public void logInFb(Session session){
        Intent i = new Intent(this, Map.class);
        i.putExtra("fb_session", session);
        startActivity(i);
    }
}
