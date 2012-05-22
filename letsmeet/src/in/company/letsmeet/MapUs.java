package in.company.letsmeet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class MapUs extends Activity {
	
	//Read the List that is populated with return addresses and plot the points as and when they are populated
	private WebView wv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapus);
		String message = getIntent().getExtras().getString("message");
		String baseUrl = "http://ec2-122-248-211-48.ap-southeast-1.compute.amazonaws.com:8080/temp.html";
		String newUrl = baseUrl + "?mylocation=" + getGpsData(getApplicationContext()) +"&locations=" +message;
		Log.i("MAP US url", newUrl);
		wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl(newUrl);
	}
	
	private List constructTestContacts() {
		List contactsWithLocations = new ArrayList<Contacts>();
		Contacts contact = new Contacts();
		contact.setPhoneNumber("12323");
		contact.setLocation("12.928828333333335,77.58171666666667");
		contactsWithLocations.add(contact);
		return contactsWithLocations;
	}
	
	public String getGpsData(Context context) {
		String latlon = "";
		try {
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			List<String> providers = locationManager.getAllProviders();
			// Add code to check if GPS is on and if it's not, provide a popup with the message 
			// "Can we turn on and turn off the GPS just to get your location"

			if(!providers.isEmpty()) {
				if(locationManager.isProviderEnabled(locationManager.GPS_PROVIDER) ){
					Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
					double latitude = location.getLatitude();
					double longitude = location.getLongitude();
					latlon = new String(String.valueOf(latitude) + "," + String.valueOf(longitude));
				} else {
					//buildAlertMessageNoGps();
					latlon = "NO GPS";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return latlon;
	}
	

}
