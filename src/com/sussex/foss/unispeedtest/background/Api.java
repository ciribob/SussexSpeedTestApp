package com.sussex.foss.unispeedtest.background;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.sussex.foss.unispeedtest.MainActivity;
import com.sussex.foss.unispeedtest.storage.DBAdapter;

public class Api {

	Context context;

	public Api(Context context) {
		this.context = context;
	}

	public int checkRunningServices() {
		boolean GSMLoggerRunning = false;
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.sussex.foss.unispeedtest.background.GSMLoggerService"
					.equals(service.service.getClassName())) {
				GSMLoggerRunning = true;
				break;
			}
			
		}

		boolean scheduledAlarm = false;

		// now check and see if our service is scheduled

		Intent service = new Intent(context, LocationAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, service,
				PendingIntent.FLAG_NO_CREATE);
		
		

		// pi is null if no pending alarms
		scheduledAlarm = (pi != null);

		if (scheduledAlarm && GSMLoggerRunning) {
			return 3; //all A OK!
		} else if (GSMLoggerRunning) {
			return 2; //gsm strength working
		} else if (scheduledAlarm) {
			return 1; //speed test working
		}

		return 0;

	}

	public void startBackgroundService(long period) {
		
		
		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent service = new Intent(context, LocationAlarmReceiver.class);

		PendingIntent pi = PendingIntent.getBroadcast(context, 0, service,
				PendingIntent.FLAG_CANCEL_CURRENT);
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + 5000, period, pi);
		


		// start service
		Intent myIntent = new Intent(context.getApplicationContext(),
				GSMLoggerService.class);
		context.startService(myIntent);
	}

	public void stopBackgroundService() {
		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent service = new Intent(context, LocationAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, service, 0);
		mgr.cancel(pi); //cancels the alarm
		pi.cancel(); //cancels the pending intent

		Intent myIntent = new Intent(context.getApplicationContext(),
				GSMLoggerService.class);
		context.stopService(myIntent);

	}

	/*
	 * @return boolean return true if the application can access the internet
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public String dumpDBtoCSV(String nickName) {
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/sussex/speedtest");

		String path = dir.getAbsolutePath();
		// make dirs if they dont exist
		if (!dir.exists()) {
			dir.mkdirs();
		}

		DBAdapter db = new DBAdapter(context);
		db.open();
		Cursor tests = db.getSpeedTestsCursor();

		String fileName = getFileName();

		FileWriter writer;
		try {

			File file = new File(dir, fileName);
			file.createNewFile();

			writer = new FileWriter(file);

			// write headers
			writer.write("Device Nick Name:"+nickName+" \n ");
			writer.write("Timestamp (ms), Lat, Lon, Accuracy, Rx, Tx, Time,Signal Type, Signal Strength, Battery,Request Size\n ");

			if (tests.moveToFirst()) {
				// using string builder means we dont have to keep converting
				StringBuilder line = null;
				do {

					line = new StringBuilder();
					int count = 0;
					line.append(tests.getString(count++)); // timestamp has to
															// be a string and is in milliseconds NOT SECONDS
					line.append(",");
					line.append(tests.getDouble(count++));
					line.append(",");
					line.append(tests.getDouble(count++));
					line.append(",");
					line.append(tests.getInt(count++));
					line.append(",");
					line.append(tests.getInt(count++));
					line.append(",");
					line.append(tests.getInt(count++));
					line.append(",");
					line.append(tests.getInt(count++));
					line.append(",");
					line.append(tests.getString(count++));
					line.append(",");
					line.append(tests.getInt(count++));
					line.append(",");
					line.append(tests.getInt(count++));
					line.append(",");
					line.append(tests.getInt(count++));

					line.append("\n");
					writer.write(line.toString());

				} while (tests.moveToNext());

			}

			writer.flush();
			writer.close();

		} catch (IOException e) {

			Log.e(MainActivity.LOG_NAME,
					"Failed to write file: " + e.getMessage());
			return "";
		}

		tests.close();
		db.close();

		return fileName;

	}

	public String getFileName() {
		Date date = new Date(System.currentTimeMillis());

		StringBuilder builder = new StringBuilder("speedtest");

		Calendar c = Calendar.getInstance();
		int seconds = c.get(Calendar.SECOND);

		builder.append("-");
		builder.append(c.get(Calendar.HOUR_OF_DAY));
		builder.append(".");
		builder.append(c.get(Calendar.MINUTE));
		builder.append(".");
		builder.append(c.get(Calendar.SECOND));
		builder.append("-");
		builder.append(c.get(Calendar.DAY_OF_MONTH));
		builder.append("-");
		builder.append(c.get(Calendar.MONTH + 1));
		builder.append("-");
		builder.append(c.get(Calendar.YEAR));
		builder.append(".csv");

		return builder.toString();

	}

}
