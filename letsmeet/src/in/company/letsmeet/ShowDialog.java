package in.company.letsmeet;

import java.util.Calendar;

import in.company.letsmeet.alarm.AlarmReceiver;
import in.company.letsmeet.alarm.SetAlarm;
import in.company.letsmeet.common.Common;
import in.company.letsmeet.common.HttpConnectionHelper;
import in.company.letsmeet.locationutil.BestLocationFinder;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;


public class ShowDialog extends Activity implements DialogInterface.OnClickListener{
	private static final int DIALOG_ALERT = 10;
	private String sender;
	private String myNumber;
	private HttpConnectionHelper connectionHelper;
	private String date;
	private String destinationLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		// TODO Auto-generated method stub
		sender = getIntent().getExtras().getString("sender");
		myNumber = getIntent().getExtras().getString("mynumber");
		date = getIntent().getExtras().getString("DATE");
		destinationLocation = getIntent().getExtras().getString("DEST");
		super.onCreate(savedInstanceState);
		showDialog(DIALOG_ALERT);

	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		String formatString = "";
		if(date != null) {
			formatString = "Your friend " + sender + "wants to setup a meeting on" + "\n" + date + "Set a reminder" ;
		} else {
			formatString = "Your friend " + sender + "wants you to meet him, share your location";
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
				if(date == null) {
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
					String[] dateArray = date.split(";");
					int day = Integer.parseInt(dateArray[0]);
					int month = Integer.parseInt(dateArray[1]);
					int year = Integer.parseInt(dateArray[2]);
					int hour = Integer.parseInt(dateArray[3]);
					int min = Integer.parseInt(dateArray[4]);
					
					Calendar c = Calendar.getInstance();
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

