import java.awt.*;

class BookFonts {
	private int minSize;
	private int maxSize;
	private Font[] fonts;

	BookFonts(String fontname, int minSize, int maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		fonts = new Font[maxSize - minSize + 1];
		for (int i = 0; i < fonts.length; i++) {
			fonts[i] = new Font(fontname, Font.PLAIN, minSize + i);
		}
	}

	Font getFont(int size) {
		size -= minSize;
		if (size < 0)  size = 0;
		if (size >= fonts.length)  size = fonts.length - 1;
		return fonts[size];
	}

	int getMinSize()  {return minSize;}
	int getMaxSize()  {return maxSize;}
}
