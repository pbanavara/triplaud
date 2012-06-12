package in.company.letsmeet.locationutil;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;

public abstract class LocationUpdater {
	
	protected LocationManager locationManager;
	
	protected LocationUpdater(LocationManager locationManager) {
		this.locationManager = locationManager;
	}
	
	public void requestPassiveLocationUpdates(long minTime, long minDistance, PendingIntent pendingIntent) {
		
	}
	
	public void requestLocationUpdates(long minTime, long minDistance, Criteria criteria, PendingIntent pendingIntent) {
		
	}

}
