package in.company.letsmeet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class ReceiveSms extends BroadcastReceiver {
	@Override
	/*
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 * In the onReceive method, we pull the message received and extract the sender id and address
	 * pass the same to the dialog activity.
	 */
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
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
				String[] splitMessage = message.split(":");
				String myNumber = splitMessage[1];
				Intent showDialogIntent = new Intent(context, ShowDialog.class);
				showDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	
				showDialogIntent.putExtra("sender", address);
				showDialogIntent.putExtra("mynumber", myNumber);
				context.startActivity(showDialogIntent);
			}
		}
	}
}


