package in.socialeyez.letsmeet;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import in.socialeyez.letsmeet.R;
import in.socialeyez.letsmeet.common.Common;
import in.socialeyez.letsmeet.common.Writer;
import in.socialeyez.letsmeet.locationutil.BestLocationFinder;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class AloneMapActivity extends MapActivity {
	private Drawable drawable ;
	private Drawable fsDrawable;
	private MapView mapView;
	private MapItemizedOverlay<?> itemizedOverlay;
	private List<Overlay> mapOverlays;
	
	private MapController mapControl;
	private Context context;

	private String displayMessage;
	private Writer writer;
	
	@Override
	protected void onCreate(Bundle bundle) {
		BestLocationFinder finder = new BestLocationFinder(getApplicationContext(), LocationManager.NETWORK_PROVIDER, false);
		finder.getBestLocation(System.currentTimeMillis(), 0);
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
		super.onCreate(bundle);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
