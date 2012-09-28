package com.sussex.foss.unispeedtest.storage;

public class SpeedTestResult {
	
	private long timestamp;
	
	private double lat;
	private double lon;
	private float accuracy;
	
	private int received;
	private int sent;
	private int time =0;
	
	private String signalType;
	private int signalStrength;
	
	private int battery;
	
	private int requestSize;

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public int getReceived() {
		return received;
	}

	public void setReceived(int received) {
		this.received = received;
	}

	public int getSent() {
		return sent;
	}

	public void setSent(int sent) {
		this.sent = sent;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getSignalType() {
		return signalType;
	}

	public void setSignalType(String signalType) {
		this.signalType = signalType;
	}

	public int getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(int signalStrength) {
		this.signalStrength = signalStrength;
	}

	public int getBattery() {
		return battery;
	}

	public void setBattery(int battery) {
		this.battery = battery;
	}
	
	public int getRequestSize() {
		return requestSize;
	}
	
	public void setRequestSize(int requestSize) {
		this.requestSize = requestSize;
	}
	
	
	
	

}
