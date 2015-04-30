import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;

public class TategakiPanel extends JPanel {

	// 縦書きする文字種と設定値
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

	// basic latin（半角英数字）
	private static final double BASICLATIN_SHIFT_X = -1.0;
	private static final double BASICLATIN_SHIFT_Y = -0.2;


	public TategakiPanel() {
		super();
	}

	public float drawVerticalString(Graphics2D g, String string, float x, float y) {
		return drawVerticalString(g, string, x, y, false);
	}

	public float drawVerticalString(Graphics2D g, String string, float x, float y, boolean calcOnly) {
		if (string == null)  return 0;
		char[] stringChar = string.toCharArray();
		Font font = g.getFont();

		float origY = y;

		char c;
		for (int i = 0; i < stringChar.length; i++) {
			if (isPunctuations(stringChar[i])) {
				// ずらして表示する
				Rectangle2D rect = font.getStringBounds(stringChar, i, i + 1, g.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				// System.out.println(stringChar[i] + ": w = " + w + ", h = " + h);
				if (!calcOnly)  g.drawChars(stringChar, i, 1, (int)(x - w / 2 + w * PUNCTUATIONS_SHIFT_X), (int)(y + h * PUNCTUATIONS_SHIFT_Y));
				y += h;
			} else if (isSmallKana(stringChar[i])) {
				// ずらして表示する。字体によっては調整しきれていない？
				Rectangle2D rect = font.getStringBounds(stringChar, i, i + 1, g.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				// System.out.println(stringChar[i] + ": w = " + w + ", h = " + h);
				if (!calcOnly)  g.drawChars(stringChar, i, 1, (int)(x - w / 2 + w * SMALLKANA_SHIFT_X), (int)(y + h * SMALLKANA_SHIFT_Y));
				y += h;
			} else if ((c = isParenthesis(stringChar[i])) != 0) {
				// Unicodeに縦書き文字があるので置き換える
				char[] c_array = new char[1];
				c_array[0] = c;
				Rectangle2D rect = font.getStringBounds(c_array, 0, 1, g.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				// System.out.println(c + ": w = " + w + ", h = " + h);
				if (!calcOnly)  g.drawChars(c_array, 0, 1, (int)(x - w / 2), (int)y);
				y += h;
			} else if (stringChar[i] == PROLONGED) {
				// 長音記号は反転＆90度回転させてすこしずらす
				Rectangle2D rect = font.getStringBounds(stringChar, i, i + 1, g.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				// System.out.println(stringChar[i] + ": w = " + w + ", h = " + h);
				if (!calcOnly) {
					AffineTransform tx = g.getTransform();
					// System.out.println("x = " + center + ", y = " + (y + h / 2.0));

					g.translate(x, y + h / 2.0);
					g.scale(-1.0, 1.0);
					g.translate(-x, -(y + h / 2.0));

					g.translate(x - h / 2.0, y);
					g.rotate(Math.PI / 2.0);
					g.drawChars(stringChar, i, 1, (int)(w * PROLONGED_SHIFT_X), (int)(h * PROLONGED_SHIFT_Y));

					g.setTransform(tx);
				}
				y += w;
			} else if (Character.UnicodeBlock.of(stringChar[i]) == Character.UnicodeBlock.BASIC_LATIN) {
				// 半角英数は90度回転させる
				Rectangle2D rect = font.getStringBounds(stringChar, i, i + 1, g.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				// System.out.println(stringChar[i] + ": w = " + w + ", h = " + h);
				if (!calcOnly) {
					AffineTransform tx = g.getTransform();
					g.translate(x - h / 2.0, y);
					g.rotate(Math.PI / 2.0);
					g.drawChars(stringChar, i, 1, (int)(w * BASICLATIN_SHIFT_X), (int)(h * BASICLATIN_SHIFT_Y));
					g.setTransform(tx);
				}
				y += w;
			} else {
				// 他はそのまま描く
				Rectangle2D rect = font.getStringBounds(stringChar, i, i + 1, g.getFontRenderContext());
				double w = rect.getWidth();
				double h = rect.getHeight();
				// System.out.println(stringChar[i] + ": w = " + w + ", h = " + h);
				if (!calcOnly)  g.drawChars(stringChar, i, 1, (int)(x - w / 2), (int)y);
				y += h;
			}
		}

		return y - origY;
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


	// fot test
	/*
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		String string = "あぁいぃうーぅえぇおぉゎ、アァイィウゥエェオォヮ。";
		// string = "アーイーウーエーオーABCDE12345";
		// string = "—−ー（）｛｝〔〕【】《》〈〉「」『』";

		Font font = new Font("Serif", Font.PLAIN, 50);

		g.setColor(Color.BLACK);
		g.setFont(font);
		int center = getSize().width / 2;
		drawVerticalString((Graphics2D)g, string, 50, 50);
	}
	*/

	static public void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);

		TategakiPanel app = new TategakiPanel();
		app.setBackground(Color.LIGHT_GRAY);
		frame.add(app);
		frame.setVisible(true);

	}
}

