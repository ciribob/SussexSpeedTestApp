package com.sussex.foss.unispeedtest.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sussex.foss.unispeedtest.MainActivity;
import com.sussex.foss.unispeedtest.background.MyLocation.LocationResult;
import com.sussex.foss.unispeedtest.storage.SpeedTestResult;

/**
 * This is the class that runs the background location service and sends the
 * location to the web service using the API
 * 
 * @author ciaranfisher
 * 
 */
public class BackgroundTask extends WakeService {

	private Semaphore semaphore; // used to hold the main thread while a
									// location fix is obtained
	private int batteryLevel = -1;
	private int signalStrength = -1;
	private Location location = null;

	// location result callback class called by the MyLocation class
	// releases them semaphore after the location has been sent allowing the
	// main thread to finish
	// and wakelock to be released
	private LocationResult locationResult = new LocationResult() {

		@Override
		public void gotLocation(final Location location) {

			// Got the location!
			if (location != null) {
				Log.d(MainActivity.LOG_NAME, "GOT LOCATION");
			} else {
				Log.d(MainActivity.LOG_NAME, "Failed to get Location");
			}

			// store location in class
			BackgroundTask.this.location = location;

			try {
				// releasing semaphore causing the dowakeful work method to
				// continue
				semaphore.release();
			} catch (InterruptedException e) {
			}
		}
	};

	// contains the broadcast receiver for the battery level and GSM
	private BroadcastReceiver infoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {

			if (!intent.getAction().equals("GSM_SIGNAL_REPLY")) {
				try {
					batteryLevel = intent.getIntExtra("level", -1);
				} catch (Exception ex) {
					Log.e(MainActivity.LOG_NAME, "Exception getting battery");
				}
			}
			else
			{
				Log.d(MainActivity.LOG_NAME, "Received reply for GSM signal Level");
				signalStrength = intent.getIntExtra("signal", -1);
			}
		}
	};

	// phone state listener

	/**
	 * Constructor
	 */
	public BackgroundTask() {
		super("Stadium Background Location Service");
	}

	/**
	 * Attempts to get a users location
	 */
	protected void updateLocation() {
		try {
			Log.d(MainActivity.LOG_NAME, "Took First location Semaphore");
			// take a semaphone stopping the main thread from exiting
			semaphore.take();
		} catch (InterruptedException e) {
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		
		//get user selected timeout time for location
		long timeout = Long.parseLong(prefs.getString("PREF_WAIT_FOR_LOCATION_TIME", "120"));
		// get current location
		MyLocation location = new MyLocation(timeout *1000); //needs to be in milliseconds
		// runs get location
		location.getLocation(this, locationResult);

	}

	/**
	 * Carries out work while holding wake lock
	 */
	@Override
	public void doWakefulWork(Intent intent) {
		Log.d(MainActivity.LOG_NAME, "Starting background service");

		// register battery receiver to get battery level, hopefully we'll
		// receive a message before the main thread ends...
		this.registerReceiver(this.infoReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));

		this.registerReceiver(this.infoReceiver, new IntentFilter(
				"GSM_SIGNAL_REPLY"));
		
		this.sendBroadcast(new Intent("REQUEST_GSM"));

		semaphore = new Semaphore();

		updateLocation();

		try {
			Log.d(MainActivity.LOG_NAME, "Sleeping service until work done");
			semaphore.take(); // as semaphone has already been taken in
								// updateLocation method, this causes
								// this thread to wait and NOT release
								// the wake lock as if it does you'll never
								// get a result for
								// for the location

			Log.d(MainActivity.LOG_NAME, "Service Woken");
			semaphore.release(); // dont need semaphore as work is done
									// so quit and release wake lock
		} catch (InterruptedException ex) {
			Log.e(MainActivity.LOG_NAME, ex.getMessage());
		}

		// run the speed test now we have a location

		SpeedTest speedTest = new SpeedTest(this.getApplicationContext(),
				location, batteryLevel, signalStrength,1);
		speedTest.runSpeedTest();
		
		//see how long that one took and try again
		
		SpeedTestResult result = speedTest.getTestResult();
		
		//if request worked and was less than 5 seconds do a second run
		if(result.getTime() > 0 && result.getTime() < 5000)
		{
			speedTest = new SpeedTest(this.getApplicationContext(),
					location, batteryLevel, signalStrength,10);
			speedTest.runSpeedTest();
		}

		// we are now done working so cleanup listeners

		// get rid of battery listener
		this.unregisterReceiver(infoReceiver);
		
		// when this method quits the wakelock is handed back

	}

}
