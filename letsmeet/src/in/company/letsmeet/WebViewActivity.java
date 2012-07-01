package in.company.letsmeet;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			String newLine = "";
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.mapus);
			MapView mView = (MapView) findViewById(R.id.mapview);
			List<Overlay> mOverlay = mView.getOverlays();
			MapController controller = mView.getController();
			final String sourceLoc = getIntent().getExtras().getString("SOURCE");
			final String destinationLoc = getIntent().getExtras().getString("DEST");

			String[] sourceArr = sourceLoc.split(",");
			String[] destinationArr = destinationLoc.split(",");

			double sLoc = Double.parseDouble(sourceArr[0]) * 1e6;
			double sLon = Double.parseDouble(sourceArr[1]) * 1e6;
			double dLoc = Double.parseDouble(destinationArr[0]) * 1e6;
			double dLon = Double.parseDouble(destinationArr[1]) * 1e6;

			double centerLat = (sLoc + dLoc) /2;
			double centerLong = (sLon + dLon) /2;

			GeoPoint smGp = new GeoPoint((int)sLoc, (int)sLon);
			GeoPoint dmGp = new GeoPoint((int)dLoc, (int)dLon);

			Drawable sDrawable = getApplicationContext().getResources().getDrawable(R.drawable.marker); 
			Drawable dDrawable = getApplicationContext().getResources().getDrawable(R.drawable.greenicon);

			OverlayItem sItem = new OverlayItem(smGp, "", "");
			OverlayItem dItem = new OverlayItem(dmGp, "", "");

			MapItemizedOverlay<OverlayItem> sOverlay = new MapItemizedOverlay<OverlayItem>(sDrawable, this, mView);
			MapItemizedOverlay<OverlayItem> dOverlay = new MapItemizedOverlay<OverlayItem>(dDrawable, this, mView);
			sOverlay.addOverlay(sItem);
			dOverlay.addOverlay(dItem);
			mOverlay.add(sOverlay);
			mOverlay.add(dOverlay);
			controller.setZoom(15);
			controller.setCenter(new GeoPoint((int)centerLat, (int)centerLong));
			final List<Overlay> newOverlay = mView.getOverlays();
			new Thread( new Runnable() {
				public void run() {
					displayRouteFromLeafLet(sourceLoc, destinationLoc, newOverlay);
				}
			}).start();
			Toast.makeText(getApplicationContext(), "Please tap anywhere on the map to see directions",Toast.LENGTH_LONG).show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void displayRouteFromLeafLet(String sourceLoc, String destinationLoc, List<Overlay> mOverlay) {
		try {
			String url = "http://navigation.cloudmade.com/05de9601467f4e8c9e890a2622541715/api/0.3/";
			String newUrl = url.concat(sourceLoc).concat(",").concat(destinationLoc).concat("/car.js?tId=CloudMade");
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
					MyOverLay mLay = new MyOverLay(sGp,dGp,2,999);
					mOverlay.add(mLay);	
				}

			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

