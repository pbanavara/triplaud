package in.company.letsmeet;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LetsMeetActivity extends Activity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meet);
		
		Spinner spin = (Spinner)findViewById(R.id.spinner1);
		String[] occasions = {"A drink", "Coffee", "Lunch", "Dinner"};
		ArrayAdapter ap = new ArrayAdapter(this, android.R.layout.simple_spinner_item,occasions);
		ap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(ap);
		
	}

	public void selectContacts(View v) {
		Intent intent = new Intent(this, ContactsListActivity.class);
		startActivity(intent);
	}

	

	
}