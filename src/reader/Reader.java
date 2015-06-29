package reader;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.*;
import javax.sound.sampled.LineEvent.Type;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import writer.Creator;
import writer.SWTUtils;
import writer.UserSettings;
import writer.Yearbook;
import writer.YearbookClickableElement;
import writer.YearbookClickableImageElement;
import writer.YearbookElement;
import writer.YearbookIcons;
import writer.YearbookImageElement;
import writer.YearbookImages;
import writer.YearbookTextElement;

/**
 * Displays a yearbook to the end user.
 * @author Cody Crow
 *
 */
public class Reader {
	Display display;
	Shell shell;
	
	int canvasHeight;
	Yearbook yearbook;

	Composite bigCanvasWrapper;
	Composite canvasWrapper;
	Composite canvasWrapper2;
	Composite frontCover;
	Composite backCover;
	private Canvas canvas;
	private Canvas rightCanvas;
	private Color canvasBackgroundColor;
	
	public Reader() throws ClassNotFoundException, IOException {
		display = new Display();
		shell = new Shell(display);
		/*
		 * Actual end-user program will not have a dialog.
		 */
		FileDialog picker = new FileDialog(shell, SWT.OPEN);
		picker.setText("Open Yearbook");
		picker.setFilterExtensions(new String[] {"*.ctc"});
		String fileName = picker.open();
		if (fileName == null) return;
		
		
		this.initialize();
		yearbook = Yearbook.readFromDisk(fileName);
		this.createNewYearbook();
		
		this.refresh();
		
		shell.setLayout(new GridLayout());
		shell.setText(Creator.COMPANY_NAME + " Digital Yearbook");
		shell.pack();
		//Magic number, chosen for being near center.
		shell.setLocation((int) (.09375 * display.getClientArea().width), 0);
		shell.setFullScreen(true);
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())	display.sleep();
		}
		display.dispose();
	}
	
	private void initialize() {

		canvasHeight = display.getClientArea().height - 120;
		
		canvasBackgroundColor = new Color(display, 254, 254, 254);

		bigCanvasWrapper = new Composite(shell, SWT.BORDER);
		bigCanvasWrapper.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, true));
		GridLayout wrapperLayout = new GridLayout(2, false);
		wrapperLayout.marginHeight = (int) (1.1 * (68.0 / 2868.0) * canvasHeight);
		wrapperLayout.marginWidth = (int) (2.2 * (68.0 / 2868.0) * canvasHeight);
		bigCanvasWrapper.setLayout(wrapperLayout);
		bigCanvasWrapper.setVisible(false);
		
		frontCover = new Composite(shell, SWT.BORDER);
		frontCover.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, true));
		GridLayout frontLayout = new GridLayout(1, false);
		frontLayout.marginHeight = 0;
		frontCover.setLayout(frontLayout);
		frontCover.setVisible(false);
		
		backCover = new Composite(shell, SWT.BORDER);
		backCover.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, true));
		GridLayout backLayout = new GridLayout(1, false);
		backLayout.marginHeight = 0;
		backCover.setLayout(backLayout);
		backCover.setVisible(false);
		
		unshowBookCovers();
		
		canvasWrapper = new Composite(bigCanvasWrapper, SWT.NONE);
		canvas = new Canvas(canvasWrapper, SWT.NONE);
		canvas.setBackground(canvasBackgroundColor);
		
		canvasWrapper2 = new Composite(bigCanvasWrapper, SWT.NONE);
		rightCanvas = new Canvas(canvasWrapper2, SWT.NONE);
		rightCanvas.setBackground(canvasBackgroundColor);
		
		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				turnPageLeft();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (rightIsActive() && yearbook.activePage - 1 >= 0) {
					yearbook.activePage--;
				}
				
				if (yearbook.page(yearbook.activePage).isClickableAtPoint(e.x, e.y) && leftIsActive()) {
					//Show their video.
					//Using the system player for now.
					File file;
					if (yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y).isImage()) {
						file = new File(((YearbookClickableImageElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y)).getVideo().getSrc());
					} else {
						file = new File(((YearbookClickableElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y)).getVideo().getSrc());
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
				
			}
			
		});
		
		rightCanvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				turnPageRight();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (leftIsActive() && yearbook.activePage + 1 < yearbook.size()) { 
					yearbook.activePage++;
				}

				if (yearbook.page(yearbook.activePage).isClickableAtPoint(e.x, e.y) && rightIsActive()) {
					//Show their video.
					//Using the system player for now.
					File file;
					if (yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y).isImage()) {
						file = new File(((YearbookClickableImageElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y)).getVideo().getSrc());
					} else {
						file = new File(((YearbookClickableElement) yearbook.page(yearbook.activePage).getElementAtPoint(e.x, e.y)).getVideo().getSrc());
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
		
		frontCover.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				System.out.println("made it");
				if (yearbook.page(0).noBackground) {
					e.gc.setBackground(canvasBackgroundColor);
					e.gc.fillRectangle(0, 0, rightCanvas.getBounds().width, rightCanvas.getBounds().height);
				}
				
				Creator.paintPage(e.gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), 0, yearbook.settings.width, yearbook.settings.height);
				e.gc.dispose();
				
			}
			
		});
		
		Composite navbar = new Composite(shell, SWT.NONE);
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
		image.dispose();
		image = YearbookIcons.navBack(display);
		backBtn.setImage(image);
		image.dispose();
		image = YearbookIcons.navNext(display);
		nextBtn.setImage(image);
		image.dispose();
		image = YearbookIcons.navEnd(display);
		endBtn.setImage(image);
		image.dispose();
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
				e.gc.drawImage(bg, 0, 0, bg.getBounds().width, bg.getBounds().height, 0, 0, bigCanvasWrapper.getBounds().width, bigCanvasWrapper.getBounds().height);
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
		
	}

	private void createNewYearbook() {
		

		canvasHeight = display.getClientArea().height - 120;
		
		yearbook.settings.height = canvasHeight;
		yearbook.settings.width = (int) ((8.5 / 11.0) * canvasHeight);
		canvas.setSize(yearbook.settings.width, yearbook.settings.height);
		rightCanvas.setSize(yearbook.settings.width, yearbook.settings.height);
		
		yearbook.activePage = 0;

		frontCover.setSize(yearbook.settings.width, yearbook.settings.height);
		
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
		
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), activePage, yearbook.settings.width, yearbook.settings.height);
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
		Creator.paintPage(gc, display, yearbook, new ArrayList<YearbookElement>(), null, new UserSettings(), activePage, yearbook.settings.width, yearbook.settings.height);
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
		if (yearbook.activePage == 0) {
			showFrontCover();
			return;
		} else if (yearbook.activePage == yearbook.size() - 1) {
			showBackCover();
			return;
		}
		unshowBookCovers();
		loadPages(yearbook.activePage);
	}
	
	private void showFrontCover() {
		backCover.setVisible(false);
		bigCanvasWrapper.setVisible(false);
		frontCover.setVisible(true);
		shell.redraw();
		frontCover.update();
	}
	
	private void showBackCover() {
		bigCanvasWrapper.setVisible(false);
		frontCover.setVisible(false);
		backCover.setVisible(true);
		shell.redraw();
	}
	
	private void unshowBookCovers() {
		frontCover.setVisible(false);
		backCover.setVisible(false);
		bigCanvasWrapper.setVisible(true);
		bigCanvasWrapper.redraw();
	}

	private void turnPageLeft() {
		yearbook.activePage -= 2;
		if (yearbook.activePage < 0) yearbook.activePage = 0;
		refresh();
		pageTurnSound();
	}

	private void turnPageRight() {
		yearbook.activePage += 2;
		if (yearbook.activePage >= yearbook.size()) yearbook.activePage = yearbook.size() - 1;
		refresh();
		pageTurnSound();
	}
	
	public static void pageTurnSound() {
		try {
			File file = new File("icons/sounds/pageflip.wav");
			playClip(file);
		} catch (IOException | UnsupportedAudioFileException
				| LineUnavailableException
				| InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void playClip(File clipFile) throws IOException, 
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
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
