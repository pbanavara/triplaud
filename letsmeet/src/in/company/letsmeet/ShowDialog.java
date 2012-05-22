package in.company.letsmeet;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;


public class ShowDialog extends Activity {
	private static final int DIALOG_ALERT = 10;
	private String sender;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		sender = getIntent().getExtras().getString("sender");
		super.onCreate(savedInstanceState);
	//	setContentView(R.layout.showdialog);
		showDialog(DIALOG_ALERT);
		
	}

	protected Dialog onCreateDialog(int id) {
		
		String formatString = "Your friend " + sender + " wants you to meet him, do you want to share your location" ;
		switch (id) {
		case DIALOG_ALERT:
			Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(formatString);
			builder.setCancelable(true);
			builder.setPositiveButton("OK", new OkOnClickListener());
			builder.setNegativeButton("Cancel", new CancelOnClickListener());
			AlertDialog dialog = builder.create();
			//dialog.show();
			return dialog;
		}
		return null;
	}
	
	private final class OkOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Intent intent = new Intent(getApplicationContext(), SendSms.class);
			intent.putExtra("replymessage", "meet-response:" + getGpsData(getApplicationContext()));
			intent.putExtra("replyphonenumber", sender);
			startActivity(intent);
			//ShowDialog.this.finish();
		}
	}

	private final class CancelOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			ShowDialog.this.finish();
		}
	}
	
	public String getGpsData(Context context) {
		String latlon = "";
		try {
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			List<String> providers = locationManager.getAllProviders();
			// Add code to check if GPS is on and if it's not, provide a popup with the message 
			// "Can we turn on and turn off the GPS just to get your location"

			if(!providers.isEmpty()) {
				if(locationManager.isProviderEnabled(locationManager.GPS_PROVIDER) ){
					Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
					double latitude = location.getLatitude();
					double longitude = location.getLongitude();
					latlon = new String(String.valueOf(latitude) + "," + String.valueOf(longitude));
				} else {
					//buildAlertMessageNoGps();
					latlon = "NO GPS";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return latlon;
	}
}

