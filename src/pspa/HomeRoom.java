package pspa;

import java.io.Serializable;
import java.util.ArrayList;

public class HomeRoom implements Serializable {
	private static final long serialVersionUID = 436268447082694482L;
	public String name;
	public ArrayList<Person> people;
	
	public HomeRoom() {
		people = new ArrayList<Person>();
	}
	
	public HomeRoom(String name) {
		this();
		this.name = name;
	}
	
	@Override
	public String toString() {
		String str = "Home room: " + name + "\n";
		for (Person p : people) {
			str += p;
		}
		return str;
	}
}
