import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

class BookPanel extends TategakiPanel implements Runnable {
	private static final float DEFAULT_SCROLL_X = 2.0f;
	private static final float BASELINE_POS = 60.0f;
	private static final int INDICATOR_POS = 10;

	private BookData book;
	private int currentLine;
	private int currentSegment;
	private float segmentOffset = 0.0f;  // distance from the center of the panel (minus value)

	private int maxFontSize;
	private int minFontSize;
	private Font[] fonts;

	private int segmentGap;
	private int paragraphGap;
	private float startMaxFontSizeArea;
	private float endMaxFontSizeArea;

	private int firstDownX;
	private int mouseDownX;
	private Date lastDraggedDate;
	private float scrollX = DEFAULT_SCROLL_X;

	private Thread thread;
	private static final int INTERVAL_TIME = 10;


	BookPanel() {
		this(false);
	}

	BookPanel(boolean isVertical) {
		super();
		setFocusable(true);
		setBackground(Color.LIGHT_GRAY);

		// get parameters
		maxFontSize = InfiniteScrollReader.maxFontSize;
		minFontSize = InfiniteScrollReader.minFontSize;
		segmentGap = InfiniteScrollReader.segmentGap;
		paragraphGap = InfiniteScrollReader.paragraphGap;
		startMaxFontSizeArea = InfiniteScrollReader.startMaxFontSizeArea;
		endMaxFontSizeArea = InfiniteScrollReader.endMaxFontSizeArea;

		fonts = new Font[maxFontSize - minFontSize + 1];
		for (int size = 0; size < fonts.length; size++) {
			fonts[size] = new Font(InfiniteScrollReader.fontname, Font.PLAIN, minFontSize + size);
		}

		MouseAdapter mouseAdapter = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getY() < INDICATOR_POS) {
				// indicator
					setCurrentPos(e.getX());
				} else {
					lastDraggedDate = new Date();
					mouseDownX = e.getX();
					firstDownX = mouseDownX;
				}
			}

			public void mouseDragged(MouseEvent e) {
				if (e.getY() < INDICATOR_POS) {
				// indicator
					setCurrentPos(e.getX());
				} else {
					segmentOffset -= mouseDownX - e.getX();
					mouseDownX = e.getX();
					repaint();
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.getY() < INDICATOR_POS) {
				// indicator
					setCurrentPos(e.getX());
				} else {
					Date currentDate = new Date();
					long deltaT = currentDate.getTime() - lastDraggedDate.getTime();
					scrollX = (float)(firstDownX - e.getX()) / (float)deltaT * INTERVAL_TIME;
					System.out.println("mouseReleased, scrollX = " + scrollX + ", deltaT = " + deltaT);
					if (thread == null)  start();
				}
			}
		};
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);

		addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				switch (e.getKeyChar()) {
				case ' ':
					if (thread == null) {
						start();
					} else {
						stop();
					}
					break;
				case '-':
				case '_':
					scrollX -= 0.25;
					break;
				case '=':
				case '+':
					scrollX += 0.25;
					break;
				case '0':
					scrollX = DEFAULT_SCROLL_X;
					break;
				default:
					// ignore it
				} 
			}
		});
	}

	void setBook(BookData book) {
		this.book = book;
		currentLine = 0;
		currentSegment = 0;
		segmentOffset = 0;
		repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (book == null)  return;

		int panelWidth = getSize().width;
		int newCurrentLine = currentLine;
		int newCurrentSegment = currentSegment;
		float newSegmentOffset = segmentOffset;

		int startWords, endWords;
		startWords = endWords = book.getWordCount(currentLine) + currentSegment;

		g.setColor(Color.BLACK);

		// draw forward
		int line = currentLine;
		int seg = currentSegment;
		float offset = segmentOffset + panelWidth / 2;
		forwardLoop: while(true) {
			java.util.List<String> segments = book.getSegments(line);
			if (segments == null)  break;

			for (; seg < segments.size(); seg++) {
				String s = segments.get(seg);
				float width = drawSegment((Graphics2D)g, s, offset, true);
				endWords++;
				offset += width + segmentGap;
				if (offset > panelWidth)  break forwardLoop;
				if (offset < panelWidth / 2) {
					newCurrentSegment++;
					if (newCurrentSegment >= segments.size()) {
						newCurrentLine++;
						newCurrentSegment = 0;
						newSegmentOffset += width + paragraphGap;
					} else {
						newSegmentOffset += width + segmentGap;
					}
				}
			}

			offset += paragraphGap - segmentGap;
			line++;
			seg = 0;
		}

		// draw backward
		line = currentLine;
		seg = currentSegment - 1;
		if (seg < 0)  line--;
		offset = segmentOffset - segmentGap + panelWidth / 2;
		backwardLoop: while(true) {
			if (line < 0)  break;

			java.util.List<String> segments = book.getSegments(line);
			if (segments == null)  break;
			if (seg < 0) {
				seg = segments.size() - 1;
				offset += segmentGap - paragraphGap;
			}

			for (; seg >= 0; seg--) {
				String s = segments.get(seg);
				float width = drawSegment((Graphics2D)g, s, offset, false);
				startWords--;
				offset -= width + segmentGap;
				if (offset < 0)  break backwardLoop;
				if (offset > panelWidth / 2) {
					newCurrentSegment--;
					if (newCurrentSegment < 0) {
						newCurrentLine--;
						if (newCurrentLine < 0) {
							newCurrentLine = 0;
							newCurrentSegment = 0;
						} else {
							newCurrentSegment = book.getSegments(newCurrentLine).size() - 1;
						}
						newSegmentOffset -= width + paragraphGap;
					} else {
						newSegmentOffset -= width + segmentGap;
					}
				}
			}

			offset -= paragraphGap - segmentGap;
			line--;
			seg = -1;
		}

		// draw indicator
		g.drawLine(0, INDICATOR_POS, panelWidth, INDICATOR_POS);
		float numWords = (float)book.getNumWords();
		int start = (int)((float)panelWidth * (float)startWords / numWords);
		int end = (int)((float)panelWidth * (float)endWords / numWords);
		g.fillRect(start, 0, end - start, INDICATOR_POS);


		// draw bookmark (dummy)
		/*
		g.setColor(Color.RED);
		start = (int)((float)panelWidth * 0.4f);
		g.drawLine(start, 0, start, INDICATOR_POS);
		start = (int)((float)panelWidth * 0.8f);
		g.drawLine(start, 0, start, INDICATOR_POS);
		start = (int)((float)panelWidth * 0.3f);
		g.drawLine(start, 0, start, INDICATOR_POS);
		*/


		currentLine = newCurrentLine;
		currentSegment = newCurrentSegment;
		segmentOffset = newSegmentOffset;
	}

	private float drawSegment(Graphics2D g, String segment, float x, boolean isForward) {
		Font font = getFont(x);
		g.setFont(font);
		Rectangle2D bounds = font.getStringBounds(segment, g.getFontRenderContext());
		float width = (float)(bounds.getWidth());

		if (isForward) {
			g.drawString(segment, x, BASELINE_POS);
		} else {
			g.drawString(segment, x - width, BASELINE_POS);
		}

		return width;
	}

	private Font getFont(float x) {
		int width = getSize().width;
		float leftBound = width * startMaxFontSizeArea;
		float rightBound = width * endMaxFontSizeArea;

		if (x < leftBound) {
			int size = (int)(maxFontSize - (float)(maxFontSize - minFontSize) * (leftBound - x) / leftBound);
			if (size < minFontSize)  return fonts[0];
			return fonts[size - minFontSize];
		} else if (x > rightBound) {
			int size = (int)(minFontSize + (float)(maxFontSize - minFontSize) * (width - x) / (width - rightBound));
			if (size < minFontSize)  return fonts[0];
			return fonts[size - minFontSize];
		} else {
			return fonts[maxFontSize - minFontSize];
		}
	}

	void setCurrentPos(int x) {
		float pos = (float)x / (float)getSize().width;
		int words = (int)(book.getNumWords() * pos);
		int line = 0;
		while (words > book.getWordCount(line))  line++;
		line--;
		int segment = words - book.getWordCount(line);
		currentLine = line;
		currentSegment = segment;
		segmentOffset = 0.0f;
		repaint();
	}


	void start() {
		thread = new Thread(this);
		thread.start();
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while (thisThread == thread) {
			try {
				segmentOffset -= scrollX;
				repaint();
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
}
