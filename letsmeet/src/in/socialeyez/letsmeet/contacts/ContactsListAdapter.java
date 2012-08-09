package in.socialeyez.letsmeet.contacts;

import in.socialeyez.letsmeet.R;

import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.TextView;

/**
 * @author pradeep
 * An implementation of Array adapter to display contact names with check boxes.
 */
public class ContactsListAdapter extends ArrayAdapter<Contacts> {
	private final Activity context;
	private final ArrayList<Contacts> contactValues;
	private ContactsFilter cFilter;
	private final Object myLock = new Object();

	public ContactsListAdapter(Activity context, ArrayList<Contacts> values) {
		super(context, R.layout.contactslist, values);
		this.context = context;
		this.contactValues = values;
		
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		//Load and inflate the contactslist xml
		LayoutInflater inflater = context.getLayoutInflater();
		//row.xml contains the layout details for each row
		view = inflater.inflate(R.layout.row, null);
		TextView tv = (TextView)view.findViewById(R.id.label);
		if( position < contactValues.size()) {
			tv.setText(this.contactValues.get(position).getName());
			CheckBox check = (CheckBox)view.findViewById(R.id.phonecheckbox);
			final Contacts contact = contactValues.get(position);
			final int pos = position;
			check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub				
					contact.setSelected(buttonView.isChecked());
					contactValues.set(pos, contact);
				}
			});
		} else {
			view = convertView;
		}
		return view;
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		if (cFilter == null) {
			cFilter = new ContactsFilter();
		}
		return cFilter;
	}

	private class ContactsFilter extends Filter {
		private static final String TAG = "ContactsFilter";
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// TODO Auto-generated method stub
			FilterResults results = new FilterResults();

			if(contactValues == null) {
				synchronized(myLock) {
					//contactValues = new ArrayList<Contacts>(); 
				}
			}
			if (constraint == null || constraint.length() == 0) {
				synchronized(myLock) {
					Log.d(TAG, "constraint is" + constraint);
					ArrayList<Contacts> list = new ArrayList<Contacts>(contactValues);
					//list.addAll(contactValues);
					Log.d(TAG, "List size" + list.size() + contactValues.size());
					results.values = list;
					results.count = list.size();
				}
			} else {
				String prefixString = constraint.toString().toLowerCase();
				final ArrayList<Contacts> values = contactValues;
				final int count = values.size();
				final ArrayList<Contacts> newValues = new ArrayList<Contacts>(count);

				for(int i=0;i<count;++i) {
					final Contacts value = values.get(i);
					final String valueText = value.getName().toLowerCase();

					if(valueText.startsWith(prefixString)) {
						newValues.add(value);
					} else {
						final String[] words = valueText.split(" ");
						for(int k=0;k<words.length;++k) {
							if(words[k].startsWith(prefixString)) {
								newValues.add(value);
								break;
							}
						}
					}
				}
				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			ArrayList<Contacts> subItems = (ArrayList<Contacts>)results.values;
			// TODO Auto-generated method stub
			//contactValues = (ArrayList<Contacts>) results.values;
			Log.d(TAG, "results" + results.count);
			if(results.count > 0) {	
				clear();
				for(int i = 0; i < subItems.size(); i++) {
					   add((Contacts) subItems.get(i));
				}
				//notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}

		}

	};

}
