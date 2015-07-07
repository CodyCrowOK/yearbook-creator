package writer;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.pdfbox.exceptions.COSVisitorException;
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
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * The yearbook editor
 * @author Cody Crow
 *
 */
public class Creator {

	public static final String VERSION = "0.08";
	public static final String COMPANY_NAME = "Digital Express";
	public static final String SOFTWARE_NAME = "Yearbook Designer";

	//Used so we don't "Save As..." every time.
	public String saveFileName;

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
	private MenuItem insertLinkItem;
	private MenuItem insertPageNumbersItem;
	private MenuItem insertToCItem;
	private MenuItem pageMenuItem;
	private Menu pageMenu;
	private MenuItem pageMirrorItem;
	private MenuItem pageBackgroundItem;
	private MenuItem pageClearBackgroundItem;
	private MenuItem pageUseCoverItem;
	private MenuItem pageShowGridItem;
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

	private Creator() {
		display = new Display();
		shell = new Shell(display);
		settings = new UserSettings();
		setWindowTitle(SWT.DEFAULT);
		//clipboard.elements = new ArrayList<YearbookElement>();
		clipboard = new Clipboard();

		shell.setSize(800, 600);

		this.buildMenu();
		this.setMenuListeners();

		this.loadFonts();
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
				if (selectedPageIndex < 0 || selectedPageIndex > pagesList.getItemCount()) return;
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
		
		this.finalPrep();

		shell.setMaximized(true);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())	display.sleep();
		}
		display.dispose();

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
		canvasGridData.horizontalSpan = 6;
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

				if (event.button == 3 && yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
					int trueX = event.x;
					int trueY = event.y;
					Menu menu = new Menu(shell);
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
						if (result == SWT.YES) yearbook.page(yearbook.activePage).removeElement(clipboard.elements.get(0));
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
							yearbook.page(yearbook.activePage).findElement(selectedElement).setLocationRelative(newX, newY);
						}
					} else {
						int newX, newY;
						for (YearbookElement element : clipboard.elements) {
							newX = element.getBounds().x + xDiff;
							newY = element.getBounds().y + yDiff;
							element.setLocationRelative(newX, newY);
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
							newX = (int) (xPercent * selectedElement.getBounds(yearbook.settings.width, yearbook.settings.height).width);
							newY = (int) (yPercent * selectedElement.getBounds(yearbook.settings.width, yearbook.settings.height).height);
							
							if (yearbook.page(yearbook.activePage).findElement(selectedElement) != null) {
								yearbook.page(yearbook.activePage).findElement(selectedElement).resize(display, newX, newY);
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

				if (event.button == 3 && yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
					int trueX = event.x;
					int trueY = event.y;
					Menu menu = new Menu(shell);
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
						if (result == SWT.YES) yearbook.page(yearbook.activePage).removeElement(clipboard.elements.get(0));
						refresh();
					}
					break;
				case RESIZE:
					if (yearbook.page(yearbook.activePage).isElementAtPoint(event.x, event.y)) {
						selectElement(yearbook.page(yearbook.activePage).getElementAtPoint(event.x, event.y));
						refresh();
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
						if (yearbook.page(yearbook.activePage).findElement(selectedElement) != null && event.button == 1) {
							int newX, newY;
							newX = selectedElement.getBounds().x + xDiff;
							newY = selectedElement.getBounds().y + yDiff;
							yearbook.page(yearbook.activePage).findElement(selectedElement).setLocationRelative(newX, newY);
						}
					} else {
						int newX, newY;
						for (YearbookElement element : clipboard.elements) {
							newX = element.getBounds().x + xDiff;
							newY = element.getBounds().y + yDiff;
							element.setLocationRelative(newX, newY);
						}
					}

					refreshNoPageList();
					break;
				case ERASE:
					break;
				case RESIZE:
					xDiff += event.x;
					yDiff += event.y;

					for (YearbookElement selectedElement : clipboard.elements) {
						if (yearbook.page(yearbook.activePage).findElement(selectedElement) != null) {
							yearbook.page(yearbook.activePage).findElement(selectedElement).resize(display, xDiff, yDiff);
						}
					}
					refreshNoPageList();
					startX = startY = xDiff = yDiff = 0;

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
						int newY = e.getBounds().y + 1;
						e.setLocationRelative(e.getBounds().x, newY);
						if (e.getBounds().y < newY) {
							e.setLocationRelative(e.getBounds().x, newY + 1);
						}
					}
					break;
				case SWT.ARROW_UP:
					for (YearbookElement e : clipboard.elements) {
						int newY = e.getBounds().y - 1;
						e.setLocationRelative(e.getBounds().x, newY);
						if (e.getBounds().y > newY) {
							e.setLocationRelative(e.getBounds().x, newY + 1);
						}
					}
					break;
				case SWT.ARROW_RIGHT:
					for (YearbookElement e : clipboard.elements) {
						int newX = e.getBounds().x + 1;
						e.setLocationRelative(newX, e.getBounds().y);
						if (e.getBounds().x < newX) {
							e.setLocationRelative(newX + 1, e.getBounds().y);
						}
					}
					break;
				case SWT.ARROW_LEFT:
					for (YearbookElement e : clipboard.elements) {
						int newX = e.getBounds().x - 1;
						e.setLocationRelative(newX, e.getBounds().y);
						if (e.getBounds().x > newX) {
							e.setLocationRelative(newX - 1, e.getBounds().y);
						}
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
						int newY = e.getBounds().y + 1;
						e.setLocationRelative(e.getBounds().x, newY);
						if (e.getBounds().y < newY) {
							e.setLocationRelative(e.getBounds().x, newY + 1);
						}
					}
					break;
				case SWT.ARROW_UP:
					for (YearbookElement e : clipboard.elements) {
						int newY = e.getBounds().y - 1;
						e.setLocationRelative(e.getBounds().x, newY);
						if (e.getBounds().y > newY) {
							e.setLocationRelative(e.getBounds().x, newY + 1);
						}
					}
					break;
				case SWT.ARROW_RIGHT:
					for (YearbookElement e : clipboard.elements) {
						int newX = e.getBounds().x + 1;
						e.setLocationRelative(newX, e.getBounds().y);
						if (e.getBounds().x < newX) {
							e.setLocationRelative(newX + 1, e.getBounds().y);
						}
					}
					break;
				case SWT.ARROW_LEFT:
					for (YearbookElement e : clipboard.elements) {
						int newX = e.getBounds().x - 1;
						e.setLocationRelative(newX, e.getBounds().y);
						if (e.getBounds().x > newX) {
							e.setLocationRelative(newX - 1, e.getBounds().y);
						}
					}
					break;
				}
				refresh();

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
			}
		});

		final Spinner text = new Spinner(dialog, SWT.WRAP);
		text.setMinimum(-179);
		text.setMaximum(180);
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				int count = 0;
				elementAtPoint.rotation = Float.valueOf(text.getText());
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
			}
		});

		dialog.setDefaultButton (ok);
		dialog.pack();
		dialog.open();
		
	}



	protected void openTextDialog(YearbookTextElement element) {

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


		String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
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

		editYearbookNameItem = new MenuItem(editMenu, SWT.PUSH);
		editYearbookNameItem.setText("Yearbook Name...");


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

		pageUseCoverItem = new MenuItem(pageMenu, SWT.CHECK);
		pageUseCoverItem.setText("Show Cover");

		new MenuItem(pageMenu, SWT.SEPARATOR);

		pageShowGridItem = new MenuItem(pageMenu, SWT.CHECK);
		pageShowGridItem.setText("Show &Grid");



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
		isInsertingText = false;
		MOD1 = false;

		canvasBackgroundColor = new Color(display, 254, 254, 254);

		/*
		 * Let's create a splash screen.
		 */
		Shell splash = new Shell(display, SWT.DIALOG_TRIM);
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
				fileNewItem.getListeners(SWT.Selection)[0].handleEvent(event);
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
				} catch (COSVisitorException | IOException e) {
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
				yearbook.page(yearbook.activePage).addElement(element);
				//refresh();
				GC gc = new GC(canvas);
				gc.drawImage(element.getImage(display), 0, 0, element.getImage(display).getBounds().width, element.getImage(display).getBounds().height, 0, 0, element.getBounds().width, element.getBounds().height);
				gc.dispose();
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
				System.out.println(clipboard.elements.size());
				for (YearbookElement selectedElement : clipboard.elements) {
					if ((settings.cursorMode != CursorMode.SELECT || selectionRectangle == null) && clipboard.elements.size() == 0) {
						MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION);
						box.setText("Insert Video");
						box.setMessage("Please select an area of the page to link to the video.");
						box.open();
						return;
					} else if (selectedElement != null && selectionRectangle == null && selectedElement.isImage()) {
						try {
							attachVideoToImage((YearbookImageElement) selectedElement);
						} catch (IOException e) {
							e.printStackTrace();
						}
						return;
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
					yearbook.page(yearbook.activePage).addElement(e);
				} catch (IOException e) {
					e.printStackTrace();
				}

				modeReset();
				refresh();


			}

		});
		
		insertPageNumbersItem.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				yearbook.settings.showPageNumbers = true;
				openPageNumberDialog(yearbook.pageNumber);
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
				//TODO: implement UI.
				//Assume horizontal for now.
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

	protected void openPageNumberDialog(YearbookPageNumberElement element) {
		Shell textTool = new Shell(display, SWT.DIALOG_TRIM);
		textTool.setText("Page Number Tool");
		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		layout.makeColumnsEqualWidth = true;
		textTool.setLayout(layout);

		/*
		Text textbox = new Text(textTool, SWT.BORDER | SWT.MULTI);
		textbox.setText(element.text);
		GridData textboxData = new GridData(SWT.FILL, SWT.FILL, true, true);
		textboxData.horizontalSpan = 10;
		textbox.setLayoutData(textboxData);
		*/
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
				refresh();
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

	protected String imagePicker() {
		FileDialog picker = new FileDialog(shell, SWT.OPEN);
		String[] allowedExtensions = {"*.jpg; *.jpeg; *.gif; *.tif; *.tiff; *.bpm; *.ico; *.png", "*.*"};
		picker.setFilterExtensions(allowedExtensions);
		return picker.open();
	}

	protected void attachVideoToImage(YearbookImageElement element) throws IOException {
		YearbookClickableImageElement e = new YearbookClickableImageElement(display, element.getImage(display).getImageData(), element.getPageWidth(), element.getPageHeight());

		e.x = element.x;
		e.y = element.y;
		e.scale = element.scale;
		e.rotation = element.rotation;
		e.imageData = element.imageData;

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
							yearbook.page(yearbook.activePage).removeElement(element);
						}
						clipboard.elements.clear();
					}
				}
				modeReset();
				shell.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
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
				shell.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
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

		int canvasHeight = display.getClientArea().height - 150;

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
		paintPage(gc, display, yearbook, clipboard.elements, selectionRectangle, settings, index, yearbook.settings.width, yearbook.settings.height, false);
		gc.dispose();
	}
	
	private void loadRightCanvas(int index) {
		GC gc;
		gc = new GC(rightCanvas);
		paintPage(gc, display, yearbook, clipboard.elements, selectionRectangle, settings, index, yearbook.settings.width, yearbook.settings.height, false);
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
		gc.drawText("Front Cover", (yearbook.settings.width / 2) - (gc.textExtent("Front Cover").x / 2), yearbook.settings.height / 2); 
		gc.dispose();		
	}
	
	private void blankRightCanvas() {
		GC gc;
		gc = new GC(rightCanvas);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gc.fillRectangle(0, 0, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		gc.drawText("Back Cover", (yearbook.settings.width / 2) - (gc.textExtent("Back Cover").x / 2), yearbook.settings.height / 2);
		gc.dispose();		
	}

	/**
	 * This function handles the painting of the canvas for the currently
	 * selected yearbook page.
	 * @param activePage The page to draw on the canvas.
	 */
	private void loadActivePage(int activePage) {
		GC gc;
		gc = new GC(canvas);
		paintPage(gc, display, yearbook, clipboard.elements, selectionRectangle, settings, activePage, yearbook.settings.width, yearbook.settings.height, false);
		gc.dispose();
	}

	public static void paintPage(GC gc, Display display, Yearbook yearbook, 
			ArrayList<YearbookElement> selectedElements, 
			Rectangle selectionRectangle, UserSettings settings,
			int activePage, int pageWidth, int pageHeight, boolean isReader) {

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
			tr.rotate(element.rotation);
			gc.setTransform(tr);
			gc.drawImage(element.getImage(display), 0, 0, element.getImage(display).getBounds().width, element.getImage(display).getBounds().height, element.getBounds(pageWidth, pageHeight).x, element.getBounds(pageWidth, pageHeight).y, element.getBounds(pageWidth, pageHeight).width, element.getBounds(pageWidth, pageHeight).height);
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
			tr.dispose();
		}

		//If the user has selected an area, we should do something about that.
		if (selectionRectangle != null && settings.cursorMode == CursorMode.SELECT) {
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
			tr.rotate(e.rotation);
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
			Transform tr = new Transform(display);
			tr.rotate(e.rotation);
			gc.setTransform(tr);
			gc.setAdvanced(true);
			gc.setTextAntialias(SWT.ON);
			gc.setFont(e.getFont(display, pageWidth, pageHeight));
			
			//int x = (int) (e.x * pageWidth);
			//int y = (int) (e.y * pageHeight);

			if (e.shadow) {
				int offset = e.size >= 72 ? 3 : e.size >= 36 ? 2 : 1;
				gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
				gc.setAlpha(0x8f);
				gc.drawText(e.text, e.getBounds(pageWidth, pageHeight).x + offset, e.getBounds(pageWidth, pageHeight).y + offset, true);
				gc.setAlpha(0xff);
			}

			gc.setForeground(e.getColor(display));
			
			//System.out.println(x + " " + e.getBounds().x);
			
			/*
			 * Inform the text element of its bounds.
			 * This must be done here, regrettably.
			 */
			//e.setBounds(new Rectangle(x, y, gc.stringExtent(e.text).x, gc.stringExtent(e.text).y));
			
			gc.drawText(e.text, e.getBounds(pageWidth, pageHeight).x, e.getBounds(pageWidth, pageHeight).y, true);

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
				gc.drawLine(e.getBounds(pageWidth, pageHeight).x + 1, e.getBounds(pageWidth, pageHeight).y + e.getBounds(pageWidth, pageHeight).height - (int) (e.getBounds(pageWidth, pageHeight).height * .1), e.getBounds(pageWidth, pageHeight).x + e.getBounds(pageWidth, pageHeight).width - 1, e.getBounds(pageWidth, pageHeight).y + e.getBounds(pageWidth, pageHeight).height - (int) (e.getBounds(pageWidth, pageHeight).height * .1));

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
			tr.dispose();

		}

		boolean displayNumbers = !(activePage == 0 || activePage - 1 == yearbook.size()) && yearbook.settings.showPageNumbers;
		
		//Paint the page numbers
		if (displayNumbers) {
			YearbookTextElement element = yearbook.pageNumber;
			String text = Integer.toString(activePage);
			gc.setAdvanced(true);
			gc.setAntialias(SWT.ON);
			gc.setFont(element.getFont(display));
			Rectangle bounds = YearbookPageNumberElement.generateBounds(pageWidth, pageHeight, yearbook.pageNumber.location, activePage, gc.textExtent(text));
			
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
	}

	public static void main(String[] args) {
		new Creator();
		//reader.Reader.main(null);
	}

}
