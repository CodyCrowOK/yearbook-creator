package pspa;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Point;

/**
 * Data model for a volume from a PSPA disk.
 * @author Cody Crow
 *
 */
public class Volume {
	public Point grid;
	public ArrayList<Grade> grades;
	//public ArrayList<Faculty> faculty;
	public HashMap<String, Integer> columns;
	
	/**
	 * 
	 * @param root The root directory of the volume
	 * @throws IOException
	 * @throws PSPAIndexNotFoundException
	 */
	public Volume(File root) throws IOException, PSPAIndexNotFoundException {
		grades = new ArrayList<Grade>();
		columns = new HashMap<String, Integer>();
		boolean hasMaster = false;
		boolean hasIndex = false;
		
		File[] files = root.listFiles();
		for (File f : files) {
			if (f.getName().equalsIgnoreCase("readme.txt")) {
				parseReadme(f);
				break;
			}
			
			if (f.getName().equalsIgnoreCase("master.txt")) {
				hasMaster = true;
				continue;
			}
			
			if (f.getName().equalsIgnoreCase("index.txt")) {
				hasIndex = true;
				continue;
			}
		}
		
		if (!(hasMaster || hasIndex)) throw new PSPAIndexNotFoundException();
		
		if (hasMaster) {
			for (File f : files) {
				if (f.getName().equalsIgnoreCase("master.txt")) {
					parseIndex(f);
					break;
				}
			}
		} else {
			for (File f : files) {
				if (f.getName().equalsIgnoreCase("index.txt")) {
					parseIndex(f);
					break;
				}
			}
		}
	}
	
	private void parseIndex(File f) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		String line;
		while (br.ready()) {
			line = br.readLine();
			String[] tokens = line.split("\t");
			
			//Skip if no grade...
			if (tokens[columns.get(Keys.GRADE.toString())].isEmpty()) continue;
			
			if (!gradeExists(tokens[columns.get(Keys.GRADE.toString())])) {
				Grade grade = new Grade(tokens[columns.get(Keys.GRADE.toString())]);
				grades.add(grade);
			}
			
			String firstName = tokens[columns.get(Keys.FIRSTNAME.toString())];
			String lastName = tokens[columns.get(Keys.LASTNAME.toString())];
			String fileName = tokens[columns.get(Keys.FILENAME.toString())];
			String folderName = tokens[columns.get(Keys.FOLDER.toString())];
			
			Person person;

			try {
				if (tokens[columns.get(Keys.HOMEROOM.toString())].isEmpty()) {
					person = new Faculty(firstName, lastName, folderName, fileName);
					grades.get(findGrade(tokens[columns.get(Keys.GRADE.toString())])).people.add(person);
				} else {
					person = new Student(firstName, lastName, folderName, fileName);
					grades.get(findGrade(tokens[columns.get(Keys.GRADE.toString())])).addStudentToHomeRoom(person, tokens[columns.get(Keys.HOMEROOM.toString())]);
				}
			} catch (Exception e) {
				//This is terrible but I'm doing it anyway.
				person = new Student(firstName, lastName, folderName, fileName);
				grades.get(findGrade(tokens[columns.get(Keys.GRADE.toString())])).people.add(person);
				
			}
			
		}
		br.close();
		
	}

	private int findGrade(String string) {
		int i = 0;
		for (Grade grade : grades) {
			if (grade.name.equalsIgnoreCase(string)) return i;
			i++;
		}
		return -1;
	}

	private void parseReadme(File readme) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(readme));
		ArrayList<String> lines = new ArrayList<String>();
		while (br.ready()) {
			lines.add(br.readLine());
		}
		
		for (String line : lines) {
			String[] tokens = line.split(" = ");
			if (tokens.length <= 1) continue;

			if (tokens[0].length() >= 19) {
				if (tokens[0].substring(0, 19).equalsIgnoreCase("[Field Definition #")) {
					int value = Integer.parseInt(tokens[0].substring(19).split("]")[0]) - 1;
					String key = tokens[1];
					columns.put(key, value);
				}
			}
		}
		br.close();
	}
	
	private boolean gradeExists(String s) {
		for (Grade grade : grades) {
			if (grade.name.equalsIgnoreCase(s)) return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		String str = "";
		for (Grade g : grades) {
			str += g + "\n";
		}
		return str;
	}
}
