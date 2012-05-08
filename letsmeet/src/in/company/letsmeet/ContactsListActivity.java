package in.company.letsmeet;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;


import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author pradeep
 * Activity class to display the contact names and the phone numbers.
 */
public class ContactsListActivity extends ListActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Cursor mCursor = getContacts();
		startManagingCursor(mCursor);
		ArrayList<Contacts> values = new ArrayList<Contacts>();
		
		while(mCursor.moveToNext()) {
			String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			String phoneNumber = getPhoneNumber(name);
			Contacts contact = new Contacts();
			contact.setName(name);
			contact.setPhoneNumber(phoneNumber);
			contact.setSelected(false);
			values.add(contact);
		}
		
		//Construct an adapter to display contact names with check boxes.
		
		ArrayAdapter<Contacts> adapter = new ContactsListAdapter(this, values);
		setListAdapter(adapter);
		setContentView(R.layout.contactslist);
		/*
		Button confirmButton = (Button)findViewById(R.id.button1);
//		confirmButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Iterator<Contacts> iter = values.iterator();
			}
			
		});
		*/
		
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
	
	private String getPhoneNumber(String displayName) {
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
