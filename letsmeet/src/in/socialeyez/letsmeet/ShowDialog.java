package in.socialeyez.letsmeet;

import in.socialeyez.letsmeet.alarm.SetAlarm;
import in.socialeyez.letsmeet.common.Common;
import in.socialeyez.letsmeet.common.HttpConnectionHelper;
import in.socialeyez.letsmeet.locationutil.BestLocationFinder;

import java.util.Calendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
public class ShowDialog extends Activity implements DialogInterface.OnClickListener{
	private static final String TAG = "ReceiveConfirmDialog";
	private static final int DIALOG_ALERT = 10;
	private String sender;
	private String myNumber;
	private HttpConnectionHelper connectionHelper;
	private String date;
	private String destinationLocation;
	private String destinationAddress;
	private int day;
	private int month;
	private int year;
	private int hour;
	private int min;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		// TODO Auto-generated method stub
		sender = getIntent().getExtras().getString("sender");
		myNumber = getIntent().getExtras().getString("mynumber");
		date = getIntent().getExtras().getString("DATE");
		destinationLocation = getIntent().getExtras().getString("DEST");
		destinationAddress = convertLatLngToAddress(destinationLocation);
		super.onCreate(savedInstanceState);
		showDialog(DIALOG_ALERT);

	}
	
	private String convertLatLngToAddress(String destinationLocation) {
		String returnAddress = null;
		try{
		HttpConnectionHelper helper = new HttpConnectionHelper();
		String returnData = helper.getData("http://maps.googleapis.com/maps/api/geocode/json?address=" + destinationLocation + "&sensor=true");
		Log.i(TAG, "return data" + returnData);
		JSONObject object = new JSONObject(returnData);
		JSONArray results = object.getJSONArray("results");
		for(int i=0;i<results.length();++i) {
			returnAddress = results.getJSONObject(i).getString("formatted_address");
		}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return returnAddress;
		
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		String formatString = "";
		if(date != null) {
			String[] dateArray = date.split(";");
			day = Integer.parseInt(dateArray[0]);
			month = Integer.parseInt(dateArray[1]);
			year = Integer.parseInt(dateArray[2]);
			hour = Integer.parseInt(dateArray[3]);
			min = Integer.parseInt(dateArray[4]);
			
			StringBuffer message = new StringBuffer();
			message = message.append(day).append(":").append(month).append(":").append(year).append("\n").append(hour).append(":").append(min).append("\n");
			formatString = "Your friend " + sender + " wants to setup a meeting at " + destinationAddress + "\n" + "on " + message.toString() + "Set a reminder ?" ;
		} else {
			formatString = "Your friend " + sender + " wants to meet you, share your current location ?";
		}
		
		switch (id) {
		case DIALOG_ALERT:
			Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(formatString);
			AlertDialog dialog = builder.create();
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", this);
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", this);
			return dialog;
		}
		return null;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if(which == AlertDialog.BUTTON_POSITIVE) {
			try{
				//If there is no reminder then carry on as usual
				if(destinationLocation == null) {
					//Push the current location to the back-end server as a JSON object.
					BestLocationFinder finder = new BestLocationFinder(getApplicationContext(),LocationManager.NETWORK_PROVIDER,false);
					finder.getBestLocation(System.currentTimeMillis(),0);
					Location loc = Common.getLocation();
					if (loc == null) {
						Toast.makeText(getApplicationContext(), "Location Fix not obtained, Application closing", Toast.LENGTH_LONG).show();
						finish();
					}
					String locString = String.valueOf(loc.getLatitude()) + "," + String.valueOf(loc.getLongitude());
					//String locString = "12.981596" + "," + "77.628913";
					connectionHelper = new HttpConnectionHelper();
					JSONObject obj = new JSONObject();
					obj.put("id", myNumber);
					obj.put("loc", locString);
					connectionHelper.postData(Common.URL + "/id=" + Common.MY_ID, obj);
					Intent mapIntent = new Intent(getApplicationContext(), CommonMapActivity.class);
					mapIntent.putExtra("singleusermode", false);
					startActivity(mapIntent);
					//If there is a reminder (date != null) then setup the alarmreceiver
				} else {
					Common.setAddressLocationLatLng(destinationLocation);
					
					
					Calendar c = Calendar.getInstance(Locale.getDefault());
					c.set(Calendar.MINUTE, min);
					c.set(Calendar.HOUR, hour);
					c.set(Calendar.DAY_OF_MONTH, day);
					c.set(Calendar.MONTH, month);
					c.set(Calendar.YEAR, year);
					Common.setDestinationTime(c);
					// Set the common date as well.
					/*
					Intent intent = new Intent(this, AlarmReceiver.class);
					PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), 11111, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
					long frequency = Common.AlARM_INTERVAL;
					manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), frequency, pIntent);
					*/
					SetAlarm.setRepeatingAlarm(getApplicationContext(), myNumber);
					setResult(Common.CANCEL_ALL);
					finish();
					
				}
				
			} catch(Exception e){
				e.printStackTrace();
			}
		} else if(which == AlertDialog.BUTTON_NEGATIVE) {
			SetAlarm.cancelRepeatingAlarm();
			finish();
		}
		
	}
	
	
}

