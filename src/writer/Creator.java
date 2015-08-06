package writer;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import command.Command;
import command.Commands;
import command.ElementCommand;
import command.Stack;
import pspa.Grade;
import pspa.HomeRoom;
import pspa.PSPAIndexNotFoundException;
import pspa.Person;
import pspa.Volume;
import reader.ProductKey;

/**
 * The yearbook editor
 * @author Cody Crow
 *
 */
public class Creator {

	//Meta information
	public static final String VERSION = "0.10";
	public static final String COMPANY_NAME = "Digital Express";
	public static final String SOFTWARE_NAME = "Yearbook Designer";
	
	//Constants
	public static final String RESOURCE_DIR = "resources";
	public static final String BACKGROUNDS_DIR = RESOURCE_DIR + File.separator + "backgrounds";
	public static final String CLIPART_DIR = RESOURCE_DIR + File.separator + "clipart";
	public static final String LAYOUTS_DIR = RESOURCE_DIR + File.separator + "layouts";

	//Used for way too many things
	int canvasHeight;
	
	//Used so we don't "Save As..." every time.
	public String saveFileName;
	
	//Command stack
	public Stack stack;

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
	private MenuItem fileSaveItem;
	private MenuItem fileSaveAsItem;
	private MenuItem fileExportItem;
	private MenuItem fileExportJPEGItem;
	private MenuItem fileExportVideoItem;
	private MenuItem fileCloseItem;
	private MenuItem editMenuItem;
	private Menu editMenu;
	private MenuItem editUndoItem;
	private MenuItem editRedoItem;
	private MenuItem editCutItem;
	private MenuItem editCopyItem;
	private MenuItem editPasteItem;
	private MenuItem editYearbookNameItem;
	private MenuItem insertMenuItem;
	private Menu insertMenu;
	private MenuItem insertTextItem;
	private MenuItem insertImageItem;
	private MenuItem insertVideoItem;
	private MenuItem insertPSPAItem;
	private MenuItem insertGeneratePSPAItem;
	private MenuItem insertPageNumbersItem;
	private MenuItem insertToCItem;
	private MenuItem pageMenuItem;
	private Menu pageMenu;
	private MenuItem pageMirrorItem;
	private MenuItem pageBackgroundItem;
	private MenuItem pageClearBackgroundItem;
	private MenuItem pageAddCoverItem;
	private MenuItem pageUseCoverItem;
	private MenuItem pageShowGridItem;
	private MenuItem pageShowTextItem;
	private MenuItem helpMenuItem;
	private Menu helpMenu;
	private MenuItem helpAboutItem;
	private MenuItem helpGenerateKeysItem;

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
	Button moveBtn;
	Button resizeBtn;
	Button selectBtn;
	Button eraseBtn;
	Button rotateBtn;


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

	//private YearbookElement selectedElement;
	//private ArrayList<YearbookElement> clipboard.elements;
	private UserSettings settings;
	private Rectangle selectionRectangle;
	private Clipboard clipboard;

	private boolean isInsertingText;
	protected String comboValue;

	private boolean MOD1;
	
	private Tree layoutTree;
	private String[] fontNames;

