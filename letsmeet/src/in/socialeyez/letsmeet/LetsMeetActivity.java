package in.socialeyez.letsmeet;


import in.socialeyez.letsmeet.R;
import in.socialeyez.letsmeet.alarm.SetAlarm;
import in.socialeyez.letsmeet.common.Common;
import in.socialeyez.letsmeet.common.HttpConnectionHelper;
import in.socialeyez.letsmeet.common.SendSms;
import in.socialeyez.letsmeet.contacts.ContactsListActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TimePicker;

/**
 * @author pradeep
 * Presents user with two options. One to check locations and the other to input known locations.
 */
public class LetsMeetActivity extends Activity implements OnClickListener, OnItemSelectedListener{
	private EditText eAddress;
	private RadioButton noLocButton;
	private RadioButton yesLocButton;
	private Spinner spin;
	private static final String TAG = "LetsMeetActivity";
	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMin;
	private Context context;
	private String selectedSpinnerValue;
	private CheckBox checkDate;
//	private Button aloneButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meet);
		this.context = getApplicationContext();

		String[] occasions = {"Restaurant", "Cafe", "Bar"};
		ArrayAdapter ap = new ArrayAdapter(this, android.R.layout.simple_spinner_item,occasions);
		ap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin = (Spinner)findViewById(R.id.spinner);
		spin.setAdapter(ap);
		spin.setOnItemSelectedListener(this);

		eAddress = (EditText)findViewById(R.id.editDestinationAddress);
		eAddress.setEnabled(false);
		
		spin.setEnabled(true);

		noLocButton = (RadioButton)findViewById(R.id.radioFindLocationNo);
		noLocButton.setOnClickListener(this);

		yesLocButton = (RadioButton)findViewById(R.id.radioFindLocationYes);
		yesLocButton.setOnClickListener(this);

		//	dateButton = (Button) findViewById(R.id.buttonDatePick);
		//dateButton.setOnClickListener(this);

		Button button = (Button)findViewById(R.id.buttonSelectContacts);
		button.setOnClickListener(this);
		
		//aloneButton = (Button)findViewById(R.id.buttonGoAlone);
		//aloneButton.setEnabled(false);
		//aloneButton.setOnClickListener(this);
		
