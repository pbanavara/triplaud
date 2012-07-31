package in.company.letsmeet.alarm;

import in.company.letsmeet.common.Common;
import in.company.letsmeet.common.HttpConnectionHelper;
import in.company.letsmeet.locationutil.BestLocationFinder;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "ALARMRECEIVER";
	@Override
	public void onReceive(Context context, Intent intent) {
		try{
			// TODO Auto-generated method stub
			Log.d("ALarm receiver", "alarm called");
			BestLocationFinder finder = new BestLocationFinder(context, LocationManager.NETWORK_PROVIDER, false);
			finder.getBestLocation(System.currentTimeMillis(), 0);

			Location sourceLocation = Common.getLocation();
			Double sourceLat = sourceLocation.getLatitude();
			Double sourceLng = sourceLocation.getLongitude();

			String destinationLocation = Common.getAddressLocationLatLng();
			
			if(destinationLocation != null) {
				String newUrl = Common.DIRECTIONS_URL.concat(String.valueOf(sourceLat)).concat(",").concat(String.valueOf(sourceLng)).concat(",").concat(destinationLocation).concat("/car.js?tId=CloudMade");
				Log.i(TAG, "Distance URL" + newUrl);
				HttpConnectionHelper helper = new HttpConnectionHelper();
				String newLine = helper.getData(newUrl);
				JSONObject jMapData = new JSONObject(newLine);
				JSONArray routes = jMapData.getJSONArray("route_geometry");
				JSONObject routeSummary = jMapData.getJSONObject("route_summary");
				int distance = routeSummary.getInt("total_distance");
				Log.i(TAG, "DISTANCE:::" + distance);
				int averageSpeed = (20000/3600);
				long requiredTime = ( distance/averageSpeed ) * 1000;
				Calendar destinationDate = Common.getDestinationTime();
				long destinationTime = destinationDate.getTimeInMillis() - (24* 3600 *1000);
				Calendar today = Calendar.getInstance();
				long currentTime = today.getTimeInMillis();

				if ((destinationTime - currentTime ) <= (requiredTime - (Common.AlARM_INTERVAL))){
					Intent intentStartWeb = new Intent(context, AlarmActivity.class);
					String friendId = intent.getExtras().getString("FROMFRIEND");
					intentStartWeb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intentStartWeb.putExtra("FROMFRIEND",friendId);
					context.startActivity(intentStartWeb);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
