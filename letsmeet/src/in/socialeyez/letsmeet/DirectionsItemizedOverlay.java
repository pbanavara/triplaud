package in.socialeyez.letsmeet;

import in.socialeyez.letsmeet.common.HttpConnectionHelper;
import in.socialeyez.letsmeet.locationutil.BestLocationFinder;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
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
public class DirectionsItemizedOverlay<Item> extends ItemizedOverlay<OverlayItem> {
	private static final String TAG = "MapItemizedOverlay";
	private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
	private HttpConnectionHelper helper;
	private Context context;
	private GeoPoint tappedPoint;
	private String title;
	private String organizer;
	private String markerId;


	public DirectionsItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}

	public DirectionsItemizedOverlay(Drawable defaultMarker, Context context, MapView mapView) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;


		// Workaround for bug that Google refuses to fix:
		// <a href="http://osdir.com/ml/AndroidDevelopers/2009-08/msg01605.html">http://osdir.com/ml/AndroidDevelopers/2009-08/msg01605.html</a>
		// <a href="http://code.google.com/p/android/issues/detail?id=2035">http://code.google.com/p/android/issues/detail?id=2035</a>
		populate();

	}



	public DirectionsItemizedOverlay(Drawable defaultMarker, Context context, View view) {
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
		// TODO Auto-generated method stub
		OverlayItem item = items.get(index);
			Log.e(TAG, "Marker tapped");
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setCancelable(false);
			title = item.getTitle();
			dialog.setTitle(title);
			dialog.setMessage(item.getSnippet());
			dialog.setPositiveButton("YES", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int id) {
						
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

}

