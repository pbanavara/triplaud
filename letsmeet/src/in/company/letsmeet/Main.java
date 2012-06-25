package in.company.letsmeet;

import in.company.letsmeet.locationutil.BestLocationFinder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * @author pradeep
 *
 */
public class Main extends Activity {
	
	private Location location;
	private BestLocationFinder finder;
	private static final String TAG = "Main";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Common.context = this;
		setContentView(R.layout.main);
		finder = new BestLocationFinder(getApplicationContext());
		location = finder.getLastBestLocation(System.currentTimeMillis());
		if (location != null) {
			Log.i(TAG, "Location update" + location.getLatitude() + location.getLongitude());
		} else {
			Log.i(TAG, "Location is null");
			this.finish();
		}
	}
	
	public void openLetsMeet(View view) {
		Intent intent = new Intent(this, LetsMeetActivity.class);
		startActivity(intent);
	}
	
	public void dismissApp(View view) {
		this.finish();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		finder = new BestLocationFinder(getApplicationContext());
		location = finder.getLastBestLocation(System.currentTimeMillis());
		
	}
	
	
}
