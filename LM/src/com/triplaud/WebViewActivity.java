package com.triplaud;

import greendroid.app.ActionBarActivity;
import greendroid.app.GDMapActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.triplaud.common.Common;
import com.triplaud.common.HttpConnectionHelper;
import com.triplaud.contacts.InviteContactsActivity;

/**
 * @author pradeep
 *
 */
/**
 * @author pradeep
 *
 */
public class WebViewActivity extends GDMapActivity implements OnClickListener{

	private static final String TAG = "WebViewActivity";
	private MapView mView;
	private LocationManager locationManager;
	private Timer parseTimer;
	private TimerTask parseTimerTask;
	DirectionsItemizedOverlay<OverlayItem> sItemizedOverlay;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			//Hide the title bar
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			//End Hide title bar
			//Start GreenDroid
			setActionBarContentView(R.layout.mapus);
			addActionBarItem(Type.LocateMyself, R.id.action_bar_search);
			addActionBarItem(Type.AllFriends, R.id.action_bar_allfriends);
			//End GreenDroid

			mView = (MapView) findViewById(R.id.mapview);
			final List<Overlay> mOverlay = mView.getOverlays();
			MapController controller = mView.getController();

			final String destinationLoc = getIntent().getExtras().getString("DEST");

			/*
			 * Obtain destination address, put markers for those addresses.
			 */
			String[] destinationArr = destinationLoc.split(",");
			double dLoc = Double.parseDouble(destinationArr[0]) * 1e6;
			double dLon = Double.parseDouble(destinationArr[1]) * 1e6;
			GeoPoint dmGp = new GeoPoint((int)dLoc, (int)dLon);
			setZoom(mView.getController(), dmGp, Common.friendMap);
			Drawable sDrawable = getApplicationContext().getResources().getDrawable(R.drawable.marker); 
			Drawable dDrawable = getApplicationContext().getResources().getDrawable(R.drawable.greenicon);
			OverlayItem dItem = new OverlayItem(dmGp, "", "");

			/*
			 * Obtain the map overlays and add the source and destination markers to those overlays.
			 */
			sItemizedOverlay = new DirectionsItemizedOverlay<OverlayItem>(sDrawable, this, mView);
			DirectionsItemizedOverlay<OverlayItem> dItemizedOverlay = new DirectionsItemizedOverlay<OverlayItem>(dDrawable, this, mView);
			dItemizedOverlay.addOverlay(dItem);
			mOverlay.add(sItemizedOverlay);
			mOverlay.add(dItemizedOverlay);
			controller.setZoom(15);
			mView.setBuiltInZoomControls(true);
			
			/*
			 * Changed to handler in order to save some exceptions resulting from Thread class.
			 */
			new Thread(new Runnable() {
				public void run() {
					int color = 999;
					/*
					 * Common.Friendlist contains all the friend's and organizer's locations.
					 */				
					HashMap<String, TrackerPoint> map = new HashMap<String, TrackerPoint>();
					map.putAll(Common.friendMap);
					Collection<TrackerPoint> values = (Collection<TrackerPoint>) map.values();
					Iterator<TrackerPoint> iterator = values.iterator();
					while(iterator.hasNext()) {
						TrackerPoint tPoint = iterator.next();
						GeoPoint point = tPoint.getInitialPoint();
						OverlayItem nsItem = new OverlayItem(point, tPoint.getName(), "");
						sItemizedOverlay.addOverlay(nsItem);
						double lat = point.getLatitudeE6() / 1e6;
						double lng = point.getLongitudeE6() / 1e6;
						String friendLoc = lat + "," + lng;
						Log.i(TAG, "Destination location" + destinationLoc);
						displayRouteFromLeafLet(friendLoc, destinationLoc, mOverlay, color);
						color = color - 50;
					}

				}
			}).start();

			//Initialize the timer task for getting directions.
			parseTimerTask = new ParseTimerTask();
			//Check if the timer is started in the onclick by checking for null. If the timer has been initialized, that is it's not null,
			// Then schedule the timer task.
			if (parseTimer != null) {
				parseTimer.schedule(parseTimerTask,0,Common.UPDATE_MAP_FREQUENCY);
			}
			Toast.makeText(getApplicationContext(), "Please wait for directions",Toast.LENGTH_LONG).show();


