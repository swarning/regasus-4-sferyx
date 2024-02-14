package com.lambdalogic.util.rcp;

import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.AutoCorrectionHelper;
import com.lambdalogic.util.StringHelper;

public class AutoCorrectionWidgetHelper {

	/**
	 * Corrects the text value of the given {@code text}.
	 * 
	 * @param text GUI text component whose input should be corrected automatically.
	 */
	public static void correctAndSet(Text text) {
		if (text != null) {
			String input = text.getText();
			input = StringHelper.trim(input);
			
			if (input != null) {
				String correctInput = AutoCorrectionHelper.correct(input);
				if (! correctInput.equals(input)) {
					text.setText(correctInput);
				}
			}
		}
	}
	
}
