package in.socialeyez.letsmeet.contacts;

import greendroid.app.ActionBarActivity;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import in.socialeyez.letsmeet.CommonMapActivity;
import in.socialeyez.letsmeet.Main;
import in.socialeyez.letsmeet.R;
import in.socialeyez.letsmeet.SearchMapActivity;
import in.socialeyez.letsmeet.common.Common;
import in.socialeyez.letsmeet.common.HttpConnectionHelper;
import in.socialeyez.letsmeet.common.SendSms;
import in.socialeyez.letsmeet.locationutil.BestLocationFinder;

import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;


/**
 * @author pradeep
 * Activity class to display the contact names and the phone numbers.
 */
public class InviteContactsActivity extends GDActivity implements OnClickListener{

	private static final String TAG = "ContactsListActivity";
	private ArrayList<Contacts> values;
	private HttpConnectionHelper connectionHelper;
	BestLocationFinder finder;
	private Location location ;
	private ArrayList<String> smsContactList;
	private JSONObject finalObject = null;
	private JSONArray selectedContacts = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Hide the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//End Hide title bar
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

		//Start GreenDroid
		setActionBarContentView(R.layout.contactslist);
		addActionBarItem(Type.LocateMyself, R.id.action_bar_search);
		addActionBarItem(Type.AllFriends, R.id.action_bar_allfriends);
		//End GreenDroid
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
				Log.d(TAG, "Positions :::" + start + ":" + before + ":" + count);
				adapter.getFilter().filter(filterText, lView);
			}

		});

		Button cButton = (Button)findViewById(R.id.contactsbutton);
		cButton.setOnClickListener(this);
		Button exploreButton = (Button)findViewById(R.id.fsbutton);
		exploreButton.setOnClickListener(this);

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
		
		try {
			smsContactList = new ArrayList<String>();
			selectedContacts = new JSONArray();
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
			finalObject = new JSONObject();
			finalObject.put("MYID", Common.MY_ID);
			location = Common.getLocation();
			String newLoc = location.getLatitude() + "," + location.getLongitude();
			finalObject.put("MYLOCATION", newLoc);
			finalObject.put("FRIENDS", selectedContacts);
		if(v.getId() == R.id.contactsbutton) {
			
			

				Log.i("ContactsList", String.valueOf(selectedContacts.length()));
				Intent mapIntent = new Intent(this, SearchMapActivity.class);
				mapIntent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Search meeting place");
				mapIntent.putStringArrayListExtra("CONTACTS", smsContactList);
				mapIntent.putExtra("OBJECT", finalObject.toString());
				startActivity(mapIntent);
		
		//Location choices from foursquare
		} else if ( v.getId() == R.id.fsbutton) {
			showLocationChooserDialog();
			//Open CommonMapActivity
			
		}
		} catch (Exception je) {
			je.printStackTrace();
		}
	}
	
	
	private void showLocationChooserDialog() {
		final Dialog dialog = new Dialog(this, android.R.style.Theme_Dialog);
		dialog.setContentView(R.layout.chooseoccasion);
		dialog.setTitle(Common.DIALOG_TITLE);
		dialog.setCancelable(true);
		Button dialogButton = (Button) dialog.findViewById(R.id.chButtonYes);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RadioButton coffeeButton = (RadioButton) dialog.findViewById(R.id.chRadioCoffee);
				RadioButton restaurantButton = (RadioButton) dialog.findViewById(R.id.chRadioCoffee);
				RadioButton barButton = (RadioButton) dialog.findViewById(R.id.chRadioCoffee);
				if(coffeeButton.isChecked()) {
					Common.setSelectedRestaurantType("cafe");
				} else if (restaurantButton.isChecked()) {
					Common.setSelectedRestaurantType("restaurant");
				} else if(barButton.isChecked()) {
					Common.setSelectedRestaurantType("bar");
				}
				
				String locationChoice = Common.getSelectedRestaurantType();
				try {
					finalObject.put("OCCASION", locationChoice);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				connectionHelper = new HttpConnectionHelper();
				connectionHelper.postData(Common.URL + "/id=" + Common.MY_ID, finalObject);	
				SendSms sendSms = new SendSms(getApplicationContext());
				sendSms.sendBulkSms(smsContactList);
				Intent commonMapIntent = new Intent(InviteContactsActivity.this, CommonMapActivity.class);
				commonMapIntent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Group center point");
				commonMapIntent.putExtra("FS", "yes");
				startActivity(commonMapIntent);
				dialog.dismiss();
			}
		});
		dialog.show();
		
		
	}
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.action_bar_search:
			intent = new Intent(this, SearchMapActivity.class);
			intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Search meeting place");
			startActivity(intent);
			break;

		case R.id.action_bar_allfriends:
			//Start the contactsList Activity with the required parameters
			intent = new Intent(this, InviteContactsActivity.class);
			intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "Invite Friends");
			startActivityForResult(intent, Common.START_CONTACT_LIST);
			break;

		case R.id.action_bar_home:
			intent = new Intent(this, Main.class);
			intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "LetsMeet");
			startActivity(intent);
			break;

		default:
			return super.onHandleActionBarItemClick(item, position);
		}

		return true;
	}

}
