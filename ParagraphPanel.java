import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

class ParagraphPanel extends TategakiPanel implements Runnable {
	private static final float DEFAULT_SCROLL_SPEED = 2.0f;
	private static final float BASELINE_POS_H = 60.0f;
	private static final float BASELINE_POS_V = 35.0f;
	private static final int INDICATOR_POS = 10;
	private static final int INDICATOR_WIDTH = 10;

	private BookLine line;
	private int currentSegment;
	private float segmentOffset = 0.0f;  // distance from the center of the panel (minus value)

	private int maxFontSize;
	private int minFontSize;
	private BookFonts fonts;
	
	private float baselinePosH;
	private float baselinePosV;

	private int segmentGap;
	private int paragraphGap;
	private float startMaxFontSizeArea;
	private float endMaxFontSizeArea;
	private int indicatorPos = INDICATOR_POS;

	private int firstDownPos;
	private int mouseDownPos;
	private Date lastDraggedDate;
	private float scrollSpeed = DEFAULT_SCROLL_SPEED;

	private boolean isVertical;

	private boolean scrollable;

	private Thread thread;
	private static final int INTERVAL_TIME = 10;


	ParagraphPanel(boolean isV, BookFonts fonts) {
		super();
		setFocusable(true);
		// setBackground(Color.LIGHT_GRAY);
		setBackground(Color.WHITE);

		// get parameters
		segmentGap = ScrollsReader.segmentGap;
		paragraphGap = ScrollsReader.paragraphGap;
		startMaxFontSizeArea = ScrollsReader.startMaxFontSizeArea;
		endMaxFontSizeArea = ScrollsReader.endMaxFontSizeArea;

		this.fonts = fonts;
		maxFontSize = fonts.getMaxSize();
		minFontSize = fonts.getMinSize();

		this.isVertical = isV;

		MouseAdapter mouseAdapter = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int pos = isVertical ? e.getY() : e.getX();
				if ((isVertical && (e.getX() > indicatorPos)) ||
				    (!isVertical && (e.getY() < indicatorPos))) {
				// indicator
					setCurrentPos(pos);
				} else {
					lastDraggedDate = new Date();
					mouseDownPos = pos;
					firstDownPos = mouseDownPos;
				}
			}

			public void mouseDragged(MouseEvent e) {
				int pos = isVertical ? e.getY() : e.getX();
				if ((isVertical && (e.getX() > indicatorPos)) ||
				    (!isVertical && (e.getY() < indicatorPos))) {
				// indicator
					setCurrentPos(pos);
				} else {
					segmentOffset -= mouseDownPos - pos;
					mouseDownPos = pos;
					repaint();
				}
			}

