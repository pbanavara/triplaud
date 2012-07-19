package in.company.letsmeet;

import in.company.letsmeet.common.Common;
import in.company.letsmeet.common.HttpConnectionHelper;
import in.company.letsmeet.locationutil.BestLocationFinder;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;


public class ShowDialog extends Activity{
	private static final int DIALOG_ALERT = 10;
	private String sender;
	private String myNumber;
	private HttpConnectionHelper connectionHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		// TODO Auto-generated method stub
		sender = getIntent().getExtras().getString("sender");
		myNumber = getIntent().getExtras().getString("mynumber");
		super.onCreate(savedInstanceState);
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
			try{
				//Push the current location to the back-end server as a JSON object.
					BestLocationFinder finder = new BestLocationFinder(getApplicationContext(),LocationManager.NETWORK_PROVIDER,false);
					finder.getBestLocation(System.currentTimeMillis(),0);
					Location loc = Common.getLocation();
					if (loc == null) {
						Toast.makeText(getApplicationContext(), "Location Fix not obtained, Application closing", Toast.LENGTH_LONG).show();
						finish();
					}
					String locString = String.valueOf(loc.getLatitude()) + "," + String.valueOf(loc.getLongitude());
					//String locString = "12.981596" + "," + "77.628913";
					connectionHelper = new HttpConnectionHelper();
					JSONObject obj = new JSONObject();
					obj.put("id", myNumber);
					obj.put("loc", locString);
					connectionHelper.postData(Common.URL + "/id=" + Common.MY_ID, obj);
					Intent mapIntent = new Intent(getApplicationContext(), CommonMapActivity.class);
					mapIntent.putExtra("singleusermode", false);
					startActivity(mapIntent);
				
			} catch(Exception e){
				e.printStackTrace();
			}

		}
	}

	private final class CancelOnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			ShowDialog.this.finish();
		}
	}
	
	
}

