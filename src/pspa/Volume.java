package pspa;

import java.io.*;
import java.util.*;

import org.eclipse.swt.graphics.Point;

import writer.YearbookTextElement;

/**
 * Data model for a volume from a PSPA disk.
 * @author Cody Crow
 *
 */
public class Volume implements Serializable {
	private static final long serialVersionUID = 6310827207245198135L;
	public String name;
	public String fileName;
	public String path;
	public Point grid;
	public ArrayList<Grade> grades;
	public YearbookTextElement textElement;
	HashMap<String, Integer> columns;
	
	public Volume() {
		name = "";
		grades = new ArrayList<Grade>();
	}
	
	/**
	 * 
	 * @param root The root directory of the volume
	 * @throws IOException
	 * @throws PSPAIndexNotFoundException
	 */
	public Volume(File root) throws IOException, PSPAIndexNotFoundException {
		this();
		processRoot(root);
	}
	
	public Volume(int pageWidth, int pageHeight) {
		this();
		textElement = new YearbookTextElement(pageWidth, pageHeight);
	}
	
	public Volume(File root, int pageWidth, int pageHeight) throws IOException, PSPAIndexNotFoundException {
		this(root);
		textElement = new YearbookTextElement(pageWidth, pageHeight);
	}
	
	public void processRoot(File root) throws IOException, PSPAIndexNotFoundException {
		path = root.getAbsolutePath();
		fileName = root.getName();
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
			try {
				if (tokens[columns.get(Keys.GRADE.toString())].isEmpty()) continue;
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}
			
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
	
	public Grade getGradeByName(String string) {
		int i = findGrade(string);
		if (i < 0) return null;
		return grades.get(i);
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
	
	public static Point photoSpacing(Point grid, int pageWidth, int pageHeight) {
		
		int adjustedHeight = pageHeight;
		int adjustedWidth = pageWidth;
		
		int photoXWidth = (int) ((double) adjustedWidth / (grid.x));
		int photoYWidth = (int) ((double) adjustedHeight / (grid.y));
		
		int marginX = (int) (1 * (double) photoXWidth / (grid.x)); 
		int marginY = (int) (1 * (double) photoYWidth / (grid.y));
		
		return new Point(marginX, marginY);
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
