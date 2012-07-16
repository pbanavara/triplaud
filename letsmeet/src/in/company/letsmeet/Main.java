package in.company.letsmeet;

import in.company.letsmeet.locationutil.BestLocationFinder;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author pradeep
 *
 */
public class Main extends Activity implements OnClickListener{
	//private FrequentLocationListener listener;
	private static final String TAG = "Main";
	private Context context;
	private Button yButton;
	private Button nButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.context = getApplicationContext();
		BestLocationFinder finder = new BestLocationFinder(getApplicationContext(), LocationManager.NETWORK_PROVIDER,0,false);
		finder.getBestLocation(System.currentTimeMillis());
		Common.MY_ID = String.valueOf(new Random().nextInt(Integer.MAX_VALUE) +1);
		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		Common.context = this;
		setContentView(R.layout.main);
		yButton = (Button) findViewById(R.id.mButtonYes);
		yButton.setOnClickListener(this);
		nButton = (Button) findViewById(R.id.mButtonNo);
		nButton.setOnClickListener(this);
	}
		
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
			
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//locationManager.removeUpdates(listener);
	}

	@Override
	public void onClick(View v) {
		boolean singleUserModeFlag = false;
		if(v.getId() == R.id.mButtonYes) {
			Intent intent = new Intent(this, LetsMeetActivity.class);
			startActivity(intent);
		} else if(v.getId() == R.id.mButtonNo) {
			singleUserModeFlag = true;
			yButton.setEnabled(false);
			Intent intent = new Intent(this, SingleUserModeActivity.class);
			JSONObject finalObject = null;
			try {
				finalObject = new JSONObject();
				Location location = Common.getLocation();
				JSONObject newContact = new JSONObject();
				newContact.put("NAME", "name");
				newContact.put("PHONE_NUMBER", Common.SINGLE_USER_FRIEND_ID);
				newContact.put("LOC","");
				JSONArray selectedContacts = new JSONArray();
				selectedContacts.put(newContact);
				String newLoc = location.getLatitude() + "," + location.getLongitude();
				finalObject.put("MYLOCATION", newLoc);
				finalObject.put("FRIENDS", selectedContacts);
				finalObject.put("MYID", Common.MY_ID);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			intent.putExtra("singleusermode", singleUserModeFlag);
			intent.putExtra("id", Common.SINGLE_USER_FRIEND_ID);
			HttpConnectionHelper helper = new HttpConnectionHelper();
			helper.postData(Common.URL + "/id=" + Common.MY_ID, finalObject);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			startActivity(intent);
		}
}
}