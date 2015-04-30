import java.util.*;

/**
	represent one line, including segment information, ruby, etc.
*/
class BookLine {
	private String line;
	private List<String> segments;

	BookLine(String line) {
		this.line = line;
		segments = new ArrayList<String>();
		String[] seg = line.split(" ");
		for (String s : seg)  segments.add(s);
	}

	String getLine()  {return line;}
	List<String> getSegments()  {return segments;}
	int getNumSegments()  {return segments.size();}
}
