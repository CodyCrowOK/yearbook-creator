package reader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import writer.DigitalYearbook;

public class Reader {
	DigitalYearbook digitalYearbook;
	
	public Reader() throws IOException, ClassNotFoundException {
		this.digitalYearbook = readYearbook();
	}
	
	private DigitalYearbook readYearbook() throws IOException, ClassNotFoundException {
		Display display = new Display();
		Shell shell = new Shell(display);
		
		FileDialog picker = new FileDialog(shell, SWT.OPEN);
		picker.setText("Open Digital Yearbook");
		String fileName = picker.open();
		if (fileName == null) return null;
		
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		DigitalYearbook dy = (DigitalYearbook) ois.readObject();
		ois.close();
		return dy;
	}

	public static void main(String[] args) {
		
		try {
			new Reader();
			//System.out.println(reader.digitalYearbook.name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
