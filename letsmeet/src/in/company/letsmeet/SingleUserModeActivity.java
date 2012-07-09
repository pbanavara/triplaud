package in.company.letsmeet;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SingleUserModeActivity extends Activity {
	private static final String TAG="Single User mode";
	private String id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.singleuser);
		id = getIntent().getExtras().getString("id");
		
	}
	
	public void dismiss(View v) {
		this.finish();
	}
	
	public void simulate(View v) {
		Location location = Common.getLocation();
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		double newLat = lat + (lat * (10/ 111));
		double tempLng = 10 * ( 1 / (110 * Math.cos(lat)));
		double newLng = lng + tempLng;
		String newLocation = newLat + "," + newLng;
		Log.i(TAG, "Friend location" + newLocation);
		HttpConnectionHelper helper = new HttpConnectionHelper();
		JSONObject obj = new JSONObject();
		try{
			obj.put("id", id);
			obj.put("loc", newLocation);
			helper.postData(Common.URL + "/id=" + Common.MY_ID, obj);
			Intent intent = new Intent(this, CommonMapActivity.class);
			intent.putExtra("singleusermode", true);
			startActivity(intent);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
