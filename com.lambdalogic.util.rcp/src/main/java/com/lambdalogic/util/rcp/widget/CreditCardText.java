package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.CreditCardHelper;

public class CreditCardText extends Composite implements VerifyListener, FocusListener {

	/**
	 * Contains the actual credit card number, not the masked one.
	 */
	private StringBuffer realText = new StringBuffer();

	private boolean modifying;

	// Widgets
	private Text text;


	/**
	 * Create the composite
	 * 
	 * @param parent
	 * @param style
	 */
	public CreditCardText(Composite parent, int style) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());

		text = new Text(this, style);
		text.addVerifyListener(this);
		text.addFocusListener(this);
	}

	@Override
	public void setEnabled(boolean enabled) {
		text.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	public String getText() {
		return realText.toString();
	}


	/**
	 * The given string is a) put in the buffer for the real content, and b) shown with the first but 4 digits replaced
	 * by asterisks.
	 * 
	 * @param string
	 */
	public void setText(String string) {

		realText.delete(0, realText.length());
		realText.append(string);

		String newReplacementText = CreditCardHelper.replaceAllButLast4DigitsByStar(string);

		// Check whether we really have to set a new text, because if not needed, we don't want to have a modified
		// event.
		if (!newReplacementText.equals(text.getText())) {
			// Do not verify input now
			modifying = true;
			text.setText(newReplacementText);
			modifying = false;
		}

	}


	public void verifyText(VerifyEvent e) {
		if (modifying) {
			e.doit = true;
			return;
		}
		if (e.keyCode == SWT.BS || e.keyCode == SWT.DEL) {
			realText.delete(e.start, e.end);
		}
		else {
			if (!e.text.matches("[0-9]*")) {
				getDisplay().beep();
				e.doit = false;
				return;
			}
			else if (e.end > e.start) {
				// Something is selected that must be deleted first, but first check if we should do it anyway
				if (realText.length() + e.text.length() - (e.end - e.start) > text.getTextLimit()) {
					getDisplay().beep();
					e.doit = false;
					return;
				}

				// Something is selected that must be deleted now
				realText.delete(e.start, e.end);

			}

			// We cannot add something if result is too long
			if (realText.length() + e.text.length() > text.getTextLimit()) {
				getDisplay().beep();
				e.doit = false;
				return;
			}
			else {
				realText.insert(e.start, e.text);
			}
		}
	}


	public void focusGained(FocusEvent e) {
		// Do nothing
	}


	/**
	 * When the user leaves the text widget after entering a number, we set the text anew programmatically, because by
	 * that measure the digits get replaced by asterisks.
	 */
	public void focusLost(FocusEvent e) {
		setText(getText());
	}


	public void addModifyListener(ModifyListener listener) {
		text.addModifyListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		text.removeModifyListener(listener);
	}


	/**
	 * @return
	 * @see org.eclipse.swt.widgets.Text#getTextLimit()
	 */
	public int getTextLimit() {
		return text.getTextLimit();
	}


	/**
	 * @param limit
	 * @see org.eclipse.swt.widgets.Text#setTextLimit(int)
	 */
	public void setTextLimit(int limit) {
		text.setTextLimit(limit);
	}

}
