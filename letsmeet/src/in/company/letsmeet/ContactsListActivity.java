package in.company.letsmeet;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
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
	private static final int SMS_INTENT = 1;
	private HttpConnectionHelper connectionHelper;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Cursor mCursor = getContacts();
		startManagingCursor(mCursor);
		values = new ArrayList<Contacts>();

		//Parse the cursor and populate the ArrayList
		while(mCursor.moveToNext()) {
			String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			String id = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
			Contacts contact = new Contacts();
			contact.setName(name);
			contact.setId(id);
			contact.setSelected(false);
			values.add(contact);
		}

		//Construct the custom adapter to display contact names with check boxes.
		ArrayAdapter<Contacts> adapter = new ContactsListAdapter(this, values);
		setListAdapter(adapter);
		setContentView(R.layout.contactslist);
	}

	
	/**
	 * @param view
	 * Called when the confirm button is clicked. Check for selected contacts objects in the Arraylist, copy them to a JSONArray and 
	 * send to a HttpServer. Also send SMS messages to the selected contacts.
	 */
	public void sendSelectedContacts(View view) {
		Intent smsIntent = new Intent(this, SendSms.class);
		ArrayList<String> tempList = new ArrayList<String>();
		try {
			JSONArray selectedContacts = new JSONArray();
			for(int i=0;i<values.size();++i) {
				Contacts contact = values.get(i);
				if(contact.isSelected()) {
					String name = contact.getName();
					String id = contact.getId();
					String phoneNumber = getPhoneNumberForContact(id);
					String smsContact = name + "," + phoneNumber;
					tempList.add(smsContact);
					JSONObject newContact = new JSONObject();
					newContact.put("NAME", contact.getName());
					newContact.put("PHONE_NUMBER", phoneNumber);
					newContact.put("LOC", "");
					selectedContacts.put(newContact);
				}
			}
			JSONObject finalObject = new JSONObject();
			finalObject.put("MYID", Common.MY_ID);
			finalObject.put("MYLOCATION", "12.974934355257243,77.6464695623548");
			finalObject.put("FRIENDS", selectedContacts);
			Log.i("ContactsList", String.valueOf(selectedContacts.length()));
			smsIntent.putStringArrayListExtra("contacts", tempList);
			//startActivityForResult(smsIntent, SMS_INTENT);
			connectionHelper = new HttpConnectionHelper();
			connectionHelper.postData(Common.URL, finalObject);
			Intent mapIntent = new Intent(this, MapUs.class);
			mapIntent.putExtra("myid", Common.MY_ID);
			Thread.sleep(2000);
			startActivity(mapIntent);
		} catch (Exception je) {
			je.printStackTrace();
		}
		Toast.makeText(getApplicationContext(), "Your cntacts have been sent", Toast.LENGTH_SHORT);
		//this.finish();
	}

	/**
	 * @return cursor
	 * Return the phone contacts as a cursor
	 */
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

	
	/**
	 * @param displayName
	 * @return phoneNumber
	 * Given a display name query and obtain the phone number.
	 */
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

}
