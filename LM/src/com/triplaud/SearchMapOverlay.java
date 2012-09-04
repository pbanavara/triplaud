package com.triplaud;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.triplaud.alarm.SetAlarm;
import com.triplaud.common.Common;
import com.triplaud.common.HttpConnectionHelper;
import com.triplaud.common.SendSms;

public class SearchMapOverlay extends com.google.android.maps.Overlay implements DialogInterface.OnClickListener{
	private static final int TIME_DIALOG = 0;
	private static final int DATE_DIALOG = 1;
	private static final String TAG = "SearchMapOverlay";

	private Context context = null;
	private GeoPoint destinationPoint;
	private String address;
	private double destinationLat;
	private double destinationLng;
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMin;
	private ArrayList<String> contacts;
	private String jsonObjectString;
	private JSONObject finalObject;
	private UploadObject obj;
	private static final String REMINDER_DIALOG_MESSAGE = "If you want to travel right away, click on Get Directions button, or to set a location aware reminder click on Set Reminder";
	private static final String CONFIRM_REMINDER_DIALOG_MESSAGE="Confirm Reminder";

	public SearchMapOverlay (Context context, GeoPoint destination, String address, ArrayList<String> contacts, String jsonObjectString) {
		this.context = context;
		this.destinationPoint = destination;
		this.address = address;
		this.destinationLat = destination.getLatitudeE6() / 1e6;
		this.destinationLng = destination.getLongitudeE6() / 1e6;
		this.contacts = contacts;
		this.jsonObjectString = jsonObjectString;
		MyApplication myApp = (MyApplication)context.getApplicationContext();
		obj = myApp.getObj();
		try {
			// Populate the JSON Object 
			if(jsonObjectString != null) {
				finalObject = new JSONObject(jsonObjectString);
				JSONArray fsItems = new JSONArray();
				JSONObject indFsItem = new JSONObject();
				indFsItem.put("name", "Destination");
				indFsItem.put("address", address);
				indFsItem.put("id", 0);
				indFsItem.put("lat", destinationLat);
				indFsItem.put("lng", destinationLng);
				indFsItem.put("selected", "yes");
				fsItems.put(indFsItem);
				finalObject.put("FSITEMS",fsItems);
				finalObject.put("NOFS", "yes");
			}

		} catch ( Exception e) {
			e.printStackTrace();
		}
	}

	public boolean draw(Canvas canvas, MapView mapView, 
			boolean shadow, long when) 
	{
		super.draw(canvas, mapView, shadow);                   

		//---translate the GeoPoint to screen pixels---
		Point screenPts = new Point();
		mapView.getProjection().toPixels(destinationPoint, screenPts);

		//---add the marker---
		Bitmap bmp = BitmapFactory.decodeResource(
				context.getResources(), R.drawable.greenicon);            
		canvas.drawBitmap(bmp, screenPts.x, screenPts.y-32, null);         
		return true;
	}

