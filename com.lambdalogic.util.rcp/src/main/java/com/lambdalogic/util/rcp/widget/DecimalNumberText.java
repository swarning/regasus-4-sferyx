package com.lambdalogic.util.rcp.widget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.SystemHelper;

/**
 * A specialized Text widget that only accepts input that is a legal BigDecimal, within the minValue and maxValue, if
 * given; an empty text is also possible if nullAllowed is set to true. A suffix may be set (like "%" or " EUR"). The
 * search cue message is used to indicate that the field wants to have int input.
 * <p>
 * Two usage examples:
 * 
 * <pre>
 * DecimalNumberText sd1 = new DecimalNumberText(shell, SWT.BORDER);
 * sd1.setFractionDigits(2);
 * sd1.setNullAllowed(true);
 * sd1.setSuffix(&quot; EUR&quot;);
 * sd1.setValue(null);
 * 
 * DecimalNumberText sd2 = new DecimalNumberText(shell, SWT.BORDER);
 * sd2.setFractionDigits(1);
 * sd2.setNullAllowed(false);
 * sd2.setShowPercent(true);
 * sd2.setMaxValue(100);
 * sd2.setMinValue(0);
 * sd2.setValue(new BigDecimal(19));
 * </pre>
 * 
 * @author manfred
 * 
 */

public class DecimalNumberText extends Text implements VerifyListener {

	/**
	 * A helper object that gives the decimal separator and the percent sign for the current locale.
	 */
	private DecimalFormatSymbols symbols = new DecimalFormatSymbols();

	/**
	 * If present, contains the highest number that may be entered in this DecimalNumberText
	 */
	private BigDecimal maxValue;

	/**
	 * If present, contains the lowest number that may be entered in this DecimalNumberText
	 */
	private BigDecimal minValue;

	/**
	 * Whether a null value is allowed. If true, the user may delete the complete contents of this widget, whereupon a
	 * search message indicates the expected input.
	 */
	private boolean nullAllowed;

	/**
	 * The number of digits after the decimal separator.
	 */
	private int fractionDigits;

	private BigDecimal increment = BigDecimal.ONE;

	private BigDecimal pageIncrement = BigDecimal.TEN;

	private BigDecimal currentValue;

