package in.company.letsmeet;


import in.company.letsmeet.alarm.AlarmReceiver;
import in.company.letsmeet.alarm.SetAlarm;
import in.company.letsmeet.common.Common;
import in.company.letsmeet.common.HttpConnectionHelper;
import in.company.letsmeet.contacts.ContactsListActivity;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;

/**
 * @author pradeep
 * Presents user with two options. One to check locations and the other to input known locations.
 */
public class LetsMeetActivity extends Activity implements OnClickListener, OnItemSelectedListener{
	private EditText eAddress;
	private RadioGroup radioGroup;
	private RadioButton rButton;
	private RadioButton noLocButton;
	private RadioButton yesLocButton;
	private int selectedRadioButton;
	private Spinner spin;
	private static final String TAG = "LetsMeetActivity";

	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMin;
	private Context context;
	private Button dateButton;
	private String selectedSpinnerValue;

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

		dateButton = (Button) findViewById(R.id.buttonDatePick);
		dateButton.setOnClickListener(this);

		Button button = (Button)findViewById(R.id.buttonSelectContacts);
		button.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.radioFindLocationNo) {
			spin.setEnabled(false);
			eAddress.setEnabled(true);
			dateButton.setEnabled(true);
			((RadioButton)findViewById(R.id.radioFindLocationYes)).setChecked(false);
		} 
		if(v.getId() == R.id.radioFindLocationYes) {
			spin.setEnabled(true);
			eAddress.setEnabled(false);
			dateButton.setEnabled(false);
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
			}

			// Create a new DatePicker dialog upon clicking this button.


			Intent intent = new Intent(this, ContactsListActivity.class);
			startActivityForResult(intent, Common.START_CONTACT_LIST);
		}

		if(v.getId() == R.id.buttonDatePick) {
			Calendar c = Calendar.getInstance();//To initialize with the current date 
		
			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);
			showDialog(1);
			Log.d(TAG,"Selected date" + mDay + ":" + mMonth + ":" + mYear);
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
			
			//long intervalTime = alarmTime - System.currentTimeMillis();
			Calendar cTime = Calendar.getInstance();
			int cHour = cTime.get(Calendar.HOUR);
			int cMin = cTime.get(Calendar.MINUTE);
			
			cTime.add(Calendar.HOUR, mHour - cHour);
			cTime.add(Calendar.MINUTE, mMin - cMin);
			Log.d(TAG, "Date and time selected ::: " + mYear + ":" + mMonth + ":" + mDay + ":" + mHour + ":" + mMin); 
			
			Calendar newCal = Calendar.getInstance();
			newCal.set(Calendar.MINUTE, mMin);
			newCal.set(Calendar.HOUR, mHour);
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
	
	
}