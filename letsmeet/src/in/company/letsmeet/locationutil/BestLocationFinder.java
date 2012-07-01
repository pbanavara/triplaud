package in.company.letsmeet.locationutil;

import in.company.letsmeet.Common;

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
	  public Location bestResult;
	  
	  public BestLocationFinder(Context context) {
		  locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		  criteria = new Criteria();
		  criteria.setPowerRequirement(Criteria.POWER_LOW);
		  criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		  this.context = context;
	  }
	  
	  public void getBestLocation(long minTime) {
		  String provider = (locationManager.getProvider(LocationManager.NETWORK_PROVIDER)).getName();
		     Location location = locationManager.getLastKnownLocation(provider);
		      if (location != null) {
		       
		        long time = location.getTime();
		        if (time < minTime) {		          
		          locationManager.requestLocationUpdates(provider, 0, 0, singeUpdateListener, context.getMainLooper());
		          Log.i(TAG, "Old update");
		        }
		        else if (time >= minTime) {
		          Common.setLocation(location);
		        }
		      } 
		      
	  }
	  
	  public Location getBestResult() {
		  return this.bestResult;
	  }

	protected LocationListener singeUpdateListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	      Log.d(TAG, "Location Update Received: " + location.getLatitude() + "," + location.getLongitude());
	      Common.setLocation(location);
	      locationManager.removeUpdates(this);
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
