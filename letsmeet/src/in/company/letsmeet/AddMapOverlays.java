package in.company.letsmeet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class AddMapOverlays extends AsyncTask<MapView, Void, Void>{
	private Context context;
	private static final String TAG = "AddMapOverlays";
	
	public AddMapOverlays(Context context) {
		this.context = context;
	}
	
	public void addMapOverlays(MapView mapView) {
		try {
			List<Overlay> mapOverlays = mapView.getOverlays();
			Drawable drawable = this.context.getResources().getDrawable(R.drawable.purpleicon);
			MapItemizedOverlay<?> itemizedOverlay = new MapItemizedOverlay<Object>(drawable, this.context);
			HttpConnectionHelper connectionHelper = new HttpConnectionHelper();
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
		for(int i = 0; i<params.length;++i) {
			addMapOverlays(params[i]);
		}
		return null;
	}

}
