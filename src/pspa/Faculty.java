package pspa;

import java.io.Serializable;

public class Faculty extends Person implements Serializable {
	private static final long serialVersionUID = -2220839399952264538L;

	public Faculty(String firstName, String lastName, String folderName,
			String fileName) {
		super(firstName, lastName, folderName, fileName);
	}

	@Override
	public boolean isFaculty() {
		return true;
	}
}
