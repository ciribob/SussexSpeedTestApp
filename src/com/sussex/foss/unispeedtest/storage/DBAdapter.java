package com.sussex.foss.unispeedtest.storage;

import java.util.ArrayList;

import com.sussex.foss.unispeedtest.MainActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Handles all Database tasks including dropping creation of tables
 * 
 * @author ciaranfisher
 * 
 */
public class DBAdapter {

	private static final String DATABASE_NAME = "speedTest.db";
	private static final int DATABASE_VERSION = 3; // needs to be incremented if
													// database
													// structure changes to drop
													// existing DB on users
													// phone if app upgraded

	// Speed Test TABLE
	private static final String TEST_TABLE = "SpeedTest";
	private static final String TEST_TIMESTAMP = "timestamp";
	public static final String TEST_LAT = "lat";
	public static final String TEST_LON = "lon";
	public static final String TEST_ACCURACY = "accuracy";

	public static final String TEST_RECEIVED = "received";
	public static final String TEST_SENT = "sent";
	public static final String TEST_TIME = "time";
	public static final String TEST_SIGNAL_TYPE = "signalType";
	public static final String TEST_SIGNAL_STRENGTH = "signalStrength";

	public static final String TEST_BATTERY = "battery";
	
	public static final String TEST_REQUEST = "request";

	private SQLiteDatabase db; // the database
	private final Context context;
	private SpeedTestDBHelper dbHelper;

	private boolean open = false;

	/**
	 * Constructor takes the Current Activities context needed to access the db
	 * 
	 * @param _context
	 */
	public DBAdapter(Context _context) {
		this.context = _context;
		dbHelper = new SpeedTestDBHelper(context, DATABASE_NAME, null,
				DATABASE_VERSION);
	}

	/**
	 * Closes the database connection
	 */
	public void close() {
		if (open) {
			open = false;
			db.close();
		}
	}

	/**
	 * Opens the database for access
	 */
	public void open() {
		if (!open) {

			try {
				db = dbHelper.getWritableDatabase();
				open = true;
			} catch (SQLiteException ex) {
				Log.e(MainActivity.LOG_NAME, "DB ERROR:" + ex.getMessage());
			}
		}
	}

	public void storeSpeedTest(SpeedTestResult result) {
		// Create a new row of values to insert.
		ContentValues insertResult = new ContentValues();
		// Assign values for each row.
		insertResult.put(TEST_TIMESTAMP, (result.getTimestamp())+""); //store as string as int overflows
		insertResult.put(TEST_LAT, result.getLat());
		insertResult.put(TEST_LON, result.getLon());
		insertResult.put(TEST_ACCURACY, result.getAccuracy());
		insertResult.put(TEST_RECEIVED, result.getReceived());
		insertResult.put(TEST_SENT, result.getSent());
		insertResult.put(TEST_TIME, result.getTime());
		insertResult.put(TEST_SIGNAL_TYPE, result.getSignalType());
		insertResult.put(TEST_SIGNAL_STRENGTH, result.getSignalStrength());
		insertResult.put(TEST_BATTERY, result.getBattery());
		insertResult.put(TEST_REQUEST, result.getRequestSize());

		try {
			db.insertOrThrow(TEST_TABLE, null, insertResult);
			Log.d(MainActivity.LOG_NAME, "inserted new test result");
		} catch (SQLException ex) {

			Log.e(MainActivity.LOG_NAME, "error inserting new test result "
					+ ex.getMessage());
		}

	}

	/**
	 * Empties all the data stored in the database
	 */
	public void clearTables() {
		db.delete(TEST_TABLE, null, null);

	}
	
	public Cursor getSpeedTestsCursor() {
		return db.query(TEST_TABLE, null, null, null, null, null, null);

	}

	/**
	 * Inner class used to handle database creation and opening
	 * 
	 * @author ciaranfisher
	 * 
	 */
	private class SpeedTestDBHelper extends SQLiteOpenHelper {

		public SpeedTestDBHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		private static final String CREATE_SPEED_TEST_TABLE = "CREATE TABLE "
				+ TEST_TABLE + "(" + TEST_TIMESTAMP + " STRING, " + TEST_LAT
				+ " REAL," + TEST_LON + " REAL," + TEST_ACCURACY + " INTEGER,"
				+ TEST_RECEIVED + " INTEGER," + TEST_SENT + " INTEGER,"
				+ TEST_TIME + " INTEGER," + TEST_SIGNAL_TYPE + " STRING,"
				+ TEST_SIGNAL_STRENGTH + " INTEGER," + TEST_BATTERY
				+ " INTEGER, " 
				+ TEST_REQUEST+ " INTEGER, " 
				+ " PRIMARY KEY( " + TEST_TIMESTAMP + " ) )";

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_SPEED_TEST_TABLE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(MainActivity.LOG_NAME, "Upgrading from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data");

			// Drop the old table.
			db.execSQL("DROP TABLE IF EXISTS " + TEST_TABLE);

			// Create a new one.
			onCreate(db);
		}
	}

	
}
