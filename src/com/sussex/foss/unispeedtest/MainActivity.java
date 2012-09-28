package com.sussex.foss.unispeedtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sussex.foss.unispeedtest.background.Api;
import com.sussex.foss.unispeedtest.storage.DBAdapter;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener {

	public final static String LOG_NAME = "StadiumApp";
	private Api api;
	SharedPreferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		api = new Api(getApplicationContext());
		
		startSharedPreferencesListener();
		
		

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		checkRunningServices();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		//clean up listener
		preferences.unregisterOnSharedPreferenceChangeListener(this);
		
	}
	
	private void startSharedPreferencesListener()
	{
		// register shared preference listener
		preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		preferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		
		if(!key.equals("PREF_NICKNAME"))
		{
			//either the location time was changed or the test interval was changed
			//restart the services!
			api.startBackgroundService(getSpeedTestIntervals());
			
			Toast.makeText(getApplicationContext(),
					"Experiment Settings Changed - Restarting Services",
					Toast.LENGTH_LONG).show();
			
		}
		
	}
	
	private long getSpeedTestIntervals()
	{
		long interval = Long.parseLong(preferences.getString("PREF_TEST_INTERVAL", "120"));
		
		return interval*1000; //needs to be in milliseconds not seconds
	}
	
	public void openPreferencesPanel(View view)
	{
		startActivity(new Intent(this,
				com.sussex.foss.unispeedtest.SpeedTestPreferences.class));
		
	}

	public void stopBackgroundService(View view) {

		api.stopBackgroundService();
		
		checkRunningServices();
	}

	public void startBackgroundService(View view) {

		api.startBackgroundService(getSpeedTestIntervals());
		
		checkRunningServices();
	}

	public void exportToCSV(View view) {
		// start Async Task
		new Export().execute();

	}
	
	//triggered by pressing on status
	public void checkServiceStatus(View view) {
		checkRunningServices();
	}

	public void checkRunningServices() {
		int result = api.checkRunningServices();
		TextView statusText = (TextView) findViewById(R.id.status);

		switch (result) {
		case 0:
			statusText.setText("Not Running");
			statusText.setTextColor(Color.RED);
			break;
		case 1:
			statusText.setText("Partial - Speed Test ");
			statusText.setTextColor(Color.YELLOW);
			break;
		case 2:
			statusText.setText("Partial - GSM Logger ");
			statusText.setTextColor(Color.YELLOW);
			break;
		case 3:
			statusText.setText("All Running");
			statusText.setTextColor(Color.GREEN);
			break;
		}

	}

	public void emptyDatabase(View view) {
		
		new AlertDialog.Builder(MainActivity.this)
	    .setTitle("Wipe Database")
	    .setMessage("Are you sure????")
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	DBAdapter db = new DBAdapter(getApplicationContext());
	    		db.open();
	    		db.clearTables();
	    		db.close();
	        }
	    }).setNegativeButton("No!", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// /getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private class Export extends AsyncTask<Void, String, String> {

		private final ProgressDialog dialog = new ProgressDialog(
				MainActivity.this);

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Exporting...");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		@Override
		protected void onProgressUpdate(String... message) {
			dialog.setMessage(message[0]);
		}

		@Override
		protected String doInBackground(Void... arg0) {
			Api api = new Api(getApplicationContext());

			String nickName = preferences.getString("PREF_NICKNAME", "Unknown");
			
			return api.dumpDBtoCSV(nickName);

		}

		@Override
		protected void onPostExecute(String result) {

			this.dialog.setMessage("Finished Export");
			this.dialog.dismiss();

			if (result.length() > 0)

			{
				Toast.makeText(getApplicationContext(),
						"Export Successful to SD @ Sussex/SpeedTest/" + result,
						Toast.LENGTH_LONG).show();
			} else {
				// failed
				Toast.makeText(getApplicationContext(),
						"Writing Failed to SD Card... Shout at Ciaran! ",
						Toast.LENGTH_LONG).show();

			}

		}
	}

	

}
