package in.company.letsmeet;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * @author pradeep
 * An implementation of Array adapter to display contact names with check boxes.
 */
public class ContactsListAdapter extends ArrayAdapter<Contacts> {
	private final Activity context;
	private final ArrayList<Contacts> values;
	
	public ContactsListAdapter(Activity context, ArrayList<Contacts> values) {
		super(context, R.layout.contactslist, values);
		this.context = context;
		this.values = values;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
	//	if(convertView == null) {
			//Load and inflate the contactslist xml
			LayoutInflater inflater = context.getLayoutInflater();
			//row.xml contains the layout details for each row
			view = inflater.inflate(R.layout.row, null);
			TextView tv = (TextView)view.findViewById(R.id.label);
			tv.setText(this.values.get(position).getName());
			CheckBox check = (CheckBox)view.findViewById(R.id.phonecheckbox);
			final Contacts contact = values.get(position);
			final int pos = position;
			check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub				
					contact.setSelected(buttonView.isChecked());
					values.set(pos, contact);
				}
			});
			
	//	} else {
	//		view = convertView;
	//	}
		return view;
	}
	
}
