package in.company.letsmeet;

import in.company.letsmeet.common.Common;
import in.company.letsmeet.common.HttpConnectionHelper;
import in.company.letsmeet.locationutil.BestLocationFinder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * @author pradeep
 *
 */
public class WebViewActivity extends MapActivity {

	private static final String TAG = "WebViewActivity";
	private MapView mView;
	private BestLocationFinder finder;
	private LocationManager locationManager;
	DirectionsItemizedOverlay<OverlayItem> sOverlay;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			finder = new BestLocationFinder(getApplicationContext(), LocationManager.GPS_PROVIDER, true);
			setContentView(R.layout.mapus);

			mView = (MapView) findViewById(R.id.mapview);
			List<Overlay> mOverlay = mView.getOverlays();
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
			sOverlay = new DirectionsItemizedOverlay<OverlayItem>(sDrawable, this, mView);
			DirectionsItemizedOverlay<OverlayItem> dOverlay = new DirectionsItemizedOverlay<OverlayItem>(dDrawable, this, mView);
			dOverlay.addOverlay(dItem);
			mOverlay.add(sOverlay);
			mOverlay.add(dOverlay);
			controller.setZoom(15);
			mView.setBuiltInZoomControls(true);
			
			final List<Overlay> newOverlay = mView.getOverlays();
			
			new Thread( new Runnable() {
				public void run() {
					int color = 999;
					/*
					 * Common.Friendlist contains all the friend's locations.
					 */				
					HashMap<String, TrackerPoint> map = new HashMap<String, TrackerPoint>();
					map.putAll(Common.friendMap);
				
					Collection<TrackerPoint> values = (Collection<TrackerPoint>) map.values();
					Iterator<TrackerPoint> iterator = values.iterator();
					while(iterator.hasNext()) {
						TrackerPoint tPoint = iterator.next();
						GeoPoint point = tPoint.getInitialPoint();
						OverlayItem nsItem = new OverlayItem(point, tPoint.getName(), "");
						sOverlay.addOverlay(nsItem);
						double lat = point.getLatitudeE6() / 1e6;
						double lng = point.getLongitudeE6() / 1e6;
						String friendLoc = lat + "," + lng;
						Log.i(TAG, "Destination location" + destinationLoc);
						displayRouteFromLeafLet(friendLoc, destinationLoc, newOverlay, color);
						color = color - 50;
					}
					
				}
			}).start();
			
			Toast.makeText(getApplicationContext(), "Please wait for directions",Toast.LENGTH_LONG).show();
		} catch(Exception e) {
			e.printStackTrace();
		}

		/*
		 * Tracking each other on the map
		 */
		Button trackYes = (Button)findViewById(R.id.enableTrackButton);
		trackYes.setEnabled(true);
		Button trackNo = (Button)findViewById(R.id.disableTrackButton);
		trackNo.setEnabled(true);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		trackYes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.enableTrackButton) {
					Toast.makeText(getApplicationContext(), "Your location will be uploaded every 2 minutes for tracking purposes", Toast.LENGTH_LONG).show();
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120000, 1, listener);
					getAndDisplayLocationUpdates(Common.friendMap);
				} 
			}
		});
		
		trackNo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.disableTrackButton) {
					Log.i(TAG,"Location listener disabled");
					locationManager.removeUpdates(listener);
				}
			}
		});
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		locationManager.removeUpdates(listener);
	}

	/**
	 * Obtain all the location updates from Parse for the List of Ids and display the locations.
	 */
	private void getAndDisplayLocationUpdates(final HashMap<String,TrackerPoint> ids) {
		new Thread ( new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					Set<String> idSet = ids.keySet();
					Iterator<String> iterator = idSet.iterator();
					while(iterator.hasNext()) {
						String id = iterator.next();
						GeoPoint currentPoint = getValuesFromParse(id);
						if (currentPoint != null) {
							TrackerPoint tPoint = ids.get(id);
							OverlayItem nsItem = new OverlayItem(currentPoint, tPoint.getName(), "");
							sOverlay.addOverlay(nsItem);
							tPoint.setCurrentPoint(currentPoint);
							ids.put(id, tPoint);
						} else {
							Log.i(TAG, "No values obtained from Parse");
							mView.postInvalidate();
						}

					}
					
					try {
						Thread.sleep(Common.UPDATE_PARSE_FREQUENCY);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
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

		}).start();
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
	      uploadDataToParse(location);
	      
	    }
	    
	   
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


		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}
	};
}

