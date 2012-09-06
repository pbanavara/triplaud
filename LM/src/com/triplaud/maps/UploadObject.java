package com.triplaud.maps;

import greendroid.app.ActionBarActivity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.triplaud.common.Common;
import com.triplaud.message.SendSms;

public class UploadObject {
    /**
     * 
     */
    private ArrayList<String> contacts;
    private Context context;
    private GeoPoint destination;
    private double destinationLat;
    private double destinationLng;
    GoogleAnalyticsTracker tracker;
    public UploadObject(ArrayList<String> contacts, Context context, GeoPoint destination) {
        tracker = GoogleAnalyticsTracker.getInstance();
        this.contacts = contacts;
        this.context = context;
        this.destination = destination;
        this.destinationLat = destination.getLatitudeE6() /1e6;
        this.destinationLng = destination.getLongitudeE6() / 1e6;
    }

    public void setupMeetingRightNow() {
        //Upload the data to backend
        try {

            Intent intent;
            Location location = Common.getLocation();
            if(contacts != null) {
                SendSms sendSms = new SendSms(context);
                sendSms.sendBulkSms(contacts);
                intent = new Intent(context, CommonMapActivity.class);
                intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Group locations");
                intent.putExtra(Common.SINGLE_USER_MODE_FLAG, false);
            } else {
                Common.friendMap = new HashMap<String, TrackerPoint>();
                String organizer = Common.MY_ID;
                GeoPoint oPoint = new GeoPoint( (int)(location.getLatitude() * 1000000) , (int)(location.getLongitude() * 1000000));
                TrackerPoint toPoint = new TrackerPoint();
                toPoint.setInitialPoint(oPoint);
                toPoint.setName("organizer");
                if (!Common.friendMap.containsKey(organizer)) {
                    Common.friendMap.put(organizer, toPoint);
                }
                tracker.trackPageView("/Directions");
                intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Directions");
                intent.putExtra("DEST", String.valueOf(destinationLat) + "," + String.valueOf(destinationLng));
                intent.putExtra(Common.SINGLE_USER_MODE_FLAG, true);
            }
            context.startActivity(intent);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
