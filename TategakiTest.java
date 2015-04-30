import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

class TategakiTest extends JPanel {

	private static String FONTNAME = "Serif";
	private static int FONTSIZE = 35;
	private static double Y_ORIGIN = 50.0;

	private static final String PUNCTUATIONS = "\u3001\u3002\uFF0C\uFF0E";
		// 、。，．
	private static final double PUNCTUATIONS_SHIFT_X = 0.5;
	private static final double PUNCTUATIONS_SHIFT_Y = -0.5;

	private static final String SMALLKANA = "\u3041\u3043\u3045\u3047\u3049\u3063\u3083\u3085\u3087\u308E\u30A1\u30A3\u30A5\u30A7\u30A9\u30F5\u30F6\u30C3\u30E3\u30E5\u30E7\u30EE";
		// ぁぃぅぇぉっゃゅょゎァィゥェォヵヶッャュョヮ
	private static final double SMALLKANA_SHIFT_X = 0.08;
	private static final double SMALLKANA_SHIFT_Y = -0.04;

	private static final String PARENTHESIS = "\u2014\uFF08\uFF09\uFF5B\uFF5D\u3014\u3015\u3010\u3011\u300A\u300B\u3008\u3009\u300C\u300D\u300E\u300F";
		// —（）｛｝〔〕【】《》〈〉「」『』
	private static final String V_PARENTHESIS = "\uFE31\uFE35\uFE36\uFE37\uFE38\uFE39\uFE3A\uFE3B\uFE3C\uFE3D\uFE3E\uFE3F\uFE40\uFE41\uFE42\uFE43\uFE44";

	private static final char PROLONGED = 'ー';
		// 長音記号
	private static final double PROLONGED_SHIFT_X = -0.9;
	private static final double PROLONGED_SHIFT_Y = -0.15;

	private static final double BASICLATIN_SHIFT_X = -1.0;
	private static final double BASICLATIN_SHIFT_Y = -0.2;


	private String	string;
	private char[]	stringChar;
	private Font	font;


	TategakiTest() {
		super();
		setBackground(Color.LIGHT_GRAY);

		font = new Font(FONTNAME, Font.PLAIN, FONTSIZE);
	}

	void setString(String string) {
		this.string = string;
		stringChar = string.toCharArray();
		repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// int center = getSize().width / 2;
		int center = 100;

		if (string == null)  return;

		Graphics2D g2d = (Graphics2D)g;

		g.setColor(Color.BLACK);
		g.setFont(font);

		double y = Y_ORIGIN;

		char c;
		for (int i = 0; i < stringChar.length; i++) {
			if (isPunctuations(stringChar[i])) {
				// ずらして表示する
				Rectangle2D rect = font.getStringBounds(stringChar, i, i + 1, g2d.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				System.out.println(stringChar[i] + ": w = " + w + ", h = " + h);
				g.drawChars(stringChar, i, 1, (int)(center - w / 2 + w * PUNCTUATIONS_SHIFT_X), (int)(y + h * PUNCTUATIONS_SHIFT_Y));
				y += h;
			} else if (isSmallKana(stringChar[i])) {
				// ずらして表示する。字体によっては調整しきれていない？
				Rectangle2D rect = font.getStringBounds(stringChar, i, i + 1, g2d.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				System.out.println(stringChar[i] + ": w = " + w + ", h = " + h);
				g.drawChars(stringChar, i, 1, (int)(center - w / 2 + w * SMALLKANA_SHIFT_X), (int)(y + h * SMALLKANA_SHIFT_Y));
				y += h;
			} else if ((c = isParenthesis(stringChar[i])) != 0) {
				// Unicodeに縦書き文字があるので置き換える
				char[] c_array = new char[1];
				c_array[0] = c;
				Rectangle2D rect = font.getStringBounds(c_array, 0, 1, g2d.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				System.out.println(c + ": w = " + w + ", h = " + h);
				g.drawChars(c_array, 0, 1, (int)(center - w / 2), (int)y);
				y += h;
			} else if (stringChar[i] == PROLONGED) {
				// 長音記号は反転＆90度回転させてすこしずらす
				Rectangle2D rect = font.getStringBounds(stringChar, i, i + 1, g2d.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				System.out.println(stringChar[i] + ": w = " + w + ", h = " + h);
				AffineTransform tx = g2d.getTransform();
				System.out.println("x = " + center + ", y = " + (y + h / 2.0));

				g2d.translate(center, y + h / 2.0);
				g2d.scale(-1.0, 1.0);
				g2d.translate(-center, -(y + h / 2.0));

				g2d.translate(center - h / 2.0, y);
				g2d.rotate(Math.PI / 2.0);
				g.drawChars(stringChar, i, 1, (int)(w * PROLONGED_SHIFT_X), (int)(h * PROLONGED_SHIFT_Y));

				g2d.setTransform(tx);
				y += w;
			} else if (stringChar[i] == PROLONGED || Character.UnicodeBlock.of(stringChar[i]) == Character.UnicodeBlock.BASIC_LATIN) {
				// 半角英数は90度回転させる
				Rectangle2D rect = font.getStringBounds(stringChar, i, i + 1, g2d.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				System.out.println(stringChar[i] + ": w = " + w + ", h = " + h);
				AffineTransform tx = g2d.getTransform();
				g2d.translate(center - h / 2.0, y);
				g2d.rotate(Math.PI / 2.0);
				g.drawChars(stringChar, i, 1, (int)(w * BASICLATIN_SHIFT_X), (int)(h * BASICLATIN_SHIFT_Y));
				g2d.setTransform(tx);
				y += w;
			} else {
				Rectangle2D rect = font.getStringBounds(stringChar, i, i + 1, g2d.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				System.out.println(stringChar[i] + ": w = " + w + ", h = " + h);
				g.drawChars(stringChar, i, 1, (int)(center - w / 2), (int)y);
				y += h;
			}
		}

	}

	private boolean isPunctuations(char c) {
		int index = PUNCTUATIONS.indexOf((int)c);
		return (index != -1);
	}

	private boolean isSmallKana(char c) {
		int index = SMALLKANA.indexOf((int)c);
		return (index != -1);
	}

	private char isParenthesis(char c) {
		int index = PARENTHESIS.indexOf((int)c);
		if (index != -1 ) {
			return V_PARENTHESIS.charAt(index);
		} else {
			return 0;
		}
	}

	static public void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);

		TategakiTest app = new TategakiTest();
		frame.add(app);
		frame.setVisible(true);

		// app.setString("あぁいぃうーぅえぇおぉゎ、アァイィウゥエェオォヮ。");
		app.setString("アーイーウーエーオーABCDE12345");
		// app.setString("—−ー（）｛｝〔〕【】《》〈〉「」『』");
	}
}

