package in.company.letsmeet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * @author pradeep
 * Activity class to display the contact names and the phone numbers.
 */
public class ContactsListActivity extends ListActivity {
	private ArrayList<Contacts> values;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Cursor mCursor = getContacts();
		startManagingCursor(mCursor);
		values = new ArrayList<Contacts>();
		ArrayList<String> tempValues = new ArrayList<String>();

		while(mCursor.moveToNext()) {
			String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			//	String phoneNumber = getPhoneNumber(name);
			Contacts contact = new Contacts();
			contact.setName(name);
			contact.setSelected(false);
			values.add(contact);
		}

		//Construct an adapter to display contact names with check boxes.

		ArrayAdapter<Contacts> adapter = new ContactsListAdapter(this, values);
		setListAdapter(adapter);
		setContentView(R.layout.contactslist);
	}

	public void sendSelectedContacts(View view) {
		try {
			JSONArray selectedContacts = new JSONArray();
			for(int i=0;i<values.size();++i) {
				Contacts contact = values.get(i);
				if(contact.isSelected()) {
					String name = contact.getName();
					String phoneNumber = getPhoneNumberForContact(name);
					JSONObject newContact = new JSONObject();
					newContact.put("NAME", contact.getName());
					newContact.put("PHONE_NUMBER", phoneNumber);
					selectedContacts.put(newContact);
				}
			}
			Log.i("ContactsList", String.valueOf(selectedContacts.length()));
			testHttpConnection(selectedContacts);
		} catch (Exception je) {
			je.printStackTrace();
		}
		Toast.makeText(getApplicationContext(), "Your cntacts have been sent", Toast.LENGTH_SHORT);
		//this.finish();
	}

	private Cursor getContacts() {
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME };
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
				+ ("1") + "'";
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";

		return managedQuery(uri, projection, selection, selectionArgs,
				sortOrder);
	}

	private String getPhoneNumberForContact(String displayName) {
		String phoneNumber = new String("");
		ContentResolver resolver = getContentResolver();
		try {
			Cursor cur = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.
					CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] {displayName}, null);
			while(cur.moveToNext()) {
				phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return phoneNumber;
	}

	private void testHttpConnection(JSONArray data) {
		try {
			HttpClient client = new DefaultHttpClient();
			String url = "http://ec2-122-248-211-48.ap-southeast-1.compute.amazonaws.com:8080";
			String postUrl = url+ "?data=" + data;
			HttpPost post = new HttpPost(postUrl);	
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

}
