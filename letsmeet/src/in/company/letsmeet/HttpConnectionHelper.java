package in.company.letsmeet;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.util.Log;

/**
 * @author pradeep
 * Helper class to send Http get/post requests.
 */
public class HttpConnectionHelper {
	private static final String TAG = "HttpConnectionHelper";
	
	public void postData(String url, JSONObject object) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);	
			StringEntity jsonData = new StringEntity(object.toString(1));
			jsonData.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
			post.setEntity(jsonData);	
			org.apache.http.HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getData(String url) {
	String newLine = "";
	try{
		Log.i(TAG, "In Get Request");
		HttpClient client = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(url);
		getRequest.setHeader(HTTP.CONTENT_TYPE, "application/json");
		org.apache.http.HttpResponse response = client.execute(getRequest);
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent()));
		String line = "";
		
		while ((line = rd.readLine()) != null) {
			newLine = line.concat(line);
		}
		
		return newLine;
	}catch(Exception e) {
		e.printStackTrace();
	}
	return newLine;
	}

}
