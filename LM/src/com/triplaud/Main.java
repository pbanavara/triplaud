package com.triplaud;

import greendroid.app.ActionBarActivity;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.util.Random;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Window;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.triplaud.common.Common;
import com.triplaud.contacts.InviteContactsActivity;
import com.triplaud.locationutil.BestLocationFinder;

public class Main extends GDActivity {
    // Google analytics code
    GoogleAnalyticsTracker tracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Clear and initialize all previous values
        Common.MY_ID = String.valueOf(new Random().nextInt(Integer.MAX_VALUE) +1);
        Common.ORGANIZER_ID = new String(Common.MY_ID);
        Common.setDestinationTime(null);
        Common.setAddressLocationLatLng(null);
        Common.setAddressLocationName(null);

        super.onCreate(savedInstanceState);
        //Initialize google tracker code
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession("UA-34562016-1", this);
        //End Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(R.string.app_name);

        BestLocationFinder finder = new BestLocationFinder(getApplicationContext(), LocationManager.NETWORK_PROVIDER, false);
        finder.getBestLocation(System.currentTimeMillis(), 0);
        //Start GreenDroid
        setActionBarContentView(R.layout.main);
        addActionBarItem(Type.LocateMyself, R.id.action_bar_search);
        addActionBarItem(Type.AllFriends, R.id.action_bar_allfriends);

        //End GreenDroid

    }

    @Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        Intent intent;
        switch (item.getItemId()) {
        case R.id.action_bar_search:
            tracker.trackPageView("/search_single_user");
            intent = new Intent(this, SearchMapActivity.class);
            intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Search meeting place");
            startActivity(intent);
            break;

        case R.id.action_bar_allfriends:
            tracker.trackPageView("/search_group");
            //Start the contactsList Activity with the required parameters
            intent = new Intent(this, InviteContactsActivity.class);
            intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Invite Friends");
            startActivityForResult(intent, Common.START_CONTACT_LIST);
            break;

        case R.id.action_bar_home:
            tracker.trackPageView("/home_page");
            intent = new Intent(this, Main.class);
            intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "TripLaud-Beta");
            startActivity(intent);
            break;

        default:
            return super.onHandleActionBarItemClick(item, position);
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        //Common.setDestinationTime(null);
        super.onDestroy();
        tracker.stopSession();
    }



}
