package in.company.letsmeet;

import in.company.letsmeet.locationutil.BestLocationFinder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
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
		// TODO Auto-generated method stub
		BestLocationFinder finder = new BestLocationFinder(context, LocationManager.NETWORK_PROVIDER,0,false);
		finder.getBestLocation(System.currentTimeMillis());
		Bundle bundle = intent.getExtras();
		System.out.println("In the SMS Receive body");
		Object[] messages = (Object[])bundle.get("pdus");
		SmsMessage[] sms = new SmsMessage[messages.length];
		
		for(int i=0;i<messages.length;++i){
			sms[i] = SmsMessage.createFromPdu((byte[])messages[i]);
		}
		for(int i=0;i<messages.length;++i){
			String address = sms[i].getOriginatingAddress();
			String message = sms[i].getMessageBody().toString();
			if(message.contains("meet-me")) {
				Common.setFriend(true);
				String[] splitMessage = message.split(":");
				Log.i(TAG, "ORG ID" + splitMessage[1]);
				Common.MY_ID = splitMessage[1];
				String myNumber = splitMessage[2];
				String friend_id = splitMessage[3];
				Intent showDialogIntent = new Intent(context, ShowDialog.class);
				showDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	
				showDialogIntent.putExtra("sender", address);
				showDialogIntent.putExtra("mynumber",friend_id);
				context.startActivity(showDialogIntent);
			}
		}
	}
}


