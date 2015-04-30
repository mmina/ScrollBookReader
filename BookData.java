import java.util.*;
import java.io.*;

class BookData {
	private List<BookLine> book;
	private int numWords;
	private List<Integer> wordCount;

	BookData() {
		book = new ArrayList<BookLine>();
		wordCount = new ArrayList<Integer>();
		wordCount.add(new Integer(0));
	}

	List<BookLine> getBook()  {return book;}
	int size()  {return book.size();}

	
	int getNumWords()  {return numWords;}

	String getLine(int index) {
		BookLine line = book.get(index);
		if (line != null) return line.getLine();
		return null;
	}

	BookLine getBookLine(int index) {
		BookLine line = book.get(index);
		return line;
	}

	int getWordCount(int index) {
		Integer value = wordCount.get(index);
		if (value != null) {
			return value.intValue();
		} else {
			return 0;
		}
	}

	List<String> getSegments(int index) {
		try {
			BookLine line = book.get(index);
			if (line != null) return line.getSegments();
			return null;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	void readBook(String filename) {
		BufferedReader in = null;
		try {
			/*
			in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "SJIS"));
			*/
			in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				/* assumed to be preprocessed
				// currently, cut off ruby data
				str = str.replaceAll("《.*?》", "");
				str = str.replaceAll("｜", "");
				*/
				book.add(new BookLine(str));
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (in != null)  in.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		// count words
		for (BookLine line : book) {
			int num = line.getNumSegments();
			numWords += num;
			wordCount.add(new Integer(numWords));
		}

		System.out.println(book.size() + "lines read");
	}

	static public void main(String[] args) {
		BookData bookdata = new BookData();
		bookdata.readBook(args[0]);
		int lines = bookdata.size();
		for (int i = 0; i < lines; i++)  System.out.println(bookdata.getLine(i));
	}
}
