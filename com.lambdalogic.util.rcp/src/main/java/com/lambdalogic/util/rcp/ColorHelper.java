package com.lambdalogic.util.rcp;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColorHelper {

	public static final int COLOR_WHITE = 1;
	public static final int COLOR_BLACK = 2;
	public static final int COLOR_RED = 3;
	public static final int COLOR_DARK_RED = 4;
	public static final int COLOR_GREEN = 5;
	public static final int COLOR_DARK_GREEN = 6;
	public static final int COLOR_YELLOW = 7;
	public static final int COLOR_DARK_YELLOW = 8;
	public static final int COLOR_BLUE = 9;
	public static final int COLOR_DARK_BLUE = 10;
	public static final int COLOR_MAGENTA = 11;
	public static final int COLOR_DARK_MAGENTA = 12;
	public static final int COLOR_CYAN = 13;
	public static final int COLOR_DARK_CYAN = 14;
	public static final int COLOR_GRAY = 15;
	public static final int COLOR_DARK_GRAY = 16;

	public static final Color GRAY_1 = createColor(224, 224, 224);
	public static final Color GRAY_2 = createColor(192, 192, 192);
	public static final Color GRAY_3 = createColor(160, 160, 160);
	public static final Color GRAY_4 = createColor(128, 128, 128);
	public static final Color GRAY_5 = createColor( 96,  96,  96);
	public static final Color GRAY_6 = createColor( 64,  64,  64);
	public static final Color GRAY_7 = createColor( 32,  32,  32);

	public static final Color RED_1 = createColor(255, 0, 0);
	public static final Color RED_2 = createColor(224, 0, 0);
	public static final Color RED_3 = createColor(192, 0, 0);
	public static final Color RED_4 = createColor(160, 0, 0);
	public static final Color RED_5 = createColor(128, 0, 0);
	public static final Color RED_6 = createColor( 96, 0, 0);
	public static final Color RED_7 = createColor( 64, 0, 0);
	public static final Color RED_8 = createColor( 32, 0, 0);

	public static final Color GREEN_1 = createColor(0, 255, 0);
	public static final Color GREEN_2 = createColor(0, 224, 0);
	public static final Color GREEN_3 = createColor(0, 192, 0);
	public static final Color GREEN_4 = createColor(0, 160, 0);
	public static final Color GREEN_5 = createColor(0, 128, 0);
	public static final Color GREEN_6 = createColor(0,  96, 0);
	public static final Color GREEN_7 = createColor(0,  64, 0);
	public static final Color GREEN_8 = createColor(0,  32, 0);

	public static final Color BLUE_1 = createColor(0, 0, 255);
	public static final Color BLUE_2 = createColor(0, 0, 224);
	public static final Color BLUE_3 = createColor(0, 0, 192);
	public static final Color BLUE_4 = createColor(0, 0, 160);
	public static final Color BLUE_5 = createColor(0, 0, 128);
	public static final Color BLUE_6 = createColor(0, 0,  96);
	public static final Color BLUE_7 = createColor(0, 0,  64);
	public static final Color BLUE_8 = createColor(0, 0,  32);


	/**
	 * Determine a {@link Color} from an SWT constant.
	 * Example: ColorHelper.getColor(SWT.COLOR_RED)
	 * @param swtColor
	 * @return
	 */
	public static Color getSystemColor(int swtColor) {
		Color color = Display.getCurrent().getSystemColor(swtColor);
		return color;
	}


	public static Color createColor(int red, int green, int blue) {
		Color color = new Color(Display.getCurrent(), red, green, blue);
		return color;
	}

}
