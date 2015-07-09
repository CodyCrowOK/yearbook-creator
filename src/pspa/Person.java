package pspa;

public abstract class Person {
	public String firstName;
	public String lastName;
	public String folderName;
	public String fileName;
	
	/**
	 * 
	 * @param firstName First name
	 * @param lastName Last name
	 * @param folderName Folder containing the image file
	 * @param fileName Image file name
	 */
	public Person(String firstName, String lastName, String folderName, String fileName) {
		this.fileName = fileName;
		this.firstName = firstName;
		this.folderName = folderName;
		this.lastName = lastName;
	}
	
	public boolean isFaculty() {
		return false;
	}
	
	public boolean isStudent() {
		return false;
	}
	
	@Override
	public String toString() {
		String str = "";
		str += firstName + " " + lastName + "\t" + folderName + "/" + fileName + "\n";
		return str;
	}
}
