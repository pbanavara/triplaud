package in.company.letsmeet;

import in.company.letsmeet.locationutil.BestLocationFinder;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author pradeep
 *
 * @param <Item>
 * Customization of ItemizedOerlay to populate icons. The implementation has some known bugs.
 * Refer to this URL on how these are rectified.
 * http://developmentality.wordpress.com/2009/10/19/android-itemizedoverlay-arrayindexoutofboundsexception-nullpointerexception-workarounds/#comment-815
 */
public class MapItemizedOverlay<Item> extends ItemizedOverlay<OverlayItem> {
	private static final String TAG = "MapItemizedOverlay";
	private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
	private HttpConnectionHelper helper;
	private Context context;
	private GeoPoint tappedPoint;
	private String title;
	private String organizer;
	private String markerId;


	public MapItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}

	public MapItemizedOverlay(Drawable defaultMarker, Context context, MapView mapView) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;


		// Workaround for bug that Google refuses to fix:
		// <a href="http://osdir.com/ml/AndroidDevelopers/2009-08/msg01605.html">http://osdir.com/ml/AndroidDevelopers/2009-08/msg01605.html</a>
		// <a href="http://code.google.com/p/android/issues/detail?id=2035">http://code.google.com/p/android/issues/detail?id=2035</a>
		populate();

	}



	public MapItemizedOverlay(Drawable defaultMarker, Context context, View view) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
		// Workaround for bug that Google refuses to fix:
		// <a href="http://osdir.com/ml/AndroidDevelopers/2009-08/msg01605.html">http://osdir.com/ml/AndroidDevelopers/2009-08/msg01605.html</a>
		// <a href="http://code.google.com/p/android/issues/detail?id=2035">http://code.google.com/p/android/issues/detail?id=2035</a>
		populate();

	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return items.get(i);
	}


	@Override
	public int size() {
		// TODO Auto-generated method stub
		return items.size();
	}

	public void addOverlay(OverlayItem overlay) {
		items.add(overlay);
		setLastFocusedIndex(-1);
		populate();
	}

	@Override
	public boolean onTap(int index) {
		super.onTap(index);
		
		String friendMessage = "Do you like this place";
		String orgMessage = "Do you want to confirm this location and get directions";
		// TODO Auto-generated method stub
		OverlayItem item = items.get(index);
		GeoPoint point = item.getPoint();
		String message = item.getSnippet();
		String[] organizerArr = message.split(":");
		organizer = organizerArr[0];
		markerId  = organizerArr[1];
		this.tappedPoint = point;
		Log.e(TAG, "Marker tapped");
			
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setCancelable(true);
			title = item.getTitle();
			dialog.setTitle(title);
			if (Common.isFriend() == true) {
				dialog.setMessage(friendMessage);
			} else {
				dialog.setMessage(orgMessage);
			}
			if (Common.isConfirm() == true){
				dialog.setMessage("Your organzier has confirmed this location. Need directions ?");
			}
			dialog.setPositiveButton("YES", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id) {
					try{
						Location location = Common.getLocation();
						if(location == null) {
							BestLocationFinder finder = new BestLocationFinder(context);
							finder.getBestLocation(System.currentTimeMillis());
						}

						String sourceLoc = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
						String destLoc = String.valueOf(tappedPoint.getLatitudeE6() / 1E6) + "," + String.valueOf(tappedPoint.getLongitudeE6() / 1E6);	
						Intent intent = new Intent(context,WebViewActivity.class);
						intent.putExtra("SOURCE", sourceLoc);
						intent.putExtra("DEST", destLoc);
						Log.e(TAG, "Location Finalized");
						if(Common.isFriend() == true && Common.isConfirm() == false) {
							postMarkerData("maybe");
						} else {
							postMarkerData("yes");
							context.startActivity(intent);
						}
						

					} catch(Exception e){
						e.printStackTrace();
					}
				}
			});
			dialog.setNegativeButton("No", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id) {
					if(Common.isFriend() == false) {
						postMarkerData("maybe");
					}
				}
			});
			dialog.show();
			//postMarkerData("maybe");
		return true;
	}

	public void clear() {
		items.clear();
		setLastFocusedIndex(-1);
		populate();
	}

	public void postMarkerData(String selected) {
		try {
			helper = new HttpConnectionHelper();
			JSONObject obj = new JSONObject();
			obj.put("selected",selected);
			helper.postData(Common.URL + "/id=" + markerId + "&organizer=" + organizer, obj);
			Log.i("Inside MapItemized Handler onTap" , "data sent");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

