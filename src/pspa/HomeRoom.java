package pspa;

import java.util.ArrayList;

public class HomeRoom {
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
