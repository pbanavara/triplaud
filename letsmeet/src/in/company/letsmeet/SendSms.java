/**
 * 
 */
package in.company.letsmeet;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * @author pradeep
 * Activity class for sending SMS
 *
 */
public class SendSms {
	private static final String message = "meet-me:";
	private WebView wv;
	private Context context;
	
	public SendSms(Context context) {
		this.context = context;
	}
	
	public void sendBulkSms(ArrayList<String> contacts) {
		if (contacts != null) {
		Log.i(this.toString(), "Contacts ArrayList Obtained" + String.valueOf(contacts.size()));
		Iterator<String> iterator = contacts.iterator();
		while(iterator.hasNext()) {
			String[] name = ((String)iterator.next()).split(",");
			String phoneNumber = name[1];
			String cName = name[0];
			Log.i(this.toString(), "Contact name" + cName + "Contact Number" + phoneNumber);
			String newMessage = message + phoneNumber;
			
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
