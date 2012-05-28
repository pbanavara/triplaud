package in.company.letsmeet;

/**
 * @author pradeep
 * Custom object to hold a contact and the selected value. Could have used a hashmap but for the required boolean isSelected.
 */
public class Contacts {
	private String name;
	private String phoneNumber;
	private boolean isSelected;
	private String id;
	private String location;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String id) {
		this.location = id;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public boolean isSelected() {
		return isSelected;
	}
	
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
}
