/**
 * 
 */
package com.triplaud.common;


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
    private Context context;
    public SendSms(Context context) {
        this.context = context;
        Toast.makeText(context, "Sending SMS", Toast.LENGTH_LONG).show();
    }

    public void sendBulkSms(ArrayList<String> contacts) {
        try{
            String messageToBeSent;
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
                    JSONObject object = new JSONObject();
                    object.put("f", friend_id);
                    object.put("mme", Common.MY_ID);
                    //object.put("u", "http://tinyurl.com/bskh5qm");
                    object.put("l", Common.getAddressLocationLatLng());
                    if(sendAppointment != null) {
                        int year = sendAppointment.get(Calendar.YEAR);
                        //Add one to the month because months are stored starting from 0 in the Calendar object.
                        int month = sendAppointment.get(Calendar.MONTH) + 1;
                        int day = sendAppointment.get(Calendar.DATE);
                        int hour = sendAppointment.get(Calendar.HOUR_OF_DAY);
                        int min = sendAppointment.get(Calendar.MINUTE);
                        addAppointment = new String();
                        addAppointment = addAppointment +  day + ";" + month + ";"+ year+ ";"+ hour+ ";"+ min;
                        object.put("d", addAppointment);

                        messageToBeSent = object.toString();		
                    } else {
                        messageToBeSent = object.toString();
                        Log.d("SENDSMS", "Message to be sent" + messageToBeSent);
                    }
                    if(phoneNumber != null && phoneNumber.length()>0) {
                        sendSms(phoneNumber, messageToBeSent);
                    }
                }
            } else { 
                Log.i(this.toString(),"No contacts in the ArrayList, stop processing");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

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
