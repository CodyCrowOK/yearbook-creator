import java.util.ArrayList;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Creator {
	
	//Use the file extension .ctc for yearbook saves.
	
	public static final String VERSION = "0.01";
	public static final String COMPANY_NAME = "Digital Express";
	public static final String SOFTWARE_NAME = "Smartbook Pro™";

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
	private MenuItem insertMenuItem;
	private Menu insertMenu;
	private MenuItem insertTextItem;
	private MenuItem insertImageItem;
	private MenuItem insertVideoItem;
	private MenuItem insertLinkItem;
	private MenuItem insertPageNumbersItem;
	private MenuItem insertToCItem;
	private MenuItem helpMenuItem;
	private Menu helpMenu;
	private MenuItem helpAboutItem;
	
	//Toolbar
	Composite toolbarWrapper;
	RowLayout barLayout;
	Button newBtn;
	Button openBtn;
	Button saveBtn;
	Button previewBtn;
	Button printBtn;
	Button undoBtn;
	Button redoBtn;
	Button cutBtn;
	Button copyBtn;
	Button pasteBtn;
	Button textBtn;
	Button imageBtn;
	Button videoBtn;
	Button linkBtn;
	Button zoomBtn;
	
	
	private Composite content;
	
	private GridLayout gridLayout;
	private GridData listGridData;
	private GridData canvasGridData;
	
	private List pagesList;
	private final Menu pagesListMenu;
	
	private Yearbook yearbook;

	private Canvas canvas;
	private Canvas rightCanvas;
	private Color canvasBackgroundColor;
	
	//private GC leftGC;
	//private GC rightGC;
	
	private Creator() {
		display = new Display();
		shell = new Shell(display);
		setWindowTitle(SWT.DEFAULT);

		shell.setSize(800, 600);
		
		this.buildMenu();
		this.setMenuListeners();
		
		this.initialize();
		
		//Create the layout.
		shell.setLayout(new ColumnLayout());
		
		this.buildToolbar();	

		gridLayout = new GridLayout(7, true);
		content = new Composite(shell, SWT.NONE);
		content.setLayout(gridLayout);
		
		pagesList = new List(content, SWT.BORDER | SWT.V_SCROLL);
		listGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		listGridData.horizontalSpan = 1;
		pagesList.setLayoutData(listGridData);

		this.initializeCanvas();
		
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
							
							yearbook.page(selectedPageIndex).name = text.getText(); 
							
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
					messageBox.setMessage("Are you sure you want to delete this page?\n\t" + yearbook.page(selectedPageIndex));
					int yesno = messageBox.open();
					if (yesno == SWT.YES) {
						yearbook.removePage(selectedPageIndex);
						refresh();
					}
					
				}
		        	
		        });
		    }
		});
		
		this.buildPagesListDnD();
		
		shell.setMaximized(true);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())	display.sleep();
		}
		display.dispose();
		
	}
	
	private void initializeCanvas() {

		Composite bigCanvasWrapper = new Composite(content, SWT.NONE);
		canvasGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		canvasGridData.horizontalSpan = 6;
		bigCanvasWrapper.setLayoutData(canvasGridData);
		bigCanvasWrapper.setLayout(new GridLayout(2, false));
		
		Composite canvasWrapper = new Composite(bigCanvasWrapper, SWT.NONE);
		canvas = new Canvas(canvasWrapper, SWT.BORDER);
		canvas.setBackground(canvasBackgroundColor);
		
		Composite canvasWrapper2 = new Composite(bigCanvasWrapper, SWT.NONE);
		rightCanvas = new Canvas(canvasWrapper2, SWT.BORDER);
		rightCanvas.setBackground(canvasBackgroundColor);
		
		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseDown(MouseEvent event) {
				if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
					YearbookElement element = yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y);
					yearbook.page(yearbook.activePage).getElements().remove(element);
					refresh();
				}
				
			}

			@Override
			public void mouseUp(MouseEvent event) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
	}

	private void buildPagesListDnD() {
		
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		DragSource source = new DragSource(pagesList, DND.DROP_MOVE | DND.DROP_COPY);
		source.setTransfer(types);

		source.addDragListener(new DragSourceAdapter()
		{
			@Override
			public void dragSetData(DragSourceEvent event)
			{
			    // Get the selected items in the drag source
			    DragSource ds = (DragSource) event.widget;
			    List list = (List) ds.getControl();
			    String[] selection = list.getSelection();
			    event.data = selection[0];
			}
		});
		DropTarget target = new DropTarget(pagesList, DND.DROP_MOVE | DND.DROP_COPY
		        | DND.DROP_DEFAULT);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter()
		{
			@Override
			public void dragEnter(DropTargetEvent event)
			{
				if (event.detail == DND.DROP_DEFAULT)
				{
				    event.detail = (event.operations & DND.DROP_COPY) != 0 ? DND.DROP_COPY
				            : DND.DROP_NONE;
				}
		
				// Allow dropping text only
				for (int i = 0, n = event.dataTypes.length; i < n; i++)
				{
				    if (TextTransfer.getInstance().isSupportedType(event.dataTypes[i]))
				    {
				        event.currentDataType = event.dataTypes[i];
				    }
				}
			}
	
			@Override
			public void dragOver(DropTargetEvent event)
			{
			    event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
			}
	
			@Override
			public void drop(DropTargetEvent event)
			{
				String sourceItemIndex = (String) event.data;
				String targetItemIndex = null;
				if (TextTransfer.getInstance().isSupportedType(event.currentDataType))
				{
				    int dropYCordinate = event.y
				            - pagesList.toDisplay(pagesList.getLocation()).y;
				    int itemTop = 0;
				    // Search for the item index where the drop took place
				    for (int i = 0; i < pagesList.getItemCount(); i++)
				    {
		
				        if (dropYCordinate >= itemTop
				                && dropYCordinate <= itemTop + pagesList.getItemHeight())
				        {
				            targetItemIndex = pagesList.getTopIndex() + i + "";
				        }
				        itemTop += pagesList.getItemHeight();
				    }
				}
				sourceItemIndex = Integer.toString(Integer.parseInt(sourceItemIndex.split(":")[0].split(" ")[1]) - 1);
				
				try {
					yearbook.movePage(Integer.parseInt(sourceItemIndex), Integer.parseInt(targetItemIndex));
				} catch (NumberFormatException e) {
				    //ignore
				}
				refresh();
			}
		});	
	
		pagesList.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				yearbook.activePage = pagesList.getSelectionIndex();
			}
			
		});
		
		
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
		
		
		//Create the insert menu.
		insertMenuItem = new MenuItem(menubar, SWT.CASCADE);
		insertMenuItem.setText("&Insert");
		Menu insertMenu = new Menu(shell, SWT.DROP_DOWN);
		insertMenuItem.setMenu(insertMenu);

		insertTextItem = new MenuItem(insertMenu, SWT.PUSH);
		insertTextItem.setText("&Text");

		insertImageItem = new MenuItem(insertMenu, SWT.PUSH);
		insertImageItem.setText("&Image");

		insertVideoItem = new MenuItem(insertMenu, SWT.PUSH);
		insertVideoItem.setText("&Video");

		insertLinkItem = new MenuItem(insertMenu, SWT.PUSH);
		insertLinkItem.setText("&Link");
		
		new MenuItem(insertMenu, SWT.SEPARATOR);
		
		insertPageNumbersItem = new MenuItem(insertMenu, SWT.PUSH);
		insertPageNumbersItem.setText("Page Numbers...");
		
		insertToCItem = new MenuItem(insertMenu, SWT.PUSH);
		insertToCItem.setText("Table of Contents...");
		
		
		
		
		
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
		canvasBackgroundColor = new Color(display, 252, 252, 252);
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
				exit();
				
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
		
		insertImageItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				FileDialog picker = new FileDialog(shell, SWT.OPEN);
				String fileName = picker.open();
				YearbookImageElement element = new YearbookImageElement(display, fileName, yearbook.settings.width, yearbook.settings.height);
				yearbook.page(yearbook.activePage).addElement(element);
				//refresh();
				GC gc = new GC(canvas);
				gc.drawImage(element.getImage(), 0, 0, element.getImage().getBounds().width, element.getImage().getBounds().height, 0, 0, element.getBounds().width, element.getBounds().height);
				gc.dispose();
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
	
	private void buildToolbar() {
		toolbarWrapper = new Composite(shell, SWT.NONE);
		barLayout = new RowLayout();
		barLayout.pack = true;
		barLayout.marginBottom = 0;
		barLayout.marginRight = 0;
		barLayout.marginLeft = 5;
		barLayout.marginTop = 0;
		barLayout.spacing = 0;
		
		toolbarWrapper.setLayout(barLayout);
		
		newBtn = new Button(toolbarWrapper, SWT.PUSH);
		newBtn.setImage(YearbookIcons.newDocument(display));
		newBtn.pack();

		openBtn = new Button(toolbarWrapper, SWT.PUSH);
		openBtn.setImage(YearbookIcons.open(display));
		openBtn.pack();
		
		saveBtn = new Button(toolbarWrapper, SWT.PUSH);
		saveBtn.setImage(YearbookIcons.save(display));
		saveBtn.pack();
		
		Label sep1 = new Label(toolbarWrapper, SWT.NONE);
		sep1.setText("   ");
		
		previewBtn = new Button(toolbarWrapper, SWT.PUSH);
		previewBtn.setImage(YearbookIcons.printPreview(display));
		previewBtn.pack();
		
		printBtn = new Button(toolbarWrapper, SWT.PUSH);
		printBtn.setImage(YearbookIcons.print(display));
		printBtn.pack();
		
		Label sep2 = new Label(toolbarWrapper, SWT.NONE);
		sep2.setText("   ");

		undoBtn = new Button(toolbarWrapper, SWT.PUSH);
		undoBtn.setImage(YearbookIcons.undo(display));
		undoBtn.pack();

		redoBtn = new Button(toolbarWrapper, SWT.PUSH);
		redoBtn.setImage(YearbookIcons.redo(display));
		redoBtn.pack();

		Label sep3 = new Label(toolbarWrapper, SWT.NONE);
		sep3.setText("   ");

		cutBtn = new Button(toolbarWrapper, SWT.PUSH);
		cutBtn.setImage(YearbookIcons.cut(display));
		cutBtn.pack();

		copyBtn = new Button(toolbarWrapper, SWT.PUSH);
		copyBtn.setImage(YearbookIcons.copy(display));
		copyBtn.pack();

		pasteBtn = new Button(toolbarWrapper, SWT.PUSH);
		pasteBtn.setImage(YearbookIcons.paste(display));
		pasteBtn.pack();

		Label sep4 = new Label(toolbarWrapper, SWT.NONE);
		sep4.setText("   ");

		textBtn = new Button(toolbarWrapper, SWT.PUSH);
		textBtn.setImage(YearbookIcons.text(display));
		textBtn.pack();

		imageBtn = new Button(toolbarWrapper, SWT.PUSH);
		imageBtn.setImage(YearbookIcons.image(display));
		imageBtn.pack();

		videoBtn = new Button(toolbarWrapper, SWT.PUSH);
		videoBtn.setImage(YearbookIcons.video(display));
		videoBtn.pack();

		linkBtn = new Button(toolbarWrapper, SWT.PUSH);
		linkBtn.setImage(YearbookIcons.link(display));
		linkBtn.pack();
		
		newBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				fileNewPageItem.getListeners(SWT.Selection)[0].handleEvent(event);
			}
			
		});
		
		imageBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				insertImageItem.getListeners(SWT.Selection)[0].handleEvent(event);
			}
			
		});

	}
	
	private void createNewYearbook(String name) {
		yearbook = new Yearbook(name);

		int canvasHeight = display.getClientArea().height - 150;
		
		yearbook.settings.height = canvasHeight;
		yearbook.settings.width = (int) ((8.5 / 11.0) * canvasHeight);
		canvas.setSize(yearbook.settings.width, yearbook.settings.height);
		rightCanvas.setSize(yearbook.settings.width, yearbook.settings.height);
	}

	private void updatePageList() {
		pagesList.removeAll();
		for (int i = 0; i < yearbook.size(); i++) {
			pagesList.add("Page " + (i + 1) + ": " + yearbook.page(i).name);
		}
	}
	
	private void updateCanvas() {
		this.loadActivePage(yearbook.activePage);
	}
	
	private void loadActivePage(int activePage) {
		//Reset the canvas to a blank slate so we can refresh it.
		GC gc = new GC(canvas);
		gc.setBackground(canvasBackgroundColor);
		gc.fillRectangle(0, 0, canvas.getBounds().width, canvas.getBounds().height);
		gc.dispose();
		
		//Apparently there's no map function in Java.
		//Map the YearbookImageElements to images...
		ArrayList<YearbookImageElement> images = new ArrayList<YearbookImageElement>();
		for (int i = 0; i < yearbook.page(activePage).getElements().size(); i++) {
			if (yearbook.page(activePage).element(i).isImage()) {
				images.add((YearbookImageElement) yearbook.page(activePage).element(i));
			}
		}
		//...and display them.
		for (YearbookImageElement element : images) {
			gc = new GC(canvas);
			gc.drawImage(element.getImage(), 0, 0, element.getImage().getBounds().width, element.getImage().getBounds().height, 0, 0, element.getBounds().width, element.getBounds().height);
			gc.dispose();
		}
		
		
	}
	
	private void createNewPage(String name) {
		yearbook.addPage(name);
		refresh();
	}
	
	private void setWindowTitle(String title) {
		setWindowTitle(SWT.DEFAULT);
		shell.setText(title + " - " + shell.getText());
	}
	
	private void setWindowTitle(int status) {
		if (status == SWT.DEFAULT) {
			shell.setText(Creator.COMPANY_NAME + " " + Creator.SOFTWARE_NAME);
		}
	}
	
	public void exit() {
		//Need to prompt for saving or whatever eventually, but for now:
		shell.close();
		shell.dispose();
	}

	public void refresh() {
		updatePageList();
		updateCanvas();
		shell.layout();
		if (!shell.getText().contains(yearbook.name)) setWindowTitle(yearbook.name);
		else if (yearbook.name.isEmpty()) setWindowTitle(SWT.DEFAULT);
	}
	
	public static void main(String[] args) {
		new Creator();
	}

}
