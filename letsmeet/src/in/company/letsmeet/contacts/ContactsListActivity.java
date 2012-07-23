package in.company.letsmeet.contacts;

import in.company.letsmeet.CommonMapActivity;
import in.company.letsmeet.R;
import in.company.letsmeet.common.Common;
import in.company.letsmeet.common.HttpConnectionHelper;
import in.company.letsmeet.common.SendSms;
import in.company.letsmeet.locationutil.BestLocationFinder;

import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * @author pradeep
 * Activity class to display the contact names and the phone numbers.
 */
public class ContactsListActivity extends Activity implements OnClickListener{
	
	private static final String TAG = "ContactsListActivity";
	private ArrayList<Contacts> values;
	private HttpConnectionHelper connectionHelper;
	BestLocationFinder finder;
	private Location location ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		finder = new BestLocationFinder(getApplicationContext(), LocationManager.NETWORK_PROVIDER,false);
		finder.getBestLocation(System.currentTimeMillis(),0);
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
		final ArrayAdapter<Contacts> adapter = new ContactsListAdapter(this, values);
		
		setContentView(R.layout.contactslist);
		Log.d(TAG, "Parent list size" + values.size());
		final ListView lView = (ListView)findViewById(R.id.mylist);
		lView.setAdapter(adapter);
		
		//Provide auto complete textView and set the adapter to that view
		EditText acView = (EditText) findViewById(R.id.autocomplete_contacts);
		acView.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
				
			}

			@Override
			public void onTextChanged(CharSequence filterText, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				adapter.getFilter().filter(filterText, lView);
			}
			
		});
		
		Button cButton = (Button)findViewById(R.id.contactsbutton);
		cButton.setOnClickListener(this);
		
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

	/**
	 * @param view
	 * Called when the confirm button is clicked. Check for selected contacts objects in the Arraylist, copy them to a JSONArray and 
	 * send to a HttpServer. Also send SMS messages to the selected contacts.
	 */
@Override
	public void onClick(View v) {
		if(v.getId() == R.id.contactsbutton) {
			try {
				ArrayList<String> smsContactList = new ArrayList<String>();
				JSONArray selectedContacts = new JSONArray();
				for(int i=0;i<values.size();++i) {
					Contacts contact = values.get(i);
					if(contact.isSelected()) {
						String name = contact.getName();
						String id = contact.getId();
						String phoneNumber = getPhoneNumberForContact(id);
						String friend_id = String.valueOf(new Random().nextInt(Integer.MAX_VALUE) +1);
						String smsContact = name + "," + phoneNumber + ","  + friend_id;
						smsContactList.add(smsContact);
						JSONObject newContact = new JSONObject();
						newContact.put("NAME", contact.getName());
						newContact.put("PHONE_NUMBER", friend_id);
						newContact.put("LOC", "");
						selectedContacts.put(newContact);
					}
				}	
				JSONObject finalObject = new JSONObject();
				finalObject.put("MYID", Common.MY_ID);
				location = Common.getLocation();
				String newLoc = location.getLatitude() + "," + location.getLongitude();
				finalObject.put("MYLOCATION", newLoc);
				finalObject.put("FRIENDS", selectedContacts);
				Log.i("ContactsList", String.valueOf(selectedContacts.length()));
				connectionHelper = new HttpConnectionHelper();
				connectionHelper.postData(Common.URL + "/id=" + Common.MY_ID, finalObject);
				SendSms sendSms = new SendSms(getApplicationContext());
				sendSms.sendBulkSms(smsContactList);	
				Log.i(TAG, "SMS Sent");
				Intent mapIntent = new Intent(this, CommonMapActivity.class);
				mapIntent.putExtra("singleusermode", false);
				startActivity(mapIntent);

			} catch (Exception je) {
				je.printStackTrace();
			}
		
		}
	}

}
