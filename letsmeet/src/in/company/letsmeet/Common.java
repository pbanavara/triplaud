package in.company.letsmeet;

import java.util.Random;

import android.content.Context;
import android.location.LocationManager;

public final class Common {
 public static final String URL = "http://ec2-122-248-211-48.ap-southeast-1.compute.amazonaws.com:8888";
 public static final String MY_ID = String.valueOf(new Random().nextInt());
 private static String currentLocation ;
 public static LocationManager locationManager;
 public static Context context;
 public static final String DIRECTIONS_URL = "http://maps.googleapis.com/maps/api/directions/json?";
 
 public static void setLocation(String location) {
	 currentLocation = location;
 }
 
 public static String getLocation() {
	 return currentLocation;
 }
 
}
