package in.company.letsmeet;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MapUs extends Activity {
	private WebView wv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapus);
		//wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl(Common.URL);
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
