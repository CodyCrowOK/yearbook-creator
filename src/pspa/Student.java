package pspa;

public class Student extends Person {
	
	public Student(String firstName, String lastName, String folderName,
			String fileName) {
		super(firstName, lastName, folderName, fileName);
	}

	@Override
	public boolean isStudent() {
		return true;
	}
}
