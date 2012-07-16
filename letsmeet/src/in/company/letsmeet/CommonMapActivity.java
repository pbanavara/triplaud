package in.company.letsmeet;

import in.company.letsmeet.locationutil.BestLocationFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class CommonMapActivity extends MapActivity{
	private Drawable drawable ;
	private Drawable fsDrawable;
	private MapView mapView;
	private MapItemizedOverlay<?> itemizedOverlay;
	private List<Overlay> mapOverlays;
	private Timer timer;
	private TimerTask doAsynchronousTask;
	
	private MapController mapControl;
	private String oldObject;
	private boolean refreshFlag = false;
	private Context context;

	private String displayMessage;

	private HttpConnectionHelper connectionHelper = new HttpConnectionHelper();

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isLocationDisplayed() {
		// TODO Auto-generated method stub
		return super.isLocationDisplayed();
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 * Initialize the drawables , MapItemizedOverlay and mapview.
	 * Sets the center of the map to user's current location.
	 */
	@Override
	protected void onCreate(Bundle bundle) {
		// TODO Auto-generated method stub
		Common.friendMap = new HashMap<String, TrackerPoint>();
		super.onCreate(bundle);
		this.context = getApplicationContext();
		BestLocationFinder finder = new BestLocationFinder(getApplicationContext(), LocationManager.NETWORK_PROVIDER,0, false);
		finder.getBestLocation(System.currentTimeMillis());
		drawable = this.getResources().getDrawable(R.drawable.marker);
		fsDrawable = getApplicationContext().getResources().getDrawable(R.drawable.orangeicon);
		setContentView(R.layout.mapus);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.invalidate();
		itemizedOverlay = new MapItemizedOverlay<Object>(drawable, this, mapView);
		mapOverlays = mapView.getOverlays();
		mapControl = mapView.getController();
		mapControl.setZoom(15);
		mapView.setBuiltInZoomControls(true); 
		MyLocationOverlay myOverlay = new MyLocationOverlay(this, mapView);
		myOverlay.enableMyLocation();
		myOverlay.enableCompass();
		mapOverlays.add(myOverlay);
		Location loc = Common.getLocation();
		//singleMode = getIntent().getExtras().getBoolean("singleusermode");
		GeoPoint point = new GeoPoint((int) (loc.getLatitude() * 1000000),(int) (loc.getLongitude() * 1000000));
		mapControl.setCenter(point);
		Toast.makeText(this, "Your current location is shown, please wait for your friend's locations", Toast.LENGTH_LONG);
		Log.i("map ctivity", String.valueOf(point.getLatitudeE6()) + String.valueOf(point.getLongitudeE6()));
		mapView.setBuiltInZoomControls(true);    
		toCallAsynchronous(mapView);
		Toast.makeText(getApplicationContext(), "Please wait while we retrieve locations",Toast.LENGTH_LONG).show();
		Button trackYes = (Button)findViewById(R.id.enableTrackButton);
		trackYes.setVisibility(View.INVISIBLE);
		Button trackNo = (Button)findViewById(R.id.disableTrackButton);
		trackNo.setVisibility(View.INVISIBLE);
		

	}

	/**
	 * @param mapView
	 * Calls the AsyncTask at an interval of every 4 seconds. This may not be the most efficient implementation.
	 */
	public void toCallAsynchronous(final MapView mapView) {

		final Handler handler = new Handler();
		timer = new Timer();
		doAsynchronousTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.post(new Runnable() {
					public void run() {
						try {
							new AddMapOverlays().execute(mapView);

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});

			}

		};
		// Find out an optimized timer frequency.
		timer.schedule(doAsynchronousTask,2000,10000);
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//timer.cancel();
		//timer.purge();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//timer.schedule(doAsynchronousTask,0,10000);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		timer.cancel();
		timer.purge();
	}

	/**
	 * @author pradeep
	 * Custom Asynctask implementation as follows:
	 * @param MapView - the mapview on which to draw/manipulate the markers and routes.
	 * @param List<OverlayItem> - The list of OverlayItems that will be populated on the map.
	 * Construct the overlay items by calling the rest API from the backend. The return is a JSON object
	 * Keep calling the addMapOverlays method at a specific interval. As and when friends reply the locations will change accordingly.
	 *
	 */
	private class AddMapOverlays extends AsyncTask<MapView, Void, List<OverlayItem>>{
		private static final String TAG = "AddMapOverlaysAsync";
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			mapView.invalidate();
		}


		/**
		 * @param mapView
		 * @return List of Overlayitems containing the locations for the organizer, friends and meeting locations.
		 */
		public List<OverlayItem> addMapOverlays(MapView mapView) {
			List<OverlayItem> pointList = new ArrayList<OverlayItem>();
			
			try {
				String url = null;
				url = Common.URL + "/id=" + Common.MY_ID;
				String obtainedLocations = connectionHelper.getData(url);
				Log.i(TAG, "Locations obtained from backend ::::" + obtainedLocations);
				JSONObject object = new JSONObject(obtainedLocations);
				String newObject = object.toString();
				if(oldObject == null) {
					oldObject = object.toString();
					refreshFlag = true;
				}
				if(!oldObject.equalsIgnoreCase(newObject)) {
					refreshFlag = true;
					oldObject = new String(newObject);
				}
				
				if(refreshFlag) {
					
				//Organizer location
				String[] organizerLocation = ((String)object.get("MYLOCATION")).split(",");
				String organizer = object.getString("MYID");
				
						GeoPoint oPoint = new GeoPoint( (int)(Double.parseDouble(organizerLocation[0]) * 1000000) , (int)(Double.parseDouble(organizerLocation[1]) * 1000000));
						OverlayItem oItem = new OverlayItem(oPoint, "organizer location","Do you need directions to this point");
						Drawable ordrawable = getResources().getDrawable(R.drawable.greenicon);
						oItem.setMarker(ordrawable);
						pointList.add(oItem);
						//Adding the organizer to the tracking HashMap
						TrackerPoint toPoint = new TrackerPoint();
						toPoint.setInitialPoint(oPoint);
						toPoint.setName("organizer");
						if (!Common.friendMap.containsKey(organizer)) {
							Common.friendMap.put(organizer, toPoint);
						}

						//Friends locations
						if (!object.isNull("FRIENDS")) {
							JSONArray array = object.getJSONArray("FRIENDS");
							displayMessage = "Friends location found";
							for(int i=0;i<array.length();++i) {
								JSONObject obj = (JSONObject) array.get(i);
								String[] tempArr = obj.getString("LOC").split(",");
								String id = obj.getString("PHONE_NUMBER");
								String name = obj.getString("NAME");
								if (tempArr.length > 1) {
									Double tempLat = Double.parseDouble(tempArr[0]);
									Double tempLon = Double.parseDouble(tempArr[1]);
									GeoPoint point = new GeoPoint((int)(tempLat * 1000000) , (int)(tempLon * 1000000));
									//HashMap<String, GeoPoint> mapId = new HashMap<String, GeoPoint>();
									TrackerPoint friendPoint = new TrackerPoint();
									friendPoint.setInitialPoint(point);
									friendPoint.setName(name);
								
									if (!Common.friendMap.containsKey(id)) {
										Common.friendMap.put(id, friendPoint);
									}
									OverlayItem fItem = new OverlayItem(point,"Friend: " + name, organizer + ":" + id);
									fItem.setMarker(drawable);
									pointList.add(fItem);
								} else {
									Log.i(TAG, "Friends location not yet obtained");
								}
							}
						} else {
							displayMessage = "Selected friends have not yet responded";
						}

						//Four square points
						if (!object.isNull("FSITEMS")) {
							//Toast.makeText(getApplicationContext(), "Received meeting place locations", Toast.LENGTH_SHORT).show();
							displayMessage = "Meeting locations from FourSquare obtained, Tap the restaurant markers for details";
							JSONArray fsArray = object.getJSONArray("FSITEMS");
							for(int i=0;i<fsArray.length();++i) {
								JSONObject obj = (JSONObject) fsArray.get(i);
								String id = obj.getString("id");
								String lat = obj.getString("lat");
								String lng = obj.getString("lng");
								String name = obj.getString("name");
								String address = obj.getString("address");
								String selected = obj.getString("selected");
								
								organizer = object.getString("MYID");
								Double tempLat = Double.parseDouble(lat);
								Double tempLon = Double.parseDouble(lng);
								GeoPoint fsPoint = new GeoPoint((int)(tempLat * 1000000) , (int)(tempLon * 1000000));
								OverlayItem fsItem = new OverlayItem(fsPoint, name + ":" + address, organizer + ":" + id);
								if (selected.equalsIgnoreCase("maybe")) {
									fsDrawable = getApplicationContext().getResources().getDrawable(R.drawable.purpleicon); 
								} else if(selected.equalsIgnoreCase("yes")) {
									fsDrawable = getApplicationContext().getResources().getDrawable(R.drawable.greenicon);
									Common.setConfirm(true);
								} else {
									fsDrawable = getApplicationContext().getResources().getDrawable(R.drawable.orangeicon);
								}
								fsDrawable.setBounds(0, 0, fsDrawable.getIntrinsicWidth(),fsDrawable.getIntrinsicHeight());
								fsItem.setMarker(fsDrawable);
								pointList.add(fsItem);	
							}
						} else {
							displayMessage = "Didn't find any meeting locations";
						}
						refreshFlag = false;
						return pointList;
				}
			} catch (Exception e) {
				e.printStackTrace();
				//return null;
			}

			return pointList;
		}

		@Override
		protected List<OverlayItem> doInBackground(MapView... params) {
			// TODO Auto-generated method stub
			List<OverlayItem> points = null;
			try {
				for(int i = 0; i<params.length;++i) {
					points = addMapOverlays(params[i]);
				}
				return points;	
			} catch (Exception e) {
				e.printStackTrace();
			}
			return points;
		}


		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 * Overriding onPostExecute to draw the overlays. Since the overlays are a UI component they cannot be updated in doInBackground
		 */
		@Override
		protected void onPostExecute(List<OverlayItem> result) {
			try {
				int minLat = Integer.MAX_VALUE;
				int minLng = Integer.MAX_VALUE;
				int maxLat = Integer.MIN_VALUE;
				int maxLng = Integer.MIN_VALUE;
				
				Log.i(TAG, "On post execute result size" + result.size());
				if(result.size() > 0) {
					Log.i(TAG, "On post fired");
					Toast.makeText(getApplicationContext(), displayMessage, Toast.LENGTH_LONG).show();
					// The arraySize is added as an optimizer to restrict map updates
					itemizedOverlay.clear();
					Iterator<OverlayItem> iterator = result.iterator();		
					while(iterator.hasNext()) {					
						OverlayItem item = iterator.next();	
						GeoPoint point = item.getPoint();
						int lat = point.getLatitudeE6();
						int lng = point.getLongitudeE6();
						if (lat < minLat) {
							minLat = lat;
						}
						if (lng < minLng) {
							minLng = lng;
						}
						if(lat > maxLat) {
							maxLat = lat;
						}
						if (lng > maxLng) {
							maxLng = lng;
						}
						
						itemizedOverlay.addOverlay(item);
					}

					mapOverlays.add(itemizedOverlay);
					Log.i("zoom", "zoom values ::" + maxLat + ":" + minLat +":" + maxLng +":" + minLng);
					mapControl.setCenter(new GeoPoint( ((maxLat + minLat )/ 2), ((maxLng + minLng) /2)));
					mapControl.zoomToSpan( (int)((maxLat - minLat)*1.5), (int)((maxLng - minLng)*1.5));
					mapView.invalidate();
				}

			} catch(Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	

}
