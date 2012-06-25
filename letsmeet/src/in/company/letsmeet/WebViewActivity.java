package in.company.letsmeet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

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

			String sourceLoc = getIntent().getExtras().getString("SOURCE");
			String destinationLoc = getIntent().getExtras().getString("DEST");
			String url = "http://navigation.cloudmade.com/05de9601467f4e8c9e890a2622541715/api/0.3/";
			String newUrl = url.concat(sourceLoc).concat(",").concat(destinationLoc).concat("/car.js?tId=CloudMade");
			//String newUrl = "http://navigation.cloudmade.com/05de9601467f4e8c9e890a2622541715/api/0.3/13.038924999999999,77.555035,12.9273097,77.5862775/car.js?tId=CloudMade";
			HttpConnectionHelper helper = new HttpConnectionHelper();
			newLine = helper.getData(newUrl);
			/*
			InputStream is = null;
			HttpURLConnection urlConnection = null;
			URL finalUrl = new URL(newUrl);
			urlConnection = (HttpURLConnection) finalUrl.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			is = urlConnection.getInputStream();
			urlConnection.connect();

			InputStreamReader inputStream = new InputStreamReader(is);
			BufferedReader r = new BufferedReader(inputStream);
			String line = "";
			while ((line = r.readLine()) != null) {
				newLine = newLine.concat(line);
			}
			*/
			JSONObject jMapData = new JSONObject(newLine);
			JSONArray routes = jMapData.getJSONArray("route_geometry");
			for (int i=0;i<routes.length();++i) {
				JSONArray startLocation = routes.getJSONArray(i);
				String ssLat = startLocation.getString(0);
				String ssLon = startLocation.getString(1);
				if(i< routes.length()) {
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

		} catch(Exception e) {
			e.printStackTrace();
		}

		/*
		WebView wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		String url = "file:///android_asset/route.html?source=" + sourceLoc + "&destination=" + destinationLoc;	
		Log.e(TAG, "URL" + url);
		wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});
		wv.loadUrl(url);
		 */
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
