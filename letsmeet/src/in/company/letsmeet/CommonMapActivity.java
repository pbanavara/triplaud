package in.company.letsmeet;

import in.company.letsmeet.locationutil.BestLocationFinder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class CommonMapActivity extends MapActivity{
	private Drawable drawable ;
	private Drawable fsDrawable;
	private MapItemizedOverlay<?> itemizedOverlay;
	private List<Overlay> mapOverlays;
	private Timer timer;
	private TimerTask doAsynchronousTask;

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
		super.onCreate(bundle);
		drawable = this.getResources().getDrawable(R.drawable.purpleicon);
		fsDrawable = getApplicationContext().getResources().getDrawable(R.drawable.orangeicon);
		setContentView(R.layout.mapus);
		MapView mapView = (MapView) findViewById(R.id.mapview);
		mapView.invalidate();
		View popUp = getLayoutInflater().inflate(R.layout.popup,mapView,false);
		itemizedOverlay = new MapItemizedOverlay<Object>(drawable, this, popUp);
		mapOverlays = mapView.getOverlays();
		mapView.buildDrawingCache(true);
		MapController mapControl = mapView.getController();
		mapControl.setZoom(14);

		BestLocationFinder finder = new BestLocationFinder(getApplicationContext());
		Location loc = finder.getLastBestLocation(System.currentTimeMillis());
		GeoPoint point = new GeoPoint((int) (loc.getLatitude() * 1000000),(int) (loc.getLongitude() * 1000000));
		mapControl.setCenter(point);
		mapControl.animateTo(point);
		Log.i("map ctivity", String.valueOf(point.getLatitudeE6()) + String.valueOf(point.getLongitudeE6()));
		mapView.setBuiltInZoomControls(true);     
		toCallAsynchronous(mapView);
		AddMapOverlays asyncTask = new AddMapOverlays();
		try {
			asyncTask.execute(mapView);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
		timer.schedule(doAsynchronousTask,0,5000);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		timer.cancel();
		timer.purge();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
		/**
		 * @param mapView
		 * @return List of Overlayitems containing the locations for the organizer, friends and meeting locations.
		 */
		public List<OverlayItem> addMapOverlays(MapView mapView) {
			List<OverlayItem> pointList = new ArrayList<OverlayItem>();
			try {
				String url = Common.URL + "/id=" + Common.MY_ID;
				String obtainedLocations = connectionHelper.getData(url);
				Log.i(TAG, "Locations obtained from backend" + obtainedLocations);
				JSONObject object = new JSONObject(obtainedLocations);
				//Organizer location
				String[] organizerLocation = ((String)object.get("MYLOCATION")).split(",");
				GeoPoint oPoint = new GeoPoint( (int)(Double.parseDouble(organizerLocation[0]) * 1000000) , (int)(Double.parseDouble(organizerLocation[1]) * 1000000));
				OverlayItem oItem = new OverlayItem(oPoint, "Organizer location","Do you need directions to this point");
				oItem.setMarker(drawable);
				pointList.add(oItem);

				//Friends locations
				JSONArray array = object.getJSONArray("FRIENDS");
				for(int i=0;i<array.length();++i) {
					JSONObject obj = (JSONObject) array.get(i);
					String[] tempArr = obj.getString("LOC").split(",");
					String id = obj.getString("PHONE_NUMBER");
					if (tempArr.length > 1) {
						Double tempLat = Double.parseDouble(tempArr[0]);
						Double tempLon = Double.parseDouble(tempArr[1]);
						GeoPoint point = new GeoPoint((int)(tempLat * 1000000) , (int)(tempLon * 1000000));
						OverlayItem fItem = new OverlayItem(point,"Friend: " + id + " Location","Do you need directions to this location");
						fItem.setMarker(drawable);
						pointList.add(fItem);

					} else {
						Log.i(TAG, "Friends location not yet obtained");
					}
				}
				
				//Four square points
				JSONArray fsArray = object.getJSONArray("FSITEMS");
				if(fsArray != null) {
					for(int i=0;i<fsArray.length();++i) {
						JSONObject obj = (JSONObject) fsArray.get(i);
						String lat = obj.getString("lat");
						String lng = obj.getString("lng");
						Double tempLat = Double.parseDouble(lat);
						Double tempLon = Double.parseDouble(lng);
						GeoPoint fsPoint = new GeoPoint((int)(tempLat * 1000000) , (int)(tempLon * 1000000));
						OverlayItem fsItem = new OverlayItem(fsPoint, "Meeting Location", "Do you need directions to this point");
						fsDrawable.setBounds(0, 0, fsDrawable.getIntrinsicWidth(),fsDrawable.getIntrinsicHeight());
						fsItem.setMarker(fsDrawable);
						pointList.add(fsItem);	
					}
				}
				return pointList;
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
				// TODO Auto-generated method stub
				mapOverlays.clear();
				itemizedOverlay.clear();
				Iterator<OverlayItem> iterator = result.iterator();
				while(iterator.hasNext()) {					
					OverlayItem item = iterator.next();			
					itemizedOverlay.addOverlay(item);
				}
				mapOverlays.add(itemizedOverlay);


			} catch(Exception e) {
				e.printStackTrace();
			}
		}

	}

}
