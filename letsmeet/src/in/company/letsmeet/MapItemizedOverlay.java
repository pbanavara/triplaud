package in.company.letsmeet;

import in.company.letsmeet.locationutil.BestLocationFinder;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
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
	private Context context;
	private GeoPoint tappedPoint;
	
	public MapItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		// TODO Auto-generated method stub
		return super.draw(canvas, mapView, shadow, when);
	}

	public MapItemizedOverlay(Drawable defaultMarker, Context context) {
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
		// TODO Auto-generated method stub
		super.onTap(index);
		
		OverlayItem item = items.get(index);
		
		GeoPoint point = item.getPoint();
		
		/*
		 * Directions Logic
		 * Source = BestLocationFinder.getLastBestLocation()
		 * Destination = point
		 * Call leaflet with Source and dest
		 * Returns an array of intermediate points.
		 * Loop through the array {
		 *  get intsource, intdest
		 *  set classvariables gp1 to intsource, gp2 to intdest
		 *  call Draw()
		 * 
		 */
		
		/*
		 * Tap selection transmission
		 * Move foursquare to the backend.
		 * 
		 */
		this.tappedPoint = point;
		Log.e(TAG, "Marker tapped");
		
		 AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		 dialog.setCancelable(true);
		 dialog.setPositiveButton("YES", new OkOnClickListener());
		 dialog.setNegativeButton("NO", new CancelOnClickListener());
		 String snippet = item.getSnippet();
		 String title = item.getTitle();
		 dialog.setTitle(title);
		 dialog.setMessage(snippet);
		 dialog.show();
		 return true;
	}
	
	private final class OkOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			try{
				//Push the current location to the back-end server as a JSON object.
					BestLocationFinder finder = new BestLocationFinder(context);
					Location loc = finder.getLastBestLocation(System.currentTimeMillis());
					double latitude = tappedPoint.getLatitudeE6() / 1E6;
					double longitude = tappedPoint.getLongitudeE6() / 1E6;

					String sourceLoc = String.valueOf(loc.getLatitude()) + "," + String.valueOf(loc.getLongitude());
					String destinationLoc = String.valueOf(latitude) + "," + String.valueOf(longitude);
					Intent intent = new Intent(context, WebViewActivity.class);
					intent.putExtra("SOURCE", sourceLoc);
					intent.putExtra("DEST", destinationLoc);
					context.startActivity(intent);
					
			} catch(Exception e){
				e.printStackTrace();
			}

		}
	}

	private final class CancelOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			
		}
	}
	
	public void clear() {
		items.clear();
		setLastFocusedIndex(-1);
		populate();
	}

	

}
