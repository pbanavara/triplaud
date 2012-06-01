package in.company.letsmeet;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;


/*
 * Location listener implementation, upload data to parse every time the listener is invoked.
 */
public class LocListener implements LocationListener {
	
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		try{
			Common.setLocation(new String(String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude())));
			Log.i("LocListener",Common.getLocation());	
			Common.locationManager.removeUpdates(this);
			Common.locationManager = null;
		} catch(Exception e) {
			e.printStackTrace();
		}

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
		

	}

}
