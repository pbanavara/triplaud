package in.company.letsmeet;

import java.util.Random;

import android.location.LocationManager;

public final class Common {
 public static final String URL = "http://ec2-122-248-211-48.ap-southeast-1.compute.amazonaws.com:8888";
 public static final String MY_ID = String.valueOf(new Random().nextInt());
 public static String currentLocation ;
 public static LocationManager locationManager;
 
}
