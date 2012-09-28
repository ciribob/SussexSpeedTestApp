package com.sussex.foss.unispeedtest.background;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * 
 * This class allows the programmer to ask for a location and know that a location 
 * will be returned before the timeout limit or null. Using a semaphore in the callback function
 * allows the main thread to wait for a result
 * 
 * Android has no default way to do this.
 *
 * 
 * Based on the Idea from Here: 
 * http://stackoverflow.com/questions/3145089/what-is-the-simplest-and-most-robust-way-to-get-the-users-current-location-in-an/3145655#3145655
 * 
 * @author ciaranfisher
 *
 */
public class MyLocation
{

	private Timer timeoutTimer; // used to stop the location listeners if no location fix is obtained
	private LocationManager lm;
	private LocationResult locationResult;
	private boolean gpsEnabled = false;
	private boolean networkEnabled = false;
	private  long timeout = 30000; //30 seconds
	
	public MyLocation(long timeout) {
		// TODO Auto-generated constructor stub
		this.timeout = timeout;
	}
	
	//the  GPS location listener
	LocationListener gpsLocationListener = new LocationListener() {
		
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		public void onLocationChanged(Location location) {
			timeoutTimer.cancel();
			locationResult.gotLocation(location);
			stopListeners();
			
		}
	};
	//the network listener
	LocationListener networkLocationListener = new LocationListener()
	{

		public void onLocationChanged(Location location)
		{
			timeoutTimer.cancel();
			locationResult.gotLocation(location);
			stopListeners();
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	};

	/**
	 * Gets a users location, starts timeout timer
	 * 
	 * @param context
	 * @param result
	 * @return
	 */
	public boolean getLocation(Context context, LocationResult result)
	{
		//location result callback class
		locationResult = result;
		if (lm == null)
		{
			lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		}

		// exceptions will be thrown if provider is not permitted.
		try
		{
			gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			
		}
		catch (Exception ex)
		{}
		try
		{
			networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}
		catch (Exception ex)
		{}

		// don't start listeners if no provider is enabled
		if (!gpsEnabled && !networkEnabled)
		{
			return false;
		}

		//start listeners
		if (gpsEnabled)
		{
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
		}
		if (networkEnabled)
		{
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
					networkLocationListener);
		}
		timeoutTimer = new Timer();
		timeoutTimer.schedule(new LocationTimeout(), timeout);
		return true; //location method has started!
	}

	/**
	 * Stop the location listeners
	 */
	public void stopListeners()
	{
		lm.removeUpdates(gpsLocationListener);
		lm.removeUpdates(networkLocationListener);
	}
	
	/**
	 * This inner class is the Timer Task that runs when the timeout fires.
	 * 
	 * It checks the last known locations of both the GPS and Network locations and 
	 * uses the newest location.
	 * 
	 * If there are no locations then NULL is returned to the callback class
	 *  and no locations could be found
	 * 
	 * @author ciaranfisher
	 *
	 */
	class LocationTimeout extends TimerTask
	{

		@Override
		public void run()
		{
			//stop listeners as they failed
			//try to return last known good position
			stopListeners();

			Location networkLocation= null;
			Location gpsLocation = null;
			if (gpsEnabled)
			{
				gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			if (networkEnabled)
			{
				networkLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}

			//compare location values
			// if there are both values use the latest one
			if (gpsLocation != null && networkLocation != null)
			{
				if (gpsLocation.getTime() > networkLocation.getTime())
				{
					locationResult.gotLocation(gpsLocation);
				}
				else
				{
					locationResult.gotLocation(networkLocation);
				}
				return;
			}

			if (gpsLocation != null)
			{
				locationResult.gotLocation(gpsLocation);
				return;
			}
			if (networkLocation != null)
			{
				locationResult.gotLocation(networkLocation);
				return;
			}
			locationResult.gotLocation(null);
		}
	}

	/**
	 * 
	 * call back class which is called when a location has been obtained or the 
	 * timer has expired
	 * best way to do it as the gotLocation method needs to access the semaphore and
	 *  battery level listener which are all in the location service class
	 * 
	 */
	public static abstract class LocationResult
	{
		public abstract void gotLocation(Location location);
	}
}