			/*
			 * Tracking each other on the map
			 */
			Button trackYes = (Button)findViewById(R.id.enableTrackButton);
			trackYes.setEnabled(true);
			Button trackNo = (Button)findViewById(R.id.disableTrackButton);
			trackNo.setEnabled(true);
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			trackYes.setOnClickListener(this);
			trackNo.setOnClickListener(this);

		} catch(Exception e) {
			e.printStackTrace();
		}


	}

	private class ParseTimerTask extends TimerTask {
		Handler handler = new Handler();
		@Override

		public void run() {
			handler.post(new Runnable() {
				public void run() {
					try {
						new DisplayParseData().execute();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		}


	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(locationManager != null) {
			locationManager.removeUpdates(listener);
		}
	}

	/**
	 * @author pradeep
	 * Changed from Thread to Handler and finally to AsyncTask. Repeatedly fetch locations from the node backend and display them.
	 */
	private class DisplayParseData extends AsyncTask<Void, Void, List<OverlayItem>> {
		/**
		 * Obtain all the location updates from Parse for the List of Ids and display the locations.
		 */
		@Override
		protected List<OverlayItem> doInBackground(Void ...params) {
			List<OverlayItem> listOfItems = new ArrayList<OverlayItem>();
			try {
					listOfItems = retrieveDataFromEC();
				return listOfItems;
			} catch(Exception e) {
				e.printStackTrace();
			}
			return listOfItems;
		}
		
		
		/**
		 * @return List Of Overlay Items to be added to MapItemizedOverlay
		 * Doesn't take any inputs as the inputs are already stored in Common
		 */
		private List<OverlayItem> retrieveDataFromEC() {
			List<OverlayItem> locations = new ArrayList<OverlayItem>();
			try {
				String url;
				if (Common.friend) {
					url = Common.URL + "/getLocation&friend=" + Common.MY_ID;
				} else {
					url = Common.URL + "/getLocation&organizer=" + Common.MY_ID;
				}
				HttpConnectionHelper helper = new HttpConnectionHelper();
				String returnData = helper.getData(url);
				JSONObject obj = new JSONObject(returnData);
				if(!obj.isNull("FRIENDS")) {
				JSONArray friends = obj.getJSONArray("FRIENDS");
				for(int i=0;i<friends.length();++i) {
					String id = friends.getJSONObject(i).getString("NAME");
					String loc = friends.getJSONObject(i).getString("LOC");
					String[] locArray = loc.split(",");
					String lat = locArray[0];
					String lng = locArray[1];
					int latPoint = (int)(Double.parseDouble(lat) * 1000000);
					int lngPoint = (int)(Double.parseDouble(lng) * 1000000);
					
					GeoPoint point = new GeoPoint(latPoint, lngPoint);
					OverlayItem item = new OverlayItem(point, id, "");
					
					locations.add(item);
				}
				}
				String oId = obj.getString("ORGID");
				String oLoc = obj.getString("ORG_LOC");
				String[] locArray = oLoc.split(",");
				String lat = locArray[0];
				String lng = locArray[1];
				int latPoint = (int)(Double.parseDouble(lat) * 1000000);
				int lngPoint = (int)(Double.parseDouble(lng) * 1000000);
				GeoPoint point = new GeoPoint(latPoint, lngPoint);
				OverlayItem item = new OverlayItem(point, oId, "");
				locations.add(item);
				return locations;
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			return locations;
		}

		/*
		 * Deprecated
		 */
		private GeoPoint getValuesFromParse(String id) {
			StringBuffer buffer = new StringBuffer();
			GeoPoint point = null;
			try {

				HttpClient client = new DefaultHttpClient();
				String query="where={" +"\"uid\":" +"\"" + id + "\"}&limit=1&order=-createdAt";
				URI uri = new URI(
						"https", 
						"api.parse.com", 
						"/1/classes/trackdata",
						query,
						null);
				String url = uri.toASCIIString();
				Log.d(TAG, "Parse URL" + url);
				HttpGet get = new HttpGet(url);
				get.setHeader("X-Parse-Application-Id", "8gA50gSiVTZzzJwXyLbCLVYWuXvGyA4fkrhnC6OK");
				get.setHeader("X-Parse-REST-API-Key", "RY1gi8mxESXYEUCH6J8bWza6j7xmexmJ3xYcbPCj");
				get.setHeader("Content-Type", "application/json");
				HttpResponse response = client.execute(get);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						buffer.append(line);
					}
				}
				Log.i(TAG, "Recveived data from Parse" + buffer.toString());
				JSONObject object = new JSONObject(buffer.toString());
				JSONArray results = object.getJSONArray("results");
				for (int i=0;i<results.length();++i) {
					JSONObject objectLoc = (JSONObject)results.get(i);
					JSONObject location = (JSONObject) objectLoc.get("location");
					String latitude = location.getString("latitude");
					String longitude = location.getString("longitude");
					Log.i(TAG, "Returned value from parse" + latitude +":" + longitude);
					point = new GeoPoint( (int)(Double.parseDouble(latitude) * 1e6), (int)(Double.parseDouble(longitude) * 1e6) );
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return point;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			mView.invalidate();
		}

		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();

		}

		@Override
		protected void onPostExecute(List<OverlayItem> result) {
			//sItemizedOverlay.clear();
			Iterator<OverlayItem> iterator = result.iterator();		
			while(iterator.hasNext()) {					
				OverlayItem item = iterator.next();	
				sItemizedOverlay.addOverlay(item);
				Log.d(TAG, "On Post Execute called");
			}

		}


	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}


	/**
	 * @param control
	 * @param sourceLocation
	 * @param destinationLocations
	 * Calculates the center and zooms the map based on the source location and a list of destination locations. Source is the originator's address.
	 * Destination addresses are the addresses of all friends involved in the session.
	 */
	public void setZoom(MapController control, GeoPoint restaurantLocation, HashMap<String, TrackerPoint> sourceLocations) {

		Collection<TrackerPoint> list = (Collection<TrackerPoint>)sourceLocations.values();
		Iterator<TrackerPoint> iterator = list.iterator();	
		int maxLat = Integer.MIN_VALUE;
		int maxLng = Integer.MIN_VALUE;	
		while(iterator.hasNext()) {					
			GeoPoint point = iterator.next().getInitialPoint();
			int lat = point.getLatitudeE6();
			int lng = point.getLongitudeE6();
			if(lat > maxLat) {
				maxLat = lat;
			}
			if (lng > maxLng) {
				maxLng = lng;
			}

			int zoomLat = (int) (Math.abs(maxLat - restaurantLocation.getLatitudeE6()) * 1.5);
			int zoomLng = (int) (Math.abs(maxLng - restaurantLocation.getLongitudeE6()) * 1.5);
			control.setCenter(new GeoPoint( ((maxLat + restaurantLocation.getLatitudeE6()) / 2), ((maxLng +
					restaurantLocation.getLongitudeE6()) / 2)));
			control.zoomToSpan(zoomLat, zoomLng);

		}

	}


	/**
	 * @param sourceLoc
	 * @param destinationLoc
	 * @param mOverlay
	 * Draw routes from a source to a destination using leaflet directions API. Leaflet API returns an array of intermediate points that are used to draw the route.
	 * Preferred Leaflet to google directions as the google directions API does not return a continuous set of points.
	 */
	public void displayRouteFromLeafLet(String sourceLoc, String destinationLoc, List<Overlay> mOverlay, int color) {
		try {
			//mView.invalidate();
			String newUrl = Common.DIRECTIONS_URL.concat(sourceLoc).concat(",").concat(destinationLoc).concat("/car.js?tId=CloudMade");
			//String newUrl = "http://navigation.cloudmade.com/05de9601467f4e8c9e890a2622541715/api/0.3/13.038924999999999,77.555035,12.9273097,77.5862775/car.js?tId=CloudMade";
			HttpConnectionHelper helper = new HttpConnectionHelper();
			String newLine = helper.getData(newUrl);
			JSONObject jMapData = new JSONObject(newLine);
			JSONArray routes = jMapData.getJSONArray("route_geometry");

			for (int i=0;i<routes.length();++i) {
				JSONArray startLocation = routes.getJSONArray(i);
				String ssLat = startLocation.getString(0);
				String ssLon = startLocation.getString(1);
				if(i< (routes.length() -1)) {
					JSONArray endLocation = routes.getJSONArray(i+1);
					String esLat = endLocation.getString(0);
					String esLon = endLocation.getString(1);
					double sLat = Double.parseDouble(ssLat) * 1E6;
					double sLng = Double.parseDouble(ssLon) * 1E6;
					GeoPoint sGp = new GeoPoint((int)sLat,(int)sLng);
					double dLat = Double.parseDouble(esLat) * 1E6;
					double dLng = Double.parseDouble(esLon) * 1E6;
					GeoPoint dGp = new GeoPoint((int)dLat,(int)dLng);	
					MyOverLay mLay = new MyOverLay(sGp,dGp,2,color);
					mOverlay.add(mLay);	
				}

			}
			mView.postInvalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private LocationListener listener = new LocationListener() {

		public void onLocationChanged(Location location) {
			Log.d(TAG, "Updated location" + location.getLatitude() + "," + location.getLongitude());
			//uploadDataToParse(location);
			uploadDataToEC(location);

		}


		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		private void uploadDataToEC(Location location) {
			JSONObject obj = new JSONObject();
			String locString = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
			String url;
			try {
				//Friend posting data
				if(Common.friend == true) {
					obj.put("MYID", Common.MY_ID);
					url = Common.URL + "/updateFriendLocation";
				} else {
					obj.put("MYID", Common.ORGANIZER_ID);
					url = Common.URL + "/updateOrganizerLocation";
					
				}
				obj.put("MYID", Common.ORGANIZER_ID);
				obj.put("MYLOCATION", locString);
				HttpConnectionHelper helper = new HttpConnectionHelper();
				helper.postData(url, obj);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		
		/**
		 * @param location
		 * Deprecated
		 */
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


		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}


		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}
	};


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.enableTrackButton) {
			checkForGps();
			//Toast.makeText(getApplicationContext(), "Your location will be uploaded every 2 minutes for tracking purposes", Toast.LENGTH_LONG).show();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Common.UPDATE_PARSE_FREQUENCY, 1, listener);
			parseTimer = new Timer();
			parseTimerTask = new ParseTimerTask();
			parseTimer.schedule(parseTimerTask,0,Common.UPDATE_PARSE_FREQUENCY);
		} 
		if (v.getId() == R.id.disableTrackButton) {
			Log.i(TAG,"Location listener disabled");
			locationManager.removeUpdates(listener);
			if(parseTimer != null) {
				parseTimer.cancel();
			}
		}

	}

	/**
	 * Alert user if GPS is not turned on.
	 */
	private void checkForGps() {
		// TODO Auto-generated method stub
		if(! locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Yout GPS seems to be disabled, do you want to enable it?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick( final DialogInterface dialog, final int id) {
					startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 100);
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int id) {
					dialog.cancel();
				}
			});
			final AlertDialog alert = builder.create();
			alert.show();

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.i("Activity Returned", "Result" + resultCode);
		super.onActivityResult(requestCode, resultCode, data);
	}

	/* (non-Javadoc)
	 * @see greendroid.app.GDMapActivity#onHandleActionBarItemClick(greendroid.widget.ActionBarItem, int)
	 * Greendroid overrides to bring up the action bar.
	 */
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.action_bar_search:
			intent = new Intent(this, SearchMapActivity.class);
			intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Search meeting place");
			startActivity(intent);
			break;

		case R.id.action_bar_allfriends:
			//Start the contactsList Activity with the required parameters
			intent = new Intent(this, InviteContactsActivity.class);
			intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Invite Friends");
			startActivityForResult(intent, Common.START_CONTACT_LIST);
			break;

		case R.id.action_bar_home:
			intent = new Intent(this, Main.class);
			intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "LetsMeet");
			startActivity(intent);
			break;

		default:
			return super.onHandleActionBarItemClick(item, position);
		}

		return true;
	}

}

