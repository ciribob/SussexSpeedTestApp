package com.sussex.foss.unispeedtest.background;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sussex.foss.unispeedtest.MainActivity;
import com.sussex.foss.unispeedtest.storage.DBAdapter;
import com.sussex.foss.unispeedtest.storage.SpeedTestResult;

public class SpeedTest {

	private SpeedTestResult test;
	private Context context;

	public SpeedTest(Context context, Location location, int battery,
			int signalStrength, int requestSize) {
		this.context = context;
		test = new SpeedTestResult();

		test.setBattery(battery);
		test.setSignalStrength(signalStrength);

		if (location == null) {
			test.setLat(0);
			test.setLon(0);
			test.setAccuracy(0);
		} else {
			test.setLat(location.getLatitude());
			test.setLon(location.getLongitude());
			test.setAccuracy(location.getAccuracy());

		}

		test.setRequestSize(requestSize);
	}

	public void runSpeedTest() {
		// get network details
		getNetworkInfo();
		getRTT();
		speedTest();
		storeSpeedTest();

	}

	private void speedTest() {

		long rXBytes = TrafficStats.getUidRxBytes(android.os.Process.myUid());
		long tXBytes = TrafficStats.getUidTxBytes(android.os.Process.myUid());

		if (Api.isNetworkAvailable(context)) {

			String url = "http://www.sussex.ac.uk/Users/ianw/digitalStadium/speedtest.php?downloadsize="
					+ test.getRequestSize() + "&random=" + System.nanoTime(); // add
																				// on
																				// the
																				// end
																				// to
																				// stop
																				// caching

			// do it a better way with sockets for more accurate??

			HttpGet httpGet = new HttpGet(url);
			HttpClient httpClient = new DefaultHttpClient();

			try {
				// Start time
				long timer = System.currentTimeMillis();
				// execute request
				HttpResponse response = httpClient.execute(httpGet);

				StatusLine statusLine = response.getStatusLine();

				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

					// save time only if request worked
					test.setTime((int) (System.currentTimeMillis() - timer));

					HttpEntity entity = response.getEntity();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					entity.writeTo(out);
					out.close();

					// received
					// Log.d(MainActivity.LOG_NAME, out.toString());

					Log.d(MainActivity.LOG_NAME, "Speed Test Ran");

				} else {
					// handle bad response
					Log.e(MainActivity.LOG_NAME,
							"BAD RESPONSE: " + statusLine.getStatusCode());

				}
			} catch (ClientProtocolException e) {
				// handle exception
				Log.e(MainActivity.LOG_NAME, e.getMessage());
			} catch (IOException e) {
				// handle exception
				Log.e(MainActivity.LOG_NAME, e.getMessage());
			}

		} else {

			Log.d(MainActivity.LOG_NAME,
					"Speed Test fAiled, no internet connection");

			test.setTime(0);

		}

		test.setTimestamp(System.currentTimeMillis());

		// compute difference in rX and tX
		test.setReceived((int) (TrafficStats.getUidRxBytes(android.os.Process
				.myUid()) - rXBytes));
		test.setSent((int) (TrafficStats.getUidTxBytes(android.os.Process
				.myUid()) - tXBytes));

	}

	public void getRTT() {
		if (Api.isNetworkAvailable(context)) {

			String url = "http://www.sussex.ac.uk/Users/ianw/digitalStadium/speedtest.php?gettime=1&random=" + System.nanoTime(); 
																				// add
																				// on
																				// the
																				// end
																				// to
																				// stop
																				// caching

			HttpGet httpGet = new HttpGet(url);
			HttpClient httpClient = new DefaultHttpClient();

			try {
			
				long sentTime = System.currentTimeMillis();
				// execute request
				HttpResponse response = httpClient.execute(httpGet);
				
				long receiveTime = System.currentTimeMillis();

				StatusLine statusLine = response.getStatusLine();

				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					entity.writeTo(out);
					out.close();
					
					//get RTT by calculating difference in clocks
					long webTime = (long) (Double.parseDouble(out.toString().trim())* (double)1000);
					
					
					
					long totalToSend = webTime - sentTime;
					
					long totalToReceive = receiveTime - webTime;
					
					test.setRtt((int)(totalToSend +totalToReceive)); 

					// received
					// Log.d(MainActivity.LOG_NAME, out.toString());

					Log.d(MainActivity.LOG_NAME, "Calculated RTT. StartTime: "+sentTime+" WebTime: "+webTime+" Sending Time: "+totalToSend+ " Receive Time:"+totalToReceive+" TOTAL:"+test.getRtt() );

				} else {
					// handle bad response
					Log.e(MainActivity.LOG_NAME,
							"BAD RESPONSE: " + statusLine.getStatusCode());
					
					Log.d(MainActivity.LOG_NAME, "Failed to Calculate RTT");

				}
			} catch (ClientProtocolException e) {
				// handle exception
				Log.e(MainActivity.LOG_NAME, e.getMessage());
			} catch (IOException e) {
				// handle exception
				Log.e(MainActivity.LOG_NAME, e.getMessage());
			}

		} else {

			Log.d(MainActivity.LOG_NAME,
					"RTT Test fAiled, no internet connection");
		}



	}

	private void storeSpeedTest() {
		DBAdapter db = new DBAdapter(this.context);
		db.open();
		db.storeSpeedTest(test);
		db.close();

	}

	private void getNetworkInfo() {
		NetworkInfo networkInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (networkInfo != null) {

			// possible fix for ians phone always thinking it was on wifi??
			if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE
					|| networkInfo.getType() == ConnectivityManager.TYPE_MOBILE_DUN
					|| networkInfo.getType() == ConnectivityManager.TYPE_MOBILE_HIPRI
					|| networkInfo.getType() == ConnectivityManager.TYPE_MOBILE_MMS
					|| networkInfo.getType() == ConnectivityManager.TYPE_MOBILE_SUPL) {

				// its a mobile connection but what kind...
				TelephonyManager tel = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);

				switch (tel.getNetworkType()) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					test.setSignalType("1xRTT");
					break;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					test.setSignalType("CDMA");
					break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					test.setSignalType("EDGE");
					break;
				case TelephonyManager.NETWORK_TYPE_EHRPD:
					test.setSignalType("EHRPD");
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					test.setSignalType("EVDO_0");
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					test.setSignalType("EVDO_A");
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
					test.setSignalType("EVDO_B");
					break;

				case TelephonyManager.NETWORK_TYPE_GPRS:
					test.setSignalType("GPRS");
					break;
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					test.setSignalType("HSDPA");
					break;
				case TelephonyManager.NETWORK_TYPE_HSPA:
					test.setSignalType("HSPA");
					break;
				case TelephonyManager.NETWORK_TYPE_HSPAP:
					test.setSignalType("HSPAP");
					break;
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					test.setSignalType("HSUPA");
					break;
				case TelephonyManager.NETWORK_TYPE_IDEN:
					test.setSignalType("IDEN");
					break;
				case TelephonyManager.NETWORK_TYPE_LTE:
					test.setSignalType("LTE");
					break;

				case TelephonyManager.NETWORK_TYPE_UMTS:
					test.setSignalType("UMTS");
					break;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					test.setSignalType("UNKNOWN");
					break;
				default:
					test.setSignalType("UNKNOWN");
					break;

				}

			} else
				// not mobile so just put the string in
				test.setSignalType(networkInfo.getTypeName());
		} else {
			test.setSignalType("NONE");
		}
	}

	public SpeedTestResult getTestResult() {
		return this.test;
	}

}
