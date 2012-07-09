package in.company.letsmeet;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * @author pradeep
 * Common class for all the required constants.
 */
public final class Common {
 public static final String URL = "http://ec2-122-248-211-48.ap-southeast-1.compute.amazonaws.com:8888";
 public static String MY_ID;
 public static final String SINGLE_USER_ID = "1111";
 public static final String SINGLE_USER_FRIEND_ID = "55555";
 public static boolean friend;
 public static boolean confirm;
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

private static Location currentLocation;
 public static LocationManager locationManager;
 public static Context context;
 public static final String DIRECTIONS_URL = "http://maps.googleapis.com/maps/api/directions/json?";
 public static final String FOURSQUARE_URL = "https://api.foursquare.com/v2/venues/explore?client_id=N3RMDIQFPLHPLTJMRKLNV4ULXJCXWOPY3HZ2EOMXBSJWU1SW&client_secret=EQKC52S2W5RP1N2CQYQ2CPM1H55PISXUE41UIG55LEJQSDTY&section=coffee&oauth_token=4ENF4MW3PJMMUPS5D5FJC1OP5WXRVF2FFAZCMFG1PDSLBRAH&v=20120516&limit=4&intent=browse&radius=2000&ll=";
 
 public static void setLocation(Location location) {
	 currentLocation = location;
 }
 
 public static Location getLocation() {
	 return currentLocation;
 }
 
}
