package in.company.letsmeet;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class CommonMapActivity extends MapActivity{
	
	private static final String TAG = "CommonMapActivity";
	private Drawable drawable ;
	private MapItemizedOverlay<?> itemizedOverlay;
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

	@Override
	protected void onCreate(Bundle bundle) {
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		drawable = this.getResources().getDrawable(R.drawable.purpleicon);
		itemizedOverlay = new MapItemizedOverlay<Object>(drawable, this);
		setContentView(R.layout.mapus);
		MapView mapView = (MapView) findViewById(R.id.mapview);
		mapView.buildDrawingCache(true);
		MapController mapControl = mapView.getController();
		mapControl.setZoom(12);
		GeoPoint point = new GeoPoint((int) (13.0392518 * 1000000),(int) (77.554774 * 1000000));
		mapControl.setCenter(point);
		Log.i("map ctivity", String.valueOf(point.getLatitudeE6()) + String.valueOf(point.getLongitudeE6()));
	    mapView.setBuiltInZoomControls(true);    
	    //String markerLocations = getIntent().getExtras().getString("locations"); 
	    new AddMapOverlays().execute(mapView);  
	   
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	private class AddMapOverlays extends AsyncTask<MapView, Void, Void>{
		
		private static final String TAG = "AddMapOverlays";
		
		public void addMapOverlays(MapView mapView) {
			try {
				List<Overlay> mapOverlays = mapView.getOverlays();
				Drawable drawable = getApplicationContext().getResources().getDrawable(R.drawable.purpleicon);
				String obtainedLocations = connectionHelper.getData(Common.URL);
				Log.i(TAG, "Locations obtained from backend" + obtainedLocations);
				JSONObject object = new JSONObject(obtainedLocations);
				String[] organizerLocation = ((String)object.get("MYLOCATION")).split(",");
				GeoPoint oPoint = new GeoPoint( (int)(Double.parseDouble(organizerLocation[0]) * 1000000) , (int)(Double.parseDouble(organizerLocation[1]) * 1000000));
				OverlayItem oItem = new OverlayItem(oPoint, "Restaurant","");
				itemizedOverlay.addOverlay(oItem);
				JSONArray array = object.getJSONArray("FRIENDS");
				for(int i=0;i<array.length();++i) {
					JSONObject obj = (JSONObject) array.get(i);
					String[] tempArr = obj.getString("LOC").split(",");
					if (tempArr.length > 1) {
						GeoPoint point = new GeoPoint( (int)(Double.parseDouble(tempArr[0]) * 1000000) , (int)(Double.parseDouble(tempArr[1]) * 1000000));
						
						OverlayItem item = new OverlayItem(point, "Restaurant","");
						itemizedOverlay.addOverlay(item);
					} else {
						Log.i(TAG, "Friends location not yet obtained");
					}
				}	
				mapOverlays.add(itemizedOverlay);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		@Override
		protected Void doInBackground(MapView... params) {
			// TODO Auto-generated method stub
			try {
			for (int count = 0;count<1000; ++count) {
				Log.i(TAG, "Background process");
				for(int i = 0; i<params.length;++i) {
					addMapOverlays(params[i]);
				}
				Thread.sleep(1000);
				
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
			return null;
			
		}
		
	}
	
	
}
