package in.company.letsmeet;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class ReceiveSms extends BroadcastReceiver {
	List respondedParties = new ArrayList<String>();

	@Override
		/*
		 * (non-Javadoc)
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 * In the onReceive method, we pull the message sent
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
					Intent showDialogIntent = new Intent(context, ShowDialog.class);
					showDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	
					showDialogIntent.putExtra("sender", address);
					context.startActivity(showDialogIntent);
				} else if(message.contains("meet-response")) {
					respondedParties.add(address);
					Intent showMapIntent = new Intent(context, MapUs.class);
					showMapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					String[] newMessage = message.split(":");
					String newMs = newMessage[1];
					//showMapIntent.putExtra("message", newMs);
					//Hard coded values for demo purposes
					showMapIntent.putExtra("message", "12.959840681934615,77.64945907313533");
					showMapIntent.putExtra("recipient", address);
					context.startActivity(showMapIntent);
				}
			}
		}
	


	}


