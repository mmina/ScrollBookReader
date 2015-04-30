import java.util.*;
import java.io.*;

class BookPreprocessor {
	static private final ProcessBuilder CABOCHA_PROCESS = new ProcessBuilder("cabocha", "-f1", "-n1");

	private List<String> book;

	BookPreprocessor() {
		book = new LinkedList<String>();
	}

	List<String> getBook()  {return book;}
	int size()  {return book.size();}

	String getLine(int index) {
		return book.get(index);
	}

	void processBook(String filename) {
		BufferedReader in = null;
		Process cabocha = null;
		BufferedReader cabochaIn = null;
		BufferedWriter cabochaOut = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "SJIS"));
			cabocha = CABOCHA_PROCESS.start();
			cabochaIn = new BufferedReader(new InputStreamReader(cabocha.getInputStream()));
			cabochaOut = new BufferedWriter(new OutputStreamWriter(cabocha.getOutputStream()));
			String str;
			while ((str = in.readLine()) != null) {
				// currently, cut off ruby data
				str = str.replaceAll("《.*?》", "");
				str = str.replaceAll("｜", "");

				cabochaOut.write(str);
				cabochaOut.newLine();
				cabochaOut.flush();
				String result;
				List<String> segments = new LinkedList<String>();
				StringBuilder sb = null;
				while (!(result = cabochaIn.readLine()).equals("EOS")) {
					if (result.charAt(0) == '*') {
						if (sb == null) {
							sb = new StringBuilder();
						} else {
							segments.add(sb.toString());
							sb = new StringBuilder();
						}
						continue;
					}
					String[] fields = result.split("[\t,]");
					sb.append(fields[0]);
				}
				if (sb != null)  segments.add(sb.toString());

				sb = new StringBuilder();
				for (String s : segments) {
					sb.append(s);
					sb.append(" ");
				}
				book.add(sb.toString());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (in != null)  in.close();
				if (cabochaIn != null)  cabochaIn.close();
				if (cabochaOut != null)  cabochaOut.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			cabocha.destroy();
		}
	}

	static public void main(String[] args) {
		BookPreprocessor preprocessor = new BookPreprocessor();
		preprocessor.processBook(args[0]);
		for (String line : preprocessor.getBook()) {
			System.out.println(line);
		}
	}
}