			public void mouseReleased(MouseEvent e) {
				int pos = isVertical ? e.getY() : e.getX();
				if ((isVertical && (e.getX() > indicatorPos)) ||
				    (!isVertical && (e.getY() < indicatorPos))) {
				// indicator
					setCurrentPos(pos);
				} else {
					Date currentDate = new Date();
					long deltaT = currentDate.getTime() - lastDraggedDate.getTime();
					scrollSpeed = (float)(firstDownPos - pos) / (float)deltaT * INTERVAL_TIME;
					System.out.println("mouseReleased, scrollSpeed = " + scrollSpeed + ", deltaT = " + deltaT);
					if (thread == null)  start();
					if (scrollSpeed == 0)  stop();
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
					scrollSpeed -= 0.25;
					break;
				case '=':
				case '+':
					scrollSpeed += 0.25;
					break;
				case '0':
					scrollSpeed = DEFAULT_SCROLL_SPEED;
					break;
				default:
					// ignore it
				} 
			}
		});
	}

	void setParagraph(BookLine line) {
		this.line = line;
		currentSegment = 0;
		segmentOffset = 0;
		repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (line == null)  return;

		int panelWidth = getSize().width;
		int panelHeight = getSize().height;
		int panelSize = isVertical ? panelHeight : panelWidth;
		int newCurrentSegment = currentSegment;
		float newSegmentOffset = segmentOffset;

		baselinePosH = INDICATOR_WIDTH + (panelHeight - INDICATOR_WIDTH - maxFontSize) / 2.0f + maxFontSize;
		baselinePosV = (panelWidth - INDICATOR_WIDTH) / 2.0f;

		if (isVertical) {
			indicatorPos = panelWidth - INDICATOR_WIDTH;
		}

		int startWords, endWords;
		startWords = endWords = currentSegment;

		g.setColor(Color.BLACK);

		java.util.List<String> segments = line.getSegments();
		if (segments == null)  return;

		float middle = (startMaxFontSizeArea + endMaxFontSizeArea) / 2.0f;

		// draw forward
		int seg = currentSegment;
		float offset = segmentOffset + panelSize * middle;
		for (; seg < segments.size(); seg++) {
			String s = segments.get(seg);
			float size = drawSegment((Graphics2D)g, s, offset, true);
			endWords++;
			offset += size + segmentGap;
			if (offset > panelSize)  break;
			if (offset < panelSize * middle) {
				newCurrentSegment++;
				if (newCurrentSegment < segments.size()) {
					newSegmentOffset += size + segmentGap;
				}
			}
		}

		// draw backward
		seg = currentSegment - 1;
		offset = segmentOffset - segmentGap + panelSize * middle;
		for (; seg >= 0; seg--) {
			String s = segments.get(seg);
			float size = drawSegment((Graphics2D)g, s, offset, false);
			startWords--;
			offset -= size + segmentGap;
			if (offset < 0)  break;
			if (offset > panelSize * middle) {
				newCurrentSegment--;
				if (newCurrentSegment >= 0) {
					newSegmentOffset -= size + segmentGap;
				}
			}
		}

		// draw indicator
		if (isVertical) {
			g.drawLine(indicatorPos, 0, indicatorPos, panelSize);
		} else {
			g.drawLine(0, indicatorPos, panelSize, indicatorPos);
		}
		float numWords = (float)line.getNumSegments();
		if (numWords != 0) {
			int start = (int)((float)panelSize * (float)startWords / numWords);
			int end = (int)((float)panelSize * (float)endWords / numWords);
			if (isVertical) {
				g.fillRect(indicatorPos, start, panelSize, end - start);
			} else {
				g.fillRect(start, 0, end - start, indicatorPos);
			}
		}

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


		currentSegment = newCurrentSegment;
		segmentOffset = newSegmentOffset;

		// System.out.println("currentSegment = " + currentSegment + ", segmentOffset = " + segmentOffset);
		if (thread != null && currentSegment <= 0 && segmentOffset > 0) {
			stop();
		}
		if (thread != null && currentSegment >= line.getNumSegments() - 1 && segmentOffset < 0) {
			stop();
			ScrollsReader.getInstance().goNextParagraph(scrollSpeed);
		}
	}

	private float drawSegment(Graphics2D g, String segment, float pos, boolean isForward) {
		Font font = getFont(pos);
		g.setFont(font);

		float size = 0;

		if (isVertical) {
			if (isForward) {
				size = drawVerticalString(g, segment, baselinePosV, pos);
			} else {
				size = drawVerticalString(g, segment, baselinePosV, pos, true);
				drawVerticalString(g, segment, baselinePosV, pos - size);
			}
			// g.drawLine((int)baselinePosV, 0, (int)baselinePosV, 400);
		} else {
			Rectangle2D bounds = font.getStringBounds(segment, g.getFontRenderContext());
			size = (float)(bounds.getWidth());
			if (isForward) {
				g.drawString(segment, pos, baselinePosH);
			} else {
				g.drawString(segment, pos - size, baselinePosH);
			}
			// g.drawLine(0, (int)baselinePosH, 400, (int)baselinePosH);
		}

		return size;
	}

	private Font getFont(float pos) {
		int size;
		if (isVertical) {
			size = getSize().height;
		} else {
			size = getSize().width;
		}
		float upperBound = size * startMaxFontSizeArea;
		float lowerBound = size * endMaxFontSizeArea;

		if (pos < upperBound) {
			int fontSize = (int)(maxFontSize - (float)(maxFontSize - minFontSize) * (upperBound - pos) / upperBound);
			return fonts.getFont(fontSize);
		} else if (pos > lowerBound) {
			int fontSize = (int)(minFontSize + (float)(maxFontSize - minFontSize) * (size - pos) / (size - lowerBound));
			return fonts.getFont(fontSize);
		} else {
			return fonts.getFont(maxFontSize);
		}
	}

	void setCurrentPos(int p) {
		float size = isVertical ? (float)getSize().height : (float)getSize().width;
		float pos = (float)p / size;
		currentSegment = (int)(line.getNumSegments() * pos);
		segmentOffset = 0.0f;
		repaint();
	}


	void start() {
		if (!scrollable)  return;
		thread = new Thread(this);
		thread.start();
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while (thisThread == thread) {
			try {
				segmentOffset -= scrollSpeed;
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

	void setScrollable(boolean scrollable) {
		this.scrollable = scrollable;
		if (!scrollable)  stop();
	}

	void setScrollSpeed(float speed)  {scrollSpeed = speed;}
}
