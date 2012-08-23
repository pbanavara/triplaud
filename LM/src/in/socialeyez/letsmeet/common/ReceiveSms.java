package in.socialeyez.letsmeet.common;

import in.socialeyez.letsmeet.ReceiveMessageActivity;
import in.socialeyez.letsmeet.locationutil.BestLocationFinder;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

public class ReceiveSms extends BroadcastReceiver {
	private static final String TAG = "ReceiveSMS";
	@Override
	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 * In the onReceive method, we pull the message received and extract the sender id and address
	 * pass the same to the dialog activity.
	 */
	public void onReceive(Context context, Intent intent) {
		try {
		// TODO Auto-generated method stub
		BestLocationFinder finder = new BestLocationFinder(context, LocationManager.NETWORK_PROVIDER,false);
		Common.setConfirm(false);
		finder.getBestLocation(System.currentTimeMillis(),0);
		Bundle bundle = intent.getExtras();
		System.out.println("In the SMS Receive body");
		Object[] messages = (Object[])bundle.get("pdus");
		SmsMessage[] sms = new SmsMessage[messages.length];
		
		for(int i=0;i<messages.length;++i){
			sms[i] = SmsMessage.createFromPdu((byte[])messages[i]);
		}
		for(int i=0;i<messages.length;++i){
			String senderNumber = sms[i].getOriginatingAddress();
			String contactName = getContactName(senderNumber, context);
			String message = sms[i].getMessageBody().toString();
			if(message.contains("mme")) {
				Common.setFriend(true);
				JSONObject object = new JSONObject(message);
				Common.MY_ID = object.getString("mme");
				String destinationLocation = null;
				String friend_id = null;
				
				//String[] splitMessage = message.split(":");
				//Log.i(TAG, "ORG ID" + splitMessage[1]);
				//Common.MY_ID = splitMessage[1];
				//String myNumber = splitMessage[2];
				//String friend_id = splitMessage[2];
				//String date = splitMessage[3];
				String date = null;
				if(! object.isNull("f")) {
					friend_id = object.getString("f");
				}
				if(!object.isNull("d")) {
					date = object.getString("d");
				}
				if(!object.isNull("l")) {
					destinationLocation = object.getString("l");
				}
				Common.setAddressLocationLatLng(destinationLocation);
				
				//Split the date and set the appointment date in common
				Intent showDialogIntent = new Intent(context, ReceiveMessageActivity.class);
				
				if (date != null) {
					showDialogIntent.putExtra("DATE", date);
				}
 				if (destinationLocation != null ) {
 					showDialogIntent.putExtra("DEST", destinationLocation);
 				}
				showDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				if(contactName != null) {
					showDialogIntent.putExtra("sender", contactName);
				} else {
					showDialogIntent.putExtra("sender", senderNumber);
				}
				showDialogIntent.putExtra("mynumber",friend_id);
				context.startActivity(showDialogIntent);
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getContactName(String phoneNumber, Context context) {
		String cName = null;
		String[] projection = new String[] {
		        ContactsContract.PhoneLookup.DISPLAY_NAME,
		        ContactsContract.PhoneLookup._ID};

		// encode the phone number and build the filter URI
		Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

		// query time
		Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

		if (cursor.moveToFirst()) {
		    // Get values from contacts database:
		    cName =      cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
		    Log.i(TAG, "Started uploadcontactphoto: Contact name  = " + cName);
		} else {
		    Log.v(TAG, "Started uploadcontactphoto: Contact Not Found @ " + phoneNumber);
		    cName = null;

		}
		return cName;
	}
}