	private Creator() {
		display = new Display();
		shell = new Shell(display);
		settings = new UserSettings();
		setWindowTitle(SWT.DEFAULT);
		//clipboard.elements = new ArrayList<YearbookElement>();
		clipboard = new Clipboard();
		stack = new Stack();
		
		canvasHeight = (int) (.80 * display.getClientArea().height);

		shell.setSize(800, 600);

		this.buildMenu();
		this.setMenuListeners();

		this.loadFonts();
		this.initialize();

		//Create the layout.
		shell.setLayout(new ColumnLayout());

		this.buildToolbar();	

		gridLayout = new GridLayout(8, true);
		content = new Composite(shell, SWT.NONE);
		content.setLayout(gridLayout);

		pagesList = new List(content, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		listGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		listGridData.horizontalSpan = 1;
		listGridData.heightHint = 150;
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
				int selectedPageIndices[] = pagesList.getSelectionIndices();
				for (int i : selectedPageIndices) {
					if (i < 0 || i > pagesList.getItemCount()) return;
				}
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
								dialog.dispose();
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
								for (int i : selectedPageIndices) {
									yearbook.page(i).name = text.getText();
								}

								refresh();
								dialog.close();
								dialog.dispose();
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
						if (selectedPageIndices.length == 1) messageBox.setMessage("Are you sure you want to delete this page?\n\t" + yearbook.page(selectedPageIndices[0]));
						else messageBox.setMessage("Are you sure you want to delete these pages?");
						int yesno = messageBox.open();
						if (yesno == SWT.YES) {
							/*for (int i : selectedPageIndices) {
								stack.push(new PageCommand(Commands.REMOVE_PAGE, yearbook.page(i), i, -1));	
							}*/
							yearbook.removePages(selectedPageIndices);
							refresh();
						}

					}

				});
			}
		});
		
		this.buildPagesListDnD();
		
		this.buildExpandBar();
		
		this.finalPrep();

		shell.setMaximized(true);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())	display.sleep();
		}
		display.dispose();

	}

	private void buildExpandBar() {
		ExpandBar bar = new ExpandBar(content, SWT.V_SCROLL);
		
		GridData data = new GridData(SWT.BEGINNING, SWT.FILL, true, true);
		data.horizontalSpan = 2;
		data.minimumWidth = 250;
		bar.setLayoutData(data);
		
		bar.setLayout(new FillLayout());

		Composite composite = new Composite(bar, SWT.NONE);
		composite.setLayout(new ColumnLayout());

		//Load backgrounds into list.
		File backgroundsRoot = new File(BACKGROUNDS_DIR);
		File[] backgroundsList = backgroundsRoot.listFiles();
		
		//Convert them to thumbnails to save RAM.
		int i = 0;
		for (File f : backgroundsList) {
			try {
				Image large = new Image(display, new ImageData(f.getAbsolutePath()));
				Image thumbnail = new Image(display, 116, 150);
				GC gc = new GC(thumbnail);
				gc.drawImage(large, 0, 0, large.getBounds().width, large.getBounds().height, 0, 0, thumbnail.getBounds().width, thumbnail.getBounds().height);
				gc.dispose();
				large.dispose();
				Label label = new Label(composite, SWT.NONE);
				label.setText(Integer.toString(i));
				label.setImage(thumbnail);
				
				label.addMouseTrackListener(new MouseTrackListener() {

					@Override
					public void mouseEnter(MouseEvent e) {
						shell.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
						
					}

					@Override
					public void mouseExit(MouseEvent e) {
						shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
						
					}

					@Override
					public void mouseHover(MouseEvent e) {
						
					}
					
				});
				
				label.addMouseListener(new MouseListener() {

					@Override
					public void mouseDoubleClick(MouseEvent e) {
						
					}

					@Override
					public void mouseDown(MouseEvent e) {
						int index = Integer.parseInt(label.getText());
						MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
						box.setText("Change Background");
						box.setMessage("Set this as the background for page " + Integer.toString(yearbook.activePage + 1) + "?");
						int result = box.open();
						if ((result & SWT.CANCEL) == SWT.CANCEL) return;
						Image bg = new Image(display, new ImageData(backgroundsList[index].getAbsolutePath()));
						yearbook.page(yearbook.activePage).setBackgroundImageData(bg.getImageData());
						bg.dispose();
						refreshNoPageList();
					}

					@Override
					public void mouseUp(MouseEvent e) {
						
					}
					
				});
				
			} catch (SWTException e) {
				//Ignore
			}
			i++;
		}
		
		ExpandItem item0 = new ExpandItem(bar, SWT.NONE, 0);
		item0.setText("Backgrounds");
		item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(composite);
		
		//Clip art tree
		
		composite = new Composite(bar, SWT.NONE);
		composite.setLayout(new FillLayout());

		File clipartRoot = new File(CLIPART_DIR);
		File[] clipartFiles = clipartRoot.listFiles();

		Tree tree = new Tree(composite, SWT.BORDER);
		populateFileTree(tree, clipartFiles);
		tree.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					TreeItem item = ((TreeItem) e.item);
					String path = (String) item.getData("path");
					File f = new File(path);
					if (f.isDirectory()) return;
					MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
					box.setText("Insert Clip Art");
					box.setMessage("Would you like to insert this clip art?");
					int response = box.open();
					if ((response & SWT.CANCEL) == SWT.CANCEL || path == null) return;
					YearbookImageElement element = new YearbookImageElement(display, path, yearbook.settings.width, yearbook.settings.height);
					stack.push(new ElementCommand(Commands.ADD_ELEMENT, null, element.copy(), yearbook.page(yearbook.activePage).id));
					yearbook.page(yearbook.activePage).addElement(element);
					refreshNoPageList();
				} catch (Exception ex) {
					//Ignore
				}
				
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
		ExpandItem item1 = new ExpandItem(bar, SWT.NONE, 1);
		item1.setText("Clip Art");
		//item1.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item1.setHeight((int) Math.ceil(.92 * canvasHeight));
		item1.setControl(composite);
		
		//Layouts
		
		composite = new Composite(bar, SWT.NONE);
		GridLayout layout = new GridLayout();
		
		composite.setLayout(layout);
		
		Button addBtn = new Button(composite, SWT.PUSH);
		addBtn.setText("Create New Layout");
		
		addBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (!clipboard.elements.isEmpty()) {
					MessageBox box = new MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
					box.setText("Create New Layout");
					box.setMessage("Would you like to create a new layout from the selected elements?");
					int res = box.open();
					if ((res & SWT.CANCEL) == SWT.CANCEL) return;
					
					
					final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
					dialog.setText("Create New Layout");
					dialog.setSize(400, 300);
					FormLayout formLayout = new FormLayout();
					formLayout.marginWidth = 10;
					formLayout.marginHeight = 10;
					formLayout.spacing = 10;
					dialog.setLayout(formLayout);

					Label label = new Label(dialog, SWT.NONE);
					label.setText("Layout Name:");
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
							dialog.dispose();
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
							YearbookLayout yep = new YearbookLayout(text.getText(), clipboard.elements);
							try {
								yep.save();
							} catch (IOException e1) {
								MessageBox box = new MessageBox(shell, SWT.ERROR | SWT.OK);
								box.setText("Error");
								box.setMessage("Yearbook layout " + text.getText() + " could not be saved. Please try again.");
								box.open();
							}
							
							dialog.close();
							dialog.dispose();
							refreshNoPageList();
							updateLayoutTree();
						}
					});

					dialog.setDefaultButton (ok);
					dialog.pack();
					dialog.open();
				}
				
			}
			
		});
		
		layoutTree = new Tree(composite, SWT.BORDER);
		updateLayoutTree();
		
		layoutTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		ExpandItem item2 = new ExpandItem(bar, SWT.NONE, 2);
		item2.setText("Layouts");
		item2.setHeight((int) Math.ceil(.9 * canvasHeight));
		item2.setControl(composite);
		
		layoutTree.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					TreeItem item = ((TreeItem) e.item);
					String path = (String) item.getData("path");
					File f = new File(path);
					if (f.isDirectory()) return;
					MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
					box.setText("Insert Layout");
					box.setMessage("Would you like to insert this layout?");
					int response = box.open();
					if ((response & SWT.CANCEL) == SWT.CANCEL || path == null) return;
					YearbookLayout layout = YearbookLayout.read(path);
					yearbook.page(yearbook.activePage).layouts.push(layout);
					refreshNoPageList();
				} catch (Exception ex) {
					//Ignore
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
			
		});
		

		composite.pack();
		
		bar.setSpacing(8);
	}
	
	private void updateLayoutTree() {
		File layoutRoot = new File(LAYOUTS_DIR);
		File[] layoutFiles = layoutRoot.listFiles();
		
		layoutTree.clearAll(true);
		populateFileTree(layoutTree, layoutFiles);
		
	}
	
	private void populateFileTree(TreeItem tree, File[] files) {
		for (File f : files) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText(f.getName());
			item.setData("path", f.getPath());
			
			if (f.isDirectory()) populateFileTree(item, f.listFiles());
			else {
				try {
					Image image = new Image(display, new ImageData(f.getPath()));
					Image thumb;
					if (image.getBounds().height <= image.getBounds().width) {
						int height = (int) Math.floor(100.0 * ((double) image.getBounds().height / image.getBounds().width));
						thumb = new Image(display, 100, height);
						GC gc = new GC(thumb);
						gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, 100, height);
						gc.dispose();
						item.setImage(thumb);
					} else {
						int width = (int) Math.floor(100.0 * ((double) image.getBounds().width / image.getBounds().height));
						thumb = new Image(display, width, 100);
						GC gc = new GC(thumb);
						gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, 100);
						gc.dispose();
						item.setImage(thumb);
					}
					image.dispose();
				} catch (SWTException e) {
					continue;
				}
				
			}
		}
	}

	private void populateFileTree(Tree tree, File[] files) {
		for (File f : files) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText(f.getName());
			item.setData("path", f.getPath());
			if (f.isDirectory()) populateFileTree(item, f.listFiles());
			else {
				try {
					Image image = new Image(display, new ImageData(f.getPath()));
					Image thumb;
					if (image.getBounds().height <= image.getBounds().width) {
						int height = (int) Math.floor(100.0 * ((double) image.getBounds().height / image.getBounds().width));
						thumb = new Image(display, 100, height);
						GC gc = new GC(thumb);
						gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, 100, height);
						gc.dispose();
						item.setImage(thumb);
					} else {
						int width = (int) Math.floor(100.0 * ((double) image.getBounds().width / image.getBounds().height));
						thumb = new Image(display, width, 100);
						GC gc = new GC(thumb);
						gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, 100);
						gc.dispose();
						item.setImage(thumb);
					}
					image.dispose();
				} catch (SWTException e) {
					continue;
				}
				
			}
		}
	}

	private void finalPrep() {
		
		/*
		 * Handle global shortcuts
		 */
		display.addFilter(SWT.KeyDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if ((event.stateMask & SWT.MOD1) == SWT.MOD1) switch (event.keyCode) {
				case 'a':
				case 'A':
					clipboard.elements.clear();
					for (YearbookElement e : yearbook.page(yearbook.activePage).getElements()) {
						selectAnotherElement(e);
					}
					break;
				}
				
			}
			
		});
		
	}

	private void initializeCanvas() {

		Composite bigCanvasWrapper = new Composite(content, SWT.NONE);
		canvasGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		canvasGridData.horizontalSpan = 5;
		bigCanvasWrapper.setLayoutData(canvasGridData);
		bigCanvasWrapper.setLayout(new GridLayout(2, false));

		Composite canvasWrapper = new Composite(bigCanvasWrapper, SWT.NONE);
		canvas = new Canvas(canvasWrapper, SWT.BORDER);
		canvas.setBackground(canvasBackgroundColor);

		Composite canvasWrapper2 = new Composite(bigCanvasWrapper, SWT.NONE);
		rightCanvas = new Canvas(canvasWrapper2, SWT.BORDER);
		rightCanvas.setBackground(canvasBackgroundColor);

		/**
		 * Handles all of the mouse interactions on the canvas.
		 * This just compiles all of the relevant information, drawing
		 * should _NEVER_ happen here.
		 */
		canvas.addMouseListener(new MouseListener() {

			int xDiff = 0;
			int yDiff = 0;
			int startX = 0;
			int startY = 0;
			int centerX;
			int centerY;

			@Override
			public void mouseDoubleClick(MouseEvent event) {
				if (!leftIsActive()) return;
				if (!isInsertingText) switch (settings.cursorMode) {
				case MOVE:
					//Bring selected elements to front.
					for (YearbookElement selectedElement : clipboard.elements) {
						if (selectedElement != null) {
							int index = yearbook.page(yearbook.activePage).findElementIndex(selectedElement);
							if (index == -1) {
								selectedElement = null;
							} else {
								yearbook.page(yearbook.activePage).getElements().remove(index);
								yearbook.page(yearbook.activePage).addElement(selectedElement);
							}
						}
					}
					break;
				case ERASE:
					break;
				case RESIZE:
					break;
				case SELECT:
					break;
				default:
					break;
				}
			}

			@Override
			public void mouseDown(MouseEvent event) {
				makeLeftActive();
				xDiff = yDiff = 0;
				
				//Handle layouts first
				if (yearbook.page(yearbook.activePage).isInLayout(event.x, event.y, yearbook.settings.width, yearbook.settings.height)) {
					String fileName = imagePicker();
					if (fileName == null) return;
					YearbookImageElement element = new YearbookImageElement(display, fileName, yearbook.settings.width, yearbook.settings.height);
					stack.push(new ElementCommand(Commands.ADD_ELEMENT, null, element.copy(), yearbook.page(yearbook.activePage).id));
					element.x = yearbook.page(yearbook.activePage).getPrototype(event.x, event.y, yearbook.settings.width, yearbook.settings.height).x;
					element.y = yearbook.page(yearbook.activePage).getPrototype(event.x, event.y, yearbook.settings.width, yearbook.settings.height).y;
					element.rotation = yearbook.page(yearbook.activePage).getPrototype(event.x, event.y, yearbook.settings.width, yearbook.settings.height).rotation;
					yearbook.page(yearbook.activePage).addElement(element);
					refreshNoPageList();
					return;
				}

				if (event.button == 3 && yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
					int trueX = event.x;
					int trueY = event.y;
					Menu menu = new Menu(shell);
					MenuItem addBorderItem = new MenuItem(menu, SWT.PUSH);
					addBorderItem.setText("Add &Border");
					
					if (yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).isImage()) addBorderItem.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							openAddBorderDialog((YearbookImageElement) yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY));
							if (clipboard.elements.size() > 1) {
								ArrayList<YearbookImageElement> images = new ArrayList<YearbookImageElement>();
								for (YearbookElement ye : clipboard.elements) {
									if (ye.isImage()) images.add((YearbookImageElement) ye);
								}
								for (YearbookImageElement e : images) {
									e.border = ((YearbookImageElement) yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY)).border;
								}
							}
						}
						
					});
					
					else if (yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).isText()) addBorderItem.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							openAddBorderDialog((YearbookTextElement) yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY));
						}
						
					});
					
					MenuItem removeBorderItem = new MenuItem(menu, SWT.PUSH);
					removeBorderItem.setText("Remove B&order");
					
					removeBorderItem.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							YearbookElement orig = yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY); 
							yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).border.noBorder = true;
							refreshNoPageList();
							stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).copy(), yearbook.page(yearbook.activePage).id));
						}
						
					});
					
					MenuItem shadowItem = new MenuItem(menu, SWT.CHECK);
					shadowItem.setText("Show Drop &Shadow");
					
					shadowItem.setSelection(yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).shadow);
					
					shadowItem.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							YearbookElement orig = yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).copy();
							yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).shadow = !yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).shadow;
							refreshNoPageList();
							stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).copy(), yearbook.page(yearbook.activePage).id));
						}
						
					});
					
					if (yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).isPSPA()) {
						new MenuItem(menu, SWT.SEPARATOR);
						
						YearbookPSPAElement element = (YearbookPSPAElement) yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY);
						
						MenuItem changeNameItem = new MenuItem(menu, SWT.PUSH);
						changeNameItem.setText("Change Name");
						changeNameItem.addListener(SWT.Selection, new Listener() {

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
										dialog.dispose();
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
										String fullName = text.getText();
										String[] tokens = fullName.split(" ");
										if (tokens.length > 1) {
											element.person.firstName = tokens[0];
											element.person.lastName = "";
											for (int i = 1; i < tokens.length; i++) {
												element.person.lastName += tokens[i];
											}
										} else {
											element.person.firstName = fullName;
											element.person.lastName = "";
										}
										
										dialog.close();
										dialog.dispose();
										refresh();
									}
								});

								dialog.setDefaultButton (ok);
								dialog.pack();
								dialog.open();
							}
							
						});
					}
					
					new MenuItem(menu, SWT.SEPARATOR);
					
					MenuItem properties = new MenuItem(menu, SWT.PUSH);
					properties.setText("Properties");

					properties.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							if (yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).isText()) {
								openTextProperties((YearbookTextElement) yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY));
							} else {
								openProperties(yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY));
							}
						}

						private void openProperties(YearbookElement element) {
							Shell properties = new Shell(shell);
							properties.setText("Properties");
							GridLayout layout = new GridLayout();
							layout.numColumns = 2;
							layout.makeColumnsEqualWidth = true;
							properties.setLayout(layout);

							GridData data = new GridData();
							data.horizontalSpan = 1;
							data.grabExcessHorizontalSpace = true;
							data.horizontalAlignment = SWT.FILL;

							GridData data2 = new GridData();
							data2.horizontalSpan = 1;
							data2.grabExcessHorizontalSpace = true;
							data.horizontalAlignment = SWT.FILL;

							Label loc = new Label(properties, SWT.LEFT);
							loc.setText("Location:");
							loc.setLayoutData(data);

							Label xy = new Label(properties, SWT.LEFT | SWT.WRAP);
							String x = String.format("%.2f", element.x * yearbook.settings.xInches());
							String y = String.format("%.2f", element.y * yearbook.settings.yInches());
							xy.setText(x + "\", " + y + "\"");
							xy.setLayoutData(data2);

							Label dim = new Label(properties, SWT.LEFT);
							dim.setText("Dimensions:");
							dim.setLayoutData(data);

							Label sizeNumbers = new Label(properties, SWT.LEFT);
							x = String.format("%.2f", (double) element.getBounds().width / element.pageWidth * yearbook.settings.xInches());
							y = String.format("%.2f", (double) element.getBounds().height / element.pageHeight * yearbook.settings.yInches());
							sizeNumbers.setText(x + "\" x " + y + "\"");

							Label video = new Label(properties, SWT.LEFT);
							video.setText("Has video?");
							video.setLayoutData(data);

							Label yesno = new Label(properties, SWT.LEFT);
							yesno.setText(element.isClickable() ? "Yes" : "No");
							yesno.setLayoutData(data2);

							if (element.isClickable() && element.isImage()) {
								YearbookClickableImageElement e = (YearbookClickableImageElement) element;

								Label videoName = new Label(properties, SWT.LEFT);
								videoName.setText("Video Name:");
								videoName.setLayoutData(data);

								data2 = new GridData();
								data2.horizontalSpan = 1;
								data2.grabExcessHorizontalSpace = true;
								data.horizontalAlignment = SWT.FILL;

								Label name = new Label(properties, SWT.LEFT | SWT.WRAP);
								name.setText(e.getVideo().name);
								name.setLayoutData(data2);
							}

							properties.pack();
							properties.open();

						}

						private void openTextProperties(YearbookTextElement element) {
							Shell properties = new Shell(shell);
							properties.setText("Properties");
							GridLayout layout = new GridLayout();
							layout.numColumns = 2;
							layout.makeColumnsEqualWidth = true;
							properties.setLayout(layout);

							Label loc = new Label(properties, SWT.LEFT);
							loc.setText("Location:");

							GridData data = new GridData();
							data.horizontalSpan = 1;
							data.grabExcessHorizontalSpace = true;
							data.horizontalAlignment = SWT.FILL;
							loc.setLayoutData(data);

							Label xy = new Label(properties, SWT.LEFT);
							String x = String.format("%.2f", element.x * yearbook.settings.xInches());
							String y = String.format("%.2f", element.y * yearbook.settings.yInches());
							xy.setText(x + "\", " + y + "\"");
							GridData data2 = new GridData();
							data2.horizontalSpan = 1;
							data2.grabExcessHorizontalSpace = true;
							data.horizontalAlignment = SWT.FILL;
							xy.setLayoutData(data2);

							Label dim = new Label(properties, SWT.LEFT);
							dim.setText("Dimensions:");
							dim.setLayoutData(data);

							Label sizeNumbers = new Label(properties, SWT.LEFT);
							x = String.format("%.2f", (double) element.getBounds().width / element.pageWidth * yearbook.settings.xInches());
							y = String.format("%.2f", (double) element.getBounds().height / element.pageHeight * yearbook.settings.yInches());
							sizeNumbers.setText(x + "\" x " + y + "\"");

							Label color = new Label(properties, SWT.LEFT);
							color.setText("Color:");
							color.setLayoutData(data);

							Label rgb = new Label(properties, SWT.LEFT);
							try {
								String rString = String.format("%02d", Integer.parseInt(Integer.toHexString(element.getRgb().red)), 16);
								String gString = String.format("%02d", Integer.parseInt(Integer.toHexString(element.getRgb().green)), 16);
								String bString = String.format("%02d", Integer.parseInt(Integer.toHexString(element.getRgb().blue)), 16);
								rgb.setText("#" + rString + gString + bString);
							} catch (NumberFormatException e) {
								rgb.setText("#FFFFFF");
							}
							rgb.setLayoutData(data2);

							Label size = new Label(properties, SWT.LEFT);
							size.setText("Font Size:");
							size.setLayoutData(data);

							Label fontSize = new Label(properties, SWT.LEFT);
							fontSize.setText(Integer.toString(element.size) + " pt.");
							fontSize.setLayoutData(data2);

							Label font = new Label(properties, SWT.LEFT);
							font.setText("Font Family:");
							font.setLayoutData(data);
							
							data2 = new GridData();
							data2.horizontalSpan = 1;
							data2.grabExcessHorizontalSpace = true;
							data.horizontalAlignment = SWT.FILL;

							Label family = new Label(properties, SWT.LEFT);
							family.setText(element.fontFamily);
							family.setLayoutData(data2);

							properties.setSize(250, 200);
							properties.open();
						}

					});

					menu.setVisible(true);
				}

				if (!(isInsertingText || event.button == SWT.BUTTON3)) switch (settings.cursorMode) {
				case MOVE:
					if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
						if (!clipboard.elements.contains(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y))) {
							if ((event.stateMask & SWT.MOD1) == SWT.MOD1) {
								selectAnotherElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
							} else {
								selectElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
							}
						}

						refresh();
					} else {
						selectElement(null);
						refresh();
					}
					xDiff -= event.x;
					yDiff -= event.y;
					break;
				case ERASE:
					if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
						selectElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
						refresh();
						MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
						box.setText("Delete Element");
						box.setMessage("Are you sure you want to erase this element?");
						int result = box.open();
						if (result == SWT.YES) {
							YearbookElement e = clipboard.elements.get(0).copy();
							yearbook.page(yearbook.activePage).removeElement(clipboard.elements.get(0));
							stack.push(new ElementCommand(Commands.REMOVE_ELEMENT, e, null, yearbook.page(yearbook.activePage).id));
						}
						refreshNoPageList();
					}
					break;
				case RESIZE:
					if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y) && (event.stateMask & SWT.MOD1) == SWT.MOD1) {
						selectAnotherElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
						refreshNoPageList();
					} else if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
						selectElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
						refreshNoPageList();
					} else {
						selectElement(null);
					}
				case SELECT:
					startX = event.x;
					startY = event.y;
					xDiff -= event.x;
					yDiff -= event.y;					
					break;
				case ROTATE:
					if (!yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) break;
					openRotateDialog(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
					break;
				default:
					break;


				}

				if (isInsertingText) {
					YearbookTextElement element;
					if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)){
						if (yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y).isText()) {
							element = (YearbookTextElement) yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y);
						} else {
							element = new YearbookTextElement(event.x, event.y, yearbook.settings.width, yearbook.settings.height);
							yearbook.page(yearbook.activePage).addElement(element);
						}
					} else {
						int startX = 0;
						element = new YearbookTextElement(event.x, event.y, yearbook.settings.width, yearbook.settings.height);
						yearbook.page(yearbook.activePage).addElement(element);
					}

					refresh();
					openTextDialog(element);
					stack.push(new ElementCommand(Commands.ADD_ELEMENT, null, element.copy(), yearbook.page(yearbook.activePage).id));
				}

			}

			@Override
			public void mouseUp(MouseEvent event) {
				if (!leftIsActive()) return;
				if (!isInsertingText) switch (settings.cursorMode) {
				case MOVE:
					xDiff += event.x;
					yDiff += event.y;

					//Prevents accidental movement.
					if (Math.abs(xDiff) < 5 && Math.abs(yDiff) < 5) xDiff = yDiff = 0;

					if (clipboard.elements.size() == 0) return;
					if (clipboard.elements.size() == 1) {
						YearbookElement selectedElement = clipboard.elements.get(0);
						if (yearbook.page(yearbook.activePage).findElement(selectedElement) != null && event.button == 1) {
							int newX, newY;
							newX = selectedElement.getBounds().x + xDiff;
							newY = selectedElement.getBounds().y + yDiff;
							YearbookElement orig = selectedElement.copy();
							yearbook.page(yearbook.activePage).findElement(selectedElement).setLocationRelative(newX, newY);
							stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, selectedElement.copy(), yearbook.page(yearbook.activePage).id));
						}
					} else {
						int newX, newY;
						for (YearbookElement element : clipboard.elements) {
							YearbookElement orig = element.copy();
							newX = element.getBounds().x + xDiff;
							newY = element.getBounds().y + yDiff;
							element.setLocationRelative(newX, newY);
							stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, element.copy(), yearbook.page(yearbook.activePage).id));
						}
					}

					refresh();
					break;
				case ERASE:
					break;
				case RESIZE:
					xDiff += event.x;
					yDiff += event.y;
					
					if (clipboard.elements.size() > 0) { 
						double xPercent = (double) xDiff / clipboard.elements.get(clipboard.elements.size() - 1).getBounds(yearbook.settings.width, yearbook.settings.height).width;
						double yPercent = (double) yDiff / clipboard.elements.get(clipboard.elements.size() - 1).getBounds(yearbook.settings.width, yearbook.settings.height).height; 
						int newX, newY;
						
						for (YearbookElement selectedElement : clipboard.elements) {
							YearbookElement orig = selectedElement.copy();
							newX = (int) (xPercent * selectedElement.getBounds(yearbook.settings.width, yearbook.settings.height).width);
							newY = (int) (yPercent * selectedElement.getBounds(yearbook.settings.width, yearbook.settings.height).height);
							
							if (yearbook.page(yearbook.activePage).findElement(selectedElement) != null) {
								yearbook.page(yearbook.activePage).findElement(selectedElement).resize(display, newX, newY);
								stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, selectedElement.copy(), yearbook.page(yearbook.activePage).id));
							}
						}
						refreshNoPageList();
						startX = startY = xDiff = yDiff = 0;
					}

					break;
				case SELECT:
					xDiff += event.x;
					yDiff += event.y;

					//Prevents accidental movement and
					//helps users select from the edges.
					if (Math.abs(startX) <= 5) startX = 0;
					if (Math.abs(startY) <= 5) startY = 0;
					if (Math.abs(canvas.getBounds().width - event.x) <= 5) 
						if (Math.abs(xDiff) < 15 && Math.abs(yDiff) < 15) xDiff = yDiff = 0;

					selectionRectangle = new Rectangle(startX, startY, xDiff, yDiff);
					
					ArrayList<YearbookElement> selected = yearbook.page(yearbook.activePage).getElementsInRectangle(selectionRectangle, yearbook.settings.width, yearbook.settings.height);
					for (YearbookElement e : selected) {
						selectAnotherElement(e);
					}
					
					startX = startY = xDiff = yDiff = 0;

					refresh();

					break;
				case ROTATE:
					/*
					if (clipboard.elements.size() == 0) return;
					int x2 = event.x;
					int y2 = event.y;
					int x1Diff = startX - centerX;
					int y1Diff = startY - centerY;
					int x2Diff = startX - x2;
					int y2Diff = startY - y2;
					float angle = 10 * -(float) (Math.atan2(y2Diff, x2Diff) - Math.atan2(y1Diff, x1Diff));
					yearbook.page(yearbook.activePage).removeElement(clipboard.elements.get(0));
					clipboard.elements.get(0).rotation += angle;
					yearbook.page(yearbook.activePage).addElement(clipboard.elements.get(0));
					
					System.out.println(angle);
					
					startX = 0;
					startY = 0;*/
					refreshNoPageList();
					break;
				default:
					break;
				}
			}

		});

		rightCanvas.addMouseListener(new MouseListener() {
			int xDiff = 0;
			int yDiff = 0;
			int startX = 0;
			int startY = 0;

			@Override
			public void mouseDoubleClick(MouseEvent event) {
				if (!rightIsActive()) return;
				if (!isInsertingText) switch (settings.cursorMode) {
				case MOVE:
					//Bring selected elements to front.
					for (YearbookElement selectedElement : clipboard.elements) {
						if (selectedElement != null) {
							int index = yearbook.page(yearbook.activePage).findElementIndex(selectedElement);
							if (index == -1) {
								selectedElement = null;
							} else {
								yearbook.page(yearbook.activePage).getElements().remove(index);
								yearbook.page(yearbook.activePage).addElement(selectedElement);
							}
						}
					}
					break;
				case ERASE:
					break;
				case RESIZE:
					break;
				case SELECT:
					break;
				default:
					break;
				}
			}

			@Override
			public void mouseDown(MouseEvent event) {
				
				makeRightActive();
				xDiff = yDiff = 0;

				//Handle layouts first
				if (yearbook.page(yearbook.activePage).isInLayout(event.x, event.y, yearbook.settings.width, yearbook.settings.height)) {
					String fileName = imagePicker();
					if (fileName == null) return;
					YearbookImageElement element = new YearbookImageElement(display, fileName, yearbook.settings.width, yearbook.settings.height);
					stack.push(new ElementCommand(Commands.ADD_ELEMENT, null, element.copy(), yearbook.page(yearbook.activePage).id));
					element.x = yearbook.page(yearbook.activePage).getPrototype(event.x, event.y, yearbook.settings.width, yearbook.settings.height).x;
					element.y = yearbook.page(yearbook.activePage).getPrototype(event.x, event.y, yearbook.settings.width, yearbook.settings.height).y;
					element.rotation = yearbook.page(yearbook.activePage).getPrototype(event.x, event.y, yearbook.settings.width, yearbook.settings.height).rotation;
					yearbook.page(yearbook.activePage).addElement(element);
					refreshNoPageList();
					return;
				}
				
				if (event.button == 3 && yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y, yearbook.settings.height, yearbook.settings.height)) {
					int trueX = event.x;
					int trueY = event.y;
					Menu menu = new Menu(shell);
					MenuItem addBorderItem = new MenuItem(menu, SWT.PUSH);
					addBorderItem.setText("Add &Border");
					
					if (yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).isImage()) addBorderItem.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							openAddBorderDialog((YearbookImageElement) yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY));
							if (clipboard.elements.size() > 1) {
								ArrayList<YearbookImageElement> images = new ArrayList<YearbookImageElement>();
								for (YearbookElement ye : clipboard.elements) {
									if (ye.isImage()) images.add((YearbookImageElement) ye);
								}
								for (YearbookImageElement e : images) {
									e.border = ((YearbookImageElement) yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY)).border;
								}
							}
						}
						
					});
					
					else if (yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).isText()) addBorderItem.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							openAddBorderDialog((YearbookTextElement) yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY));
						}
						
					});
					
					MenuItem removeBorderItem = new MenuItem(menu, SWT.PUSH);
					removeBorderItem.setText("Remove B&order");
					
					removeBorderItem.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							YearbookElement orig = yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).copy(); 
							yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).border.noBorder = true;
							refreshNoPageList();
							stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).copy(), yearbook.page(yearbook.activePage).id));
						}
						
					});
						
					
					MenuItem shadowItem = new MenuItem(menu, SWT.CHECK);
					shadowItem.setText("Show Drop &Shadow");
					
					shadowItem.setSelection(yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).shadow);
					
					shadowItem.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							YearbookElement orig = yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).copy();
							yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).shadow = !yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).shadow;
							stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).copy(), yearbook.page(yearbook.activePage).id));
						}
						
					});
					
					new MenuItem(menu, SWT.SEPARATOR);
					
					
					MenuItem properties = new MenuItem(menu, SWT.PUSH);
					properties.setText("Properties");

					properties.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event event) {
							if (yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY).isText()) {
								openTextProperties((YearbookTextElement) yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY));
							} else {
								openProperties(yearbook.page(yearbook.activePage).getElementAtPoint(trueX, trueY));
							}
						}

						private void openProperties(YearbookElement element) {
							Shell properties = new Shell(shell);
							properties.setText("Properties");
							GridLayout layout = new GridLayout();
							layout.numColumns = 2;
							layout.makeColumnsEqualWidth = true;
							properties.setLayout(layout);

							GridData data = new GridData();
							data.horizontalSpan = 1;
							data.grabExcessHorizontalSpace = true;
							data.horizontalAlignment = SWT.FILL;

							GridData data2 = new GridData();
							data2.horizontalSpan = 1;
							data2.grabExcessHorizontalSpace = true;
							data.horizontalAlignment = SWT.FILL;

							Label loc = new Label(properties, SWT.LEFT);
							loc.setText("Location:");
							loc.setLayoutData(data);

							Label xy = new Label(properties, SWT.LEFT | SWT.WRAP);
							String x = String.format("%.2f", element.x * yearbook.settings.xInches());
							String y = String.format("%.2f", element.y * yearbook.settings.yInches());
							xy.setText(x + "\", " + y + "\"");
							xy.setLayoutData(data2);

							Label dim = new Label(properties, SWT.LEFT);
							dim.setText("Dimensions:");
							dim.setLayoutData(data);

							Label sizeNumbers = new Label(properties, SWT.LEFT);
							x = String.format("%.2f", (double) element.getBounds().width / element.pageWidth * yearbook.settings.xInches());
							y = String.format("%.2f", (double) element.getBounds().height / element.pageHeight * yearbook.settings.yInches());
							sizeNumbers.setText(x + "\" x " + y + "\"");

							Label video = new Label(properties, SWT.LEFT);
							video.setText("Has video?");
							video.setLayoutData(data);

							Label yesno = new Label(properties, SWT.LEFT);
							yesno.setText(element.isClickable() ? "Yes" : "No");
							yesno.setLayoutData(data2);

							if (element.isClickable() && element.isImage()) {
								YearbookClickableImageElement e = (YearbookClickableImageElement) element;

								Label videoName = new Label(properties, SWT.LEFT);
								videoName.setText("Video Name:");
								videoName.setLayoutData(data);

								data2 = new GridData();
								data2.horizontalSpan = 1;
								data2.grabExcessHorizontalSpace = true;
								data.horizontalAlignment = SWT.FILL;

								Label name = new Label(properties, SWT.LEFT | SWT.WRAP);
								name.setText(e.getVideo().name);
								name.setLayoutData(data2);
							}

							properties.pack();
							properties.open();

						}

						private void openTextProperties(YearbookTextElement element) {
							Shell properties = new Shell(shell);
							properties.setText("Properties");
							GridLayout layout = new GridLayout();
							layout.numColumns = 2;
							layout.makeColumnsEqualWidth = true;
							properties.setLayout(layout);

							Label loc = new Label(properties, SWT.LEFT);
							loc.setText("Location:");

							GridData data = new GridData();
							data.horizontalSpan = 1;
							data.grabExcessHorizontalSpace = true;
							data.horizontalAlignment = SWT.FILL;
							loc.setLayoutData(data);

							Label xy = new Label(properties, SWT.LEFT);
							String x = String.format("%.2f", element.x * yearbook.settings.xInches());
							String y = String.format("%.2f", element.y * yearbook.settings.yInches());
							xy.setText(x + "\", " + y + "\"");
							GridData data2 = new GridData();
							data2.horizontalSpan = 1;
							data2.grabExcessHorizontalSpace = true;
							data.horizontalAlignment = SWT.FILL;
							xy.setLayoutData(data2);

							Label dim = new Label(properties, SWT.LEFT);
							dim.setText("Dimensions:");
							dim.setLayoutData(data);

							Label sizeNumbers = new Label(properties, SWT.LEFT);
							x = String.format("%.2f", (double) element.getBounds().width / element.pageWidth * yearbook.settings.xInches());
							y = String.format("%.2f", (double) element.getBounds().height / element.pageHeight * yearbook.settings.yInches());
							sizeNumbers.setText(x + "\" x " + y + "\"");

							Label color = new Label(properties, SWT.LEFT);
							color.setText("Color:");
							color.setLayoutData(data);

							Label rgb = new Label(properties, SWT.LEFT);
							String rString = String.format("%02d", Integer.parseInt(Integer.toHexString(element.getRgb().red)), 16);
							String gString = String.format("%02d", Integer.parseInt(Integer.toHexString(element.getRgb().green)), 16);
							String bString = String.format("%02d", Integer.parseInt(Integer.toHexString(element.getRgb().blue)), 16);
							rgb.setText("#" + rString + gString + bString);
							rgb.setLayoutData(data2);

							Label size = new Label(properties, SWT.LEFT);
							size.setText("Font Size:");
							size.setLayoutData(data);

							Label fontSize = new Label(properties, SWT.LEFT);
							fontSize.setText(Integer.toString(element.size) + " pt.");
							fontSize.setLayoutData(data2);

							Label font = new Label(properties, SWT.LEFT);
							font.setText("Font Family:");
							font.setLayoutData(data);

							Label family = new Label(properties, SWT.LEFT);
							family.setText(element.fontFamily);
							family.setLayoutData(data2);

							properties.setSize(250, 200);
							properties.open();
						}

					});

					menu.setVisible(true);
				}

				if (!(isInsertingText || event.button == SWT.BUTTON3)) switch (settings.cursorMode) {
				case MOVE:
					if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
						if (!clipboard.elements.contains(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y))) {
							if ((event.stateMask & SWT.MOD1) == SWT.MOD1) {
								selectAnotherElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
							} else {
								selectElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
							}
						}

						refreshNoPageList();
					} else {
						selectElement(null);
						refresh();
					}
					xDiff -= event.x;
					yDiff -= event.y;
					break;
				case ERASE:
					if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
						selectElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
						refresh();
						MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
						box.setText("Delete Element");
						box.setMessage("Are you sure you want to erase this element?");
						int result = box.open();
						if (result == SWT.YES) {
							YearbookElement e = clipboard.elements.get(0).copy();
							yearbook.page(yearbook.activePage).removeElement(clipboard.elements.get(0));
							stack.push(new ElementCommand(Commands.REMOVE_ELEMENT, e, null, yearbook.page(yearbook.activePage).id));
						}
						refresh();
					}
					break;
				case RESIZE:
					if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y) && (event.stateMask & SWT.MOD1) == SWT.MOD1) {
						selectAnotherElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
						refreshNoPageList();
					} else if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
						selectElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
						refreshNoPageList();
					} else {
						selectElement(null);
					}
				case SELECT:
					startX = event.x;
					startY = event.y;
					xDiff -= event.x;
					yDiff -= event.y;					
					break;
				case ROTATE:
					if (!yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) break;
					openRotateDialog(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
					break;
				default:
					break;


				}

				if (isInsertingText) {
					YearbookTextElement element;
					if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)){
						if (yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y).isText()) {
							element = (YearbookTextElement) yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y);
						} else {
							element = new YearbookTextElement(event.x, event.y, yearbook.settings.width, yearbook.settings.height);
							yearbook.page(yearbook.activePage).addElement(element);
							stack.push(new ElementCommand(Commands.ADD_ELEMENT, null, element.copy(), yearbook.page(yearbook.activePage).id));
						}
					} else {
						int startX = 0;
						element = new YearbookTextElement(event.x, event.y, yearbook.settings.width, yearbook.settings.height);
						yearbook.page(yearbook.activePage).addElement(element);
						stack.push(new ElementCommand(Commands.ADD_ELEMENT, null, element.copy(), yearbook.page(yearbook.activePage).id));
					}

					refresh();
					openTextDialog(element);
					stack.push(new ElementCommand(Commands.ADD_ELEMENT, null, element.copy(), yearbook.page(yearbook.activePage).id));
				}

			}

			@Override
			public void mouseUp(MouseEvent event) {
				if (!rightIsActive()) return;
				if (!isInsertingText) switch (settings.cursorMode) {
				case MOVE:
					xDiff += event.x;
					yDiff += event.y;

					//Prevents accidental movement.
					if (Math.abs(xDiff) < 5 && Math.abs(yDiff) < 5) xDiff = yDiff = 0;

					if (clipboard.elements.size() == 0) return;
					if (clipboard.elements.size() == 1) {
						YearbookElement selectedElement = clipboard.elements.get(0);
						YearbookElement orig = selectedElement.copy();
						if (yearbook.page(yearbook.activePage).findElement(selectedElement) != null && event.button == 1) {
							int newX, newY;
							newX = selectedElement.getBounds().x + xDiff;
							newY = selectedElement.getBounds().y + yDiff;
							yearbook.page(yearbook.activePage).findElement(selectedElement).setLocationRelative(newX, newY);
							stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, yearbook.page(yearbook.activePage).findElement(selectedElement).copy(), yearbook.page(yearbook.activePage).id));
						}
					} else {
						int newX, newY;
						for (YearbookElement element : clipboard.elements) {
							YearbookElement orig = element.copy();
							newX = element.getBounds().x + xDiff;
							newY = element.getBounds().y + yDiff;
							element.setLocationRelative(newX, newY);
							stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, element.copy(), yearbook.page(yearbook.activePage).id));
						}
					}

					refreshNoPageList();
					break;
				case ERASE:
					break;
				case RESIZE:
					xDiff += event.x;
					yDiff += event.y;
					
					if (clipboard.elements.size() > 0) { 
						double xPercent = (double) xDiff / clipboard.elements.get(clipboard.elements.size() - 1).getBounds(yearbook.settings.width, yearbook.settings.height).width;
						double yPercent = (double) yDiff / clipboard.elements.get(clipboard.elements.size() - 1).getBounds(yearbook.settings.width, yearbook.settings.height).height; 
						int newX, newY;
						
						for (YearbookElement selectedElement : clipboard.elements) {
							YearbookElement orig = selectedElement.copy();
							newX = (int) (xPercent * selectedElement.getBounds(yearbook.settings.width, yearbook.settings.height).width);
							newY = (int) (yPercent * selectedElement.getBounds(yearbook.settings.width, yearbook.settings.height).height);
							
							if (yearbook.page(yearbook.activePage).findElement(selectedElement) != null) {
								yearbook.page(yearbook.activePage).findElement(selectedElement).resize(display, newX, newY);
							}
							
							stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, selectedElement.copy(), yearbook.page(yearbook.activePage).id));
						}
						refreshNoPageList();
						startX = startY = xDiff = yDiff = 0;
					}

					break;
				case SELECT:
					xDiff += event.x;
					yDiff += event.y;

					//Prevents accidental movement and
					//helps users select from the edges.
					if (Math.abs(startX) <= 5) startX = 0;
					if (Math.abs(startY) <= 5) startY = 0;
					if (Math.abs(canvas.getBounds().width - event.x) <= 5) 
						if (Math.abs(xDiff) < 15 && Math.abs(yDiff) < 15) xDiff = yDiff = 0;

					selectionRectangle = new Rectangle(startX, startY, xDiff, yDiff);
					
					ArrayList<YearbookElement> selected = yearbook.page(yearbook.activePage).getElementsInRectangle(selectionRectangle, yearbook.settings.width, yearbook.settings.height);
					for (YearbookElement e : selected) {
						selectAnotherElement(e);
					}
					
					startX = startY = xDiff = yDiff = 0;

					refresh();

					break;
				default:
					break;
				}

			}

		});
		
		//Handle arrow key movement.
		canvas.addListener(SWT.KeyUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (!leftIsActive()) return;
				if (clipboard.elements.size() == 0) return;
				if (settings.cursorMode == CursorMode.MOVE) switch (event.keyCode) {
				case SWT.ARROW_DOWN:
					for (YearbookElement e : clipboard.elements) {
						YearbookElement orig = e.copy();
						int newY = e.getBounds().y + 1;
						e.setLocationRelative(e.getBounds().x, newY);
						if (e.getBounds().y < newY) {
							e.setLocationRelative(e.getBounds().x, newY + 1);
						}
						stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, e.copy(), yearbook.page(yearbook.activePage).id));
					}
					break;
				case SWT.ARROW_UP:
					for (YearbookElement e : clipboard.elements) {
						YearbookElement orig = e.copy();
						int newY = e.getBounds().y - 1;
						e.setLocationRelative(e.getBounds().x, newY);
						if (e.getBounds().y > newY) {
							e.setLocationRelative(e.getBounds().x, newY + 1);
						}
						stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, e.copy(), yearbook.page(yearbook.activePage).id));
					}
					break;
				case SWT.ARROW_RIGHT:
					for (YearbookElement e : clipboard.elements) {
						YearbookElement orig = e.copy();
						int newX = e.getBounds().x + 1;
						e.setLocationRelative(newX, e.getBounds().y);
						if (e.getBounds().x < newX) {
							e.setLocationRelative(newX + 1, e.getBounds().y);
						}
						stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, e.copy(), yearbook.page(yearbook.activePage).id));
					}
					break;
				case SWT.ARROW_LEFT:
					for (YearbookElement e : clipboard.elements) {
						YearbookElement orig = e.copy();
						int newX = e.getBounds().x - 1;
						e.setLocationRelative(newX, e.getBounds().y);
						if (e.getBounds().x > newX) {
							e.setLocationRelative(newX - 1, e.getBounds().y);
						}
						stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, e.copy(), yearbook.page(yearbook.activePage).id));
					}
					break;
				}
				refresh();

			}

		});
		
		rightCanvas.addListener(SWT.KeyUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (!rightIsActive()) return;
				if (clipboard.elements.size() == 0) return;
				if (settings.cursorMode == CursorMode.MOVE) switch (event.keyCode) {
				case SWT.ARROW_DOWN:
					for (YearbookElement e : clipboard.elements) {
						YearbookElement orig = e.copy();
						int newY = e.getBounds().y + 1;
						e.setLocationRelative(e.getBounds().x, newY);
						if (e.getBounds().y < newY) {
							e.setLocationRelative(e.getBounds().x, newY + 1);
						}
						stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, e.copy(), yearbook.page(yearbook.activePage).id));
					}
					break;
				case SWT.ARROW_UP:
					for (YearbookElement e : clipboard.elements) {
						YearbookElement orig = e.copy();
						int newY = e.getBounds().y - 1;
						e.setLocationRelative(e.getBounds().x, newY);
						if (e.getBounds().y > newY) {
							e.setLocationRelative(e.getBounds().x, newY + 1);
						}
						stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, e.copy(), yearbook.page(yearbook.activePage).id));
					}
					break;
				case SWT.ARROW_RIGHT:
					for (YearbookElement e : clipboard.elements) {
						YearbookElement orig = e.copy();
						int newX = e.getBounds().x + 1;
						e.setLocationRelative(newX, e.getBounds().y);
						if (e.getBounds().x < newX) {
							e.setLocationRelative(newX + 1, e.getBounds().y);
						}
						stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, e.copy(), yearbook.page(yearbook.activePage).id));
					}
					break;
				case SWT.ARROW_LEFT:
					for (YearbookElement e : clipboard.elements) {
						YearbookElement orig = e.copy();
						int newX = e.getBounds().x - 1;
						e.setLocationRelative(newX, e.getBounds().y);
						if (e.getBounds().x > newX) {
							e.setLocationRelative(newX - 1, e.getBounds().y);
						}
						stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, e.copy(), yearbook.page(yearbook.activePage).id));
					}
					break;
				}
				refresh();

			}

		});
		
		canvas.addMouseMoveListener(new MouseMoveListener() {
			boolean in = false;
			Cursor previousCursor;
			
			@Override
			public void mouseMove(MouseEvent e) {
				if (yearbook.page(yearbook.activePage).isInLayout(e.x, e.y, yearbook.settings.width, yearbook.settings.height)) {
					if (!in) {
						previousCursor = shell.getCursor();
						shell.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
					}
					in = true;
				} else {
					if (in) shell.setCursor(previousCursor);
					in = false;
				}
				
			}
			
		});
		
		rightCanvas.addMouseMoveListener(new MouseMoveListener() {
			boolean in = false;
			Cursor previousCursor;
			
			@Override
			public void mouseMove(MouseEvent e) {
				if (yearbook.page(yearbook.activePage).isInLayout(e.x, e.y, yearbook.settings.width, yearbook.settings.height)) {
					if (!in) {
						previousCursor = shell.getCursor();
						shell.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
					}
					in = true;
				} else {
					if (in) shell.setCursor(previousCursor);
					in = false;
				}
				
			}
			
		});

		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				refreshNoPageList();
				
			}
			
		});

		rightCanvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				refreshNoPageList();
				
			}
			
		});

	}
	
	private void openRotateDialog(YearbookElement elementAtPoint) {
		YearbookElement orig = elementAtPoint.copy();
		float originalRotation = elementAtPoint.rotation;
		final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setText("Rotate Element");
		dialog.setSize(400, 300);
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = 10;
		formLayout.marginHeight = 10;
		formLayout.spacing = 10;
		dialog.setLayout(formLayout);

		Label label = new Label(dialog, SWT.NONE);
		label.setText("Angle of rotation (°):");
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
				elementAtPoint.rotation = originalRotation;
				refreshNoPageList();
				dialog.close();
				dialog.dispose();
			}
		});

		final Spinner text = new Spinner(dialog, SWT.WRAP);
		text.setMinimum(-179);
		text.setMaximum(180);
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				int count = 0;
				try {
					elementAtPoint.rotation = Float.valueOf(text.getText());
				} catch (NumberFormatException e1) {
					//Ignore
				}
				if (count++ % 3 == 0) refreshNoPageList();
			}
			
		});
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
				elementAtPoint.rotation = Float.valueOf(text.getText());
				dialog.close();
				refreshNoPageList();
				dialog.dispose();
				stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, elementAtPoint.copy(), yearbook.page(yearbook.activePage).id));
			}
		});

		dialog.setDefaultButton (ok);
		dialog.pack();
		dialog.open();
		
	}

	private void openAddBorderDialog(YearbookElement element) {
		YearbookElement orig = element.copy();
		YearbookElementBorder originalBorder = element.border;
		
		element.border.noBorder = false;
		
		final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setText("Add Border");
		dialog.setSize(400, 300);
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = 10;
		formLayout.marginHeight = 10;
		formLayout.spacing = 10;
		dialog.setLayout(formLayout);

		Label label = new Label(dialog, SWT.NONE);
		label.setText("Border width (px):");
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
				element.border = originalBorder;
				refreshNoPageList();
				dialog.close();
				dialog.dispose();
			}
		});

		final Spinner text = new Spinner(dialog, SWT.NONE);
		text.setMaximum(180);
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				int val = 0;
				try {
					 val = Integer.valueOf(text.getText());
				} catch (NumberFormatException ex) {
					//Ignore.
				}
				element.border.setWidth(val, yearbook.settings.width);
				refreshNoPageList();
				
			}
			
		});
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
				dialog.close();
				dialog.dispose();
				refreshNoPageList();
			}
		});

		ColorDialog colorDialog = new ColorDialog(dialog, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		colorDialog.setText("Color Picker");
		Button colorButton = new Button(dialog, SWT.PUSH);
		colorButton.setText("Pick color...");
		data = new FormData();
		data.width = 100;
		data.right = new FormAttachment(ok, 0, SWT.DEFAULT);
		data.bottom = new FormAttachment(100, 0);
		colorButton.setLayoutData(data);

		colorButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				RGB rgb = colorDialog.open();
				if (rgb != null) element.border.rgb = rgb;
				refreshNoPageList();

			}

		});
		
		dialog.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event event) {
				stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, element.copy(), yearbook.page(yearbook.activePage).id));
				
			}
			
		});

		dialog.setDefaultButton (ok);
		dialog.pack();
		dialog.open();
	}

	protected void openTextDialog(YearbookTextElement element) {
		YearbookElement orig = element.copy();
		/*
		 * Create layout for text tool.
		 */
		Shell textTool = new Shell(display, SWT.DIALOG_TRIM);
		textTool.setText("Text Tool");
		GridLayout layout = new GridLayout();
		layout.numColumns = 10;
		layout.makeColumnsEqualWidth = true;
		textTool.setLayout(layout);

		Text textbox = new Text(textTool, SWT.BORDER | SWT.MULTI);
		textbox.setText(element.text);
		GridData textboxData = new GridData(SWT.FILL, SWT.FILL, true, true);
		textboxData.horizontalSpan = 10;
		textbox.setLayoutData(textboxData);

		ColorDialog colorDialog = new ColorDialog(textTool, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		colorDialog.setText("Color Picker");
		Button colorButton = new Button(textTool, SWT.PUSH);
		colorButton.setText("Pick color...");
		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonData.horizontalSpan = 3;
		colorButton.setLayoutData(buttonData);

		Combo sizeCombo = new Combo(textTool, SWT.DROP_DOWN);
		GridData sizeData = new GridData(SWT.FILL, SWT.FILL, true, false);
		sizeData.horizontalSpan = 2;
		sizeCombo.setLayoutData(sizeData);
		String[] fontSizes = {
				"8",
				"9",
				"10",
				"11",
				"12",
				"14",
				"16",
				"18",
				"20",
				"22",
				"24",
				"26",
				"28",
				"36",
				"48",
				"72"
		};
		for (String size : fontSizes) {
			sizeCombo.add(size);
		}
		int index = Arrays.binarySearch(fontSizes, Integer.toString(element.size));
		if (index >= 0) sizeCombo.select(index);
		else sizeCombo.select(4);


		Combo fontCombo = new Combo(textTool, SWT.DROP_DOWN);
		GridData fontData  = new GridData(SWT.FILL, SWT.FILL, true, false);
		fontData.horizontalSpan = 3;
		fontCombo.setLayoutData(fontData);
		for (String fontName : fontNames) {
			fontCombo.add(fontName);
		}
		for (index = 0; index < fontNames.length; index++) {
			if (fontNames[index].equalsIgnoreCase(element.fontFamily)) break;
		}
		if (index >= 0) fontCombo.select(index);

		Composite styleWrapper = new Composite(textTool, SWT.NONE);
		styleWrapper.setLayout(new FillLayout());
		GridData styleData = new GridData(SWT.FILL, SWT.FILL, true, false);
		styleData.horizontalSpan = 2;
		styleWrapper.setLayoutData(styleData);

		Button bold = new Button(styleWrapper, SWT.PUSH);
		bold.setText("B");
		FontData fd = bold.getFont().getFontData()[0];
		fd.setStyle(SWT.BOLD);
		Font f = new Font(display, fd);
		bold.setFont(f);
		f.dispose();

		Button italic = new Button(styleWrapper, SWT.PUSH);
		italic.setText("I");
		fd = italic.getFont().getFontData()[0];
		fd.setStyle(SWT.ITALIC);
		f = new Font(display, fd);
		italic.setFont(f);
		f.dispose();

		Button underline = new Button(styleWrapper, SWT.PUSH);
		underline.setText("U");

		Button shadow = new Button(styleWrapper, SWT.PUSH);
		shadow.setText("S");

		/*
		 * Add listeners to each component.
		 */
		textbox.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				element.text = textbox.getText();
				refresh();

			}

		});

		colorButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				RGB rgb = colorDialog.open();
				if (rgb != null) element.setRGB(rgb);
				refresh();

			}

		});


		sizeCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.size = Integer.parseInt(sizeCombo.getItem(sizeCombo.getSelectionIndex()));
				refresh();

			}

		});

		sizeCombo.addListener(SWT.KeyUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				int size = element.size;
				try {
					size = Integer.parseInt(sizeCombo.getText());
				} catch (NumberFormatException e) {
					return;
				}
				element.size = size;
				refresh();
			}

		});

		fontCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.fontFamily = fontNames[fontCombo.getSelectionIndex()];
				refresh();
			}

		});

		bold.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.toggleBold();
				refresh();
			}

		});

		italic.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.toggleItalic();
				refresh();
			}

		});

		underline.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.toggleUnderline();
				refresh();
			}

		});

		shadow.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.toggleShadow();
				refresh();

			}

		});

		textTool.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event event) {
				modeReset();
				stack.push(new ElementCommand(Commands.CHANGE_ELEMENT, orig, element.copy(), yearbook.page(yearbook.activePage).id));
			}

		});

		textTool.setSize(500, 200);
		textTool.open();
	}

	private void selectElement(YearbookElement element) {
		clipboard.elements.clear();
		if (element == null) return;
		clipboard.elements.add(element);
	}

	private void selectAnotherElement(YearbookElement element) {
		if (element == null) return;
		if (clipboard.elements.contains(element)) return;
		clipboard.elements.add(element);
	}

	private void selectElements(ArrayList<YearbookElement> elements) {
		clipboard.elements.clear();
		clipboard.elements.addAll(elements);
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
				refreshNoPageList();
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

		fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveItem.setText("&Save\tCtrl+S");
		fileSaveItem.setAccelerator(SWT.MOD1 | 'S');

		fileSaveAsItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveAsItem.setText("Save &As...\tCtrl+Shift+S");
		fileSaveAsItem.setAccelerator(SWT.MOD1 | SWT.MOD2 | 'S');

		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		fileExportItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExportItem.setText("&Export to PDF (Print)");
		
		fileExportJPEGItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExportJPEGItem.setText("Export to &PNG (Screen)");
		
		fileExportVideoItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExportVideoItem.setText("Export to &Video Yearbook");

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

		editYearbookNameItem = new MenuItem(editMenu, SWT.PUSH);
		editYearbookNameItem.setText("Yearbook Name...");

		new MenuItem(editMenu, SWT.SEPARATOR);

		pageShowGridItem = new MenuItem(editMenu, SWT.CHECK);
		pageShowGridItem.setText("Show &Grid");

		pageShowTextItem = new MenuItem(editMenu, SWT.CHECK);
		pageShowTextItem.setText("Show Text on Tool Bar");


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

		new MenuItem(insertMenu, SWT.SEPARATOR);
		
		insertPSPAItem = new MenuItem(insertMenu, SWT.PUSH);
		insertPSPAItem.setText("Photos from PSPA Disk...");
		
		insertGeneratePSPAItem = new MenuItem(insertMenu, SWT.PUSH);
		insertGeneratePSPAItem.setText("Generate Pages from Volume...");

		new MenuItem(insertMenu, SWT.SEPARATOR);

		insertPageNumbersItem = new MenuItem(insertMenu, SWT.PUSH);
		insertPageNumbersItem.setText("Page Numbers...");

		insertToCItem = new MenuItem(insertMenu, SWT.PUSH);
		insertToCItem.setText("Clear Page Numbers");

		//Create Page Menu
		pageMenuItem = new MenuItem(menubar, SWT.CASCADE);
		pageMenuItem.setText("&Page");
		Menu pageMenu = new Menu(shell, SWT.DROP_DOWN);
		pageMenuItem.setMenu(pageMenu);

		pageBackgroundItem = new MenuItem(pageMenu, SWT.PUSH);
		pageBackgroundItem.setText("&Add Background...");

		pageMirrorItem = new MenuItem(pageMenu, SWT.PUSH);
		pageMirrorItem.setText("&Mirror Background...");

		pageClearBackgroundItem = new MenuItem(pageMenu, SWT.PUSH);
		pageClearBackgroundItem.setText("&Clear Background");

		new MenuItem(pageMenu, SWT.SEPARATOR);

		pageAddCoverItem = new MenuItem(pageMenu, SWT.PUSH);
		pageAddCoverItem.setText("Add Cover (Double Spread)");

		pageUseCoverItem = new MenuItem(pageMenu, SWT.CHECK);
		pageUseCoverItem.setText("Show Cover");



		//Create the help menu.
		helpMenuItem = new MenuItem(menubar, SWT.CASCADE);
		helpMenuItem.setText("&Help");
		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuItem.setMenu(helpMenu);

		helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutItem.setText("&About " + Creator.SOFTWARE_NAME);
		helpAboutItem.setAccelerator(SWT.MOD1 + 'Z');
		
		new MenuItem(helpMenu, SWT.SEPARATOR);
		
		helpGenerateKeysItem = new MenuItem(helpMenu, SWT.PUSH);
		helpGenerateKeysItem.setText("&Generate Product Keys");
	}

	private void initialize() {
		isInsertingText = false;
		MOD1 = false;

		canvasBackgroundColor = new Color(display, 254, 254, 254);

		/*
		 * Let's create a splash screen.
		 */
		Shell splash = new Shell(display, SWT.SHELL_TRIM);
		splash.setSize(500, 500);
		splash.setLayout(new FillLayout(SWT.VERTICAL));
		splash.setText(COMPANY_NAME + " " + SOFTWARE_NAME);

		Button newYearbookBtn = new Button(splash, SWT.PUSH);
		newYearbookBtn.setImage(YearbookImages.newDocument(display));
		newYearbookBtn.setText("\tNew Yearbook\t");
		FontData fd = newYearbookBtn.getFont().getFontData()[0];
		fd.setHeight(18);
		newYearbookBtn.setFont(new Font(display, fd));

		Button openYearbookBtn = new Button(splash, SWT.PUSH);
		openYearbookBtn.setImage(YearbookImages.openDocument(display));
		openYearbookBtn.setText("\tOpen Yearbook\t");
		fd = openYearbookBtn.getFont().getFontData()[0];
		fd.setHeight(18);
		openYearbookBtn.setFont(new Font(display, fd));

		Button importPDFBtn = new Button(splash, SWT.PUSH);
		importPDFBtn.setImage(YearbookImages.importPDF(display));
		importPDFBtn.setText("\tImport PDF...\t");
		fd = importPDFBtn.getFont().getFontData()[0];
		fd.setHeight(18);
		importPDFBtn.setFont(new Font(display, fd));

		newYearbookBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				splash.close();
				splash.dispose();
				shell.open();
				
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
				cancel.setEnabled(false);

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
						saveFileName = null;
						createNewYearbook(text.getText());
						dialog.close();
						dialog.dispose();
						refresh();
					}
				});

				dialog.setDefaultButton (ok);
				dialog.pack();
				dialog.open();
			}

		});

		openYearbookBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				fileOpenItem.getListeners(SWT.Selection)[0].handleEvent(event);
				if (yearbook != null) {
					splash.close();
					splash.dispose();
					shell.open();
					refresh();
				}
			}

		});

		importPDFBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				FileDialog dialog = new FileDialog(splash, SWT.OPEN);
				String[] allowedExtensions = {"*.pdf", "*.*"};
				dialog.setFilterExtensions(allowedExtensions);
				String fileName = dialog.open();
				if (fileName == null) return;
				if (!fileName.split("\\.")[fileName.split("\\.").length - 1].equalsIgnoreCase("pdf")) {
					MessageBox box = new MessageBox(splash, SWT.ICON_ERROR | SWT.OK);
					box.setText("Import PDF...");
					box.setMessage("File " + fileName + " is not a PDF file.");
					box.open();
				} else {
					Shell wait = new Shell(splash, SWT.NO_TRIM);
					wait.setSize(300, 300);
					
					wait.addPaintListener(new PaintListener() {

						@Override
						public void paintControl(
								PaintEvent e) {
							Font font = new Font(display,"Arial",14,SWT.BOLD | SWT.ITALIC); 
							e.gc.setFont(font);
							int x = (300 - e.gc.textExtent("Please wait.").x) / 2;
							int y = (300 - e.gc.textExtent("Please wait.").y) / 2;
							e.gc.drawText("Please wait.", x, y, true);
							font.dispose();
						}
						
					});
					
					wait.open();
					Yearbook newYearbook = Yearbook.importFromPDF(display, fileName);
					wait.close();
					wait.dispose();
					if (newYearbook != null) {
						yearbook = newYearbook;
						createNewYearbook();
						splash.close();
						splash.dispose();
						shell.open();
						refresh();
					} else {
						MessageBox box = new MessageBox(splash, SWT.ICON_ERROR | SWT.OK);
						box.setText("Import PDF...");
						box.setMessage("Something went wrong while creating a new yearbook from PDF.");
						box.open();
					}

				}

			}

		});

		splash.pack();
		splash.open();
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
						dialog.dispose();
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
						saveFileName = null;
						createNewYearbook(text.getText());
						dialog.close();
						dialog.dispose();
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
						dialog.dispose();
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
						dialog.dispose();
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
				FileDialog picker = new FileDialog(shell, SWT.OPEN);
				picker.setText("Open Yearbook");
				picker.setFilterExtensions(new String[] {"*.ctc"});
				String fileName = picker.open();
				if (fileName == null) return;
				try {
					saveFileName = fileName;
					yearbook = Yearbook.readFromDisk(fileName);
					createNewYearbook();

					refresh();
				} catch (Exception e) {
					MessageBox box = new MessageBox(shell, SWT.ERROR);
					box.setText("Open Yearbook");
					box.setMessage("Something went wrong while trying to open your file.\n\t" + e.getMessage());
					box.open();
					e.printStackTrace();
				}
			}

		});

		fileSaveItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (saveFileName != null)
					try {
						Yearbook.saveToDisk(yearbook, saveFileName);
					} catch (IOException e) {
						e.printStackTrace();
					}
				else fileSaveAsItem.getListeners(SWT.Selection)[0].handleEvent(event);				
			}

		});

		fileSaveAsItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				FileDialog picker = new FileDialog(shell, SWT.SAVE);
				picker.setText("Save As...");
				picker.setFilterExtensions(new String[] {"*.ctc"});
				String fileName = picker.open();
				if (fileName == null) return;
				try {
					saveFileName = fileName;
					Yearbook.saveToDisk(yearbook, fileName);
				} catch (IOException e) {
					MessageBox box = new MessageBox(shell, SWT.ERROR);
					box.setText("Save Yearbook");
					box.setMessage("Something went wrong while trying to save your file.\n\t" + e.getMessage());
					box.open();
					e.printStackTrace();
				}

			}

		});

		fileExportItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				FileDialog picker = new FileDialog(shell, SWT.SAVE);
				picker.setText("Export to PDF");
				String fileName = picker.open();
				if (fileName == null) return;

				try {
					Yearbook.exportToPDF(yearbook, fileName, display);
				} catch (Throwable e) {
					MessageBox box = new MessageBox(shell, SWT.ERROR | SWT.OK);
					box.setText("Error");
					box.setMessage("PDF export was unsuccessful. Please save and restart " + Creator.SOFTWARE_NAME + " and then try again.");
					box.open();
					e.printStackTrace();
				}
			}

		});
		
		fileExportJPEGItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				DirectoryDialog picker = new DirectoryDialog(shell);
				picker.setText("Select Folder");
				String folder = picker.open();
				if (folder == null) return;
				
				try {
					Yearbook.exportToPNG(yearbook, folder, display);
				} catch (Throwable e) {
					MessageBox box = new MessageBox(shell, SWT.ERROR | SWT.OK);
					box.setText("Error");
					box.setMessage("PNG export was unsuccessful. Please save and restart " + Creator.SOFTWARE_NAME + " and then try again.");
					box.open();
					e.printStackTrace();
				}
				
			}
			
		});
		
		fileExportVideoItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				FileDialog picker = new FileDialog(shell, SWT.SAVE);
				picker.setText("Save As...");
				picker.setFilterExtensions(new String[] {"*.ctcs"});
				String fileName = picker.open();
				if (fileName == null) return;
				
				/*
				 * Add in the Digital Express logo.
				 */
				
				Image logo = YearbookImages.logo(display);
				Image pageBg = new Image(display, yearbook.settings.width, yearbook.settings.height);
				GC gc = new GC(pageBg);

				int x = (yearbook.settings.width - logo.getBounds().width) / 2;
				int y = (yearbook.settings.height - logo.getBounds().height) / 2;
				gc.drawImage(logo, x, y);
				
				gc.dispose();
				logo.dispose();
				
				yearbook.insertPage(new YearbookPage(pageBg), 0);
				yearbook.addPage(new YearbookPage(pageBg));
				pageBg.dispose();
				
				
				/*
				 * Add in the covers, if they exist.
				 */
				if (yearbook.hasCover) {
					Image image = yearbook.cover(display);
					
					Image front = new Image(display, yearbook.settings.width, yearbook.settings.height);
					gc = new GC(front);
					gc.drawImage(image, image.getBounds().width / 2, 0, (int) Math.floor(image.getBounds().width / 2), image.getBounds().height, 0, 0, canvas.getBounds().width, canvas.getBounds().height);
					gc.dispose();
					yearbook.insertPage(new YearbookPage(front), 0);
					front.dispose();
					
					Image back = new Image(display, yearbook.settings.width, yearbook.settings.height);
					gc = new GC(back);
					gc.drawImage(image, 0, 0, (int) Math.floor(image.getBounds().width / 2), image.getBounds().height, 0, 0, canvas.getBounds().width, canvas.getBounds().height);
					gc.dispose();
					yearbook.addPage(new YearbookPage(back));
					back.dispose();
					
					image.dispose();
				}
				
				try {
					Yearbook.saveToDisk(yearbook, fileName);
				} catch (IOException e) {
					MessageBox box = new MessageBox(shell, SWT.ERROR);
					box.setText("Save Yearbook");
					box.setMessage("Something went wrong while trying to export your video yearbook.\n\t" + e.getMessage());
					box.open();
					e.printStackTrace();
				}
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
				undo();

			}

		});

		editRedoItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				redo();

			}

		});

		editCutItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				clipboard.cut = true;
				clipboard.cutElements.clear();
				clipboard.cutElements.addAll(clipboard.elements);
				refreshNoPageList();
			}

		});

		editCopyItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				clipboard.cutElements.clear();
				clipboard.cutElements.addAll(clipboard.elements);

			}

		});
		editPasteItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				for (YearbookElement e : clipboard.cutElements) {
					yearbook.page(yearbook.activePage).addElement(e.copy());
				}
				
				if (clipboard.cut) {
					for (YearbookElement e : clipboard.cutElements) {
					yearbook.removeElement(e);
					}
					clipboard.cut = false;
				}
				refreshNoPageList();
			}

		});

		editYearbookNameItem.addListener(SWT.Selection, new Listener() {

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
				label.setText("Yearbook name:");
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
						dialog.dispose();
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
						yearbook.name = text.getText();
						dialog.close();
						dialog.dispose();
						refreshYearbookName();
					}
				});

				dialog.setDefaultButton (ok);
				dialog.pack();
				dialog.open();
			}

		});

		insertTextItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				isInsertingText = !isInsertingText;

				if (isInsertingText) shell.setCursor(display.getSystemCursor(SWT.CURSOR_IBEAM));
				else modeReset();




			}

		});

		insertImageItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				String fileName = imagePicker();
				if (fileName == null) return;
				YearbookImageElement element = new YearbookImageElement(display, fileName, yearbook.settings.width, yearbook.settings.height);
				stack.push(new ElementCommand(Commands.ADD_ELEMENT, null, element.copy(), yearbook.page(yearbook.activePage).id));
				yearbook.page(yearbook.activePage).addElement(element);
				refreshNoPageList();
			}

		});

		insertVideoItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (clipboard.elements.isEmpty() && selectionRectangle == null) {
					MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION);
					box.setText("Insert Video");
					box.setMessage("Please select an area of the page to link to the video.");
					box.open();
					return;
				}
				for (YearbookElement selectedElement : clipboard.elements) {
					if ((settings.cursorMode != CursorMode.SELECT || selectionRectangle == null) && clipboard.elements.size() == 0) {
						MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION);
						box.setText("Insert Video");
						box.setMessage("Please select an area of the page to link to the video.");
						box.open();
						return;
					} else if (selectedElement != null && selectedElement.isImage()) {
						if (selectionRectangle == null) {
							try {
								attachVideoToImage((YearbookImageElement) selectedElement);
							} catch (IOException e) {
								e.printStackTrace();
							}
							return;
						}
					}
				}


				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				String[] allowedExtensions = {"*.webm;*.mkv;*.flv;*.vob;*.ogv;*.ogg;*.drc;*.avi;*.mov;*.qt;*.wmv;*.rm;*.mp4;*.m4p;*.m4v;*.mpg;*.3gp;*.3g2", "*.*"};
				dialog.setFilterExtensions(allowedExtensions);
				String fileName = dialog.open();
				if (fileName == null) return;

				//Need to make sure the video exists.
				File testFile = new File(fileName);
				if (!testFile.exists()) return;

				try {
					YearbookClickableElement e = new YearbookClickableElement(new Video(fileName), selectionRectangle, canvas.getBounds().height, canvas.getBounds().width);
					stack.push(new ElementCommand(Commands.ADD_ELEMENT, null, e.copy(), yearbook.page(yearbook.activePage).id));
					yearbook.page(yearbook.activePage).addElement(e);
				} catch (IOException e) {
					e.printStackTrace();
				}

				modeReset();
				refresh();


			}

		});
		
		insertPSPAItem.addListener(SWT.Selection, new Listener() {
			File root;
			Volume volume;

			@Override
			public void handleEvent(Event event) {
				volume = new Volume(yearbook.settings.width, yearbook.settings.height);
				Shell window = new Shell(shell, SWT.SHELL_TRIM);
				window.setText("Load PSPA Volume");
				
				GridLayout layout = new GridLayout();
				layout.makeColumnsEqualWidth = true;
				layout.numColumns = 6;
				window.setLayout(layout);
				
				Label rows = new Label(window, SWT.LEFT);
				rows.setText("Rows:");
				GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false);
				data.horizontalSpan = 3;
				rows.setLayoutData(data);
				
				FontData[] fontData = rows.getFont().getFontData();
				for(int i = 0; i < fontData.length; ++i)
				    fontData[i].setHeight(16);

				final Font newFont = new Font(display, fontData);
				rows.setFont(newFont);

				rows.addDisposeListener(new DisposeListener() {
				    public void widgetDisposed(DisposeEvent e) {
				        newFont.dispose();
				    }
				});
				
				Combo rowsCombo = new Combo(window, SWT.DROP_DOWN);
				String[] rowsOptions = new String[] {
					"3",
					"4",
					"5",
					"6",
					"7",
					"8"
				};
				for (String s : rowsOptions) {
					rowsCombo.add(s);
				}
				
				data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
				data.horizontalSpan = 3;
				rowsCombo.setLayoutData(data);
				
				Label columns = new Label(window, SWT.LEFT);
				columns.setText("Columns:");
				data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false);
				data.horizontalSpan = 3;
				columns.setLayoutData(data);
				columns.setFont(newFont);
				
				Combo columnsCombo = new Combo(window, SWT.DROP_DOWN);
				String[] columnsOptions = new String[] {
					"1",
					"2",
					"3",
					"4",
					"5",
					"6",
					"7",
					"8",
					"9"
				};
				
				data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
				data.horizontalSpan = 3;
				
				for (String s : columnsOptions) {
					columnsCombo.add(s);
				}
				
				columnsCombo.setLayoutData(data);
				
				Label name = new Label(window, SWT.LEFT);
				name.setText("Name:");
				data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false);
				data.horizontalSpan = 3;
				name.setLayoutData(data);
				name.setFont(newFont);
				
				Text nameText = new Text(window, SWT.SINGLE);
				data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
				data.horizontalSpan = 3;
				nameText.setLayoutData(data);
				
				
				Button fontBtn = new Button(window, SWT.PUSH);
				fontBtn.setText("Set Font");
				
				data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
				data.horizontalSpan = 3;
				fontBtn.setLayoutData(data);
				
				Button chooseBtn = new Button(window, SWT.PUSH);
				chooseBtn.setText("Choose PSPA Volume");
				
				data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
				data.horizontalSpan = 3;
				chooseBtn.setLayoutData(data);
				
				Label blank = new Label(window, SWT.NONE);
				
				data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
				data.horizontalSpan = 2;
				blank.setLayoutData(data);
				
				Button ok = new Button(window, SWT.PUSH);
				ok.setText("OK");
				
				data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
				data.horizontalSpan = 2;
				ok.setLayoutData(data);
				
				Button cancel = new Button(window, SWT.PUSH);
				cancel.setText("Cancel");
				
				data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
				data.horizontalSpan = 2;
				cancel.setLayoutData(data);
				
				fontBtn.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event event) {
						openPSPATextDialog(volume.textElement);
						
					}
					
				});
				
				chooseBtn.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event event) {
						DirectoryDialog picker = new DirectoryDialog(shell, SWT.NONE);
						String fileName = picker.open();
						if (fileName == null) return;
						
						root = new File(fileName);
						
					}
					
				});
				
				cancel.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event event) {
						window.close();
						window.dispose();
						
					}
					
				});
				
				ok.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event event) {
						if (root == null) {
							MessageBox box = new MessageBox(window, SWT.OK);
							box.setText("Invalid Folder");
							box.setMessage("Please select a PSPA volume.");
							box.open();
							return;
						}
						
						if (columnsCombo.getSelectionIndex() < 0 || rowsCombo.getSelectionIndex() < 0) {
							MessageBox box = new MessageBox(window, SWT.OK);
							box.setText("Invalid Option");
							box.setMessage("Please choose a row and column height.");
							box.open();
							return;
						}
						
						try {
							volume.processRoot(root);
							
							int rows = Integer.parseInt(rowsOptions[rowsCombo.getSelectionIndex()]);
							int cols = Integer.parseInt(columnsOptions[columnsCombo.getSelectionIndex()]);
							volume.grid = new Point(cols, rows);
							
							if (!nameText.getText().isEmpty()) volume.name = nameText.getText();
							
							yearbook.volumes.add(volume);
							
						} catch (IOException
								| PSPAIndexNotFoundException e) {
							MessageBox box = new MessageBox(window, SWT.ICON_ERROR);
							box.setText("Error");
							box.setMessage("The selected directory is not a valid PSPA volume.");
							box.open();
						} catch (Exception e) {
							MessageBox box = new MessageBox(window, SWT.ICON_ERROR);
							box.setText("Error");
							box.setMessage("An unknown error occurred.\n\t" + e);
							box.open();
						}
						window.close();
						window.dispose();
					}
					
				});
				
				window.setSize(300, 180);
				window.open();
			}
			
		});
		
		insertGeneratePSPAItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				Shell window = new Shell(shell, SWT.SHELL_TRIM);
				window.setText("Generate Pages");
				GridLayout layout = new GridLayout();
				layout.makeColumnsEqualWidth = true;
				layout.numColumns = 1;
				window.setLayout(layout);
				
				List list = new List(window, SWT.MULTI);
				for (Volume v : yearbook.volumes) {
					list.add(v.name + " (" + v.fileName + ")");
				}
				
				GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
				data.horizontalSpan = 1;
				list.setLayoutData(data);
				
				Button genBtn = new Button(window, SWT.PUSH);
				genBtn.setText("Generate Pages");
				data = new GridData(SWT.FILL, SWT.END, true, false);
				data.horizontalSpan = 1;
				genBtn.setLayoutData(data);

				genBtn.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event event) {
						try {
							Volume volume = yearbook.volumes.get(list.getSelectionIndex());
							
							getPSPAGradeOrder(volume);
							
							window.close();
							window.dispose();
						} catch (Exception e) {
							//Do nothing.
						}
						
					}
					
				});
				
				window.setSize(300, 300);
				window.open();
			}
			
		});
		
		insertPageNumbersItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				yearbook.settings.showPageNumbers = true;
				openPageNumberDialog(yearbook.numbers);
			}
			
		});
		
		insertToCItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				yearbook.settings.showPageNumbers = false;
				refreshNoPageList();
			}
			
		});

		pageMirrorItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				int mirrorPage = yearbook.activePage + 1;

				if (mirrorPage >= yearbook.size() || mirrorPage < 0 || yearbook.page(yearbook.activePage).getBackgroundImageData() == null) return;

				yearbook.page(mirrorPage).setBackgroundImageData(SWTUtils.horizontalFlipSWT(yearbook.page(yearbook.activePage).getBackgroundImageData()));
				refresh();
			}

		});

		pageBackgroundItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				String fileName = imagePicker();
				if (fileName == null) return;
				try {
					ImageData data = new ImageData(fileName);
					yearbook.page(yearbook.activePage).setBackgroundImageData(data);
					refreshNoPageList();
				} catch (SWTException e) {
					MessageBox helpBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
					helpBox.setText("Error");
					helpBox.setMessage("The following error occurred: \n\t" + e.getMessage());
					helpBox.open();
				}
			}

		});

		pageClearBackgroundItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				System.out.println("Don't forget to add a page chooser.");
				yearbook.page(yearbook.activePage).clearBackgroundImage();
				refreshNoPageList();

			}

		});
		
		pageAddCoverItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				FileDialog picker = new FileDialog(shell, SWT.OPEN);
				String[] allowedExtensions = {"*.jpg; *.jpeg; *.gif; *.tif; *.tiff; *.bpm; *.ico; *.png; *.pdf", "*.*"};
				picker.setFilterExtensions(allowedExtensions);
				String fileName = picker.open();
				if (fileName == null) return;
				try {
					if (fileName.endsWith("pdf") || fileName.endsWith("PDF")) {
						yearbook.setCover(SWTUtils.convertToSWT(Yearbook.convertDoubleSpreadPDFToImage(fileName)));
					} else {
						yearbook.setCover(new ImageData(fileName));
					}
				} catch (Throwable t) {
					MessageBox box = new MessageBox(shell, SWT.OK | SWT.ERROR);
					box.setText("Error");
					box.setMessage("An error occurred:\n\t" + t);
					box.open();
				}
				
			}
			
		});
		
		pageUseCoverItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				yearbook.hasCover = !yearbook.hasCover;
			}
			
		});

		pageShowGridItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				settings.showGrid = !settings.showGrid;
				refreshNoPageList();
			}

		});
		
		pageShowTextItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				settings.showNavbarText = !settings.showNavbarText;
				if (settings.showNavbarText) {
					newBtn.setText("New Page");
					openBtn.setText("Open Yearbook");
					saveBtn.setText("Save");
					previewBtn.setText("Export to PDF");
					undoBtn.setText("Undo");
					redoBtn.setText("Redo");
					cutBtn.setText("Cut");
					copyBtn.setText("Copy");
					pasteBtn.setText("Paste");
					textBtn.setText("Insert Text");
					imageBtn.setText("Insert Image");
					videoBtn.setText("Insert Video");
					moveBtn.setText("Move Mode");
					resizeBtn.setText("Resize Mode");
					selectBtn.setText("Select Mode");
					eraseBtn.setText("Erase Mode");
					rotateBtn.setText("Rotate Mode");
				} else {
					newBtn.setText("");
					openBtn.setText("");
					saveBtn.setText("");
					previewBtn.setText("");
					undoBtn.setText("");
					redoBtn.setText("");
					cutBtn.setText("");
					copyBtn.setText("");
					pasteBtn.setText("");
					textBtn.setText("");
					imageBtn.setText("");
					videoBtn.setText("");
					moveBtn.setText("");
					resizeBtn.setText("");
					selectBtn.setText("");
					eraseBtn.setText("");
					rotateBtn.setText("");
				}
				toolbarWrapper.pack();
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
		
		helpGenerateKeysItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				dialog.setText("Number of Product Keys to Generate");
				dialog.setSize(400, 300);
				FormLayout formLayout = new FormLayout();
				formLayout.marginWidth = 10;
				formLayout.marginHeight = 10;
				formLayout.spacing = 10;
				dialog.setLayout(formLayout);

				Label label = new Label(dialog, SWT.NONE);
				label.setText("Number of Product Keys to Generate:");
				FormData data = new FormData();
				label.setLayoutData(data);
				
				Button cancel = new Button(dialog, SWT.PUSH);
				cancel.setText("Cancel");
				data = new FormData();
				data.width = 60;
				data.right = new FormAttachment(100, 0);
				data.bottom = new FormAttachment(100, 0);
				cancel.setLayoutData(data);
				cancel.setEnabled(false);

				final Spinner text = new Spinner(dialog, SWT.BORDER);
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
						int n = Integer.parseInt(text.getText());
						try {
							String[] keys = ProductKey.parseJSONArray(ProductKey.generateKeys(new URL(new Config().generateProductKeyURL), n));
							Shell keyShell = new Shell(display);
							keyShell.setText("Product Keys");
							keyShell.setLayout(new FillLayout());
							Text text = new Text(keyShell, SWT.MULTI);
							String textData = "";
							for (String s : keys) {
								textData += s + "\n";
							}
							text.setText(textData);
							
							keyShell.setSize(200, 500);
							keyShell.open();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
						dialog.close();
						dialog.dispose();
					}
				});

				dialog.setDefaultButton (ok);
				dialog.pack();
				dialog.open();
				
			}
			
		});

	}
	
	protected void redo() {
		Command c = stack.redo();
		if (c.isElement()) {
			ElementCommand command = (ElementCommand) c;
			switch (c.action) {
			case ADD_ELEMENT:
				yearbook.pageById(command.pageId).addElement(command.modified);
				break;
			case CHANGE_ELEMENT:
				yearbook.swapElement(command.original, command.modified);
				break;
			case REMOVE_ELEMENT:
				yearbook.pageById(command.pageId).removeElement(command.original);
				break;
			default:
				break;
			}
		}/* else if (c.isPage()) {
			PageCommand command = (PageCommand) c;
			switch (c.action) {
			case ADD_PAGE:
				yearbook.insertPage(command.page, command.destination);
				break;
			case MOVE_PAGE:
				yearbook.movePage(command.source, command.destination);
				break;
			case REMOVE_PAGE:
				yearbook.removePage(command.page);
				break;
			default:
				break;
			}
		}*/
		
		
		refresh();
	}

	
	//TODO: Fix page stuff.
	protected void undo() {
		Command c = stack.undo();
		if (c.isElement()) {
			ElementCommand command = (ElementCommand) c;
			switch (c.action) {
			case ADD_ELEMENT:
				yearbook.removeElement(command.modified);
				break;
			case CHANGE_ELEMENT:
				yearbook.swapElement(command.modified, command.original);
				break;
			case REMOVE_ELEMENT:
				yearbook.pageById(command.pageId).addElement(command.original);
				break;
			default:
				break;
			}
		}/* else if (c.isPage()) {
			PageCommand command = (PageCommand) c;
			switch (c.action) {
			case ADD_PAGE:
				yearbook.removePage(command.page);
				break;
			case MOVE_PAGE:
				yearbook.movePage(command.destination, command.source);
				break;
			case REMOVE_PAGE:
				yearbook.insertPage(command.page, command.source);
				break;
			default:
				break;
			}
		}*/
		

		
		refresh();
	}

	private void getPSPAGradeOrder(Volume volume) {
		Shell window = new Shell(shell, SWT.SHELL_TRIM);
		window.setText("Order Grades");
		window.setLayout(new ColumnLayout());
		
		List list = new List(window, SWT.MULTI);
		for (Grade g : volume.grades) {
			list.add(g.name);
		}
		
		Composite moveComposite = new Composite(window, SWT.NONE);
		GridLayout moveLayout = new GridLayout();
		moveLayout.makeColumnsEqualWidth = true;
		moveLayout.numColumns = 2;
		moveComposite.setLayout(moveLayout);
		
		Button up = new Button(moveComposite, SWT.PUSH);
		up.setText("Move Up");
		up.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Button down = new Button(moveComposite, SWT.PUSH);
		down.setText("Move Down");
		down.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Button btn = new Button(window, SWT.PUSH);
		btn.setText("Set Grade Order");
		
		up.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				int selectedIndex = list.getSelectionIndex();
				if (selectedIndex == 0) return;
				
				String tmp = list.getItem(selectedIndex - 1);
				list.remove(selectedIndex - 1);
				list.add(tmp, selectedIndex);
				
			}
			
		});
		
		down.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				int selectedIndex = list.getSelectionIndex();
				if (selectedIndex == list.getItemCount() - 1) return;
				
				String tmp = list.getItem(selectedIndex);
				list.remove(selectedIndex);
				list.add(tmp, selectedIndex + 1);
				list.setSelection(selectedIndex + 1);
			}
			
		});
		
		btn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				ArrayList<String> items = new ArrayList<String>();
				while (list.getItemCount() > 0) {
					String item = list.getItem(0);
					list.remove(0);
					items.add(item);
				}
				generatePSPAPages(volume, items);
				window.close();
				window.dispose();
			}
			
		});
		
		window.pack();
		window.open();
	}
	
	private void generatePSPAPages(Volume volume, ArrayList<String> items) {
		
		int initialXOffset = (int) ((1.0 / 8.5) * yearbook.settings.width) / 2;
		int initialYOffset = (int) ((1.5 / 11.0) * yearbook.settings.height) / 2;
		
		for (String gradeName : items) {
			Grade grade = volume.getGradeByName(gradeName);
			
			//First, add a blank page for the grade.
			yearbook.addPage("Grade " + gradeName);
			
			for (HomeRoom h : grade.homeRooms) {
				int photosPerPage = volume.grid.x * volume.grid.y;
				int pageCount = (int) Math.ceil((double) h.people.size() / photosPerPage);

				for (int i = 0; i < pageCount; i++) {
					YearbookPage page = new YearbookPage(h.name);
					
					//YearbookImageElement element = new YearbookImageElement(display, fileName, yearbook.settings.width, yearbook.settings.height);
					//yearbook.page(yearbook.activePage).addElement(element);
					
					for (int j = 0; j < photosPerPage && j < h.people.size() - 1; j++) {
						
						int index = j + (i * photosPerPage);
						if (index > h.people.size() - 1) break;
						
						Person p = h.people.get(index);
						//System.out.println(volume.path);
						//System.out.println(p.folderName);
						//System.out.println(p.fileName);
						String path = volume.path + File.separator + p.folderName + File.separator + p.fileName;
						YearbookPSPAElement element = new YearbookPSPAElement(display, path, yearbook.settings.width, yearbook.settings.height, volume);
						int row = j / volume.grid.x;
						int col = j % volume.grid.x;
						int yOffset = initialYOffset + (row * Volume.photoSpacing(volume.grid, yearbook.settings.width, yearbook.settings.height).y) + ((row + 1) * element.getBounds(yearbook.settings.width, yearbook.settings.height).height);
						int xOffset = initialXOffset + (col * Volume.photoSpacing(volume.grid, yearbook.settings.width, yearbook.settings.height).x) + ((col + 1) * element.getBounds(yearbook.settings.width, yearbook.settings.height).width);
						element.setLocationRelative(xOffset, yOffset);
						//element.text.text = p.firstName + " " + p.lastName;
						element.person = p;
						page.addElement(element);
					}
					
					yearbook.addPage(page);
					//stack.push(new PageCommand(Commands.ADD_PAGE, page, -1, yearbook.size() - 1));
				}
			}

			int photosPerPage = volume.grid.x * volume.grid.y;
			int pageCount = (int) Math.ceil((double) grade.people.size() / photosPerPage);
			for (int i = 0; i < pageCount; i++) {
				YearbookPage page = new YearbookPage("No home room");
				
				//YearbookImageElement element = new YearbookImageElement(display, fileName, yearbook.settings.width, yearbook.settings.height);
				//yearbook.page(yearbook.activePage).addElement(element);
				
				for (int j = 0; j < photosPerPage && j < grade.people.size() - 1; j++) {
					
					int index = j + (i * photosPerPage);
					if (index > grade.people.size() - 1) break; 
					
					Person p = grade.people.get(index);
					String path = volume.path + File.separator + p.folderName + File.separator + p.fileName;
					YearbookPSPAElement element = new YearbookPSPAElement(display, path, yearbook.settings.width, yearbook.settings.height, volume);
					int row = j / volume.grid.x;
					int col = j % volume.grid.x;
					int yOffset = initialYOffset + (row * Volume.photoSpacing(volume.grid, yearbook.settings.width, yearbook.settings.height).y) + ((row + 1) * element.getBounds(yearbook.settings.width, yearbook.settings.height).height);
					int xOffset = initialXOffset + (col * Volume.photoSpacing(volume.grid, yearbook.settings.width, yearbook.settings.height).x) + ((col + 1) * element.getBounds(yearbook.settings.width, yearbook.settings.height).width);
					element.setLocationRelative(xOffset, yOffset);
					//element.text.text = p.firstName + " " + p.lastName;
					element.person = p;
					page.addElement(element);
				}
				
				yearbook.addPage(page);
				//stack.push(new PageCommand(Commands.ADD_PAGE, page, -1, yearbook.size() - 1));
				
			}
			
			
		}
		refresh();
	}

	protected void openPageNumberDialog(YearbookPageNumberElement element) {
		Shell textTool = new Shell(display, SWT.DIALOG_TRIM);
		textTool.setText("Page Number Tool");
		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		layout.makeColumnsEqualWidth = true;
		textTool.setLayout(layout);

		Canvas preview = new Canvas(textTool, SWT.BORDER | SWT.MULTI);
		GridData previewData = new GridData(SWT.FILL, SWT.FILL, true, true);
		previewData.horizontalSpan = 5;
		preview.setLayoutData(previewData);
		

		ColorDialog colorDialog = new ColorDialog(textTool, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		colorDialog.setText("Color Picker");
		Button colorButton = new Button(textTool, SWT.PUSH);
		colorButton.setText("Pick color...");
		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonData.horizontalSpan = 3;
		colorButton.setLayoutData(buttonData);

		Combo sizeCombo = new Combo(textTool, SWT.DROP_DOWN);
		GridData sizeData = new GridData(SWT.FILL, SWT.FILL, true, false);
		sizeData.horizontalSpan = 2;
		sizeCombo.setLayoutData(sizeData);
		String[] fontSizes = {
				"8",
				"9",
				"10",
				"11",
				"12",
				"14",
				"16",
				"18",
				"20",
				"22",
				"24",
				"26",
				"28",
				"36",
				"48",
				"72"
		};
		for (String size : fontSizes) {
			sizeCombo.add(size);
		}
		int index = Arrays.binarySearch(fontSizes, Integer.toString(element.size));
		if (index >= 0) sizeCombo.select(index);
		else sizeCombo.select(4);

		Composite styleWrapper = new Composite(textTool, SWT.NONE);
		styleWrapper.setLayout(new FillLayout());
		GridData styleData = new GridData(SWT.FILL, SWT.FILL, true, false);
		styleData.horizontalSpan = 2;
		styleWrapper.setLayoutData(styleData);

		Button bold = new Button(styleWrapper, SWT.PUSH);
		bold.setText("B");
		FontData fd = bold.getFont().getFontData()[0];
		fd.setStyle(SWT.BOLD);
		Font f = new Font(display, fd);
		bold.setFont(f);
		f.dispose();

		Button italic = new Button(styleWrapper, SWT.PUSH);
		italic.setText("I");
		fd = italic.getFont().getFontData()[0];
		fd.setStyle(SWT.ITALIC);
		f = new Font(display, fd);
		italic.setFont(f);
		f.dispose();

		Button shadow = new Button(styleWrapper, SWT.PUSH);
		shadow.setText("S");
		
		String[] directions = new String[] {
			"High and In",
			"High and Middle",
			"High and Out",
			"Low and In",
			"Low and Middle",
			"Low and Out"
		};
		Combo directionCombo = new Combo(textTool, SWT.DROP_DOWN);
		GridData directionData  = new GridData(SWT.FILL, SWT.FILL, true, false);
		directionData.horizontalSpan = 3;
		directionCombo.setLayoutData(directionData);
		for (String d : directions) {
			directionCombo.add(d);
		}
		
		Combo fontCombo = new Combo(textTool, SWT.DROP_DOWN);
		GridData fontData  = new GridData(SWT.FILL, SWT.FILL, true, false);
		fontData.horizontalSpan = 5;
		fontCombo.setLayoutData(fontData);
		for (String fontName : fontNames) {
			fontCombo.add(fontName);
		}
		for (index = 0; index < fontNames.length; index++) {
			if (fontNames[index].equalsIgnoreCase(element.fontFamily)) break;
		}
		if (index >= 0) fontCombo.select(index);
		
		preview.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setAdvanced(true);
				e.gc.setAntialias(SWT.ON);
				e.gc.setFont(element.getFont(e.display));
				
				if (element.shadow) {
					int offset = element.size >= 72 ? 4 : element.size >= 36 ? 2 : 1;
					e.gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
					e.gc.setAlpha(0x8f);
					e.gc.drawText("1234567890", preview.getBounds().x + offset, preview.getBounds().y + offset, true);
					e.gc.setAlpha(0xff);
				}
				
				e.gc.setForeground(element.getColor(e.display));
				e.gc.drawText("1234567890", preview.getBounds().x, preview.getBounds().y);
				
				if (element.underline) {
					//Determine the line width
					int width;
					width = element.size / 12;
					if (width <= 0) width = 1;

					if (element.bold) width *= 1.8;
					e.gc.setLineWidth(width);
					e.gc.drawLine(element.getBounds().x + 1, element.getBounds().y + element.getBounds().height - (int) (element.getBounds().height * .1), element.getBounds().x + element.getBounds().width - 1, element.getBounds().y + element.getBounds().height - (int) (element.getBounds().height * .1));

				}
			}
			
		});

		colorButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				RGB rgb = colorDialog.open();
				if (rgb != null) element.setRGB(rgb);
				refresh();
				preview.redraw();
			}

		});


		sizeCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.size = Integer.parseInt(sizeCombo.getItem(sizeCombo.getSelectionIndex()));
				refresh();
				preview.redraw();
			}

		});

		sizeCombo.addListener(SWT.KeyUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				int size = element.size;
				try {
					size = Integer.parseInt(sizeCombo.getText());
				} catch (NumberFormatException e) {
					return;
				}
				element.size = size;
				refresh();
				preview.redraw();
			}

		});

		fontCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.fontFamily = fontNames[fontCombo.getSelectionIndex()];
				refresh();
				preview.redraw();
			}

		});
		
		directionCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				switch (directionCombo.getSelectionIndex()) {
				case 0:
					element.location = PageNumberLocations.UP_IN;
					break;
				case 1:
					element.location = PageNumberLocations.UP_MIDDLE;
					break;
				case 2:
					element.location = PageNumberLocations.UP_OUT;
					break;
				case 3:
					element.location = PageNumberLocations.DOWN_IN;
					break;
				case 4:
					element.location = PageNumberLocations.DOWN_MIDDLE;
					break;
				case 5:
					element.location = PageNumberLocations.DOWN_OUT;
					break;
				}
				refreshNoPageList();
			}
			
		});
		
		bold.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.toggleBold();
				refresh();
				preview.redraw();
			}

		});

		italic.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.toggleItalic();
				refresh();
				preview.redraw();
			}

		});

		shadow.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.toggleShadow();
				refreshNoPageList();
				preview.redraw();

			}

		});

		textTool.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event event) {
				modeReset();
			}

		});

		textTool.setSize(250, 200);
		textTool.open();

	}
	
	protected void openPSPATextDialog(YearbookTextElement element) {
		Shell textTool = new Shell(display, SWT.DIALOG_TRIM);
		textTool.setText("PSPA Text Tool");
		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		layout.makeColumnsEqualWidth = true;
		textTool.setLayout(layout);

		Canvas preview = new Canvas(textTool, SWT.BORDER | SWT.MULTI);
		GridData previewData = new GridData(SWT.FILL, SWT.FILL, true, true);
		previewData.horizontalSpan = 5;
		preview.setLayoutData(previewData);
		

		ColorDialog colorDialog = new ColorDialog(textTool, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		colorDialog.setText("Color Picker");
		Button colorButton = new Button(textTool, SWT.PUSH);
		colorButton.setText("Pick color...");
		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonData.horizontalSpan = 3;
		colorButton.setLayoutData(buttonData);

		Combo sizeCombo = new Combo(textTool, SWT.DROP_DOWN);
		GridData sizeData = new GridData(SWT.FILL, SWT.FILL, true, false);
		sizeData.horizontalSpan = 2;
		sizeCombo.setLayoutData(sizeData);
		String[] fontSizes = {
				"2",
				"3",
				"4",
				"5",
				"6",
				"7",
				"8",
				"9",
				"10",
				"11",
				"12",
				"14",
				"16",
				"18",
				"20",
				"22",
				"24",
				"26",
				"28",
				"36",
				"48",
				"72"
		};
		for (String size : fontSizes) {
			sizeCombo.add(size);
		}
		int index = Arrays.binarySearch(fontSizes, Integer.toString(element.size));
		if (index >= 0) sizeCombo.select(index);
		else sizeCombo.select(4);

		Composite styleWrapper = new Composite(textTool, SWT.NONE);
		styleWrapper.setLayout(new FillLayout());
		GridData styleData = new GridData(SWT.FILL, SWT.FILL, true, false);
		styleData.horizontalSpan = 2;
		styleWrapper.setLayoutData(styleData);

		Button bold = new Button(styleWrapper, SWT.PUSH);
		bold.setText("B");
		FontData fd = bold.getFont().getFontData()[0];
		fd.setStyle(SWT.BOLD);
		Font f = new Font(display, fd);
		bold.setFont(f);
		f.dispose();

		Button italic = new Button(styleWrapper, SWT.PUSH);
		italic.setText("I");
		fd = italic.getFont().getFontData()[0];
		fd.setStyle(SWT.ITALIC);
		f = new Font(display, fd);
		italic.setFont(f);
		f.dispose();

		Button shadow = new Button(styleWrapper, SWT.PUSH);
		shadow.setText("S");
		
		Button borderBtn = new Button(textTool, SWT.PUSH);
		borderBtn.setText("Set Border");
		styleData = new GridData(SWT.FILL, SWT.FILL, true, false);
		styleData.horizontalSpan = 3;
		borderBtn.setLayoutData(styleData);
		
		
		String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		Combo fontCombo = new Combo(textTool, SWT.DROP_DOWN);
		GridData fontData  = new GridData(SWT.FILL, SWT.FILL, true, false);
		fontData.horizontalSpan = 5;
		fontCombo.setLayoutData(fontData);
		for (String fontName : fontNames) {
			fontCombo.add(fontName);
		}
		for (index = 0; index < fontNames.length; index++) {
			if (fontNames[index].equalsIgnoreCase(element.fontFamily)) break;
		}
		if (index >= 0) fontCombo.select(index);
		
		preview.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setAdvanced(true);
				e.gc.setAntialias(SWT.ON);
				e.gc.setFont(element.getFont(e.display));
				
				if (element.shadow) {
					int offset = element.size >= 72 ? 4 : element.size >= 36 ? 2 : 1;
					e.gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
					e.gc.setAlpha(0x8f);
					e.gc.drawText("John Smith", preview.getBounds().x + offset, preview.getBounds().y + offset, true);
					e.gc.setAlpha(0xff);
				}
				
				e.gc.setForeground(element.getColor(e.display));
				e.gc.drawText("John Smith", preview.getBounds().x, preview.getBounds().y);
				
				if (element.underline) {
					//Determine the line width
					int width;
					width = element.size / 12;
					if (width <= 0) width = 1;

					if (element.bold) width *= 1.8;
					e.gc.setLineWidth(width);
					e.gc.drawLine(element.getBounds().x + 1, element.getBounds().y + element.getBounds().height - (int) (element.getBounds().height * .1), element.getBounds().x + element.getBounds().width - 1, element.getBounds().y + element.getBounds().height - (int) (element.getBounds().height * .1));

				}
				
				if (!element.border.noBorder) {
					Path path = new Path(display);
					path.addString("John Smith", preview.getBounds().x, preview.getBounds().y, e.gc.getFont());
					Color c = new Color(display, element.border.rgb);
					e.gc.setForeground(c);
					e.gc.setLineWidth(element.border.getWidthInPixels(yearbook.settings.width));
					e.gc.setLineStyle(SWT.LINE_SOLID);
					e.gc.drawPath(path);
					
					path.dispose();
					e.gc.setLineWidth(1);
					e.gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
					c.dispose();
				}
			}
			
		});

		colorButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				RGB rgb = colorDialog.open();
				if (rgb != null) element.setRGB(rgb);
				refresh();
				preview.redraw();
			}

		});


		sizeCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.size = Integer.parseInt(sizeCombo.getItem(sizeCombo.getSelectionIndex()));
				refresh();
				preview.redraw();
			}

		});

		sizeCombo.addListener(SWT.KeyUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				int size = element.size;
				try {
					size = Integer.parseInt(sizeCombo.getText());
				} catch (NumberFormatException e) {
					return;
				}
				element.size = size;
				refresh();
				preview.redraw();
			}

		});

		fontCombo.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.fontFamily = fontNames[fontCombo.getSelectionIndex()];
				refresh();
				preview.redraw();
			}

		});
		
		bold.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.toggleBold();
				refresh();
				preview.redraw();
			}

		});

		italic.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.toggleItalic();
				refresh();
				preview.redraw();
			}

		});

		shadow.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				element.toggleShadow();
				refreshNoPageList();
				preview.redraw();

			}

		});
		
		borderBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				openAddBorderDialog(element);
				
			}
			
		});

		textTool.setSize(250, 200);
		textTool.open();
	}

	protected String imagePicker() {
		FileDialog picker = new FileDialog(shell, SWT.OPEN);
		String[] allowedExtensions = {"*.jpg; *.jpeg; *.gif; *.tif; *.tiff; *.bpm; *.ico; *.png", "*.*"};
		picker.setFilterExtensions(allowedExtensions);
		return picker.open();
	}

	protected void attachVideoToImage(YearbookImageElement element) throws IOException {
		if (!element.isPSPA()) {
			YearbookClickableImageElement e = new YearbookClickableImageElement(display, element.getImage(display).getImageData(), element.getPageWidth(), element.getPageHeight());
	
			e.x = element.x;
			e.y = element.y;
			e.scale = element.scale;
			e.rotation = element.rotation;
			e.imageData = element.imageData;
			e.border = element.border;
	
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			String[] allowedExtensions = {"*.webm;*.mkv;*.flv;*.vob;*.ogv;*.ogg;*.drc;*.avi;*.mov;*.qt;*.wmv;*.rm;*.mp4;*.m4p;*.m4v;*.mpg;*.3gp;*.3g2", "*.*"};
			dialog.setFilterExtensions(allowedExtensions);
			String fileName = dialog.open();
			if (fileName == null) return;
	
			Video video = new Video(fileName);
			e.video = video;
			int position = yearbook.page(yearbook.activePage).findElementIndex(element);
			yearbook.page(yearbook.activePage).removeElement(element);
			yearbook.page(yearbook.activePage).getElements().add(position, e);
			refresh();
		} else if (element.isTruePSPA()) {
			YearbookClickablePSPAElement e = new YearbookClickablePSPAElement(display, element.getImage(display).getImageData(), element.getPageWidth(), element.getPageHeight(), (YearbookPSPAElement) element);
			
			e.x = element.x;
			e.y = element.y;
			e.scale = element.scale;
			e.rotation = element.rotation;
			e.imageData = element.imageData;
			e.border = element.border;
	
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			String[] allowedExtensions = {"*.webm;*.mkv;*.flv;*.vob;*.ogv;*.ogg;*.drc;*.avi;*.mov;*.qt;*.wmv;*.rm;*.mp4;*.m4p;*.m4v;*.mpg;*.3gp;*.3g2", "*.*"};
			dialog.setFilterExtensions(allowedExtensions);
			String fileName = dialog.open();
			if (fileName == null) return;
	
			Video video = new Video(fileName);
			e.video = video;
			int position = yearbook.page(yearbook.activePage).findElementIndex(element);
			yearbook.page(yearbook.activePage).removeElement(element);
			yearbook.page(yearbook.activePage).getElements().add(position, e);
		}
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

		previewBtn = new Button(toolbarWrapper, SWT.PUSH);
		previewBtn.setImage(YearbookIcons.exportPDF(display));
		previewBtn.pack();
		/*
		printBtn = new Button(toolbarWrapper, SWT.PUSH);
		printBtn.setImage(YearbookIcons.print(display));
		printBtn.pack();
*/
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

		Label sep5 = new Label(toolbarWrapper, SWT.NONE);
		sep5.setText("   ");

		moveBtn = new Button(toolbarWrapper, SWT.TOGGLE);
		moveBtn.setImage(YearbookIcons.move(display));
		moveBtn.setSelection(true);
		moveBtn.pack();

		resizeBtn = new Button(toolbarWrapper, SWT.TOGGLE);
		resizeBtn.setImage(YearbookIcons.resize(display));
		resizeBtn.pack();

		selectBtn = new Button(toolbarWrapper, SWT.TOGGLE);
		selectBtn.setImage(YearbookIcons.select(display));
		selectBtn.pack();

		eraseBtn = new Button(toolbarWrapper, SWT.TOGGLE);
		eraseBtn.setImage(YearbookIcons.erase(display));
		eraseBtn.pack();

		rotateBtn = new Button(toolbarWrapper, SWT.TOGGLE);
		rotateBtn.setImage(YearbookIcons.rotate(display));
		rotateBtn.pack();






		newBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				fileNewPageItem.getListeners(SWT.Selection)[0].handleEvent(event);
			}

		});

		openBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				fileOpenItem.getListeners(SWT.Selection)[0].handleEvent(event);
			}

		});

		saveBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				fileSaveItem.getListeners(SWT.Selection)[0].handleEvent(event);
			}

		});
		
		previewBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				fileExportItem.getListeners(SWT.Selection)[0].handleEvent(event);
			}
			
		});
		
		undoBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				editUndoItem.getListeners(SWT.Selection)[0].handleEvent(event);
				
			}
			
		});
		
		redoBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				editRedoItem.getListeners(SWT.Selection)[0].handleEvent(event);
				
			}
			
		});
		
		cutBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				editCutItem.getListeners(SWT.Selection)[0].handleEvent(event);
				
			}
			
		});
		
		copyBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				editCopyItem.getListeners(SWT.Selection)[0].handleEvent(event);
				
			}
			
		});
		
		pasteBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				editPasteItem.getListeners(SWT.Selection)[0].handleEvent(event);
				
			}
			
		});

		textBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				insertTextItem.getListeners(SWT.Selection)[0].handleEvent(event);

			}

		});

		imageBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				insertImageItem.getListeners(SWT.Selection)[0].handleEvent(event);
			}

		});

		videoBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				insertVideoItem.getListeners(SWT.Selection)[0].handleEvent(event);
			}

		});

		moveBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Control[] children = toolbarWrapper.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control child = children[i];
					if (e.widget != child && child instanceof Button
							&& (child.getStyle() & SWT.TOGGLE) != 0) {
						((Button) child).setSelection(false);
					}
				}
				((Button) e.widget).setSelection(true);
				settings.cursorMode = CursorMode.MOVE;
				modeReset();
			}
		});

		selectBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Control[] children = toolbarWrapper.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control child = children[i];
					if (e.widget != child && child instanceof Button
							&& (child.getStyle() & SWT.TOGGLE) != 0) {
						((Button) child).setSelection(false);
					}
				}
				((Button) e.widget).setSelection(true);
				settings.cursorMode = CursorMode.SELECT;
				modeReset();
				shell.setCursor(display.getSystemCursor(SWT.CURSOR_CROSS));
			}
		});

		resizeBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Control[] children = toolbarWrapper.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control child = children[i];
					if (e.widget != child && child instanceof Button
							&& (child.getStyle() & SWT.TOGGLE) != 0) {
						((Button) child).setSelection(false);
					}
				}
				((Button) e.widget).setSelection(true);
				settings.cursorMode = CursorMode.RESIZE;
				modeReset();
				shell.setCursor(display.getSystemCursor(SWT.CURSOR_SIZESE));
			}
		});

		/**
		 * If there's an element already selected, erase it.
		 * Otherwise, go into erase mode.
		 */
		eraseBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (clipboard.elements.size() == 0) {
					Control[] children = toolbarWrapper.getChildren();
					for (int i = 0; i < children.length; i++) {
						Control child = children[i];
						if (e.widget != child && child instanceof Button
								&& (child.getStyle() & SWT.TOGGLE) != 0) {
							((Button) child).setSelection(false);
						}
					}
					((Button) e.widget).setSelection(true);
					settings.cursorMode = CursorMode.ERASE;
				} else {
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
					box.setText("Delete Element");
					box.setMessage("Are you sure you want to erase these element(s)?");
					int result = box.open();
					if (result == SWT.YES) {
						for (YearbookElement element : clipboard.elements) {
							YearbookElement orig = element.copy();
							yearbook.page(yearbook.activePage).removeElement(element);
							stack.push(new ElementCommand(Commands.REMOVE_ELEMENT, orig, null, yearbook.page(yearbook.page(yearbook.activePage).id).id));
						}
						clipboard.elements.clear();
					}
					((Button) e.widget).setSelection(false);
				}
				modeReset();
				shell.setCursor(display.getSystemCursor(SWT.CURSOR_UPARROW));
				refreshNoPageList();
			}
		});

		rotateBtn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Control[] children = toolbarWrapper.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control child = children[i];
					if (e.widget != child && child instanceof Button
							&& (child.getStyle() & SWT.TOGGLE) != 0) {
						((Button) child).setSelection(false);
					}
				}
				((Button) e.widget).setSelection(true);
				settings.cursorMode = CursorMode.ROTATE;
				modeReset();
				shell.setCursor(display.getSystemCursor(SWT.CURSOR_UPARROW));
			}
		});


	}

	/**
	 * Resets all of the global selection variables.
	 */
	protected void modeReset() {
		this.isInsertingText = false;
		this.selectionRectangle = null;
		//selectElement(null);

		shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));

	}

	private void createNewYearbook() {

		yearbook.settings.height = canvasHeight;
		yearbook.settings.width = (int) ((8.5 / 11.0) * canvasHeight);
		canvas.setSize(yearbook.settings.width, yearbook.settings.height);
		rightCanvas.setSize(yearbook.settings.width, yearbook.settings.height);

		pageUseCoverItem.setSelection(yearbook.hasCover);

	}

	private void createNewYearbook(String name) {
		yearbook = new Yearbook(name);
		createNewYearbook();
	}

	private void updatePageList() {
		pagesList.removeAll();
		for (int i = 0; i < yearbook.size(); i++) {
			pagesList.add("Page " + (i + 1) + ": " + yearbook.page(i).name);
		}
	}

	private void updateCanvas() {
		
		//Back cover
		if (yearbook.activePage + 1 == yearbook.size() && leftIsActive()) {
			blankRightCanvas();
			loadLeftCanvas(yearbook.activePage);
			return;
		}
	
		//Front cover
		if (yearbook.activePage == 0) {
			blankLeftCanvas();
			loadRightCanvas(0);
			return;
		} 
		
		//Active page is odd
		if (leftIsActive()) {
			loadLeftCanvas(yearbook.activePage);
			loadRightCanvas(yearbook.activePage + 1);
			return;
		}
		
		//Active page is even
		if (rightIsActive()) {
			loadLeftCanvas(yearbook.activePage - 1);
			loadRightCanvas(yearbook.activePage);
			return;
		}
		
		yearbook.tidyUp();
		
		
	}
	
	private void loadLeftCanvas(int index) {
		GC gc;
		gc = new GC(canvas);
		paintPage(gc, display, yearbook, clipboard.elements, selectionRectangle, settings, index, yearbook.settings.width, yearbook.settings.height, false, false);
		gc.dispose();
	}
	
	private void loadRightCanvas(int index) {
		GC gc;
		gc = new GC(rightCanvas);
		paintPage(gc, display, yearbook, clipboard.elements, selectionRectangle, settings, index, yearbook.settings.width, yearbook.settings.height, false, false);
		gc.dispose();
	}
	
	private boolean leftIsActive() {
		return Math.abs(yearbook.activePage % 2) == 1;
	}
	
	private boolean rightIsActive() {
		return yearbook.activePage % 2 == 0; 
	}
	
	private void makeLeftActive() {
		if (leftIsActive()) return;
		if (yearbook.activePage == 0) return;
		yearbook.activePage--;
	}
	
	private void makeRightActive() {
		if (rightIsActive()) return;
		if (yearbook.activePage - 1 == yearbook.size()) return;
		yearbook.activePage++;
	}
	
	private void blankLeftCanvas() {
		GC gc;
		gc = new GC(canvas);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(0, 0, canvas.getBounds().width, canvas.getBounds().height);
		if (yearbook.hasCover) {
			gc.setAlpha(0x55);
			Image image = yearbook.cover(display);
			gc.drawImage(image, image.getBounds().width / 2, 0, (int) Math.floor(image.getBounds().width / 2), image.getBounds().height, 0, 0, canvas.getBounds().width, canvas.getBounds().height);
			image.dispose();
			gc.setAlpha(0xff);
		}
		gc.drawText("Front Cover", (yearbook.settings.width / 2) - (gc.textExtent("Front Cover").x / 2), yearbook.settings.height / 2, true);
		gc.dispose();		
	}
	
	private void blankRightCanvas() {
		GC gc;
		gc = new GC(rightCanvas);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(0, 0, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		if (yearbook.hasCover) {
			gc.setAlpha(0x55);
			Image image = yearbook.cover(display);
			gc.drawImage(image, 0, 0, (int) Math.floor(image.getBounds().width / 2), image.getBounds().height, 0, 0, canvas.getBounds().width, canvas.getBounds().height);
			image.dispose();
			gc.setAlpha(0xff);
		}
		gc.drawText("Back Cover", (yearbook.settings.width / 2) - (gc.textExtent("Back Cover").x / 2), yearbook.settings.height / 2, true);
		gc.dispose();		
	}

	/**
	 * This function handles the painting of the canvas for the currently
	 * selected yearbook page.
	 * @param activePage The page to draw on the canvas.
	 * @deprecated Use updateCanvas() instead.
	 */
	@Deprecated
	private void loadActivePage(int activePage) {
		GC gc;
		gc = new GC(canvas);
		paintPage(gc, display, yearbook, clipboard.elements, selectionRectangle, settings, activePage, yearbook.settings.width, yearbook.settings.height, false, false);
		gc.dispose();
	}

	public static void paintPage(GC gc, Display display, Yearbook yearbook, 
			ArrayList<YearbookElement> selectedElements, 
			Rectangle selectionRectangle, UserSettings settings,
			int activePage, int pageWidth, int pageHeight, boolean isReader, boolean isExport) {

		Color uglyYellowColor = display.getSystemColor(SWT.COLOR_GRAY);

		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);

		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		if (yearbook.page(activePage).noBackground) gc.fillRectangle(0, 0, yearbook.settings.width, yearbook.settings.height);

		if (yearbook.page(activePage).backgroundImage(display) != null && !yearbook.page(activePage).noBackground) {
			gc.drawImage(yearbook.page(activePage).backgroundImage(display), 0, 0, yearbook.page(activePage).backgroundImage(display).getBounds().width, yearbook.page(activePage).backgroundImage(display).getBounds().height, 0, 0, pageWidth, pageHeight);
		}
		
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
			Transform tr = new Transform(display);
			tr.translate(element.getBounds(pageWidth, pageHeight).x + element.getBounds(pageWidth, pageHeight).width / 2, element.getBounds(pageWidth, pageHeight).y + element.getBounds(pageWidth, pageHeight).height / 2);
			tr.rotate(element.rotation);
			tr.translate(-element.getBounds(pageWidth, pageHeight).x - element.getBounds(pageWidth, pageHeight).width / 2, element.getBounds(pageWidth, -pageHeight).y - element.getBounds(pageWidth, pageHeight).height / 2);
			
			gc.setTransform(tr);
			
			if (element.shadow) {
				gc.setAlpha(0xbf);
				gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
				
				int offset = (int) Math.ceil((1.0 / 250.0) * element.getBounds(pageWidth, pageHeight).width);
				
				//gc.fillRectangle(element.getBounds(pageWidth, pageHeight).x + offset, element.getBounds(pageWidth, pageHeight).y + offset, element.getBounds(pageWidth, pageHeight).width, element.getBounds(pageWidth, pageHeight).height);
				gc.fillRoundRectangle(element.getBounds(pageWidth, pageHeight).x + offset, element.getBounds(pageWidth, pageHeight).y + offset, element.getBounds(pageWidth, pageHeight).width, element.getBounds(pageWidth, pageHeight).height, 2 * offset, 2 * offset);
				gc.setAlpha(0xff);
			}
			
			gc.drawImage(element.getImage(display), 0, 0, element.getImage(display).getBounds().width, element.getImage(display).getBounds().height, element.getBounds(pageWidth, pageHeight).x, element.getBounds(pageWidth, pageHeight).y, element.getBounds(pageWidth, pageHeight).width, element.getBounds(pageWidth, pageHeight).height);
			
			if (!element.border.noBorder) {
				gc.setLineWidth(element.border.getWidthInPixels(pageWidth));
				Color c = new Color(display, element.border.rgb);
				gc.setForeground(c);
				gc.setLineStyle(SWT.LINE_SOLID);
				
				gc.drawRectangle(element.getBounds(pageWidth, pageHeight));
				
				gc.setLineWidth(1);
				gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
				c.dispose();
			}
			
			if (selectedElements.contains(element)) {
				YearbookElement selectedElement = selectedElements.get(selectedElements.indexOf(element));
				if (element == selectedElement) {
					//Element is selected by user.
					//Draw a border like GIMP.
					tr.dispose();
					Transform tra = new Transform(display);
					tra.rotate(0);
					gc.setTransform(tra);
					gc.setForeground(uglyYellowColor);
					gc.setLineStyle(SWT.LINE_DASH);
					gc.setLineWidth(3);
					gc.drawRectangle(element.getBounds(pageWidth, pageHeight).x, element.getBounds(pageWidth, pageHeight).y, element.getBounds(pageWidth, pageHeight).width, element.getBounds(pageWidth, pageHeight).height);
				}
			}
			
			if (element.isPSPA()) {
				YearbookPSPAElement e = (YearbookPSPAElement) element;
				double multiplicand = (double) pageWidth / e.pageWidth;
				
				Font f = e.text.getFont(display);
				/*if (isExport) {
					FontData fd = f.getFontData()[0];
					fd.setHeight((int) (fd.getHeight() * (300.0 / 72.0)));
					f.dispose();
					f = new Font(display, fd);
				}*/
				FontData fd = f.getFontData()[0];
				fd.setHeight((int) (fd.getHeight() * multiplicand));
				f.dispose();
				f = new Font(display, fd);
				
				Color c = e.text.getColor(display);
				gc.setFont(f);
				String name = e.person.firstName + " " + e.person.lastName;
				
				
				Point nameExtent = gc.textExtent(name);
				
				int nameX = e.getBounds(pageWidth, pageHeight).x + ((e.getBounds(pageWidth, pageHeight).width - nameExtent.x) / 2);
				int nameY = (e.getBounds(pageWidth, pageHeight).y + e.getBounds(pageWidth, pageHeight).height) + Math.abs((e.margins.y - nameExtent.y) / 2);
				e.text.setBounds(new Rectangle(nameX, nameY, nameExtent.x, nameExtent.y));
				//e.text.setBounds(new Rectangle(0,0,30,30));
				
				if (e.text.shadow) {
					int offset = e.text.size >= 72 ? 3 : e.text.size >= 36 ? 2 : 1;
					gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
					gc.setAlpha(0x8f);
					gc.drawText(name, nameX + offset, nameY + offset, true);
					gc.setAlpha(0xff);
				}
				
				gc.setForeground(c);
				
				gc.drawText(name, nameX, nameY, true);

				if (e.text.underline) {
					//Determine the line width
					int width;
					width = e.text.size / 12;
					if (width <= 0) width = 1;

					if (e.text.bold) width *= 1.8;
					gc.setLineWidth(width);
					gc.drawLine(e.text.getBounds(pageWidth, pageHeight).x + 1, e.text.getBounds(pageWidth, pageHeight).y + e.text.getBounds(pageWidth, pageHeight).height - (int) (e.text.getBounds(pageWidth, pageHeight).height * .1), e.text.getBounds(pageWidth, pageHeight).x + e.text.getBounds(pageWidth, pageHeight).width - 1, e.text.getBounds(pageWidth, pageHeight).y + e.text.getBounds(pageWidth, pageHeight).height - (int) (e.text.getBounds(pageWidth, pageHeight).height * .1));

				}
				
				c.dispose();
				
				if (!e.text.border.noBorder) {
					Path path = new Path(display);
					path.addString(name, e.text.getBounds(pageWidth, pageHeight).x, e.text.getBounds(pageWidth, pageHeight).y, gc.getFont());
					c = new Color(display, e.text.border.rgb);
					gc.setForeground(c);
					gc.setLineWidth(e.text.border.getWidthInPixels(pageWidth));
					gc.setLineStyle(SWT.LINE_SOLID);
					gc.drawPath(path);
					
					path.dispose();
					gc.setLineWidth(1);
					gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
					c.dispose();
				}
				
				f.dispose();
			}
			
			tr.dispose();
		}

		//If the user has selected an area, we should do something about that.
		if (selectionRectangle != null && settings.cursorMode == CursorMode.SELECT && yearbook.activePage == activePage) {
			gc.setLineStyle(SWT.LINE_DASHDOTDOT);
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.setLineWidth(2);
			gc.drawRectangle(selectionRectangle);
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
			gc.setAlpha(20);
			gc.fillRectangle(selectionRectangle);
		}

		//We should also show the areas that are clickable.
		//Map them like we did before...
		ArrayList<YearbookElement> clickables = new ArrayList<YearbookElement>();
		for (YearbookElement e : yearbook.page(activePage).getElements()) {
			if (e.isClickable()) clickables.add(e);
		}
		//...and display those in some manner.
		if (!isReader) for (YearbookElement e : clickables) {
			Transform tr = new Transform(display);
			tr.translate(e.getBounds(pageWidth, pageHeight).x + e.getBounds(pageWidth, pageHeight).width / 2, e.getBounds(pageWidth, pageHeight).y + e.getBounds(pageWidth, pageHeight).height / 2);
			tr.rotate(e.rotation);
			tr.translate(-e.getBounds(pageWidth, pageHeight).x - e.getBounds(pageWidth, pageHeight).width / 2, e.getBounds(pageWidth, -pageHeight).y - e.getBounds(pageWidth, pageHeight).height / 2);
			gc.setTransform(tr);
			gc.setLineWidth(1);
			gc.setLineStyle(SWT.LINE_DASH);
			gc.setForeground(uglyYellowColor);
			gc.drawRectangle(e.getBounds(pageWidth, pageHeight));

			/*
			gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			gc.setAlpha(50);
			gc.fillRectangle(e.getBounds(pageWidth, pageHeight));
			gc.setAlpha(0xff);
			*/
			tr.dispose();
		}



		//If they want a grid, give them a grid.
		if (settings.showGrid) {
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.setLineWidth(1);
			gc.setLineStyle(SWT.LINE_SOLID);

			FontData fd = gc.getFont().getFontData()[0];
			fd.height = 8;
			gc.setFont(new Font(display, fd));

			//Let's do a solid line every inch...
			int x;
			int xDiff = (int) ((1.0 / 8.5) * yearbook.settings.width);
			for (int i = 1; i <= 8; i++) {
				x = i * xDiff;
				gc.drawLine(x, 0, x, yearbook.settings.height);
				gc.drawText(Integer.toString(i), x + 2, 0, true);
			}

			int y;
			int yDiff = (int) ((1.0 / 11.0) * yearbook.settings.height);
			for (int i = 1; i < 11; i++) {
				y = i * yDiff;
				gc.drawLine(0, y, yearbook.settings.width, y);
				gc.drawText(Integer.toString(i), 0, y + 2, true);
			}

			//...and a dotted line every quarter inch.
			gc.setLineStyle(SWT.LINE_DOT);

			xDiff = (int) ((.25 / 8.5) * yearbook.settings.width);
			for (int i = 1; i <= 35; i++) {
				x = i * xDiff;
				gc.drawLine(x, 0, x, yearbook.settings.height);
			}

			yDiff = (int) ((.25 / 11.0) * yearbook.settings.height);
			for (int i = 1; i < 45; i++) {
				y = i * yDiff;
				gc.drawLine(0, y, yearbook.settings.width, y);
			}

		}

		//Next, draw the text elements.
		ArrayList<YearbookTextElement> texts = new ArrayList<YearbookTextElement>();
		for (YearbookElement e : yearbook.page(activePage).getElements()) {
			if (e.isText()) texts.add((YearbookTextElement) e);
		}
		

		//...and display those in some manner.
		for (YearbookTextElement e : texts) {
			double multiplicand = (double) pageWidth / e.pageWidth;
			Transform tr = new Transform(display);
			tr.translate(e.getBounds(pageWidth, pageHeight).x + e.getBounds(pageWidth, pageHeight).width / 2, e.getBounds(pageWidth, pageHeight).y + e.getBounds(pageWidth, pageHeight).height / 2);
			tr.rotate(e.rotation);
			tr.translate(-e.getBounds(pageWidth, pageHeight).x - e.getBounds(pageWidth, pageHeight).width / 2, e.getBounds(pageWidth, -pageHeight).y - e.getBounds(pageWidth, pageHeight).height / 2);
			gc.setTransform(tr);
			gc.setAdvanced(true);
			gc.setTextAntialias(SWT.ON);
			Font f = e.getFont(display, pageWidth, pageHeight);
			FontData fd = f.getFontData()[0];
			f.dispose();
			fd.setHeight((int) (fd.getHeight() * multiplicand));
			f = new Font(display, fd);
			gc.setFont(f);
			
			int x = (int) (e.x * pageWidth);
			int y = (int) (e.y * pageHeight);
			
			Point textExtent = gc.textExtent(e.text);

			if (e.shadow) {
				int offset = e.size >= 72 ? 3 : e.size >= 36 ? 2 : 1;
				gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
				gc.setAlpha(0x8f);
				gc.drawText(e.text, x + offset, y + offset, true);
				gc.setAlpha(0xff);
			}

			gc.setForeground(e.getColor(display));
			
			//System.out.println(x + " " + e.getBounds().x);
			
			/*
			 * Inform the text element of its bounds.
			 * This must be done here, regrettably.
			 */
			e.setBounds(new Rectangle(e.getBounds(pageWidth, pageHeight).x, e.getBounds(pageWidth, pageHeight).y, gc.stringExtent(e.text).x, gc.stringExtent(e.text).y));
			
			gc.drawText(e.text, x, y, true);

			/*
			 * Handle underlining (SWT has no native GC underlining)
			 * All magic numbers were chosen for their looks.
			 */
			if (e.underline) {
				//Determine the line width
				int width;
				width = e.size / 12;
				if (width <= 0) width = 1;

				if (e.bold) width *= 1.8;
				gc.setLineWidth(width);
				gc.drawLine(x + 1, y + textExtent.y - (int) (textExtent.y * .1), x + textExtent.x - 1, y + textExtent.y - (int) (textExtent.y * .1));

			}

			if (selectedElements.contains(e)) {
				YearbookElement selectedElement = selectedElements.get(selectedElements.indexOf(e));
				if (e == selectedElement && selectedElement != null) {
					//Element is selected by user.
					//Draw a border like GIMP.
					gc.setForeground(uglyYellowColor);
					gc.setLineStyle(SWT.LINE_DASH);
					gc.setLineWidth(3);
					gc.drawRectangle(e.getBounds(pageWidth, pageHeight).x, e.getBounds(pageWidth, pageHeight).y, e.getBounds(pageWidth, pageHeight).width, e.getBounds(pageWidth, pageHeight).height);
				}
			}
			
			/*
			 * Text outlines
			 */
			if (!e.border.noBorder) {
				Path path = new Path(display);
				path.addString(e.text, e.getBounds(pageWidth, pageHeight).x, e.getBounds(pageWidth, pageHeight).y, gc.getFont());
				Color c = new Color(display, e.border.rgb);
				gc.setForeground(c);
				gc.setLineWidth(e.border.getWidthInPixels(pageWidth));
				gc.setLineStyle(SWT.LINE_SOLID);
				gc.drawPath(path);
				
				path.dispose();
				gc.setLineWidth(1);
				gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
				c.dispose();
			}
			
			tr.dispose();

		}

		boolean displayNumbers = !(activePage == 0 || activePage - 1 == yearbook.size()) && yearbook.settings.showPageNumbers;
		
		//Paint the page numbers
		if (displayNumbers) {
			YearbookTextElement element = yearbook.numbers;
			String text = Integer.toString(activePage);
			gc.setAdvanced(true);
			gc.setAntialias(SWT.ON);
			gc.setFont(element.getFont(display));
			Rectangle bounds = YearbookPageNumberElement.generateBounds(pageWidth, pageHeight, yearbook.numbers.location, activePage, gc.textExtent(text));
			
			if (element.shadow) {
				int offset = element.size >= 72 ? 4 : element.size >= 36 ? 2 : 1;
				gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
				gc.setAlpha(0x8f);
				gc.drawText(text, bounds.x + offset, bounds.y + offset, true);
				gc.setAlpha(0xff);
			}
			
			gc.setForeground(element.getColor(display));
			gc.drawText(text, bounds.x, bounds.y, true);
		}
		
		//Draw the layouts
		if (!isReader) for (YearbookLayout layout : yearbook.page(yearbook.activePage).layouts) {
			gc.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
			gc.setAlpha(0x55);
			
			for (YearbookElementPrototype yep : layout.elements) {
				Transform tr = new Transform(display);
				tr.translate(yep.getBounds(pageWidth, pageHeight).x + yep.getBounds(pageWidth, pageHeight).width / 2, yep.getBounds(pageWidth, pageHeight).y + yep.getBounds(pageWidth, pageHeight).height / 2);
				tr.rotate(yep.rotation);
				tr.translate(-yep.getBounds(pageWidth, pageHeight).x - yep.getBounds(pageWidth, pageHeight).width / 2, yep.getBounds(pageWidth, -pageHeight).y - yep.getBounds(pageWidth, pageHeight).height / 2);
				gc.setTransform(tr);
				
				gc.fillOval(yep.getBounds(pageWidth, pageHeight).x, yep.getBounds(pageWidth, pageHeight).y, yep.getBounds(pageWidth, pageHeight).width, yep.getBounds(pageWidth, pageHeight).height);
				int textX, textY;
				String text = Double.toString(Math.floor(yep.rotation)) + "°";
				textX = ((yep.getBounds(pageWidth, pageHeight).width - gc.textExtent(text).x) / 2) + yep.getBounds(pageWidth, pageHeight).x;
				textY = ((yep.getBounds(pageWidth, pageHeight).height - gc.textExtent(text).y) / 2) + yep.getBounds(pageWidth, pageHeight).y;
				gc.drawText(text, textX, textY, true);
				tr.dispose();
			}
			
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.setAlpha(0xff);
		}
		
	}

	private void createNewPage(String name) {
		yearbook.addPage(name);
		//stack.push(new PageCommand(Commands.ADD_PAGE, yearbook.page(yearbook.size() - 1), -1, yearbook.size() - 1));
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

	public void refreshNoPageList() {
		updateCanvas();
		shell.layout();
		refreshYearbookName();
	}
	
	public void refreshYearbookName() {
		if (!shell.getText().contains(yearbook.name)) setWindowTitle(yearbook.name);
		else if (yearbook.name.isEmpty()) setWindowTitle(SWT.DEFAULT);
	}

	public void refresh() {
		updatePageList();
		refreshNoPageList();
	}
	
	private void loadFonts() {
		File folder = new File("icons/fonts");
		File[] files = folder.listFiles();
		for (File f : files) {
			display.loadFont(f.getPath());
		}
		
		fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	}

	public static void main(String[] args) {
		new Creator();
		//reader.Reader.main(null);
		//ProductKey.main();
	}

}
