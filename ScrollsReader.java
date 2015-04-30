import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class ScrollsReader implements MouseListener, MouseMotionListener, Runnable {
	// for properties
	static private Properties	properties;
	static private final String	PROPERTYFILE = "ScrollsReader.prop";
	static private final String	PROP_WIDTH = "width";
	static private final String	DEFAULT_WIDTH = "1136";
	static private final String	PROP_HEIGHT = "height";
	static private final String	DEFAULT_HEIGHT = "640";
	static private final String	PROP_FONTNAME = "font";
	static private final String	DEFAULT_FONTNAME = "Serif";
	static private final String	PROP_MAXFONTSIZE = "maxFontSize";
	static private final String	DEFAULT_MAXFONTSIZE = "32";
	static private final String	PROP_MINFONTSIZE = "minFontSize";
	static private final String	DEFAULT_MINFONTSIZE = "10";
	static private final String	PROP_SEGMENTGAP = "segmentGap";
	static private final String	DEFAULT_SEGMENTGAP = "20";
	static private final String	PROP_PARAGRAPHGAP = "paragraphGap";
	static private final String	DEFAULT_PARAGRAPHGAP = "100";
	static private final String	PROP_STARTMAXFONTSIZEAREA = "startMaxFontSizeArea";
	static private final String	DEFAULT_STARTMAXFONTSIZEAREA = "0.3";
	static private final String	PROP_ENDMAXFONTSIZEAREA = "endMaxFontSizeArea";
	static private final String	DEFAULT_ENDMAXFONTSIZEAREA = "0.7";
	static private final String	PROP_DIRECTION = "direction";
	static private final String	DEFAULT_DIRECTION = "horizontal";	// or vertical
	static private final String	PROP_NUMPANELS = "numPanels";
	static private final String	DEFAULT_NUMPANELS = "5";
	static private final String	PROP_PANELSIZE = "panelSize";
	static private final String	DEFAULT_PANELSIZE = "80";


	static public String	fontname = DEFAULT_FONTNAME;
	static public int	maxFontSize = Integer.parseInt(DEFAULT_MAXFONTSIZE);
	static public int	minFontSize = Integer.parseInt(DEFAULT_MINFONTSIZE);
	static public int	segmentGap = Integer.parseInt(DEFAULT_SEGMENTGAP);
	static public int	paragraphGap = Integer.parseInt(DEFAULT_PARAGRAPHGAP);
	static public float	startMaxFontSizeArea = Float.parseFloat(DEFAULT_STARTMAXFONTSIZEAREA);
	static public float	endMaxFontSizeArea = Float.parseFloat(DEFAULT_ENDMAXFONTSIZEAREA);
	static public boolean	isVertical = DEFAULT_DIRECTION.equals("vertical");

	private JFrame  frame;
	private int  frameWidth, frameHeight;
	private BookFonts  fonts;
	private int  numPanels;
	private int  panelSize;
	private int  panelGap;
	private int  panelOffset = 0;
	private int  basePanelOffset = 0;
	private ArrayList<ParagraphPanel>  bookPanels;
	private int  paragraphNo;  // paragraph number for the first element of the bookPanels
	private int  dragPos;
	private Thread  thread;
	private static final int  INTERVAL_TIME = 10;

	private int  autoMoving = 0;  // 0:not moving, 1:up or right, -1:down or left
	private float  scrollSpeed = 0;

	private BookData  bookdata;

	static private ScrollsReader _instance = new ScrollsReader();
	
	private ScrollsReader() {
		// load properties
		properties = new Properties();
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(PROPERTYFILE));
			properties.load(in);
			fontname = properties.getProperty(PROP_FONTNAME, DEFAULT_FONTNAME);
			maxFontSize = Integer.parseInt(properties.getProperty(PROP_MAXFONTSIZE, DEFAULT_MAXFONTSIZE));
			minFontSize = Integer.parseInt(properties.getProperty(PROP_MINFONTSIZE, DEFAULT_MINFONTSIZE));
			segmentGap = Integer.parseInt(properties.getProperty(PROP_SEGMENTGAP, DEFAULT_SEGMENTGAP));
			paragraphGap = Integer.parseInt(properties.getProperty(PROP_PARAGRAPHGAP, DEFAULT_PARAGRAPHGAP));
			startMaxFontSizeArea = Float.parseFloat(properties.getProperty(PROP_STARTMAXFONTSIZEAREA, DEFAULT_STARTMAXFONTSIZEAREA));
			endMaxFontSizeArea = Float.parseFloat(properties.getProperty(PROP_ENDMAXFONTSIZEAREA, DEFAULT_ENDMAXFONTSIZEAREA));
			isVertical = !(properties.getProperty(PROP_DIRECTION, DEFAULT_DIRECTION).equals(DEFAULT_DIRECTION));
			frameWidth = Integer.parseInt(properties.getProperty(PROP_WIDTH, DEFAULT_WIDTH));
			frameHeight = Integer.parseInt(properties.getProperty(PROP_HEIGHT, DEFAULT_HEIGHT));
			numPanels = Integer.parseInt(properties.getProperty(PROP_NUMPANELS, DEFAULT_NUMPANELS));
			panelSize = Integer.parseInt(properties.getProperty(PROP_PANELSIZE, DEFAULT_PANELSIZE));
		} catch (IOException e) {
			System.err.println("loading property file error.");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					System.err.println("file close error???");
				}
			}
		}

		fonts = new BookFonts(fontname, minFontSize, maxFontSize);

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (isVertical) {
			frame.setSize(frameHeight, frameWidth);
		} else {
			frame.setSize(frameWidth, frameHeight);
		}
		frame.setLayout(null);

		bookPanels = new ArrayList<ParagraphPanel>();
		panelGap = (frameHeight - panelSize * numPanels) / (numPanels + 1);
		int nextPos = panelOffset = basePanelOffset = isVertical ? frameHeight - panelSize - panelGap : panelGap;
		for (int i = 0; i < numPanels; i++) {
			ParagraphPanel panel = new ParagraphPanel(isVertical, fonts);
			if (isVertical) {
				panel.setBounds(nextPos, 0, panelSize, frameWidth);
				nextPos -= panelGap + panelSize;
			} else {
				panel.setBounds(0, nextPos, frameWidth, panelSize);
				nextPos += panelGap + panelSize;
			}
			frame.add(panel);
			if (i == numPanels / 2)  panel.setScrollable(true);
			if (numPanels % 2 == 0 && i == numPanels / 2 - 1)  panel.setScrollable(true);
			bookPanels.add(panel);
		}

		frame.addMouseListener(this);
		frame.addMouseMotionListener(this);

		frame.setVisible(true);
	}

	// event listeners for frame
	public void mouseClicked(MouseEvent e) {
		// empty
	}

	public void mouseEntered(MouseEvent e) {
		// empty
	}

	public void mouseExited(MouseEvent e) {
		// empty
	}

	public void mousePressed(MouseEvent e) {
		dragPos = isVertical ? e.getX() : e.getY();
	}

	public void mouseReleased(MouseEvent e) {
		if (autoMoving != 0)  return;

		if (isVertical) {
			panelOffset = bookPanels.get(0).getX();
		} else {
			panelOffset = bookPanels.get(0).getY();
		}
		start();
	}

	public void mouseDragged(MouseEvent e) {
		if (autoMoving != 0)  return;

		int pos = isVertical ? e.getX() : e.getY();
		panelOffset += pos - dragPos;
		// System.out.println("panelOffset = " + panelOffset);
		adjustPanels();
		dragPos = pos;
	}

	private void adjustPanels() {
		// adjust existing panels
		int nextPos = panelOffset;
		ArrayList<ParagraphPanel>  toBeDeleted = new ArrayList<ParagraphPanel>();
		for (ParagraphPanel panel : bookPanels) {
			Point pnt = panel.getLocation();
			if (isVertical) {
				if (nextPos > frameHeight) {
					frame.remove(panel);
					toBeDeleted.add(panel);
					paragraphNo++;
					nextPos -= panelGap + panelSize;
					panelOffset -= panelGap + panelSize;
					continue;
				} else if (nextPos < -panelSize) {
					frame.remove(panel);
					toBeDeleted.add(panel);
					nextPos -= panelGap + panelSize;
					continue;
				} else {
					pnt.x = nextPos;
					nextPos -= panelGap + panelSize;
				}
			} else {
				if (nextPos < -panelSize) {
					frame.remove(panel);
					toBeDeleted.add(panel);
					paragraphNo++;
					nextPos += panelGap + panelSize;
					panelOffset += panelGap + panelSize;
					continue;
				} else if (nextPos > frameHeight) {
					frame.remove(panel);
					toBeDeleted.add(panel);
					nextPos += panelGap + panelSize;
					continue;
				} else {
					pnt.y = nextPos;
					nextPos += panelGap + panelSize;
				}
			}
			panel.setLocation(pnt);
		}
		for (ParagraphPanel panel : toBeDeleted) {
			bookPanels.remove(panel);
		}

		// add panels
		if (isVertical) {
			for (; nextPos >= -panelSize; nextPos -= panelGap + panelSize) {
				ParagraphPanel panel = new ParagraphPanel(isVertical, fonts);
				panel.setBounds(nextPos, 0, panelSize, frameWidth);
				panel.setParagraph(bookdata.getBookLine(paragraphNo + bookPanels.size()));
				frame.add(panel);
				bookPanels.add(panel);
			}
			for (nextPos = panelOffset + panelSize + panelGap; nextPos < frameHeight; nextPos += panelSize + panelGap) {
				if (paragraphNo <= 0)  break;
				paragraphNo--;
				ParagraphPanel panel = new ParagraphPanel(isVertical, fonts);
				panel.setBounds(nextPos, 0, panelSize, frameWidth);
				panel.setParagraph(bookdata.getBookLine(paragraphNo));
				frame.add(panel);
				bookPanels.add(0, panel);
				panelOffset += panelSize + panelGap;
			}
		} else {
			for (; nextPos < frameHeight; nextPos += panelGap + panelSize) {
				ParagraphPanel panel = new ParagraphPanel(isVertical, fonts);
				panel.setBounds(0, nextPos, frameWidth, panelSize);
				panel.setParagraph(bookdata.getBookLine(paragraphNo + bookPanels.size()));
				frame.add(panel);
				bookPanels.add(panel);
			}
			for (nextPos = panelOffset - panelSize - panelGap; nextPos > -panelSize; nextPos -= panelSize + panelGap) {
				if (paragraphNo <= 0)  break;
				paragraphNo--;
				ParagraphPanel panel = new ParagraphPanel(isVertical, fonts);
				panel.setBounds(0, nextPos, frameWidth, panelSize);
				panel.setParagraph(bookdata.getBookLine(paragraphNo));
				frame.add(panel);
				bookPanels.add(0, panel);
				panelOffset -= panelSize + panelGap;
			}
		}

		int i = 0;
		for (ParagraphPanel panel : bookPanels) {
			if (i == bookPanels.size() / 2) {
				panel.setScrollable(true);
			} else if (bookPanels.size() % 2 == 0 && i == bookPanels.size() / 2 - 1) {
				panel.setScrollable(true);
			} else {
				panel.setScrollable(false);
				panel.stop();
			}
			i++;
		}

		frame.repaint();
	}

	public void mouseMoved(MouseEvent e) {
		// empty
	}

	void goNextParagraph(float scrollSpeed) {
		// System.out.println("go next.");
		this.scrollSpeed = scrollSpeed;
		autoMoving = 1;
		if (isVertical) {
			panelOffset += 3;
		} else {
			panelOffset -= 3;
		}
		adjustPanels();
		start();
	}

	void start() {
		thread = new Thread(this);
		thread.start();
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while (thisThread == thread) {
			try {
				int imgPanelOffset = panelOffset;
				if (isVertical) {
					while (imgPanelOffset < basePanelOffset) {
						imgPanelOffset += panelSize + panelGap;
					}
				} else {
					while (imgPanelOffset > basePanelOffset) {
						imgPanelOffset -= panelSize + panelGap;
					}
				}

				switch(autoMoving) {
				case 0:
					// System.out.println("panelOffset = " + panelOffset + ", basePanelOffset = " + basePanelOffset);
					if (isVertical) {
						panelOffset += (imgPanelOffset - (panelSize + panelGap) / 2 < basePanelOffset ? -1 : 1);
					} else {
						panelOffset += (imgPanelOffset + (panelSize + panelGap) / 2 < basePanelOffset ? -1 : 1);
					}
					break;
				case 1:
					// System.out.println("panelOffset = " + panelOffset + ", basePanelOffset = " + basePanelOffset);
					if (isVertical) {
						if (panelOffset == basePanelOffset + 1) {
							panelOffset = basePanelOffset;
							imgPanelOffset = basePanelOffset;
							break;
						}
						panelOffset++;
					} else {
						if (panelOffset == basePanelOffset - 1) {
							panelOffset = basePanelOffset;
							imgPanelOffset = basePanelOffset;
							break;
						}
						panelOffset--;
					}
					break;
				case -1:
					if (isVertical) {
						panelOffset--;
					} else {
						panelOffset++;
					}
					break;
				}

				if (imgPanelOffset == basePanelOffset) {
					// System.out.println("stop moving panels");
					stop();
					if (autoMoving != 0) {
						ParagraphPanel panel = bookPanels.get(numPanels / 2);
						panel.setScrollSpeed(scrollSpeed);
						panel.start();
					}
					autoMoving = 0;
					break;
				}
				adjustPanels();
				Thread.sleep(INTERVAL_TIME);
			} catch (InterruptedException ie) {
				thread = null;
				break;
			}
		}
	}

	void stop() {
		thread = null;
	}

	public static ScrollsReader getInstance()  {return _instance;}

	void setBook(BookData bookdata) {
		this.bookdata = bookdata;
		for (int i = 0; i < numPanels; i++) {
			ParagraphPanel panel = bookPanels.get(i);
			panel.setParagraph(bookdata.getBookLine(i));
		}
	}

	static public void main(String[] args) {
		ScrollsReader reader = ScrollsReader.getInstance();
		BookData bookdata = new BookData();
		bookdata.readBook(args[0]);
		reader.setBook(bookdata);
	}
}
