package in.company.letsmeet;

import in.company.letsmeet.locationutil.BestLocationFinder;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.webkit.WebView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MapItemizedOverlay<Item> extends ItemizedOverlay<OverlayItem> {
	private static final String TAG = "MapItemizedOverlay";
	private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
	private Context context;
	private GeoPoint tappedPoint;
	public MapItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}
	
	public MapItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
		// TODO Auto-generated constructor stub
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
	    populate();
	}
	
	@Override
	public boolean onTap(int index) {
		// TODO Auto-generated method stub
		super.onTap(index);
		OverlayItem item = items.get(index);
		GeoPoint point = item.getPoint();
		this.tappedPoint = point;
		Log.e(TAG, "Marker tapped");
		
		 AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		 dialog.setCancelable(true);
		 dialog.setPositiveButton("YES", new OkOnClickListener());
		 dialog.setNegativeButton("NO", new CancelOnClickListener());
		 dialog.setTitle("Do you want directions to this point");
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

					String sourceLoc = String.valueOf(loc.getLatitude()) + ":" + String.valueOf(loc.getLongitude());
					String destinationLoc = String.valueOf(latitude) + ":" + String.valueOf(longitude);
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
	

}
