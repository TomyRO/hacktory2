package com.hacktory.speeddating;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.example.speeddating.R;

@SuppressLint("NewApi") public class SpeedDatingService extends Service {
	private static final int NOTIFICATION_ID = 123;

	private BeaconManager beaconManager;
	private NotificationManager notificationManager;
	private LocationManager locationManager;
	private Region region;
	
	private String TAG = "SpeedDatingService";

	private String locationProvider;

	private Utils.Proximity lastProximity = Utils.Proximity.UNKNOWN;
	private Location lastLocation = null;
	
	Beacon personalBeacon;
	private final IBinder mBinder = new MyBinder();

	public SpeedDatingService() {

	}

	@Override
	public void onCreate() {
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Log.d(TAG, "Ajunge aici");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		region = new Region("myRegion", null, 1977, null);
		beaconManager = new BeaconManager(this);
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
			@Override
			public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
				// Note that results are not delivered on UI thread.
				// Note that beacons reported here are already sorted by estimated
				// distance between device and beacon.
				Utils.Proximity currentProximity = Utils.Proximity.UNKNOWN;
				Beacon myBeacon = null;
				if (beacons.size() > 0) {
					myBeacon = beacons.get(0);
					currentProximity = Utils.computeProximity(myBeacon);
				}
				if (currentProximity == Utils.Proximity.IMMEDIATE) {
					personalBeacon = myBeacon;
				}
			}  // onBeaconsDiscovered
		});  // setRangingListener

		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override public void onServiceReady() {
				try {
					beaconManager.startRanging(region);
				} catch (RemoteException e) {
					Log.e(TAG, "Cannot start ranging", e);
				}
			}
		});    

		locationProvider = locationManager.getBestProvider(new Criteria(), true);
		locationManager.requestLocationUpdates(locationProvider, 10000, 1,
				new LocationListener() {
			@Override
			public  void onLocationChanged(Location location) {
				lastLocation = location;
				postNotification("new location :" + location.toString());
			}
			@Override
			public void onProviderDisabled(String provider) {}
			@Override
			public void onProviderEnabled(String provider) {}
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
		});


		postNotification("onStartCommand");
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		notificationManager.cancel(NOTIFICATION_ID);
		beaconManager.disconnect();
		// Tell the user we stopped.
		Toast.makeText(this, "car service destroyed", Toast.LENGTH_SHORT).show();
	}

	private void postNotification(String msg) {
		Intent notifyIntent = new Intent(SpeedDatingService.this, SpeedDatingService.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivities(
				SpeedDatingService.this,
				0,
				new Intent[]{notifyIntent},
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification.Builder(SpeedDatingService.this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("Notify Car Context")
		.setContentText(msg)
		.setAutoCancel(true)
		.setContentIntent(pendingIntent)
		.build();
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notificationManager.notify(NOTIFICATION_ID, notification);

		Toast t = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		t.show();  
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	public class MyBinder extends Binder {
	    SpeedDatingService getService() {
	      return SpeedDatingService.this;
	    }
	  }
}