	private String suffix = "";

	
	/**
	 * Determines the style parameter for different platforms.
	 * 
	 * @param style
	 * @return
	 */
	private static int getStyle(int style) {
		if (SystemHelper.isMacOSX()) {
			style = style | SWT.BORDER;
		}
		else {
			style = style | SWT.BORDER | SWT.SEARCH;
		}
		
		// if style doesn't contain infos about horizontal alignment, set it to RIGHT
		if ((style & (SWT.LEFT | SWT.CENTER | SWT.RIGHT)) == 0) {
			style = style | SWT.RIGHT;
		}
		
		return style;
	}
	
	
	public DecimalNumberText(Composite parent, int style) {
		super(parent, getStyle(style));
		addVerifyListener(this);

		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(final KeyEvent e) {
				if (e.stateMask == 0) {
					if (e.keyCode == SWT.ARROW_UP) {
						increment(increment);
					}
					else if (e.keyCode == SWT.ARROW_DOWN) {
						decrement(increment);
					}
					else if (e.keyCode == SWT.PAGE_UP) {
						increment(pageIncrement);
					}
					else if (e.keyCode == SWT.PAGE_DOWN) {
						decrement(pageIncrement);
					}
				}
			}
		});

		this.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// Don't set null value, because the editor 
				// would get dirty even if nothing was entered
				BigDecimal bigDecimal = getValue();
				if (bigDecimal != null) {
					setValue(bigDecimal);
				}
			}
		});
	}


	public BigDecimal getMaxValue() {
		return maxValue;
	}
	
	
	public void setMaxValue(BigDecimal maxValue) {
		this.maxValue = maxValue;
	}

	
	public void setMaxValue(double maxValue) {
		this.maxValue = new BigDecimal(maxValue);
	}


	public BigDecimal getMinValue() {
		return minValue;
	}
	

	public void setMinValue(BigDecimal minValue) {
		this.minValue = minValue;
	}

	
	public void setMinValue(int minValue) {
		this.minValue = new BigDecimal(minValue);
	}


	public BigDecimal getIncrement() {
		return increment;
	}
	
	
	public void setIncrement(BigDecimal increment) {
		this.increment = increment;
	}

	
	public BigDecimal getPageIncrement() {
		return pageIncrement;
	}
	
	
	public void setPageIncrement(BigDecimal pageIncrement) {
		this.pageIncrement = pageIncrement;
	}

	@Override
	public void setText(String string) {
		if (isAllowedInput(string)) {
			super.setText(string);
		}
	}


	/**
	 *  Disables the check that prevents subclassing of SWT components
	 */
	@Override
	protected void checkSubclass() {
		
	}


	/**
	 * @return the nullAllowed
	 */
	public boolean isNullAllowed() {
		return nullAllowed;
	}


	/**
	 * When null allowed, the user may delete the complete input.
	 */
	public void setNullAllowed(boolean nullAllowed) {
		this.nullAllowed = nullAllowed;
	}


	/**
	 * Returns null, if there is no text in this widget or no legal value. Otherwise 
	 * returns the BigDecimal that is the result of stripping the text from its suffix,
	 * and replacing localized with parsable chars.
	 */
	public BigDecimal getValue() {
		String currentText = getText();
		if (currentText.length() == 0) {
			return null;
		}
		else {
			try {
				isAllowedInput(getText());
				return currentValue;
			}
			catch (Exception e) {
				return null;
			}
		}
	}


	/**
	 * The given value is put as localized text in the widget. It is not stored in any other variable. When null is
	 * given the localized text for the null value is shown as search cue message.
	 * 
	 */
	public void setValue(BigDecimal value) {
		if (value != null) {
			// round
			value = value.setScale(fractionDigits, RoundingMode.HALF_UP);			
			
			String text = decimalToLocalized(value.toPlainString());
				
			if (! text.equals(getText())) {
				setText(text);
			}
		}
		else {
			// When null is given the localized text for the null value is shown as search cue message.
			setText("");
			StringBuilder sb = new StringBuilder(10);
			sb.append('0');
			
			for (int i = 0; i < fractionDigits; i++) {
				if (i == 0) {
					sb.append('.');
				}
				sb.append('0');
			}
			
			String message = decimalToLocalized(sb.toString());
			setMessage(message);
		}
	}


	/**
	 * Is called by SWT when the contents of this text should be modified. We compute the text at the end of the
	 * modification and check whether it would be allowedInput.
	 */
	@Override
	public void verifyText(VerifyEvent e) {
		StringBuffer currentText = new StringBuffer(getText());
		if (e.keyCode == SWT.BS || e.keyCode == SWT.DEL) {
			currentText.delete(e.start, e.end);
		}
		else {
			if (e.end > e.start) {
				currentText.delete(e.start, e.end);
			}

			currentText.insert(e.start, e.text);
		}

		e.doit = isAllowedInput(currentText.toString());
		if (!e.doit) {
			getDisplay().beep();
		}
	}


	/**
	 * Returns true when the given string is either null, or can be transformed into a BigDecimal that is not outside a
	 * range of possibly given maxValue and/or minValue.
	 * <p>
	 * When such a transformation was successful, the currentValue is set to the result.
	 */
	private boolean isAllowedInput(String input) {
		currentValue = null;
		
		if (input.length() == 0) {
			return nullAllowed;
		}

		if (!containsOnlyAllowedChars(input)) {
			return false;
		}

		String probablyLegalInput = localizedToDecimal(input);
		if (!hasNotMoreThanAllowedNumberOfFractionDigits(probablyLegalInput)) {
			return false;
		}

		try {
			currentValue = new BigDecimal(probablyLegalInput);
		}
		catch (Exception e) {
			return false;
		}
		if (maxValue != null && maxValue.compareTo(currentValue) == -1) {
			currentValue = null;
			return false;
		}
		if (minValue != null && minValue.compareTo(currentValue) == 1) {
			currentValue = null;
			return false;
		}

		return true;
	}


	/**
	 * Returns true when the string contains - before its possibly present suffix - only digits and the localized
	 * decimal separator and minus sign
	 */
	private boolean containsOnlyAllowedChars(String string) {
		// consider only the part before its suffix, if present
		int limit = string.length();
		if (string.endsWith(suffix)) {
			limit -= suffix.length();
		}

		for (int i = 0; i < limit; i++) {
			char c = string.charAt(i);
			if (c >= '0' && c <= '9') {
				continue;
			}
			else if (c == symbols.getDecimalSeparator()) {
				continue;
			}
			else if (c == symbols.getMinusSign() && i == 0) {
				continue;
			}
			return false;
		}
		return true;
	}


	/**
	 * Increcemts the current value by the given delta, if the result aint above the maximum. When there is no current
	 * value yet, but there is a minimum, start from the bottom. When there is also no minimum given, start with 0.
	 */
	private void increment(BigDecimal delta) {
		BigDecimal currentValue = getValue();

		if (currentValue == null) {
			if (minValue != null) {
				// no current value yet, but there is a minimum, start from the bottom.
				currentValue = minValue;
			}
			else {
				// no current value yet, no minimum given, start with 0.
				currentValue = new BigDecimal(0);
			}
		}
		else {

			BigDecimal newValue = currentValue.add(delta);
			// meaning: "if newValue <= maxValue"
			if (maxValue == null || newValue.compareTo(maxValue) <= 0) {
				currentValue = newValue;
			}
		}
		this.setValue(currentValue);
	}


	/**
	 * Decrecemts the current value by the given delta, if the result aint below the minimum. When there is no current
	 * value yet, but there is a maximum, start from the top. When there is also no maximum given, start with 0.
	 */
	private void decrement(BigDecimal delta) {
		BigDecimal currentValue = getValue();
		if (currentValue == null) {
			// no current value yet, but there is a maximum, start from the top
			if (maxValue != null) {
				currentValue = maxValue;
			}
			else {
				// no current value yet, no maximum given, start with 0.
				currentValue = new BigDecimal(0);
			}
		}
		else {
			// decrement by delta, if newValue not below minimum
			BigDecimal newValue = currentValue.subtract(delta);

			// meaning: "if newValue >= maxValue"
			if (minValue == null || newValue.compareTo(minValue) >= 0) {
				currentValue = newValue;
			}
		}
		this.setValue(currentValue);
	}


	/**
	 * Transforms a user-oriented String like 19,5% to a String that can be transformed into a BigDecimal (ie "19.5").
	 */
	private String localizedToDecimal(String string) {

		// Possibly replace , with . (Unfortunately, there is no replace in StringBuilder)
		String result = string.replace(symbols.getDecimalSeparator(), '.');
		result = result.replace(symbols.getMinusSign(), '-');

		// Cut off suffix, if present
		if (result.endsWith(suffix)) {
			result = result.substring(0, string.length() - suffix.length());
		}

		// Done
		return result;
	}


	/**
	 * Transforms the BigDecimal-Output like "1.0" to a string with localized decimal separator (and minus) adds the
	 * number of required fraction digits and the suffix.
	 */
	private String decimalToLocalized(String input) {
		// Possibly replace . with , (Unfortunately, there is no replace in StringBuilder)
		String string = input.replace('.', symbols.getDecimalSeparator());
		string = string.replace('-', symbols.getMinusSign());

		// But now, continue with StringBuilder
		StringBuilder sb = new StringBuilder(string);

		int missingZeros = 0;
		
		int indexOfDot = string.indexOf(symbols.getDecimalSeparator());
		if (indexOfDot == -1) {
			missingZeros = fractionDigits;
		}
		else {
			int currentDecimalPlaces = string.length() - indexOfDot - 1;
			missingZeros = fractionDigits - currentDecimalPlaces;
		}

		
		// and decimal separator if missing but needed
		if (missingZeros > 0 && indexOfDot == -1) {
			sb.append(symbols.getDecimalSeparator());
		}
		
		
		// append possibly needed zeroes
		while (missingZeros-- > 0) {
			sb.append('0');
		}

		
		// append possibly needed suffix
		if (suffix != null) {
			sb.append(suffix);
		}

		return sb.toString();
	}


	/**
	 * Returns true when the given text does not contain more than fractionDigits chars behind the decimal separator.
	 * 
	 * @return
	 */
	private boolean hasNotMoreThanAllowedNumberOfFractionDigits(String string) {
		if (!string.contains(".")) {
			return true;
		}

		// consider only the part before its suffix, if present
		int limit = string.length();
		if (string.endsWith(suffix)) {
			limit -= suffix.length();
		}

		return string.indexOf('.') + 1 >= limit - fractionDigits;
	}


	/**
	 * @return the fractionDigits
	 */
	public int getFractionDigits() {
		return fractionDigits;
	}


	/**
	 * Sets the number of fraction digits, which may be equal or higher than 0.
	 */
	public void setFractionDigits(int fractionDigits) {
		if (fractionDigits < 0) {
			throw new IllegalArgumentException("You may not set negative number for fraction digits");
		}
		this.fractionDigits = fractionDigits;

//		this.increment = ONE;
//		for (int i = 0; i < fractionDigits; i++) {
//			this.increment = this.increment.divide(new BigDecimal(10));
//		}
	}


	/**
	 * If true, sets suffix to the locale-specific percent symbol
	 */
	public void setShowPercent(boolean showPercent) {
		if (showPercent) {
			suffix = String.valueOf(symbols.getPercent());
		}
	}


	/**
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}


	/**
	 * Sets a String as suffix to be shown at the end of the text. If null is given, an empty string is used instead.
	 */
	public void setSuffix(String suffix) {
		if (suffix == null) {
			this.suffix = "";
		}
		else {
			this.suffix = suffix;
		}
	}


	public void setValue(Double amount) {
		if (amount == null) {
			clear();
		}
		else {
			/*
			 * With a double amount of 19.02, the "exact" constructor gave 19.0199999999999something!!! 
			 */
			setValue(BigDecimal.valueOf(amount.doubleValue()));
		}
	}


	public void clear() {
		setValue((BigDecimal) null);
	}

}
