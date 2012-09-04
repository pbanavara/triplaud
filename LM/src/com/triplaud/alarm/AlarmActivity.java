/**
 * All data copyrights of SocialEyez.co
 *
 */
package com.triplaud.alarm;

import java.util.Calendar;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.triplaud.CommonMapActivity;
import com.triplaud.MyApplication;
import com.triplaud.R;
import com.triplaud.UploadObject;
import com.triplaud.common.Common;
import com.triplaud.common.HttpConnectionHelper;

/**
 * @author pradeep
 *
 */
public class AlarmActivity extends Activity implements OnClickListener{
	private Button start;
	private Button stop;
	private TextView tView;
	private String friendId;
	private UploadObject obj;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webv);
		friendId = getIntent().getExtras().getString("FROMFRIEND");
		MyApplication app = (MyApplication)getApplication();
		obj = app.getObj();
		start = (Button) findViewById(R.id.startCommonMap);
		start.setOnClickListener(this);
		stop = (Button) findViewById(R.id.stopCommonMap);
		stop.setOnClickListener(this);
		tView = (TextView)findViewById(R.id.wvTextView);
		tView.setText("Reminder for Your meeting near " + Common.getAddressLocationName() + "\n" + "Scheduled for "+ Common.getDestinationTime().get(Calendar.HOUR_OF_DAY) + ":" + 
				Common.getDestinationTime().get(Calendar.MINUTE) + "\n" + "Do you need directions and tracking");
	}
	@Override
	public void onClick(View view) {
		try{
			if(view.getId() == R.id.startCommonMap) {
				// If this request is coming from the friend, then upload his location before starting CommonMapActivity
				Location myLocation = Common.getLocation();
				if(friendId != null) {
					HttpConnectionHelper connectionHelper = new HttpConnectionHelper();
					
					if(myLocation != null) {
						Double sourceLat = myLocation.getLatitude();
						Double sourceLng = myLocation.getLongitude();
						StringBuffer myLocString = new StringBuffer();
						String myLoc = (myLocString.append(String.valueOf(sourceLat)).append(",").append(String.valueOf(sourceLng))).toString();
						connectionHelper = new HttpConnectionHelper();
						JSONObject obj = new JSONObject();
						obj.put("id", friendId);
						obj.put("loc", myLoc);
						connectionHelper.postData(Common.URL + "/id=" + Common.MY_ID, obj);
					}
					Intent mapIntent =  new Intent(this, CommonMapActivity.class);
					mapIntent.putExtra("singleusermode", false);
					startActivity(mapIntent);
					SetAlarm.cancelRepeatingAlarm();
					finish();
				// If this is a alarm request from the organizer
				} else {
					obj.setupMeetingRightNow();
				}
				
			} else if(view.getId() == R.id.stopCommonMap) {
				SetAlarm.cancelRepeatingAlarm();
				finish();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
}
