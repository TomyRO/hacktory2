package com.hacktory.speeddating;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.speeddating.R;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.hacktory.speeddating.SpeedDatingService.MyBinder;

public class MainActivity extends Activity {
	private GraphUser connectedUser;
	private Button pairButton;
	private SpeedDatingService mSpeedDatingService;
	private boolean mBound = false;
	private String TAG = "MainActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.main_activity);
	    
	    // start Facebook Login
	    Session.openActiveSession(this, true, new Session.StatusCallback() {

	      // callback when session changes state
	      @Override
	      public void call(Session session, SessionState state, Exception exception) {
	        if (session.isOpened()) {

	          // make request to the /me API
	          Request.newMeRequest(session, new Request.GraphUserCallback() {

	            // callback after Graph API response with user object
	            @Override
	            public void onCompleted(GraphUser user, Response response) {
	              if (user != null) {
	                TextView welcome = (TextView) findViewById(R.id.welcome);
	                welcome.setText("Hello " + user.getName() + "!");
	                connectedUser = user;
	              }
	            }
	          }).executeAsync();
	        }
	      }
	    });
	    
	    pairButton = (Button) findViewById(R.id.pair_button);
	    
	    Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.hacktory.SpeedDatingService");
		startService(serviceIntent);

        Intent intent = new Intent(this, SpeedDatingService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	    
	    pairButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, mSpeedDatingService.personalBeacon.toString());
			}
		});
	  }

	  @Override
	  public void onActivityResult(int requestCode, int resultCode, Intent data) {
	      super.onActivityResult(requestCode, resultCode, data);
	      Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	  }
	  
	  @Override
      protected void onStop() {
          super.onStop();
          // Unbind from the service
          if (mBound) {
              unbindService(mConnection);
              mBound = false;
          }
      }
	  
	  @Override
      protected void onStart() {
          super.onStart();
      }
	  
	  private ServiceConnection mConnection = new ServiceConnection() {

          @Override
          public void onServiceConnected(ComponentName className,
                  IBinder service) {
              MyBinder binder = (MyBinder)service;
              mSpeedDatingService = binder.getService();
              mBound = true;
          }

          @Override
          public void onServiceDisconnected(ComponentName arg0) {
              mBound = false;
          }
      };
}