		checkDate = (CheckBox) findViewById(R.id.dateCheck);
		checkDate.setEnabled(false);
		checkDate.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.radioFindLocationNo) {
			spin.setEnabled(false);
			eAddress.setEnabled(true);
			//dateButton.setEnabled(true);
			checkDate.setEnabled(true);
			//aloneButton.setEnabled(true);
			((RadioButton)findViewById(R.id.radioFindLocationYes)).setChecked(false);
		} 
		if(v.getId() == R.id.radioFindLocationYes) {
			spin.setEnabled(true);
			eAddress.setEnabled(false);
			//dateButton.setEnabled(false);
			checkDate.setEnabled(false);
			//aloneButton.setEnabled(false);
			((RadioButton)findViewById(R.id.radioFindLocationNo)).setChecked(false);
		} 
		// Select and invite friends
		if(v.getId() == R.id.buttonSelectContacts) {
			String address = eAddress.getText().toString();
			if(address != null && address.length() != 0) {
				String newAddress = address.replaceAll("[(\\s+)(\\,)]", "+");
				String location = getCoOrdinates(newAddress);
				try {
					JSONObject object = new JSONObject(location);
					JSONArray results = object.getJSONArray("results");
					for(int i=0;i<results.length();++i) {
						JSONObject geometry = ((JSONObject)results.get(i)).getJSONObject("geometry");
						JSONObject loc = geometry.getJSONObject("location");
						String lat = loc.getString("lat");
						String lng = loc.getString("lng");
						Common.setAddressLocationName(address);
						Common.setAddressLocationLatLng(lat + "," + lng);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				//Clear all previous values
				Common.setAddressLocationLatLng(null);
				Common.setDestinationTime(null);
				Common.setAddressLocationName(null);
			}
			Intent intent = new Intent(this, ContactsListActivity.class);
			startActivityForResult(intent, Common.START_CONTACT_LIST);

		} 
		/*
		else if (v.getId() == R.id.buttonGoAlone) {
			try {
				JSONObject finalObject = new JSONObject();
				finalObject.put("MYID", Common.MY_ID);
				Location location = Common.getLocation();
				String newLoc = location.getLatitude() + "," + location.getLongitude();
				finalObject.put("MYLOCATION", newLoc);
				String restaurantType = Common.getSelectedRestaurantType();
				finalObject.put("OCCASION", restaurantType);
				String address = eAddress.getText().toString();
				if(address != null && address.length() != 0) {
					String newAddress = address.replaceAll("[(\\s+)(\\,)]", "+");
					String destinationLocation = getCoOrdinates(newAddress);
					JSONObject object = new JSONObject(destinationLocation);
					JSONArray results = object.getJSONArray("results");
					for(int i=0;i<results.length();++i) {
						JSONObject geometry = ((JSONObject)results.get(i)).getJSONObject("geometry");
						JSONObject loc = geometry.getJSONObject("location");
						String lat = loc.getString("lat");
						String lng = loc.getString("lng");
						Common.setAddressLocationName(address);
						Common.setAddressLocationLatLng(lat + "," + lng);

						JSONArray fsItems = new JSONArray();
						JSONObject indFsItem = new JSONObject();
						indFsItem.put("name", "Destination");
						indFsItem.put("address", address);
						indFsItem.put("id", 0);
						indFsItem.put("lat", Double.parseDouble(lat));
						indFsItem.put("lng", Double.parseDouble(lng));
						indFsItem.put("selected", "yes");
						fsItems.put(indFsItem);
						finalObject.put("FSITEMS",fsItems);
						finalObject.put("NOFS", "yes");
					}

				} else {
					//Clear all previous values
					Common.setAddressLocationLatLng(null);
					Common.setDestinationTime(null);
					Common.setAddressLocationName(null);
				}

				HttpConnectionHelper connectionHelper = new HttpConnectionHelper();
				connectionHelper.postData(Common.URL + "/id=" + Common.MY_ID, finalObject);
				if(!checkDate.isChecked()) {
					Intent intent = new Intent(this, CommonMapActivity.class);
					intent.putExtra(Common.SINGLE_USER_MODE_FLAG, true);
					startActivityForResult(intent, Common.START_COMMON_MAP);
				} else {
					showAlertDialog("Future appointment");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		*/

		if(v.getId() == R.id.dateCheck) {
			if(checkDate.isChecked()) {
				Calendar c = Calendar.getInstance(Locale.getDefault());
				mYear = c.get(Calendar.YEAR);
				mMonth = c.get(Calendar.MONTH);
				mDay = c.get(Calendar.DAY_OF_MONTH);
				showDialog(1);
				Log.d(TAG,"Selected date" + mDay + ":" + mMonth + ":" + mYear);
			}
		}
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		if(id == 1) {
			return new DatePickerDialog(this, mDateSetListener,
					mYear, mMonth, mDay);
		} else if ( id == 0) {
			return new TimePickerDialog(this, mTimeListener, mHour, mMin, false);
		}
		return null;
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
			showDialog(0);
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
			SetAlarm.setRepeatingAlarm(getApplicationContext());
		}
	};

	private String getCoOrdinates(String address) {
		// TODO Auto-generated method stub
		HttpConnectionHelper helper = new HttpConnectionHelper();
		String returnData = helper.getData("http://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&sensor=true");
		Log.i(TAG, "return data" + returnData);
		return returnData;
	}

	@Override
	public void onItemSelected(AdapterView<?> aView, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		selectedSpinnerValue = (String)aView.getItemAtPosition(position);
		Common.setSelectedRestaurantType(selectedSpinnerValue);

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Common.START_CONTACT_LIST) {
			if(resultCode == Common.CANCEL_ALL) {
				setResult(Common.CANCEL_ALL);
				finish();

			}
		}
	}
	
	private JSONObject constructFinalObjectToUpload() {
		JSONObject finalObject = null;
		try {
		finalObject = new JSONObject();
		finalObject.put("MYID", Common.MY_ID);
		Location location = Common.getLocation();
		String newLoc = location.getLatitude() + "," + location.getLongitude();
		finalObject.put("MYLOCATION", newLoc);
		String restaurantType = Common.getSelectedRestaurantType();
		finalObject.put("OCCASION", restaurantType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalObject;
	}

public void showAlertDialog(String message) {
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		AlertDialog dialog = dialogBuilder.create();
		dialog.setTitle(Common.DIALOG_TITLE);
		dialog.setMessage(message);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
				Intent mapIntent = new Intent(getApplicationContext(), CommonMapActivity.class);
				mapIntent.putExtra(Common.SINGLE_USER_MODE_FLAG, false);
				startActivity(mapIntent);
				Common.setAddressLocationLatLng(null);
				Common.setAddressLocationName(null);
				
			}
		});
		
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Common.setAddressLocationLatLng(null);
				Common.setAddressLocationName(null);
				finish();
			}
		});
		dialog.show();
	}

}