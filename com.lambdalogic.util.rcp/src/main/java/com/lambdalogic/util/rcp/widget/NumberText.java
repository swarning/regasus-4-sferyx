package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.error.ErrorHandler;

/**
 * A specialized Text widget that only accepts input that is a legal int, within the minValue and maxValue, if given; an
 * empty text is also possible if nullAllowed is set to true. The search cue message is used to indicate that the field
 * wants to have int input.
 */
public class NumberText extends Text implements VerifyListener {

	private int minValue = 0;
	private int maxValue = Integer.MAX_VALUE;

	private boolean nullAllowed;

	private int leadingZeros = 0;


	/**
	 * Determines the style parameter for different platforms.
	 *
	 * @param style
	 * @return
	 */
	private static int getStyle(int style) {
		if (SystemHelper.isMacOSX()) {
			style = style | SWT.BORDER | SWT.RIGHT;
		}
		else {
			style = style | SWT.BORDER | SWT.RIGHT | SWT.SEARCH;
		}
		return style;
	}


	public NumberText(Composite parent, int style) {
		super(parent, getStyle(style));
		addVerifyListener(this);
		setValue(null);
		
		// call setMaxValue() to set the text limit
		setMaxValue(maxValue);

		/* This is a workaround for the following bug in the Text widget.
		 * Values are changed if clicking another widget causes the Text widget to be disabled.
		 * However, calling getText() before preserves the value.
		 */
		this.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				getText();
			}
		});

		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (getEditable()) {
					if (e.stateMask == 0) {
						if (e.keyCode == SWT.ARROW_UP) {
							increment();
						}
						else if (e.keyCode == SWT.ARROW_DOWN) {
							decrement();
						}
					}
				}
			}
		});
	}
	
	
	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}


	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		setTextLimit( String.valueOf(maxValue).length() );
	}


	@Override
	public void setText(String string) {
		if (isAllowedInput(string)) {
			super.setText(string);
		}
	}


	public int getLeadingZeros() {
		return leadingZeros;
	}


	public void setLeadingZeroDigits(int i) {
		this.leadingZeros = i;
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	public boolean isNullAllowed() {
		return nullAllowed;
	}


	public void setNullAllowed(boolean nullAllowed) {
		this.nullAllowed = nullAllowed;
	}


	public Integer getValue() {
		Integer result = null;
		
		try {
			String currentText = getText();
			if ( ! currentText.isEmpty()) {
				result = TypeHelper.toInteger(currentText);
			}
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return result;
	}


	public void setValue(Integer value) {
		String text = "";
		if (value != null) {
			text = String.valueOf(value);
			if (leadingZeros > 0) {
				text = StringHelper.padLeft(text, '0', leadingZeros);
			}
		}
		else if (getMessage() == null) {
			String message = "0";
			if (leadingZeros > 0) {
				message = StringHelper.padLeft(message, '0', leadingZeros);
			}
			setMessage(message);
		}

		setText(text);
	}


	@Override
	public void verifyText(VerifyEvent event) {
		StringBuffer currentText = new StringBuffer(getText());
		if (event.keyCode == SWT.BS || event.keyCode == SWT.DEL) {
			currentText.delete(event.start, event.end);
		}
		else {
			if (event.end > event.start) {
				currentText.delete(event.start, event.end);
			}

			currentText.insert(event.start, event.text);
		}

		event.doit = isAllowedInput(currentText.toString());
		if (!event.doit) {
			getDisplay().beep();
		}
	}


	private boolean isAllowedInput(String currentText) {
		boolean allow = true;
		
		try {
			if (StringHelper.isEmpty(currentText)) {
				allow = nullAllowed;
			}
			else {
				int value = Integer.parseInt(currentText);
				allow = value >= minValue && value <= maxValue; 
			}
		}
		catch (NumberFormatException ex) {
			allow = false;
		}

		return allow;
	}


	private void increment() {
		Integer currentValue = getValue();

		if (currentValue == null) {
			currentValue = minValue;
		}
		else if (currentValue + 1 <= maxValue) {
			currentValue++;
		}

		this.setValue(currentValue);
	}


	private void decrement() {
		Integer currentValue = getValue();
		
		if (currentValue == null) {
			currentValue = maxValue;
		}
		else if (currentValue - 1 >= minValue) {
			currentValue--;
		}

		this.setValue(currentValue);
	}

}
