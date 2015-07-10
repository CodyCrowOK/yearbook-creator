package pspa;

import java.io.Serializable;

public enum Keys {
	HOMEROOM("Home Room"),
	FILENAME("Image File Name"),
	TEACHER("Teacher Name"),
	FOLDER("Image Folder"),
	FIRSTNAME("First Name"),
	PERIOD("Period"),
	GRADE("Grade"),
	LASTNAME("Last Name"),
	VOLNAME("Volume Name"),
	TRACK("Track");

	private final String text;

	private Keys(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
