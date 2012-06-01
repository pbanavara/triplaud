package in.company.letsmeet;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class ShowDialog extends Activity {
	private static final int DIALOG_ALERT = 10;

	private String sender;
	private String myNumber;
	private HttpConnectionHelper connectionHelper;
	private LocationListener listener;
	private WebView wv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mapus);
		
		// TODO Auto-generated method stub
		sender = getIntent().getExtras().getString("sender");
		myNumber = getIntent().getExtras().getString("mynumber");
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
			try{
				//Push the current location to the back-end server as a JSON object.
				startLocationUpdates();
				if (Common.getLocation() != null) {
					connectionHelper = new HttpConnectionHelper();
					JSONObject obj = new JSONObject();
					obj.put("id", myNumber);
					obj.put("loc", Common.getLocation());
					//obj.put("loc", "12.960298314609597,77.63908227742058");
					connectionHelper.postData(Common.URL, obj);
					wv = (WebView)findViewById(R.id.webView1);
					wv.getSettings().setJavaScriptEnabled(true);
					wv.loadUrl(Common.URL);	
					wv = (WebView)findViewById(R.id.webView1);
					wv.getSettings().setJavaScriptEnabled(true);
					wv.loadUrl(Common.URL);
					wv.setWebViewClient(new WebViewClient() {
						public boolean shouldOverrideUrlLoading(WebView view, String url) {
							view.loadUrl(url);
							return false;
						}
					});

				}
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
	
	/**
	 * Fires off the GPS listener for gathering location updates.
	 */
	public void startLocationUpdates() {
		try{
			Common.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			listener = new LocListener();
			if(Common.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Common.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
			} else if(Common.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				Common.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}

