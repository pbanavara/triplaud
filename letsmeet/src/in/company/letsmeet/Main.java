package in.company.letsmeet;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.location.Criteria;
import android.os.Bundle;
import android.view.View;

/**
 * @author pradeep
 *
 */
public class Main extends Activity {
	//private FrequentLocationListener listener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Common.MY_ID = String.valueOf(new Random().nextInt());
		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		Common.context = this;
		setContentView(R.layout.main);
	}
	
	public void openLetsMeet(View view) {
		Intent intent = new Intent(this, LetsMeetActivity.class);
		startActivity(intent);
	}
	
	public void dismissApp(View view) {
		this.finish();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
			
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//locationManager.removeUpdates(listener);
	}
		
}
