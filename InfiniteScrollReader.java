import java.util.*;
import java.io.*;
import javax.swing.*;

class InfiniteScrollReader {
	// for properties
	static private Properties	properties;
	static private final String	PROPERTYFILE = "InfiniteScrollReader.prop";
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

	static public String	fontname = DEFAULT_FONTNAME;
	static public int	maxFontSize = Integer.parseInt(DEFAULT_MAXFONTSIZE);
	static public int	minFontSize = Integer.parseInt(DEFAULT_MINFONTSIZE);
	static public int	segmentGap = Integer.parseInt(DEFAULT_SEGMENTGAP);
	static public int	paragraphGap = Integer.parseInt(DEFAULT_PARAGRAPHGAP);
	static public float	startMaxFontSizeArea = Float.parseFloat(DEFAULT_STARTMAXFONTSIZEAREA);
	static public float	endMaxFontSizeArea = Float.parseFloat(DEFAULT_ENDMAXFONTSIZEAREA);
	static public boolean	isVertical = DEFAULT_DIRECTION.equals("vertical");


	static private InfiniteScrollReader _instance = new InfiniteScrollReader();
	private BookPanel bookPanel;
	private BookPanelVertical bookPanelVertical;
	
	private InfiniteScrollReader() {
		// load properties
		properties = new Properties();
		BufferedInputStream in = null;
		int width = 0;
		int height = 0;
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
			isVertical = properties.getProperty(PROP_DIRECTION, DEFAULT_DIRECTION).equals("vertical");
			width = Integer.parseInt(properties.getProperty(PROP_WIDTH, DEFAULT_WIDTH));
			height = Integer.parseInt(properties.getProperty(PROP_HEIGHT, DEFAULT_HEIGHT));
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

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (isVertical) {
			frame.setSize(height, width);
			bookPanelVertical = new BookPanelVertical();
			frame.add(bookPanelVertical);
		} else {
			frame.setSize(width, height);
			bookPanel = new BookPanel();
			//bookPanel.setBounds(20, 10, 600, 80);
			frame.add(bookPanel);
		}
		// frame.setLayout(null);


		frame.setVisible(true);
	}

	public static InfiniteScrollReader getInstance()  {return _instance;}

	void setBook(BookData bookdata) {
		if (isVertical) {
			bookPanelVertical.setBook(bookdata);
		} else {
			bookPanel.setBook(bookdata);
		}
	}

	static public void main(String[] args) {
		InfiniteScrollReader reader = InfiniteScrollReader.getInstance();
		BookData bookdata = new BookData();
		bookdata.readBook(args[0]);
		reader.setBook(bookdata);
	}
}
