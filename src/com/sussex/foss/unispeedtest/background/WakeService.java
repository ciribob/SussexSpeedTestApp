package com.sussex.foss.unispeedtest.background;



import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * Implements a service we can use to wake the phone up when it is asleep
 * 
 * This class is based off the chapter 13 sample in Advanced Android Development
 * by Mark Murphy.
 * 
 * @author Ciaran
 */
abstract public class WakeService extends IntentService
{
	// implemented by all services that need a wake lock
	abstract void doWakefulWork(Intent intent);

	public static final String STATIC_LOCK_NAME = "com.sussex.foss.unispeedtest.background.Static";
	static PowerManager.WakeLock staticLock = null;

	public WakeService(String name)
	{
		super(name);
	}

	public static void acquireStaticLock(Context context)
	{
		getLock(context).acquire();
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	synchronized private static PowerManager.WakeLock getLock(Context context)
	{
		// check if we already have a wake lock
		if (staticLock == null)
		{
			PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

			// asks the Phone to wake up the CPU but not the screen or any
			// lights as we don't need anything else
			staticLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, STATIC_LOCK_NAME);
			staticLock.setReferenceCounted(true); // allows multiple services to
													// use the same wake lock
													// and when releasing, not
													// release until all
													// services have released
		}

		return (staticLock);
	}

	/**
	 * Catches the intent and calls the doWakeFul work method in the
	 * implementing class automatically acquiring a wake lock and releasing it
	 * when the class has finished its work
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	final protected void onHandleIntent(Intent intent)
	{
		try
		{
			doWakefulWork(intent);
		}
		finally
		{
			// this method won't actually release the lock if other services are
			// still using the wake service
			getLock(this).release();
		}
	}
}
