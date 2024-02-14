package de.regasus.participant.editor.finance;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.lambdalogic.util.CurrencyAmount;

public class AccountancyUIHelper {

	public static final String DARK_GREEN = "darkGreen";
	
	public static final String LIGHT_YELLOW = "lightYellow";

	public static final String YELLOW = "yellow";

	public static final String LIGHT_GREEN = "lightGreen";

	public static final String GREEN = "green";

	public static final String LIGHT_PINK = "lightPink";

	public static final String PINK = "pink";

	public static final String GREY = "grey";
	
	public static final String DARK_GREY = "darkGrey";
	
	public static ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
	
	static {
		colorRegistry.put(PINK, new RGB(255, 230, 230));
		colorRegistry.put(LIGHT_PINK, new RGB(255, 245, 245));
		colorRegistry.put(YELLOW, new RGB(255, 255, 192));
		colorRegistry.put(LIGHT_YELLOW, new RGB(255, 255, 230));
		colorRegistry.put(GREY, new RGB(230, 230, 230));
		colorRegistry.put(DARK_GREY, new RGB(128, 128, 128));
		colorRegistry.put(GREEN, new RGB(220, 255, 220));
		colorRegistry.put(LIGHT_GREEN, new RGB(240, 255, 240));
		colorRegistry.put(DARK_GREEN, new RGB(0, 128, 0));
	}
	
	public static Color getColor(String colorName) {
		return colorRegistry.get(colorName);
	}
	
	public static String format(CurrencyAmount currencyAmount) {
		return currencyAmount.format(false, true);
	}
	

}
