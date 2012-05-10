package in.company.letsmeet;

/**
 * @author pradeep
 * Custom object to hold a contact and the selected value. Could have used a hashmap but for the required boolean isSelected.
 */
public class Contacts {
	private String name;
	private String phoneNumber;
	private boolean isSelected;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
