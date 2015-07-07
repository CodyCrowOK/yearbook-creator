package reader;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.*;
import javax.sound.sampled.LineEvent.Type;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import writer.Creator;
import writer.UserSettings;
import writer.Yearbook;
import writer.YearbookClickableElement;
import writer.YearbookClickableImageElement;
import writer.YearbookElement;
import writer.YearbookIcons;
import writer.YearbookImages;

/**
 * Displays a yearbook to the end user.
 * @author Cody Crow
 *
 */
public class Reader {
	public static final boolean DEMO = false;
	public static final boolean PRODUCTION = false;
	
	Display display;
	Shell shell;
	
	int canvasHeight;
	Yearbook yearbook;

	Composite bigCanvasWrapper;
	Composite canvasWrapper;
	Composite canvasWrapper2;
	private Canvas canvas;
	private Canvas rightCanvas;
	private Color canvasBackgroundColor;
	
	Label yearbookName;
	Label separator;
	Text pageNumbers;
	Label pages;
	Label totalPages;
	
	boolean onPageCover;
	boolean front;
	boolean back;
	
	@SuppressWarnings("unused")
	public Reader() throws ClassNotFoundException, IOException {
		display = new Display();
		shell = new Shell(display);
		/*
		 * Actual end-user program will not have a dialog.
		 */
		String fileName;
		if (!(DEMO || PRODUCTION)) {
			FileDialog picker = new FileDialog(shell, SWT.OPEN);
			picker.setText("Open Yearbook");
			picker.setFilterExtensions(new String[] {"*.ctc"});
			fileName = picker.open();
			if (fileName == null) return;
		} else if (DEMO && !PRODUCTION) {
			fileName = "demo.ctc";
		} else {
			fileName = "yearbook.ctc";
		}
		
		/*
		 * Load the fonts.
		 */
		this.loadFonts();
		
		onPageCover = false;
		
		this.initialize();
		yearbook = Yearbook.readFromDisk(fileName);
		this.createNewYearbook();
		
		this.initializeUI();
		
		this.refresh();
		
		shell.setLayout(new GridLayout());
		shell.setText(Creator.COMPANY_NAME + " Digital Yearbook");
		shell.pack();
		//Magic number, chosen for being near center.
		shell.setLocation((int) (.09375 * display.getClientArea().width), 0);
		
		//Need both of these for compatibility with Windows.
		shell.setMaximized(true);
		shell.setFullScreen(true);
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())	display.sleep();
		}
		display.dispose();
	}
	
	private void loadFonts() {
		File folder;// = new File("media/fonts");
		File[] files;/* = folder.listFiles();
		for (File f : files) {
			display.loadFont(f.getPath());
		}*/
		
		folder = new File("icons/fonts");
		files = folder.listFiles();
		for (File f : files) {
			display.loadFont(f.getPath());
		}
	}

	private void initializeUI() {
		yearbookName.setText(yearbook.name);
		
		if (!DEMO) {
			pageNumbers.setText(Integer.toString(0));
			totalPages.setText(" / " + Integer.toString(yearbook.size() - 1));
			
			
			pageNumbers.addListener(SWT.DefaultSelection, new Listener() {
	
				@Override
				public void handleEvent(Event event) {
					try {
						int entry = Integer.parseInt(pageNumbers.getText());
						if (entry >= 0 && entry <= yearbook.size() - 1)	
							if (yearbook.activePage < entry) {
								pageTurnRightAnimation(yearbook.activePage, entry);
								yearbook.activePage = entry;
								refresh();
							}
					} catch (NumberFormatException e) {
						
					}
					
				}
				
			});
		}
	}

	private void initialize() {

		if (!DEMO) canvasHeight = display.getClientArea().height - 120;
		if (DEMO) canvasHeight = display.getClientArea().height - 50;
		
		canvasBackgroundColor = new Color(display, 254, 254, 254);
		
		Color barColor = new Color(display, 0x84, 0x2c, 0x2a);
		Color gray = new Color(display, 0xee, 0xee, 0xee);
		
		Composite topbar = new Composite(shell, SWT.NONE);
		topbar.setBackground(barColor);
		topbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		RowLayout topbarLayout = new RowLayout();
		topbarLayout.wrap = false;
		topbarLayout.marginTop = 10;
		topbarLayout.marginBottom = 10;
		topbarLayout.marginLeft = 20;
		topbar.setLayout(topbarLayout);
		
		Font tahomaBold = new Font(display, "Gill Sans MT", 16, SWT.BOLD);
		Font tahomaSmall = new Font(display, "Gill Sans MT", 14, SWT.NONE);
		
		yearbookName = new Label(topbar, SWT.NONE);
		yearbookName.setBackground(barColor);
		yearbookName.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		yearbookName.setFont(tahomaBold);
		//separator = new Label(topbar, SWT.SEPARATOR);
		//separator.setBackground(barColor);
		if (!DEMO) {
			pages = new Label(topbar, SWT.NONE);
			pages.setForeground(gray);
			pages.setBackground(barColor);
			pages.setFont(tahomaSmall);
			pages.setText("   pages: ");
			
			Composite numberWrapper = new Composite(topbar, SWT.NONE);
			numberWrapper.setLayout(new FillLayout());
			pageNumbers = new Text(numberWrapper, SWT.SINGLE);
			pageNumbers.setBackground(gray);
			
			totalPages = new Label(topbar, SWT.NONE);
			totalPages.setForeground(gray);
			totalPages.setBackground(barColor);
			totalPages.setFont(tahomaSmall);
		}
		//barColor.dispose();
		//tahomaBold.dispose();
		//tahomaSmall.dispose();
		//gray.dispose();
		
		
		bigCanvasWrapper = new Composite(shell, SWT.NONE);
		bigCanvasWrapper.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, true));
		GridLayout wrapperLayout = new GridLayout(2, false);
		//For the larger open book image:
		//wrapperLayout.marginHeight = (int) (1.1 * (68.0 / 2868.0) * canvasHeight);
		//wrapperLayout.marginWidth = (int) (2.2 * (68.0 / 2868.0) * canvasHeight);
		
		wrapperLayout.marginHeight = (int) ((13.0 / 2868.0) * (canvasHeight));
		wrapperLayout.marginWidth = (int) (1.05 * (68.0 / 2868.0) * canvasHeight);
		bigCanvasWrapper.setLayout(wrapperLayout);
		
		canvasWrapper = new Composite(bigCanvasWrapper, SWT.NONE);
		canvas = new Canvas(canvasWrapper, SWT.NONE);
		canvas.setBackground(canvasBackgroundColor);
		
		canvasWrapper2 = new Composite(bigCanvasWrapper, SWT.NONE);
		rightCanvas = new Canvas(canvasWrapper2, SWT.NONE);
		rightCanvas.setBackground(canvasBackgroundColor);
		
		canvas.addMouseListener(new MouseListener() {
			int startX;
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				turnPageLeft();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				startX = e.x;
				if (rightIsActive() && yearbook.activePage - 1 >= 0) {
					yearbook.activePage--;
				}
				
				if (yearbook.page(yearbook.activePage).isClickableAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height) && leftIsActive()) {
					//Show their video.
					//Using the system player for now.
					File file;
					if (yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height).isImage()) {
						file = new File(((YearbookClickableImageElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)).getVideo().getSrc());
					} else {
						file = new File(((YearbookClickableElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)).getVideo().getSrc());
					}
					
					Desktop dt = Desktop.getDesktop();
					
					try {
						dt.open(file);
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						MessageBox box = new MessageBox(shell, SWT.ERROR);
						box.setText("Error");
						box.setMessage("The video was not loaded successfully.");
						box.open();
					}
				}
				
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (e.x - startX >= 20) {
					if (back) {
						openFromBack();
					}
					turnPageLeft();
				}
			}
			
		});
		
		rightCanvas.addMouseListener(new MouseListener() {

			int startX;
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				turnPageRight();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				startX = e.x;
				if (leftIsActive() && yearbook.activePage + 1 < yearbook.size()) { 
					yearbook.activePage++;
				}

				if (yearbook.page(yearbook.activePage).isClickableAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height) && rightIsActive()) {
					//Show their video.
					//Using the system player for now.
					File file;
					if (yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height).isImage()) {
						file = new File(((YearbookClickableImageElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)).getVideo().getSrc());
					} else {
						file = new File(((YearbookClickableElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)).getVideo().getSrc());
					}
					
					Desktop dt = Desktop.getDesktop();
					
					try {
						dt.open(file);
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						MessageBox box = new MessageBox(shell, SWT.ERROR);
						box.setText("Error");
						box.setMessage("The video was not loaded successfully.");
						box.open();
					}
				} 
				
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (e.x - startX <= 20) {
					if (front) {
						openBook();
					}
					turnPageRight();
				}
			}
			
		});
		
		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				refresh();
				
			}
			
		});
		
		rightCanvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				refresh();
				
			}
			
		});
		
		Composite navbar = new Composite(shell, SWT.NONE);
		if (DEMO) navbar.setVisible(false);
		navbar.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, true));
		RowLayout barLayout = new RowLayout();
		barLayout.pack = true;
		barLayout.marginBottom = 0;
		barLayout.marginRight = 0;
		barLayout.marginLeft = 5;
		barLayout.marginTop = 0;
		barLayout.spacing = 0;

		navbar.setLayout(barLayout);
		Button frontBtn = new Button(navbar, SWT.PUSH);
		Button backBtn = new Button(navbar, SWT.PUSH);
		Button nextBtn = new Button(navbar, SWT.PUSH);
		Button endBtn = new Button(navbar, SWT.PUSH);
		
		Image image = YearbookIcons.navFront(display);
		frontBtn.setImage(image);
		//image.dispose();
		image = YearbookIcons.navBack(display);
		backBtn.setImage(image);
		//image.dispose();
		image = YearbookIcons.navNext(display);
		nextBtn.setImage(image);
		//image.dispose();
		image = YearbookIcons.navEnd(display);
		endBtn.setImage(image);
		//image.dispose();
		navbar.pack();
		
		nextBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				turnPageRight();
				
			}
			
		});
		
		backBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				turnPageLeft();
				
			}
			
		});
		
		frontBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				yearbook.activePage = 0;
				refresh();
				
			}
			
		});
		
		endBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				yearbook.activePage = yearbook.size() - 1;
				refresh();
				
			}
			
		});

		shell.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				Image bg = YearbookImages.readerBackground(display);
				e.gc.drawImage(bg, 0, 0, bg.getBounds().width, bg.getBounds().height, 0, 0, shell.getBounds().width, shell.getBounds().height);
				bg.dispose();
			}
			
		});
		
		bigCanvasWrapper.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				Image bg = YearbookImages.openBook(display);
				if (!onPageCover) {
					e.gc.drawImage(bg, 0, 0, bg.getBounds().width, bg.getBounds().height, 0, 0, bigCanvasWrapper.getBounds().width, bigCanvasWrapper.getBounds().height);
					e.gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BORDER));
					e.gc.setLineWidth(5);
					e.gc.drawRectangle(0, 0, bigCanvasWrapper.getBounds().width, bigCanvasWrapper.getBounds().height);
				} else {
					int x = bigCanvasWrapper.getBounds().x;
					int y = bigCanvasWrapper.getBounds().y;
					int width = bigCanvasWrapper.getBounds().width;
					int height = bigCanvasWrapper.getBounds().height;
					Image shellBg = YearbookImages.readerBackground(display);
					int xc = (int) ((double) x / shell.getBounds().width * shellBg.getBounds().width);
					int yc = (int) ((double) y / shell.getBounds().height * shellBg.getBounds().height);
					int wc = (int) ((double) width / shell.getBounds().width * shellBg.getBounds().width);
					int hc = (int) ((double) height / shell.getBounds().height * shellBg.getBounds().height);
					e.gc.drawImage(shellBg, xc, yc, wc, hc, 0, 0, width, height);
					//System.out.println(((double) x / shell.getBounds().width * shellBg.getBounds().width) + " " + yc + " " + wc + " " + hc);
					shellBg.dispose();
					
					/*
					if (front) {
						int halfWidth = (int) Math.floor(bg.getBounds().width / 2);
						int halfDestWidth = (int) Math.floor(bigCanvasWrapper.getBounds().width / 2);
						//e.gc.drawImage(bg, halfWidth, 0, halfWidth, bg.getBounds().height, halfDestWidth, 0, halfDestWidth, bigCanvasWrapper.getBounds().height);
						//System.out.println(bigCanvasWrapper.getBounds().height);
					}
					*/
				}
				bg.dispose();
				
			}
			
		});

		navbar.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				Image bg = YearbookImages.readerBackground(display);
				e.gc.drawImage(bg, 0, 0, bg.getBounds().width, bg.getBounds().height, 0, 0, shell.getBounds().width, shell.getBounds().height);
				bg.dispose();
				
			}
			
		});
		
		shell.addListener(SWT.Traverse, new Listener() {

			@Override
			public void handleEvent(Event event) {
				switch (event.detail) {
				case SWT.TRAVERSE_ESCAPE:
					shell.close();
					event.detail = SWT.TRAVERSE_NONE;
					event.doit = false;
					break;
				}
				
			}
			
		});
		
		
		
	}

	protected void openFromBack() {
		//
		
	}

	protected void openBook() {
		//pageTurnRightAnimation(0, 1);
	}
	
	private void pageTurnLeftAnimation(int current, int next) {
		if (DEMO) return;
		if (current == yearbook.size() - 1 || Math.abs(next - current) > 2 || DEMO) {
			coverTurnLeftAnimation(current, next);
		}

	}
	
	private void coverTurnLeftAnimation(int current, int next) {
		if (DEMO) return;
		Image image = new Image(display, canvas.getBounds().width, canvas.getBounds().height);
		GC gc = new GC(image);
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), current, yearbook.settings.width, yearbook.settings.height, true);
		gc.dispose();
		Image pageTwo = new Image(display, canvas.getBounds().width, canvas.getBounds().height);
		gc = new GC(pageTwo);
		if (next != MagicNumber.FIRST_PAGE) Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), next, yearbook.settings.width, yearbook.settings.height, true);
		else {
			int x = canvasWrapper.getBounds().x + bigCanvasWrapper.getBounds().x;
			int y = canvasWrapper.getBounds().y + bigCanvasWrapper.getBounds().y;
			int width = canvasWrapper.getBounds().width;
			int height = canvasWrapper.getBounds().height;
			Image shellBg = YearbookImages.readerBackground(display);
			int xc = (int) ((double) x / shell.getBounds().width * shellBg.getBounds().width);
			int yc = (int) ((double) y / shell.getBounds().height * shellBg.getBounds().height);
			int wc = (int) ((double) width / shell.getBounds().width * shellBg.getBounds().width);
			int hc = (int) ((double) height / shell.getBounds().height * shellBg.getBounds().height);
			gc.drawImage(shellBg, xc, yc, wc, hc, 0, 0, width, height);
			shellBg.dispose();
		}
		
		gc.dispose();

		int i = 0;
		int subtrahend = 0;
		
		while (i < MagicNumber.FRAMES) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			canvas.update();
			i += 1;
			subtrahend = (int) ((double) i / 500 * image.getBounds().width);
			Image consolidated = new Image(display, canvas.getBounds().width, canvas.getBounds().height);
			gc = new GC(consolidated);
			gc.drawImage(pageTwo, 0, 0);
			gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, consolidated.getBounds().width - subtrahend, 0, subtrahend, consolidated.getBounds().height);
			gc.dispose();
			
			gc = new GC(canvas);
			gc.drawImage(consolidated, 0, 0);
			gc.dispose();
			consolidated.dispose();
		}
		
		pageTwo.dispose();
		image.dispose();
	}
	
	private void pageTurnRightAnimation(int current, int next) {
		if (DEMO) return;
		if (current == 0 || Math.abs(next - current) > 2 || DEMO) {
			coverTurnRightAnimation(current, next);
		} else {
			smoothPageTurnRight(current, next);
		}
	}

	private void smoothPageTurnRight(int current, int next) {
		Image nextRight = new Image(display, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		GC gc = new GC(nextRight);
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), next, yearbook.settings.width, yearbook.settings.height, true);
		gc.dispose();

		Image currentRight = new Image(display, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		gc = new GC(currentRight);
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), current, yearbook.settings.width, yearbook.settings.height, true);
		gc.dispose();

		Image nextLeft = new Image(display, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		gc = new GC(nextLeft);
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), current + 1, yearbook.settings.width, yearbook.settings.height, true);
		gc.dispose();
		
		Image currentLeft = new Image(display, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		gc = new GC(currentLeft);
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), current - 1, yearbook.settings.width, yearbook.settings.height, true);
		gc.dispose();
		
		int i = 0;
		int srcWidth, srcHeight, destX, destWidth, destHeight;
		Image rightBuffer, leftBuffer;
		while (i++ < MagicNumber.FRAMES) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			rightCanvas.update();
			
			long k = (long) (Math.exp((double) i / 6) / MagicNumber.FRAMES * rightCanvas.getBounds().width);
			//long k = (long) (Math.log((double) i / 6) / MagicNumber.FRAMES * rightCanvas.getBounds().width);
			
			
			rightBuffer = new Image(display, nextRight.getImageData());
			srcWidth = (int) k;
			srcHeight = rightCanvas.getBounds().height;
			destX = (int) ((long) rightCanvas.getBounds().width - k);
			if (destX <= 0) break;
			destWidth = srcWidth;
			destHeight = srcHeight;
			gc = new GC(rightBuffer);
			gc.drawImage(nextLeft, 0, 0, srcWidth, srcHeight, destX, 0, destWidth, destHeight);
			gc.dispose();
			gc = new GC(rightCanvas);
			gc.drawImage(rightBuffer, 0, 0);
			gc.dispose();
			rightBuffer.dispose();
			
			leftBuffer = new Image(display, currentLeft.getImageData());
			
			gc = new GC(leftBuffer);
			gc.drawImage(nextLeft, 0, 0, srcWidth, srcHeight, destX, 0, destWidth, destHeight);
			gc.dispose();
			gc = new GC(canvas);
			gc.drawImage(leftBuffer, 0, 0);
			gc.dispose();
			leftBuffer.dispose();
		}
		
		nextRight.dispose();
		nextLeft.dispose();
		currentRight.dispose();
		currentLeft.dispose();
		
	}

	private void coverTurnRightAnimation(int current, int next) {
		if (DEMO) return;
		Image image = new Image(display, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		GC gc = new GC(image);
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), current, yearbook.settings.width, yearbook.settings.height, true);
		gc.dispose();
		Image pageTwo = new Image(display, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		gc = new GC(pageTwo);

		
		
		if (next != MagicNumber.LAST_PAGE) Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), next, yearbook.settings.width, yearbook.settings.height, true);
		else {
			int x = canvasWrapper2.getBounds().x + bigCanvasWrapper.getBounds().x;
			int y = canvasWrapper2.getBounds().y + bigCanvasWrapper.getBounds().y;
			int width = canvasWrapper2.getBounds().width;
			int height = canvasWrapper2.getBounds().height;
			Image shellBg = YearbookImages.readerBackground(display);
			int xc = (int) ((double) x / shell.getBounds().width * shellBg.getBounds().width);
			int yc = (int) ((double) y / shell.getBounds().height * shellBg.getBounds().height);
			int wc = (int) ((double) width / shell.getBounds().width * shellBg.getBounds().width);
			int hc = (int) ((double) height / shell.getBounds().height * shellBg.getBounds().height);
			gc.drawImage(shellBg, xc, yc, wc, hc, 0, 0, width, height);
			shellBg.dispose();
		}
		
		gc.dispose();
		
		Image leftOriginal = new Image(display, canvas.getBounds().width, canvas.getBounds().height);
		gc = new GC(leftOriginal);
		if (current != 0) Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), current - 1, yearbook.settings.width, yearbook.settings.height, true);
		else {
			int x = canvasWrapper.getBounds().x + bigCanvasWrapper.getBounds().x;
			int y = canvasWrapper.getBounds().y + bigCanvasWrapper.getBounds().y;
			int width = canvasWrapper.getBounds().width;
			int height = canvasWrapper.getBounds().height;
			Image shellBg = YearbookImages.readerBackground(display);
			int xc = (int) ((double) x / shell.getBounds().width * shellBg.getBounds().width);
			int yc = (int) ((double) y / shell.getBounds().height * shellBg.getBounds().height);
			int wc = (int) ((double) width / shell.getBounds().width * shellBg.getBounds().width);
			int hc = (int) ((double) height / shell.getBounds().height * shellBg.getBounds().height);
			gc.drawImage(shellBg, xc, yc, wc, hc, 0, 0, width, height);
			shellBg.dispose();
		}
		gc.dispose();
		
		Image newLeft = new Image(display, canvas.getBounds().width, canvas.getBounds().height);
		gc = new GC(newLeft);
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), current + 1, yearbook.settings.width, yearbook.settings.height, true);
		gc.dispose();
		
		int i = 0;
		int subtrahend = 0;
		
		while (i++ < MagicNumber.FRAMES) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			rightCanvas.update();
			subtrahend = (int) ((double) i / MagicNumber.FRAMES * image.getBounds().width);
			Image consolidated = new Image(display, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
			gc = new GC(consolidated);
			gc.drawImage(pageTwo, 0, 0);
			gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, consolidated.getBounds().width - subtrahend, consolidated.getBounds().height);
			gc.dispose();
			
			gc = new GC(rightCanvas);
			gc.drawImage(consolidated, 0, 0);
			gc.dispose();
			consolidated.dispose();
			
			
		}
		
		i = 0;
		subtrahend = 0;

		while (i++ < MagicNumber.FRAMES) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			canvas.update();
			Image consolidated = new Image(display, canvas.getBounds().width, canvas.getBounds().height);
			gc = new GC(consolidated);
			gc.drawImage(leftOriginal, 0, 0);
			gc.drawImage(newLeft, 0, 0, newLeft.getBounds().width, newLeft.getBounds().height, canvas.getBounds().width - subtrahend, 0, subtrahend, canvas.getBounds().height);
			gc.dispose();
			
			gc = new GC(canvas);
			gc.drawImage(consolidated, 0, 0);
			gc.dispose();
			consolidated.dispose();

		}
		canvasWrapper.setVisible(true);
		
		pageTwo.dispose();
		image.dispose();
		
	}

	private void createNewYearbook() {
		

		canvasHeight = display.getClientArea().height - 150;
		
		yearbook.settings.height = canvasHeight;
		yearbook.settings.width = (int) ((8.5 / 11.0) * canvasHeight);
		canvas.setSize(yearbook.settings.width, yearbook.settings.height);
		rightCanvas.setSize(yearbook.settings.width, yearbook.settings.height);
		
		yearbook.activePage = 0;
		
		if (DEMO) yearbook.activePage = 2;
		
	}
	
	private void loadPages(int pageIndex) {
		
		//Back cover
		if (pageIndex + 1 == yearbook.size() && leftIsActive()) {
			blankRightCanvas();
			loadLeftCanvas(pageIndex);
			return;
		}
	
		//Front cover
		if (pageIndex == 0) {
			blankLeftCanvas();
			loadRightCanvas(0);
			return;
		} 
		
		//Active page is odd
		if (leftIsActive()) {
			loadLeftCanvas(pageIndex);
			loadRightCanvas(pageIndex + 1);
			return;
		}
		
		//Active page is even
		if (rightIsActive()) {
			loadLeftCanvas(pageIndex - 1);
			loadRightCanvas(pageIndex);
			return;
		}
		
		yearbook.tidyUp();
	}
	
	private boolean leftIsActive() {
		return Math.abs(yearbook.activePage % 2) == 1;
	}
	
	private boolean rightIsActive() {
		return yearbook.activePage % 2 == 0; 
	}
	
	private void blankLeftCanvas() {
		GC gc;
		gc = new GC(canvas);
		gc.setBackground(canvasBackgroundColor);
		gc.fillRectangle(0, 0, canvas.getBounds().width, canvas.getBounds().height);
		Image logo = YearbookImages.logo(display);
		int x = (yearbook.settings.width - logo.getBounds().width) / 2;
		int y = (yearbook.settings.height - logo.getBounds().height) / 2;
		gc.drawImage(logo, x, y);
		logo.dispose();
		gc.dispose();		
	}
	
	private void blankRightCanvas() {
		GC gc;
		gc = new GC(rightCanvas);
		gc.setBackground(canvasBackgroundColor);
		gc.fillRectangle(0, 0, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		Image logo = YearbookImages.logo(display);
		int x = (yearbook.settings.width - logo.getBounds().width) / 2;
		int y = (yearbook.settings.height - logo.getBounds().height) / 2;
		gc.drawImage(logo, x, y);
		logo.dispose();
		gc.dispose();
	}
	
	private void loadLeftCanvas(int activePage) {
		//blankLeftCanvas();
		
		GC gc = new GC(canvas);
		
		if (yearbook.page(activePage).noBackground) {
			gc.setBackground(canvasBackgroundColor);
			gc.fillRectangle(0, 0, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		}
		
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), activePage, yearbook.settings.width, yearbook.settings.height, true);
		gc.dispose();
		
		canvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if (!yearbook.page(activePage).isElementAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)) {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
					return;
				}
				
				if (yearbook.page(activePage).isClickableAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)) {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
				} else {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
				}
			}
			
		});
		
	}
	
	private void loadRightCanvas(int activePage) {
		//blankRightCanvas();
		GC gc = new GC(rightCanvas);
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), activePage, yearbook.settings.width, yearbook.settings.height, true);
		gc.dispose();
		
		rightCanvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if (!yearbook.page(activePage).isElementAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)) {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
					return;
				}
				
				if (yearbook.page(activePage).isClickableAtPoint(e.x, e.y, yearbook.settings.width, yearbook.settings.height)) {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
				} else {
					shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
				}
			}
			
		});
		
	}
	
	private void refresh() {
		if (!DEMO) pageNumbers.setText(Integer.toString(yearbook.activePage));
		if (!yearbook.hasCover) {
			loadPages(yearbook.activePage);
			onPageCover = false;
		} else {
			onPageCover = true;
			if (yearbook.activePage == 0) {
				front = true;
				canvasWrapper.setVisible(false);
				canvasWrapper2.setVisible(true);
				bigCanvasWrapper.redraw();
				pageTurnLeftAnimation(0, MagicNumber.FIRST_PAGE);
				loadCover();
			} else if (yearbook.activePage == yearbook.size() - 1) {
				back = true;
				canvasWrapper.setVisible(true);
				bigCanvasWrapper.redraw();
				pageTurnRightAnimation(yearbook.activePage - 1, MagicNumber.LAST_PAGE);
				canvasWrapper2.setVisible(false);
				loadBackCover();
			} else {
				if (onPageCover || front || back) {
					onPageCover = false;
					front = back = false;
				}
				bigCanvasWrapper.redraw();
				canvasWrapper.setVisible(true);
				canvasWrapper2.setVisible(true);
				loadPages(yearbook.activePage);
			}
		}
	}

	private void loadCover() {
		if (DEMO) return;
		GC gc = new GC(rightCanvas);
		
		if (yearbook.page(0).noBackground) {
			gc.setBackground(canvasBackgroundColor);
			gc.fillRectangle(0, 0, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		}
		
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), 0, yearbook.settings.width, yearbook.settings.height, true);
		gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
		gc.setLineWidth(3);
		gc.drawRectangle(rightCanvas.getBounds());
		gc.dispose();
	}
	
	private void loadBackCover() {
		if (DEMO) return;
		GC gc = new GC(canvas);
		
		if (yearbook.page(0).noBackground) {
			gc.setBackground(canvasBackgroundColor);
			gc.fillRectangle(0, 0, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
		}
		
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), yearbook.size() - 1, yearbook.settings.width, yearbook.settings.height, true);
		gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
		gc.setLineWidth(3);
		gc.drawRectangle(rightCanvas.getBounds());
		gc.dispose();
	}

	private void turnPageLeft() {
		if (DEMO) return;
		int initial = yearbook.activePage;
		yearbook.activePage -= 2;
		if (yearbook.activePage < 0) yearbook.activePage = 0;
		if (initial != yearbook.activePage && yearbook.activePage != 0) pageTurnLeftAnimation(initial, yearbook.activePage);
		refresh();
		this.pageTurnSound();
	}

	private void turnPageRight() {
		if (DEMO) return;
		//pageTurnRightAnimation(yearbook.activePage, yearbook.activePage + 2);
		int initial = yearbook.activePage;
		yearbook.activePage += 2;
		if (yearbook.activePage >= yearbook.size()) yearbook.activePage = yearbook.size() - 1;
		if (initial != yearbook.activePage && yearbook.activePage + 1 != yearbook.size()) pageTurnRightAnimation(initial, yearbook.activePage);
		refresh();
		this.pageTurnSound();
	}
	
	private void pageTurnSound() {
		try {
			File file = new File("icons/sounds/pageflip.wav");
			playClip(file);
		} catch (IOException | UnsupportedAudioFileException
				| LineUnavailableException
				| InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void playClip(File clipFile) throws IOException, 
	UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
		class AudioListener implements LineListener {
			private boolean done = false;
			@Override public synchronized void update(LineEvent event) {
				Type eventType = event.getType();
				if (eventType == Type.STOP || eventType == Type.CLOSE) {
					done = true;
					notifyAll();
				}
			}
			public synchronized void waitUntilDone() throws InterruptedException {
				while (!done) { wait(); }
			}
		}
		AudioListener listener = new AudioListener();
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(clipFile);
		try {
			Clip clip = AudioSystem.getClip();
			clip.addLineListener(listener);
			clip.open(audioInputStream);
			try {
				clip.start();
				listener.waitUntilDone();
			} finally {
				clip.close();
			}
		} finally {
			audioInputStream.close();
		}
	}

	public static void main(String[] args) {
		try {
			new Reader();
		} catch (Exception e) {
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame, "Something went wrong.\n\t" + e);
			e.printStackTrace();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}

}
