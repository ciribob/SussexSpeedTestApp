package com.sussex.foss.unispeedtest.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class receives the LocationAlarm broadcast sent by the Alarm Service
 * and starts the location background fix service
 * @author ciaranfisher
 *
 */
public class LocationAlarmReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		//get the static lock! stop the CPU sleeping which can happen before you even start the service :/
		WakeService.acquireStaticLock(context);
		context.startService(new Intent(context, BackgroundTask.class));
		
	}
}
