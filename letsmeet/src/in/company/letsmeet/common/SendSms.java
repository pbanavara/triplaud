/**
 * 
 */
package in.company.letsmeet.common;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

/**
 * @author pradeep
 * Activity class for sending SMS
 *
 */
public class SendSms {
	private static String message;
	private Context context;
	public SendSms(Context context) {
		this.context = context;
		Log.i(this.toString(), Common.MY_ID);
		message = "mme:" + Common.MY_ID + ":";
		
		Toast.makeText(context, "Sending SMS", Toast.LENGTH_LONG).show();
	}
	
	public void sendBulkSms(ArrayList<String> contacts) {
		if (contacts != null) {
		Log.i(this.toString(), "Contacts ArrayList Obtained" + String.valueOf(contacts.size()));
		Iterator<String> iterator = contacts.iterator();
		while(iterator.hasNext()) {
			String[] name = ((String)iterator.next()).split(",");
			String phoneNumber = name[1];
			String cName = name[0];
			String friend_id = name[2];
			Log.i(this.toString(), "Contact name" + cName + "Contact Number" + phoneNumber);
			//String newMessage = message + phoneNumber + ":" + friend_id;
			Calendar sendAppointment = Common.getDestinationTime();
			String addAppointment = null;
			if(sendAppointment != null) {
				int year = sendAppointment.get(Calendar.YEAR);
				int month = sendAppointment.get(Calendar.MONTH);
				int day = sendAppointment.get(Calendar.DAY_OF_MONTH);
				int hour = sendAppointment.get(Calendar.HOUR);
				int min = sendAppointment.get(Calendar.MINUTE);
				addAppointment = new String();
				addAppointment = addAppointment +  day + ";" + month + ";"+ year+ ";"+ hour+ ";"+ min;
						
			}
			JSONObject object = new JSONObject();
			
			try {
				object.put("f", friend_id);
				object.put("mme", Common.MY_ID);
				object.put("u", "http://tinyurl.com/bskh5qm");
				object.put("d", addAppointment);
				object.put("l", Common.getAddressLocationLatLng());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//String newMessage = message + friend_id +":http://tinyurl.com/bskh5qm" + addAppointment;
			String newMessage = object.toString();
			Log.d("SENDSMS", "Message to be sent" + newMessage);
			if(phoneNumber != null && phoneNumber.length()>0) {
				sendSms(phoneNumber, newMessage);
			}
		}
		} else { 
			Log.i(this.toString(),"No contacts in the ArrayList, stop processing");
		}
		
	}
	
		/*
		wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl(Common.URL);	
		wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setBuiltInZoomControls(true);
		
		wv.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});
		wv.loadUrl(Common.URL);	
		Button viewButton = (Button)findViewById(R.id.mapButton);
		viewButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				wv.loadUrl(Common.URL);
				
			}
		});
		*/
	
	
	private void sendSms(String phoneNumber, String message) {
		
		String SENT = "SMS_SENT";
		PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
		/*
		registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context arg0, Intent arg1) {
				// TODO Auto-generated method stub
				switch(getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "Your location information has been sent", Toast.LENGTH_SHORT).show();
					break;
				}
				
			}
		},new IntentFilter(SENT));
		*/
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentIntent, null);	
	}

}
