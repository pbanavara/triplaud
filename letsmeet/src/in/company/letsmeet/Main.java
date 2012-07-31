package in.company.letsmeet;

import in.company.letsmeet.common.Common;
import in.company.letsmeet.common.HttpConnectionHelper;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author pradeep
 *
 */
public class Main extends Activity implements OnClickListener {
	//private FrequentLocationListener listener;
	private static final String TAG = "Main";

	private Context context;
	
	private Button yButton;
	private Button nButton;
	
	private Thread splashThread;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.context = getApplicationContext();

		BestLocationFinder finder = new BestLocationFinder(getApplicationContext(), LocationManager.NETWORK_PROVIDER,false);
		finder.getBestLocation(System.currentTimeMillis(),0);
		Common.MY_ID = String.valueOf(new Random().nextInt(Integer.MAX_VALUE) +1);
		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		Common.context = this;
		setContentView(R.layout.main);
		
		yButton = (Button) findViewById(R.id.buttonSingle);
		yButton.setVisibility(View.VISIBLE);
		yButton.setOnClickListener(this);
		nButton = (Button)findViewById(R.id.mButtonToApp);
		nButton.setOnClickListener(this);
		
		//For the splashscreen
		//final Main main = this;
	
		/*
		splashThread = new Thread() {
			@Override
			public void run () {
				try{
					synchronized(this) {
						wait(10000);
					}
				} catch(Exception e) {
					e.printStackTrace();	
				}
				finish();
				Intent intent = new Intent(main, LetsMeetActivity.class);
				startActivity(intent);
				//stop();
			}
		};
	splashThread.start();
	*/
	}
	
	/*
	@Override
    public boolean onTouchEvent(MotionEvent evt) {
        if(evt.getAction() == MotionEvent.ACTION_DOWN)
        {
            synchronized(splashThread){
                splashThread.notifyAll();
            }
        }
        return true;
    } 
    */


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
		if(v.getId() == R.id.buttonSingle) {
			singleUserModeFlag = true;
			yButton.setEnabled(false);
			Intent intent = new Intent(this, SingleUserModeActivity.class);
			JSONObject finalObject = null;
			try {
				finalObject = new JSONObject();
				Location location = Common.getLocation();
				JSONObject newContact = new JSONObject();
				newContact.put("NAME", "Dummy Friend");
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
			//splashThread.notifyAll();
		} else if(v.getId() == R.id.mButtonToApp) {
			
			Intent intent = new Intent(this, LetsMeetActivity.class);
			startActivityForResult(intent, Common.START_LETSMEET);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Common.START_LETSMEET) {
			if(resultCode == Common.CANCEL_ALL) {
				finish();
			}
		}
	}
	
	 
}