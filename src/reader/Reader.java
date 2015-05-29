package reader;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import writer.Yearbook;

public class Reader {
	Display display;
	Shell shell;
	
	Yearbook yearbook;
	
	public Reader() {
		display = new Display();
		shell = new Shell(display);
	}
	
	public static void main(String[] args) {
		//TODO
	}

}
