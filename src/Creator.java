import javax.swing.text.StyleConstants.ColorConstants;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Creator {
	
	//Use the file extension .ctc for yearbook saves.
	
	public static final String VERSION = "0.01";
	public static final String COMPANY_NAME = "Digital Express";
	public static final String SOFTWARE_NAME = "Yearbook Creator";

	//General SWT
	private Display display;
	private Shell shell;
	
	//Menubar-related
	private Menu menubar;
	private MenuItem fileMenuItem;
	private Menu fileMenu;
	private MenuItem fileNewItem;
	private MenuItem fileNewPageItem;
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
	
	private GridLayout gridLayout;
	private GridData listGridData;
	private GridData canvasGridData;
	private GridData canvasGridData2;
	
	private List pagesList;
	private final Menu pagesListMenu;
	
	private Yearbook yearbook;

	private Canvas canvas;
	private Canvas rightCanvas;
	
	public Creator() {
		display = new Display();
		shell = new Shell(display);
		shell.setText(Creator.COMPANY_NAME + " " + Creator.SOFTWARE_NAME);

		shell.setSize(800, 600);
		
		this.buildMenu();
		this.setMenuListeners();
		
		this.initialize();

		
		//Create the layout.
		gridLayout = new GridLayout(7, true);
		shell.setLayout(gridLayout);
		
		pagesList = new List(shell, SWT.BORDER | SWT.V_SCROLL);
		listGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		listGridData.horizontalSpan = 1;
		pagesList.setLayoutData(listGridData);

		
		Composite canvasWrapper = new Composite(shell, SWT.NONE);
		canvas = new Canvas(canvasWrapper, SWT.BORDER);
		canvas.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		canvasGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		canvasGridData.horizontalSpan = 3;
		canvasWrapper.setLayoutData(canvasGridData);
		
		Composite canvasWrapper2 = new Composite(shell, SWT.NONE);
		rightCanvas = new Canvas(canvasWrapper2, SWT.BORDER);
		rightCanvas.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		canvasGridData2 = new GridData(SWT.FILL, SWT.FILL, true, true);
		canvasGridData2.horizontalSpan = 3;
		canvasWrapper2.setLayoutData(canvasGridData2);
		
		pagesListMenu = new Menu(pagesList);
		pagesList.setMenu(pagesListMenu);
		pagesListMenu.addMenuListener(new MenuAdapter()
		{
		    public void menuShown(MenuEvent e)
		    {
		        MenuItem[] items = pagesListMenu.getItems();
		        for (int i = 0; i < items.length; i++)
		        {
		            items[i].dispose();
		        }
		        int selectedPageIndex = pagesList.getSelectionIndex();
		        MenuItem item1 = new MenuItem(pagesListMenu, SWT.NONE);
		        item1.setText("Rename");
		        item1.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
					dialog.setText("Enter Name");
					dialog.setSize(400, 300);
					FormLayout formLayout = new FormLayout();
					formLayout.marginWidth = 10;
					formLayout.marginHeight = 10;
					formLayout.spacing = 10;
					dialog.setLayout(formLayout);
					
					Label label = new Label(dialog, SWT.NONE);
					label.setText("New name:");
					FormData data = new FormData();
					label.setLayoutData(data);
					
					Button cancel = new Button(dialog, SWT.PUSH);
					cancel.setText("Cancel");
					data = new FormData();
					data.width = 60;
					data.right = new FormAttachment(100, 0);
					data.bottom = new FormAttachment(100, 0);
					cancel.setLayoutData(data);
					cancel.addSelectionListener(new SelectionAdapter () {
						@Override
						public void widgetSelected(SelectionEvent e) {
							dialog.close();
						}
					});

					final Text text = new Text(dialog, SWT.BORDER);
					data = new FormData();
					data.width = 200;
					data.left = new FormAttachment(label, 0, SWT.DEFAULT);
					data.right = new FormAttachment(100, 0);
					data.top = new FormAttachment(label, 0, SWT.CENTER);
					data.bottom = new FormAttachment(cancel, 0, SWT.DEFAULT);
					text.setLayoutData(data);

					Button ok = new Button(dialog, SWT.PUSH);
					ok.setText("OK");
					data = new FormData();
					data.width = 60;
					data.right = new FormAttachment(cancel, 0, SWT.DEFAULT);
					data.bottom = new FormAttachment(100, 0);
					ok.setLayoutData(data);
					ok.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected (SelectionEvent e) {
							//dialog.close();
							
							yearbook.pages.get(selectedPageIndex).name = text.getText(); 
							
							refresh();
							dialog.close();
						}
					});

					dialog.setDefaultButton (ok);
					dialog.pack();
					dialog.open();
					
				}
		        	
		        });
		        
		        MenuItem item2 = new MenuItem(pagesListMenu, SWT.NONE);
		        item2.setText("Delete");
		        item2.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
					messageBox.setText("Delete Page");
					messageBox.setMessage("Are you sure you want to delete this page?\n\t" + yearbook.pages.get(selectedPageIndex));
					int yesno = messageBox.open();
					if (yesno == SWT.YES) {
						yearbook.pages.remove(selectedPageIndex);
						refresh();
					}
					
				}
		        	
		        });
		    }
		});
		
		
		shell.setMaximized(true);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())	display.sleep();
		}
		display.dispose();
		
	}
	
	private void buildMenu() {
		//Create the menu bar.
		menubar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menubar);
		
		//Create the file menu.
		fileMenuItem = new MenuItem(menubar, SWT.CASCADE);
		fileMenuItem.setText("&File");
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuItem.setMenu(fileMenu);

		fileNewItem = new MenuItem(fileMenu, SWT.PUSH);
		fileNewItem.setText("New &Yearbook\tCtrl+Shift+N");
		fileNewItem.setAccelerator(SWT.MOD1 | SWT.MOD2 | 'N');

		fileNewPageItem = new MenuItem(fileMenu, SWT.PUSH);
		fileNewPageItem.setText("&New Page\tCtrl+N");
		fileNewPageItem.setAccelerator(SWT.MOD1 | 'N');

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
	}
	
	private void initialize() {
		//TODO finish initializer
		fileNewItem.getListeners(SWT.Selection)[0].handleEvent(new Event());
	}
	
	/**
	 * Sets the listeners for each item in the menu bar.
	 */
	private void setMenuListeners() {
		fileNewItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				dialog.setText("Enter Yearbook Name");
				dialog.setSize(400, 300);
				FormLayout formLayout = new FormLayout();
				formLayout.marginWidth = 10;
				formLayout.marginHeight = 10;
				formLayout.spacing = 10;
				dialog.setLayout(formLayout);
				
				Label label = new Label(dialog, SWT.NONE);
				label.setText("New yearbook name:");
				FormData data = new FormData();
				label.setLayoutData(data);
				
				Button cancel = new Button(dialog, SWT.PUSH);
				cancel.setText("Cancel");
				data = new FormData();
				data.width = 60;
				data.right = new FormAttachment(100, 0);
				data.bottom = new FormAttachment(100, 0);
				cancel.setLayoutData(data);
				cancel.addSelectionListener(new SelectionAdapter () {
					@Override
					public void widgetSelected(SelectionEvent e) {
						dialog.close();
					}
				});

				final Text text = new Text(dialog, SWT.BORDER);
				data = new FormData();
				data.width = 200;
				data.left = new FormAttachment(label, 0, SWT.DEFAULT);
				data.right = new FormAttachment(100, 0);
				data.top = new FormAttachment(label, 0, SWT.CENTER);
				data.bottom = new FormAttachment(cancel, 0, SWT.DEFAULT);
				text.setLayoutData(data);

				Button ok = new Button(dialog, SWT.PUSH);
				ok.setText("OK");
				data = new FormData();
				data.width = 60;
				data.right = new FormAttachment(cancel, 0, SWT.DEFAULT);
				data.bottom = new FormAttachment(100, 0);
				ok.setLayoutData(data);
				ok.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected (SelectionEvent e) {
						createNewYearbook(text.getText());
						dialog.close();
						refresh();
					}
				});

				dialog.setDefaultButton (ok);
				dialog.pack();
				dialog.open();
				
			}
			
		});
		
		fileNewPageItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				dialog.setText("Enter Page Name");
				dialog.setSize(400, 300);
				FormLayout formLayout = new FormLayout();
				formLayout.marginWidth = 10;
				formLayout.marginHeight = 10;
				formLayout.spacing = 10;
				dialog.setLayout(formLayout);
				
				Label label = new Label(dialog, SWT.NONE);
				label.setText("New page name:");
				FormData data = new FormData();
				label.setLayoutData(data);
				
				Button cancel = new Button(dialog, SWT.PUSH);
				cancel.setText("Cancel");
				data = new FormData();
				data.width = 60;
				data.right = new FormAttachment(100, 0);
				data.bottom = new FormAttachment(100, 0);
				cancel.setLayoutData(data);
				cancel.addSelectionListener(new SelectionAdapter () {
					@Override
					public void widgetSelected(SelectionEvent e) {
						dialog.close();
					}
				});

				final Text text = new Text(dialog, SWT.BORDER);
				data = new FormData();
				data.width = 200;
				data.left = new FormAttachment(label, 0, SWT.DEFAULT);
				data.right = new FormAttachment(100, 0);
				data.top = new FormAttachment(label, 0, SWT.CENTER);
				data.bottom = new FormAttachment(cancel, 0, SWT.DEFAULT);
				text.setLayoutData(data);

				Button ok = new Button(dialog, SWT.PUSH);
				ok.setText("OK");
				data = new FormData();
				data.width = 60;
				data.right = new FormAttachment(cancel, 0, SWT.DEFAULT);
				data.bottom = new FormAttachment(100, 0);
				ok.setLayoutData(data);
				ok.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected (SelectionEvent e) {
						createNewPage(text.getText());
						dialog.close();
					}
				});

				dialog.setDefaultButton (ok);
				dialog.pack();
				dialog.open();
				
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
	
	private void createNewYearbook(String name) {
		yearbook = new Yearbook(name);

		int canvasHeight = display.getClientArea().height - 100;
		
		yearbook.settings.height = canvasHeight;
		yearbook.settings.width = (int) ((8.5 / 11.0) * canvasHeight);
		canvas.setSize(yearbook.settings.width, yearbook.settings.height);
		rightCanvas.setSize(yearbook.settings.width, yearbook.settings.height);
	}

	private void updatePageList() {
		pagesList.removeAll();
		for (int i = 0; i < yearbook.pages.size(); i++) {
			pagesList.add("Page " + (i + 1) + ": " + yearbook.pages.get(i).name);
		}
	}
	
	private void updateCanvas() {
		this.loadActivePage(yearbook.activePage);
	}
	
	private void loadActivePage(int activePage) {
		// TODO Auto-generated method stub
		
	}
	
	private void createNewPage(String name) {
		yearbook.pages.add(new YearbookPage(name));
		refresh();
	}

	public void refresh() {
		updatePageList();
		updateCanvas();
	}
	
	public static void main(String[] args) {
		new Creator();
	}

}
