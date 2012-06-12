package in.company.letsmeet;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {
	
	private static final String TAG = "WebViewActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webv);
		String sourceLoc = getIntent().getExtras().getString("SOURCE");
		String destinationLoc = getIntent().getExtras().getString("DEST");
		WebView wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		String url = "file:///android_asset/route.html?source=" + sourceLoc + "&destination=" + destinationLoc;	
		Log.e(TAG, "URL" + url);
		wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});
		wv.loadUrl(url);
	}

}
