package com.sussex.foss.unispeedtest.background;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sussex.foss.unispeedtest.MainActivity;

public class GSMLoggerService extends Service {

	private TelephonyManager telephonyManager;
	private PowerManager.WakeLock mWakeLock;

	private int gsmSignalStrength = -1;
	private int gsmErrorRate = -1;
	
	public static boolean RUNNING = false;
	
	
	/**
	 * Receiver for Communication
	 */
	BroadcastReceiver receiver;

	/**
	 * Listens to changes in signal strength
	 */
	private PhoneStateListener mPhoneStateChangeListener;

	@Override
	public void onCreate() {
		super.onCreate();
		
		if(!GSMLoggerService.RUNNING)
		{
			GSMLoggerService.RUNNING = true;
			
			setupListeners();
			
		}
		else
			Log.d(MainActivity.LOG_NAME, "Not STARTING GSM Service - Already running");
	}
	
	public void setupListeners()
	{
		Log.d(MainActivity.LOG_NAME, "Starting GSM Service");
		
		receiver=  new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				
				//we now need to reply with a gsmSignal Level
				
				Log.d(MainActivity.LOG_NAME, "Received Request for GSM Signal Level - replying");
				Intent reply = new Intent("GSM_SIGNAL_REPLY");
				reply.putExtra("signal", gsmSignalStrength);
				GSMLoggerService.this.sendBroadcast(reply);
				
				
			}
		};
		
		mPhoneStateChangeListener  = new PhoneStateListener() {
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);
				gsmSignalStrength = signalStrength.getGsmSignalStrength();
				gsmErrorRate = signalStrength.getGsmBitErrorRate();
				
				Log.d(MainActivity.LOG_NAME, "Received GSM Signal Level");

			};
		};

		telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(mPhoneStateChangeListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		PowerManager mgr = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);

		// asks the Phone to wake up the CPU but not the screen or any
		// lights as we don't need anything else
		mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"com.sussex.GSMLoggerService");
		mWakeLock.setReferenceCounted(true); // allows multiple services to
												// use the same wake lock
												// and when releasing, not
												// release until all
												// services have released
		mWakeLock.acquire();
		
		
		
		this.registerReceiver(receiver, new IntentFilter("REQUEST_GSM"));
		
		
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {

		super.onDestroy();

		Log.d(MainActivity.LOG_NAME, "Destroying service");
		if (this.mWakeLock != null) {
			this.mWakeLock.release();
			this.mWakeLock = null;
		}

		this.unregisterReceiver(receiver);
		
		telephonyManager.listen(mPhoneStateChangeListener,
				PhoneStateListener.LISTEN_NONE);
		
		GSMLoggerService.RUNNING = false;
	}

	protected int getLastSignalStrength() {
		return gsmSignalStrength;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}