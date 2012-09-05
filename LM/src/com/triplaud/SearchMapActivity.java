package com.triplaud;

import greendroid.app.ActionBarActivity;
import greendroid.app.GDMapActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.triplaud.common.Common;
import com.triplaud.common.Writer;
import com.triplaud.contacts.InviteContactsActivity;
import com.triplaud.locationutil.BestLocationFinder;

public class SearchMapActivity extends GDMapActivity implements OnClickListener {
    private static final String TAG= "SearchMapActivity";

    private Drawable drawable ;	
    private Drawable fsDrawable;
    private MapView mapView;
    private MapItemizedOverlay<?> itemizedOverlay;
    private List<Overlay> mapOverlays;

    private MapController mapControl;
    private Context context;

    private String displayMessage;
    private Writer writer;
    private Geocoder geoCoder;
    private GeoPoint myPoint;
    private Button searchButton;
    private EditText textSearch;
    private GeoPoint destinationPoint;
    private String address;
    private double destinationLat;
    private double destinationLng;
    private ArrayList<String> contacts;
    private SearchMapOverlay mapOverlay;
    private String jsonObjectString;
    GoogleAnalyticsTracker tracker;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);	
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession("UA-34562016-1", this);
        context = SearchMapActivity.this;
        Bundle extras = getIntent().getExtras();

        if (null != extras) {
            contacts = extras.getStringArrayList("CONTACTS");
            jsonObjectString = extras.getString("OBJECT");
        }


        //Hide the title bar
        //
        //End Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        BestLocationFinder finder = new BestLocationFinder(getApplicationContext(), LocationManager.NETWORK_PROVIDER, false);
        finder.getBestLocation(System.currentTimeMillis(), 0);

        //Start GreenDroid
        setActionBarContentView(R.layout.map);
        addActionBarItem(Type.LocateMyself, R.id.action_bar_search);
        addActionBarItem(Type.AllFriends, R.id.action_bar_allfriends);

        //End GreenDroid
        drawable = this.getResources().getDrawable(R.drawable.marker);
        fsDrawable = getApplicationContext().getResources().getDrawable(R.drawable.orangeicon);
        //setContentView(R.layout.mapus);
        mapView = (MapView) findViewById(R.id.mapview);
        mapControl = mapView.getController();
        mapView.invalidate();
        mapOverlays = mapView.getOverlays();
        mapControl = mapView.getController();
        mapControl.setZoom(15);
        mapView.setBuiltInZoomControls(true); 
        MyLocationOverlay myOverlay = new MyLocationOverlay(this, mapView);
        myOverlay.enableMyLocation();
        myOverlay.enableCompass();
        mapOverlays.add(myOverlay);
        Location loc = Common.getLocation();
        myPoint = myOverlay.getMyLocation();
        //singleMode = getIntent().getExtras().getBoolean("singleusermode");
        if(loc != null) {
            GeoPoint point = new GeoPoint((int) (loc.getLatitude() * 1000000),(int) (loc.getLongitude() * 1000000));
            mapControl.setCenter(point);
        }
        Toast.makeText(this, "Your current location is shown, please wait for your friend's locations", Toast.LENGTH_LONG);
        //Log.i("map ctivity", String.valueOf(point.getLatitudeE6()) + String.valueOf(point.getLongitudeE6()));
        mapView.setBuiltInZoomControls(true);   

        searchButton = (Button)findViewById(R.id.maSearchButton);
        searchButton.setOnClickListener(this);
        textSearch = (EditText)findViewById(R.id.maEditDestinationAddress);

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        //Common.setDestinationTime(null);
        super.onDestroy();
        tracker.stopSession();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if(view.getId() == R.id.maSearchButton) {
            tracker.trackPageView("/search_address");
            geoCoder = new Geocoder(this, Locale.ENGLISH);
            List<Address> addresses = null;
            address = textSearch.getText().toString();
            try {
                addresses = geoCoder.getFromLocationName(address,5);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if(addresses.size() > 0)
            {
                destinationLat = addresses.get(0).getLatitude();
                destinationLng = addresses.get(0).getLongitude();
                destinationPoint = new GeoPoint( (int) (destinationLat * 1E6), 
                        (int) (destinationLng * 1E6));

                mapControl.animateTo(destinationPoint);
                mapControl.setZoom(15);
                List<Overlay> listOfOverlays = mapView.getOverlays();
                listOfOverlays.clear();
                UploadObject obj = new UploadObject(contacts, context, destinationPoint);
                MyApplication myApp = (MyApplication) getApplication();
                myApp.setObj(obj);
                if(contacts == null) {
                    mapOverlay = new SearchMapOverlay(context,destinationPoint, address, null,null);
                    listOfOverlays.add(mapOverlay);
                } else {
                    mapOverlay = new SearchMapOverlay(context,destinationPoint, address, contacts,jsonObjectString);
                    listOfOverlays.add(mapOverlay);
                }
                mapView.invalidate();
                textSearch.setText("");
            }
            else
            {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle("Google Map");
                adb.setMessage("Please Provide the Proper Place");
                adb.setPositiveButton("Close",null);
                adb.show();
            }
        }

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

}
