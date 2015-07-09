package pspa;

public class Faculty extends Person {
	
	public Faculty(String firstName, String lastName, String folderName,
			String fileName) {
		super(firstName, lastName, folderName, fileName);
	}

	@Override
	public boolean isFaculty() {
		return true;
	}
}
