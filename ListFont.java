import	java.awt.GraphicsEnvironment;

class ListFont {
	private ListFont() {
		// empty
	}

	static public void main(String[] args) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String fonts[] = ge.getAvailableFontFamilyNames();
		for (int i = 0; i < fonts.length; i++) {
			System.out.println(fonts[i]);
		}
	}
}