	@Override
	public boolean onTap(GeoPoint point, MapView mView) {
		// TODO Auto-generated method stub
		//return super.onTap(arg0, arg1);
		AlertDialog.Builder adb = new AlertDialog.Builder(context);
		AlertDialog dialog = adb.create();
		dialog.setTitle(Common.DIALOG_TITLE);
		dialog.setMessage(REMINDER_DIALOG_MESSAGE);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE,"Set Reminder", this);
		String buttonMessage;
		if(contacts == null) {
			buttonMessage = "Get directions";
		} else {
			buttonMessage = "Go";
			// If the user has entered the location then fill in the FSITEMS object here itself
			String userAddress = Common.getAddressLocationName();
			String userAddressLoc = Common.getAddressLocationLatLng();
			if (userAddress != null && userAddressLoc != null) {
				try {
					JSONArray fsItems = new JSONArray();
					JSONObject indFsItem = new JSONObject();
					indFsItem.put("name", userAddress);
					indFsItem.put("address", userAddress);
					indFsItem.put("id", 0);
					String[] userAddressArr = userAddressLoc.split(",");
					String userLocLat = userAddressArr[0];
					String userLocLng = userAddressArr[1];
					indFsItem.put("lat", Double.parseDouble(userLocLat));
					indFsItem.put("lng", Double.parseDouble(userLocLng));
					indFsItem.put("selected", "yes");

					fsItems.put(indFsItem);
					finalObject.put("FSITEMS",fsItems);
					finalObject.put("NOFS", "yes");
				} catch(Exception e) {
					e.printStackTrace();
				}

			}
		}
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE,buttonMessage, this);
		dialog.show();
		return true;
	}


	@Override
	public void onClick(DialogInterface dInterface, int which) {
		Common.setAddressLocationName(address);
		Common.setAddressLocationLatLng(String.valueOf(destinationLat) + "," + String.valueOf(destinationLng));
		//Set reminder
		if(which ==  AlertDialog.BUTTON_POSITIVE) {
			//Set Reminder
			Calendar c = Calendar.getInstance(Locale.getDefault());
			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);
			showDateTimeDialog(DATE_DIALOG);
			Log.d(TAG,"Selected date" + mDay + ":" + mMonth + ":" + mYear);

			//No reminder, meeting right away
		} else if(which == AlertDialog.BUTTON_NEGATIVE) {
			if(finalObject != null) {
				HttpConnectionHelper connectionHelper = new HttpConnectionHelper();
				connectionHelper.postData(Common.URL + "/id=" + Common.MY_ID, finalObject);
			}
			obj.setupMeetingRightNow();	
			
		}
	}

	private void showDateTimeDialog(int whichDialog) {
		// TODO Auto-generated method stub

		if(whichDialog == DATE_DIALOG) {
			DatePickerDialog dDialog = new DatePickerDialog(context, mDateSetListener,
					mYear, mMonth, mDay);
			dDialog.show();
		} else if (whichDialog == TIME_DIALOG) {
			TimePickerDialog tDialog = new TimePickerDialog(context, mTimeListener, mHour, mMin, false);
			tDialog.show();
		}

	}

	private DatePickerDialog.OnDateSetListener mDateSetListener =
			new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			Date cDate = new Date(System.currentTimeMillis());
			mHour = cDate.getHours();
			mMin = cDate.getMinutes();
			showDateTimeDialog(TIME_DIALOG);
		}
	};

	private TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			mHour = hourOfDay;
			mMin = minute;
			Log.d(TAG, "Date and time selected ::: " + mYear + ":" + mMonth + ":" + mDay + ":" + mHour + ":" + mMin); 

			Calendar newCal = Calendar.getInstance();
			newCal.set(Calendar.MINUTE, mMin);
			newCal.set(Calendar.HOUR_OF_DAY, mHour);
			newCal.set(Calendar.DAY_OF_MONTH, mDay);
			newCal.set(Calendar.MONTH, mMonth);
			newCal.set(Calendar.YEAR, mYear);
			Common.setDestinationTime(newCal);

			int day = Common.getDestinationTime().get(Calendar.DATE);
			int month = Common.getDestinationTime().get(Calendar.MONTH) + 1;
			int year = Common.getDestinationTime().get(Calendar.YEAR);

			StringBuffer messageBuffer = new StringBuffer();
			messageBuffer.append("You have scheduled a meeting near\n");
			messageBuffer.append(Common.getAddressLocationName());
			messageBuffer.append(" On \n");
			messageBuffer.append(day).append(":").append(month).append(":").append(year).append("\n");
			messageBuffer.append(hourOfDay).append(":").append(minute).append(" ").append("HOURS \n");
			if(contacts != null) {
				Iterator<String> iterator = contacts.iterator();
				if(iterator != null) {
					messageBuffer.append("You have invited \n");
					while(iterator.hasNext()) {
						String contact = iterator.next();
						String[] names = contact.split(",");
						String name = names[0];
						messageBuffer.append(name).append("\n");
					}
				}
			}
			showConfirmDialog(messageBuffer.toString());
		}
	};

	public void showConfirmDialog(String message) {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		AlertDialog dialog = dialogBuilder.create();
		dialog.setTitle(Common.DIALOG_TITLE);
		dialog.setMessage(message);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, CONFIRM_REMINDER_DIALOG_MESSAGE, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					SetAlarm.setRepeatingAlarm(context, obj);
					HttpConnectionHelper connectionHelper = new HttpConnectionHelper();
					connectionHelper.postData(Common.URL + "/id=" + Common.MY_ID, finalObject);
					if(contacts != null) {
						SendSms sendSms = new SendSms(context);
						sendSms.sendBulkSms(contacts);
					}

				}catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Common.setAddressLocationLatLng(null);
				Common.setAddressLocationName(null);
				//finish();
			}
		});
		dialog.show();
	}
}
