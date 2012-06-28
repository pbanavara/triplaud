package in.company.letsmeet.locationutil;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class BestLocationFinder {
	
	protected static String TAG = "BestLocationFinder";
	  
	  protected LocationListener locationListener;
	  protected LocationManager locationManager;
	  protected Criteria criteria;
	  protected Context context;
	  
	  public BestLocationFinder(Context context) {
		  locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		  criteria = new Criteria();
		  criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		  this.context = context;
	  }
	  
	  public Location getLastBestLocation(long minTime) {
		    Location bestResult = null;
		    float bestAccuracy = Float.MAX_VALUE;
		    long bestTime = Long.MAX_VALUE;
		    
		    // Iterate through all the providers on the system, keeping
		    // note of the most accurate result within the acceptable time limit.
		    // If no result is found within maxTime, return the newest Location.
		    List<String> matchingProviders = locationManager.getAllProviders();
		    for (String provider: matchingProviders) {
		      Location location = locationManager.getLastKnownLocation(provider);
		      if (location != null) {
		        float accuracy = location.getAccuracy();
		        long time = location.getTime();
		        
		        if ((time < minTime && accuracy < bestAccuracy)) {
		          bestResult = location;
		          bestAccuracy = accuracy;
		          bestTime = time;
		          Log.i(TAG, "Old update");
		        }
		        else if (time > minTime && bestAccuracy == Float.MAX_VALUE && time < bestTime) {
		          bestResult = location;
		          bestTime = time;
		        }
		      }
		    }
		    if (locationListener != null && (bestTime > minTime)) { 
		        String provider = locationManager.getBestProvider(criteria, true);
		        if (provider != null)
		          locationManager.requestLocationUpdates(provider, 0, 0, singeUpdateListener, context.getMainLooper());
		      }
		      
		      return bestResult;
	  }

	protected LocationListener singeUpdateListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	      Log.d(TAG, "Single Location Update Received: " + location.getLatitude() + "," + location.getLongitude());
	      if (locationListener != null && location != null)
	        locationListener.onLocationChanged(location);
	      locationManager.removeUpdates(singeUpdateListener);
	    }
	    
	    public void onProviderEnabled(String provider) {}    
	    public void onProviderDisabled(String provider) {}
	
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	};
  
  /**
   * {@inheritDoc}
   */
  public void setChangedLocationListener(LocationListener l) {
    locationListener = l;
  }
  
  /**
   * {@inheritDoc}
   */
  public void cancel() {
    locationManager.removeUpdates(singeUpdateListener);
  }

}
