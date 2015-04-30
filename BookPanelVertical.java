import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

class BookPanelVertical extends TategakiPanel implements Runnable {
	private static final float DEFAULT_SCROLL_Y = 2.0f;
	private static final float BASELINE_POS = 60.0f;
	private static final int INDICATOR_WIDTH = 10;

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
	private int indicatorPos = 80;

	private int firstDownY;
	private int mouseDownY;
	private Date lastDraggedDate;
	private float scrollY = DEFAULT_SCROLL_Y;

	private Thread thread;
	private static final int INTERVAL_TIME = 10;


	BookPanelVertical() {
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
				if (e.getX() > indicatorPos) {
				// indicator
					setCurrentPos(e.getY());
				} else {
					lastDraggedDate = new Date();
					mouseDownY = e.getY();
					firstDownY = mouseDownY;
				}
			}

			public void mouseDragged(MouseEvent e) {
				if (e.getX() > indicatorPos) {
				// indicator
					setCurrentPos(e.getY());
				} else {
					segmentOffset -= mouseDownY - e.getY();
					mouseDownY = e.getY();
					repaint();
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.getX() > indicatorPos) {
				// indicator
					setCurrentPos(e.getY());
				} else {
					Date currentDate = new Date();
					long deltaT = currentDate.getTime() - lastDraggedDate.getTime();
					scrollY = (float)(firstDownY - e.getY()) / (float)deltaT * INTERVAL_TIME;
					System.out.println("mouseReleased, scrollY = " + scrollY + ", deltaT = " + deltaT);
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
					scrollY -= 0.25;
					break;
				case '=':
				case '+':
					scrollY += 0.25;
					break;
				case '0':
					scrollY = DEFAULT_SCROLL_Y;
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
		int panelHeight = getSize().height;
		int newCurrentLine = currentLine;
		int newCurrentSegment = currentSegment;
		float newSegmentOffset = segmentOffset;

		indicatorPos = panelWidth - INDICATOR_WIDTH;

		int startWords, endWords;
		startWords = endWords = book.getWordCount(currentLine) + currentSegment;

		g.setColor(Color.BLACK);

		// draw forward
		int line = currentLine;
		int seg = currentSegment;
		float offset = segmentOffset + panelHeight / 2;
		forwardLoop: while(true) {
			java.util.List<String> segments = book.getSegments(line);
			if (segments == null)  break;

			for (; seg < segments.size(); seg++) {
				String s = segments.get(seg);
				float height = drawSegment((Graphics2D)g, s, offset, true);
				endWords++;
				offset += height + segmentGap;
				if (offset > panelHeight)  break forwardLoop;
				if (offset < panelHeight / 2) {
					newCurrentSegment++;
					if (newCurrentSegment >= segments.size()) {
						newCurrentLine++;
						newCurrentSegment = 0;
						newSegmentOffset += height + paragraphGap;
					} else {
						newSegmentOffset += height + segmentGap;
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
		offset = segmentOffset - segmentGap + panelHeight / 2;
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
				float height = drawSegment((Graphics2D)g, s, offset, false);
				startWords--;
				offset -= height + segmentGap;
				if (offset < 0)  break backwardLoop;
				if (offset > panelHeight / 2) {
					newCurrentSegment--;
					if (newCurrentSegment < 0) {
						newCurrentLine--;
						if (newCurrentLine < 0) {
							newCurrentLine = 0;
							newCurrentSegment = 0;
						} else {
							newCurrentSegment = book.getSegments(newCurrentLine).size() - 1;
						}
						newSegmentOffset -= height + paragraphGap;
					} else {
						newSegmentOffset -= height + segmentGap;
					}
				}
			}

			offset -= paragraphGap - segmentGap;
			line--;
			seg = -1;
		}

		// draw indicator
		g.drawLine(indicatorPos, 0, indicatorPos, panelHeight);
		float numWords = (float)book.getNumWords();
		int start = (int)((float)panelHeight * (float)startWords / numWords);
		int end = (int)((float)panelHeight * (float)endWords / numWords);
		g.fillRect(indicatorPos, start, panelWidth, end - start);


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

	private float drawSegment(Graphics2D g, String segment, float y, boolean isForward) {
		Font font = getFont(y);
		g.setFont(font);
		/* 
		Rectangle2D bounds = font.getStringBounds(segment, g.getFontRenderContext());
		float width = (float)(bounds.getWidth());
		*/
		float height = 0;

		if (isForward) {
			height = drawVerticalString(g, segment, BASELINE_POS, y);
		} else {
			height = drawVerticalString(g, segment, BASELINE_POS, y, true);
			drawVerticalString(g, segment, BASELINE_POS, y - height);
		}

		return height;
	}

	private Font getFont(float y) {
		int height = getSize().height;
		float topBound = height * startMaxFontSizeArea;
		float bottomBound = height * endMaxFontSizeArea;

		if (y < topBound) {
			int size = (int)(maxFontSize - (float)(maxFontSize - minFontSize) * (topBound - y) / bottomBound);
			if (size < minFontSize)  return fonts[0];
			return fonts[size - minFontSize];
		} else if (y > bottomBound) {
			int size = (int)(minFontSize + (float)(maxFontSize - minFontSize) * (height - y) / (height - bottomBound));
			if (size < minFontSize)  return fonts[0];
			return fonts[size - minFontSize];
		} else {
			return fonts[maxFontSize - minFontSize];
		}
	}

	void setCurrentPos(int y) {
		float pos = (float)y / (float)getSize().height;
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
				segmentOffset -= scrollY;
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
