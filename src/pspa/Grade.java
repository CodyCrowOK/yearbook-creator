package pspa;

import java.io.Serializable;
import java.util.ArrayList;

public class Grade implements Serializable {
	private static final long serialVersionUID = 8203183133859865476L;
	public String name;
	public ArrayList<HomeRoom> homeRooms;
	public ArrayList<Person> people;
	
	public Grade() {
		homeRooms = new ArrayList<HomeRoom>();
		people = new ArrayList<Person>();
	}
	
	public Grade(String name) {
		this();
		this.name = name;
	}

	public void addStudentToHomeRoom(Person person, String homeRoomName) {
		for (HomeRoom h : homeRooms) {
			if (h.name.equalsIgnoreCase(homeRoomName)) {
				h.people.add(person);
				return;
			}
		}
		//If there wasn't a home room, create one.
		HomeRoom hr = new HomeRoom(homeRoomName);
		hr.people.add(person);
		homeRooms.add(hr);
	}
	
	@Override
	public String toString() {
		String str = "";
		for (int i = 0; i < 80; i++) str += "*";
		str += "\nGrade: " + name + "\n";
		for (Person p : people) {
			str += p + "\n";
		}
		for (HomeRoom h : homeRooms) {
			str += h + "\n";
		}
		for (int i = 0; i < 80; i++) str += "*";
		str += "\n";
		return str;
	}
}
