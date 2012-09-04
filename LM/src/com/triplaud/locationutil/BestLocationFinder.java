package com.triplaud.locationutil;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.triplaud.common.Common;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class BestLocationFinder {

	protected static String TAG = "BestLocationFinder";

	protected LocationListener locationListener;
	protected LocationManager locationManager;
	protected Criteria criteria;
	protected Context context;
	public Location bestResult;
	private String provider;
	private long frequency;

	public BestLocationFinder(Context context, String provider, boolean uploadFrequent) {
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		this.context = context;
		this.provider = provider;
	}

	public void getBestLocation(long minTime, long frequency) {
		//String provider = (locationManager.getProvider(provider)).getName();
		Location location = locationManager.getLastKnownLocation(provider);
		this.frequency = frequency;
		if (location != null) {
			long time = location.getTime();
			if (time < minTime) {		          
				locationManager.requestLocationUpdates(provider, frequency, 0, singeUpdateListener);
				Log.i(TAG, "Old update");
			}
			else if (time >= minTime) {
				Common.setLocation(location);
			}
		} else {
			locationManager.requestLocationUpdates(provider, frequency, 0, singeUpdateListener);
			Log.i(TAG, "Last known location null, hence trigerring new updates");
		}

	}

	public Location getBestResult() {
		return this.bestResult;
	}

	public void removeLocationUpdates() {
		locationManager.removeUpdates(singeUpdateListener);
		Log.d(TAG, "Stopping location listener");
	}

	protected LocationListener singeUpdateListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			Log.d(TAG, "Location Update Received: " + location.getLatitude() + "," + location.getLongitude());
			Common.setLocation(location);
			if (frequency != 0) {
				Log.d(TAG, "Frequency is :" + frequency);
				uploadDataToParse(location);
			} else {
				locationManager.removeUpdates(this);
			}
		}

		public void onProviderEnabled(String provider) {}    
		public void onProviderDisabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		private void uploadDataToParse(Location location) {
			try{
				Log.i(TAG, "Uploading data to parse");
				HttpClient client = new DefaultHttpClient();
				String url = "https://api.parse.com/1/classes/trackdata";
				HttpPost post = new HttpPost(url);
				post.setHeader("X-Parse-Application-Id", "8gA50gSiVTZzzJwXyLbCLVYWuXvGyA4fkrhnC6OK");
				post.setHeader("X-Parse-REST-API-Key", "RY1gi8mxESXYEUCH6J8bWza6j7xmexmJ3xYcbPCj");
				post.setHeader("Content-Type", "application/json");
				JSONObject obj = new JSONObject();
				JSONObject loc = new JSONObject();
				loc.put("__type", "GeoPoint");
				loc.put("latitude", location.getLatitude());
				loc.put("longitude",location.getLongitude());
				obj.put("location", loc);
				obj.put("uid", Common.MY_ID);

				StringEntity se = new StringEntity(obj.toString());

				post.setEntity(se);
				org.apache.http.HttpResponse response = client.execute(post);
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					System.out.println(line);
					Log.i(TAG, line);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * {@inheritDoc}
	 */
	public void setChangedLocationListener(LocationListener l) {
		locationListener = l;
	}

	/**
	 * {@inheritDoc}
	 */
	public void cancel() {
		locationManager.removeUpdates(singeUpdateListener);
	}

}
