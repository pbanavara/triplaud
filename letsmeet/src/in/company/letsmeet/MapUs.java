package in.company.letsmeet;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class MapUs extends Activity {
	private WebView wv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		String id = getIntent().getExtras().getString("myid");
		setContentView(R.layout.mapus);
		String baseUrl = "http://ec2-122-248-211-48.ap-southeast-1.compute.amazonaws.com:8080/temp.html";
		String newUrl = baseUrl + "?mylocation=" +id + "&locations=12.97745,77.585875";
		Log.i("MAP US url", newUrl);
		wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl(newUrl);
	}
}
