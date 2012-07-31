package in.company.letsmeet.common;

import in.company.letsmeet.TrackerPoint;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

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
	public static boolean confirm = false;
	// public static List<GeoPoint> friendList;
	public static HashMap<String,TrackerPoint> friendMap;
	private static Location currentLocation;
	public static LocationManager locationManager;
	public static Context context;
	public static String FRIEND_ID;
	public static final String DIRECTIONS_URL =  "http://navigation.cloudmade.com/05de9601467f4e8c9e890a2622541715/api/0.3/";
	public static final String FOURSQUARE_URL = "https://api.foursquare.com/v2/venues/explore?client_id=N3RMDIQFPLHPLTJMRKLNV4ULXJCXWOPY3HZ2EOMXBSJWU1SW&client_secret=EQKC52S2W5RP1N2CQYQ2CPM1H55PISXUE41UIG55LEJQSDTY&section=coffee&oauth_token=4ENF4MW3PJMMUPS5D5FJC1OP5WXRVF2FFAZCMFG1PDSLBRAH&v=20120516&limit=4&intent=browse&radius=2000&ll=";
	
	//Parse update frequency in milliseconds
	public static final long UPDATE_PARSE_FREQUENCY = 120000;

	public static final long UPDATE_MAP_FREQUENCY = 5000;
	public static final String STORE_FILE_NAME = "mytriphistory.txt";
	public static final long AlARM_INTERVAL = 1 * 60 * 1000;
	public static final int START_CONTACT_LIST = 2;
	public static final int START_LETSMEET = 1;
	public static final int CANCEL_ALL = 111;

	private static String addressLocationName;
	private static String addressLocationLatLng;
	
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

	public static String getAddressLocationName() {
		return addressLocationName;
	}

	public static void setAddressLocationName(String addressLocationName) {
		Common.addressLocationName = addressLocationName;
	}

	public static String getAddressLocationLatLng() {
		return addressLocationLatLng;
	}

	public static void setAddressLocationLatLng(String addressLocationLatLng) {
		Common.addressLocationLatLng = addressLocationLatLng;
	}

	public static void setDestinationTime(Calendar setCal) {
		// TODO Auto-generated method stub
		Common.destinationCalendar = setCal;
		
	}
	
	public static Calendar getDestinationTime() {
		return Common.destinationCalendar;
	}
	
	private static Calendar destinationCalendar;
	
	public static String getSelectedRestaurantType() {
		return selectedRestaurantType;
	}

	public static void setSelectedRestaurantType(String selectedRestaurantType) {
		Common.selectedRestaurantType = selectedRestaurantType;
	}

	private static String selectedRestaurantType;
	
	
}
