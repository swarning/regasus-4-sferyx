package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;


/**
 * A text widget that can hide the text and allows to define forbidden characters.
 */
public class SuperText extends Text implements VerifyListener, FocusListener {

	private StringBuilder realText = new StringBuilder();
	
	static final String PASSWORD_STR = "\u2022";

	private boolean modifying;

	private boolean hidden = false;
	
	private char[] invalidChars = null;


	public SuperText(Composite parent, int style) {
		super(parent, style);
		
		addVerifyListener(this);
		addFocusListener(this);
	}


	@Override
	public String getText() {
		return realText.toString();
	}


	@Override
	public void setText(String text) {
		setText(text, hidden);
	}

	
	public void setText(String text, boolean hidden) {
		realText.delete(0, realText.length());
		realText.append(text);

		StringBuffer sb = new StringBuffer(text);
		if (hidden) {
			for (int i = 0; i < sb.length(); i++) {
				sb.replace(i, i + 1, PASSWORD_STR);
			}
		}
	
		// Check whether we really have to set a new text, because if not needed, we don't want to have a modified event.
		String newReplacementText = sb.toString();
	
		if (! newReplacementText.equals(super.getText())) {
			// Do not verify input now
			modifying = true;
			super.setText(newReplacementText);
			modifying = false;
		}
	}


	@Override
	public void verifyText(VerifyEvent e) {
		if (modifying) {
			e.doit = true;
			return;
		}
		
		if (e.keyCode == SWT.BS || e.keyCode == SWT.DEL) {
//			System.out.println("DELETING FROM  " + e.start + " TO " + e.end);
			realText.delete(e.start, e.end);
		}
		else {
			if (e.end > e.start) {
				// Something is selected that must be deleted first, but first check if we should do it anyway
				if (realText.length() + e.text.length() - (e.end-e.start) > getTextLimit()) {
					getDisplay().beep();
					e.doit = false;
//					System.out.println("NOT DELETING BECAUSE TOO LONG AFTER INSERT");
					return;
				}
				
				// Something is selected that must be deleted now
//				System.out.println("DELETING FROM  " + e.start + " TO " + e.end);
				realText.delete(e.start, e.end);
			}
			
			// We cannot add something if result is too long
			if (realText.length() + e.text.length() > getTextLimit()) {
//				System.out.println("NOT INSERTING BECAUSE TOO LONG");
				getDisplay().beep();
				e.doit = false;
				return;
			}
			else {
				// System.out.println("INSERTING " + e.text + " AT " + e.start);
				realText.insert(e.start, e.text);
			}
			
			e.doit = isAllowedInput(realText.toString());
			if (!e.doit) {
				getDisplay().beep();
				return;
			}
			
			if (hidden) {
				e.text = PASSWORD_STR;
			}
		}
	}
	
	
	private boolean isAllowedInput(String currentText) {
		boolean valid = true;
		
		if (invalidChars != null) {
			for (char c: invalidChars) {
				if (currentText.indexOf(c) != -1) {
					valid = false;
					break;
				}
			}
		}
		
		return valid;
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	@Override
	public void focusGained(FocusEvent e) {
		// Do nothing
	}

	
	/**
	 * When the user leaves the text widget after entering text, we set the text anew programmatically,
	 * because by that measure the digits get replaced by asterisks.
	 */
	@Override
	public void focusLost(FocusEvent e) {
		setText(getText());
	}

	
	public boolean isHidden() {
		return hidden;
	}
	
	
	public void setHidden(boolean hidden) {
		if (this.hidden != hidden) {
			this.hidden = hidden;
			setText(getText());
		}
	}

	
	public char[] getInvalidChars() {
		return invalidChars;
	}


	public void setInvalidChars(char[] invalidChars) {
		this.invalidChars = invalidChars;
	}

}
