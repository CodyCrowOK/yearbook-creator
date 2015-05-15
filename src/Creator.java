import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;


public class Creator {
	
	public static final String VERSION = "0.01";
	public static final String COMPANY_NAME = "Digital Express";
	public static final String SOFTWARE_NAME = "Yearbook Creator";

	private Display display;
	private Shell shell;
	private Menu menubar;
	private MenuItem fileMenuItem;
	private Menu fileMenu;
	private MenuItem fileNewItem;
	private MenuItem fileOpenItem;
	private MenuItem fileSaveAsItem;
	private MenuItem fileExportItem;
	private MenuItem fileCloseItem;
	private MenuItem editMenuItem;
	private Menu editMenu;
	private MenuItem editUndoItem;
	private MenuItem editRedoItem;
	private MenuItem editCutItem;
	private MenuItem editCopyItem;
	private MenuItem editPasteItem;
	private MenuItem editPreferencesItem;
	private MenuItem helpMenuItem;
	private Menu helpMenu;
	private MenuItem helpAboutItem;
	
	public Creator() {
		display = new Display();
		shell = new Shell(display);
		shell.setText(Creator.COMPANY_NAME + " " + Creator.SOFTWARE_NAME);
		
		shell.setSize(800, 600);
		
		//Create the menu bar.
		menubar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menubar);
		
		//Create the file menu.
		fileMenuItem = new MenuItem(menubar, SWT.CASCADE);
		fileMenuItem.setText("&File");
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuItem.setMenu(fileMenu);

		fileNewItem = new MenuItem(fileMenu, SWT.PUSH);
		fileNewItem.setText("&New\tCtrl+N");
		fileNewItem.setAccelerator(SWT.MOD1 + 'N');

		fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenItem.setText("&Open\tCtrl+O");
		fileOpenItem.setAccelerator(SWT.MOD1 + 'O');
		
		new MenuItem(fileMenu, SWT.SEPARATOR);

		fileSaveAsItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveAsItem.setText("Save &As...\tCtrl+Shift+S");
		fileSaveAsItem.setAccelerator(SWT.MOD1 | SWT.MOD2 | 'S');

		fileExportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExportItem.setText("&Export...");

		new MenuItem(fileMenu, SWT.SEPARATOR);

		fileCloseItem = new MenuItem(fileMenu, SWT.PUSH);
		fileCloseItem.setText("Close\tAlt+F4");
		//Probably handled by the window manager...
		fileCloseItem.setAccelerator(SWT.MOD3 | SWT.F4);
		
		
		//Create the edit menu.
		editMenuItem = new MenuItem(menubar, SWT.CASCADE);
		editMenuItem.setText("&Edit");
		Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
		editMenuItem.setMenu(editMenu);

		editUndoItem = new MenuItem(editMenu, SWT.PUSH);
		editUndoItem.setText("&Undo\tCtrl+Z");
		editUndoItem.setAccelerator(SWT.MOD1 + 'Z');

		editRedoItem = new MenuItem(editMenu, SWT.PUSH);
		editRedoItem.setText("&Redo\tCtrl+Y");
		editRedoItem.setAccelerator(SWT.MOD1 + 'Y');

		new MenuItem(editMenu, SWT.SEPARATOR);

		editCutItem = new MenuItem(editMenu, SWT.PUSH);
		editCutItem.setText("Cu&t\tCtrl+X");
		editCutItem.setAccelerator(SWT.MOD1 + 'X');

		editCopyItem = new MenuItem(editMenu, SWT.PUSH);
		editCopyItem.setText("&Copy\tCtrl+C");
		editCopyItem.setAccelerator(SWT.MOD1 + 'C');

		editPasteItem = new MenuItem(editMenu, SWT.PUSH);
		editPasteItem.setText("&Paste\tCtrl+V");
		editPasteItem.setAccelerator(SWT.MOD1 + 'V');

		new MenuItem(editMenu, SWT.SEPARATOR);

		editPreferencesItem = new MenuItem(editMenu, SWT.PUSH);
		editPreferencesItem.setText("Preferences");
		
		
		//Create the help menu.
		helpMenuItem = new MenuItem(menubar, SWT.CASCADE);
		helpMenuItem.setText("&Help");
		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuItem.setMenu(helpMenu);
		
		helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutItem.setText("&About " + Creator.SOFTWARE_NAME);
		helpAboutItem.setAccelerator(SWT.MOD1 + 'Z');
		
		this.setMenuListeners();
		
		
		shell.setMaximized(true);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())	display.sleep();
		}
		display.dispose();
	}
	
	/**
	 * Sets the listeners for each item in the menu bar.
	 */
	private void setMenuListeners() {
		fileNewItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				fileNew();
				
			}
			
		});
		
		fileOpenItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("File >> Open not implemented.");
				
			}
			
		});
		
		fileSaveAsItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("File >> Save As... not implemented.");
				
			}
			
		});
		
		fileExportItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("File >> Export... not implemented.");
				
			}
			
		});
		
		fileCloseItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("File >> Close not implemented.");
				
			}
			
		});
		
		editUndoItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("Edit >> Undo not implemented.");
				
			}
			
		});
		
		editRedoItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("Edit >> Redo not implemented.");
				
			}
			
		});
		
		editCutItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("Edit >> Cut not implemented.");
				
			}
			
		});
		
		editCopyItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("Edit >> Copy not implemented.");
				
			}
			
		});
		
		editPasteItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("Edit >> Paste not implemented.");
				
			}
			
		});
		
		editPreferencesItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("Edit >> Preferences not implemented.");
				
			}
			
		});
		
		helpAboutItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				MessageBox helpBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				helpBox.setText("About " + Creator.SOFTWARE_NAME);
				helpBox.setMessage("Version " + Creator.VERSION + "\n"
						+ "Copyright © 2015 " + Creator.COMPANY_NAME);
				helpBox.open();
				
			}
			
		});
		
	}
	
	private void fileNew() {
		System.out.println("File >> New not implemented.");
	}
	
	public static void main(String[] args) {
		new Creator();
	}

}
