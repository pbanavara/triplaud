package in.company.letsmeet;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.maps.GeoPoint;

/**
 * @author pradeep
 * Common class for all the required constants.
 */
public final class Common {
 public static final String URL = "http://ec2-122-248-211-48.ap-southeast-1.compute.amazonaws.com:8889";
 public static String MY_ID;
 public static final String SINGLE_USER_ID = "1111";
 public static final String SINGLE_USER_FRIEND_ID = "55555";
 public static boolean friend;
 public static boolean confirm;
// public static List<GeoPoint> friendList;
 public static HashMap<String,TrackerPoint> friendMap;
 private static Location currentLocation;
 public static LocationManager locationManager;
 public static Context context;
 public static String FRIEND_ID;
 public static final String DIRECTIONS_URL =  "http://navigation.cloudmade.com/05de9601467f4e8c9e890a2622541715/api/0.3/";
 public static final String FOURSQUARE_URL = "https://api.foursquare.com/v2/venues/explore?client_id=N3RMDIQFPLHPLTJMRKLNV4ULXJCXWOPY3HZ2EOMXBSJWU1SW&client_secret=EQKC52S2W5RP1N2CQYQ2CPM1H55PISXUE41UIG55LEJQSDTY&section=coffee&oauth_token=4ENF4MW3PJMMUPS5D5FJC1OP5WXRVF2FFAZCMFG1PDSLBRAH&v=20120516&limit=4&intent=browse&radius=2000&ll=";

 //Parse update frequency in milliseconds
 protected static final long UPDATE_PARSE_FREQUENCY = 120000;
 
 public static void setLocation(Location location) {
	 currentLocation = location;
 }
 
 public static Location getLocation() {
	 return currentLocation;
 }
 public static boolean isConfirm() {
	return confirm;
}

public static void setConfirm(boolean confirm) {
	Common.confirm = confirm;
}


 
 public static boolean isFriend() {
	return friend;
}

public static void setFriend(boolean isFriend) {
	Common.friend = isFriend;
}
}
