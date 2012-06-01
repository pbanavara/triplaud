package in.company.letsmeet;


import in.company.letsmeet.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LetsMeetActivity extends Activity implements LocationListener{
	private LocationListener listener;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		startLocationUpdates();
		Spinner spin = (Spinner)findViewById(R.id.spinner1);
		String[] occasions = {"A drink", "Coffee", "Lunch", "Dinner"};
		ArrayAdapter ap = new ArrayAdapter(this, android.R.layout.simple_spinner_item,occasions);
		ap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(ap);
		
	}

	public void selectContacts(View v) {
		Intent intent = new Intent(this, ContactsListActivity.class);
		startActivity(intent);
	}

	public void startLocationUpdates() {
		try{
			Common.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			
			listener = new LocListener();
			if(Common.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Common.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			} else if(Common.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				/*
				Location location = Common.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				String tempLoc = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
				Common.setLocation(tempLoc);
				*/
				Common.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}