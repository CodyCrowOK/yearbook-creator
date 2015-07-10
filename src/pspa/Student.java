package pspa;

import java.io.Serializable;

public class Student extends Person implements Serializable {
	private static final long serialVersionUID = 2127488416936664134L;

	public Student(String firstName, String lastName, String folderName,
			String fileName) {
		super(firstName, lastName, folderName, fileName);
	}

	@Override
	public boolean isStudent() {
		return true;
	}
}
