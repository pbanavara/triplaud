package in.company.letsmeet;


import in.company.letsmeet.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LetsMeetActivity extends Activity {
	private LocationListener listener;
	private ProgressDialog pDialog;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		startLocationUpdates();
		try {
		while(Common.currentLocation != null) {
			Thread.sleep(10000);
		}
		} catch(Exception e) {
			e.printStackTrace();
		}
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
				Common.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
			} else if(Common.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				Common.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}