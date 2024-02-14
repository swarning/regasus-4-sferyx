package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class CreditCardCheckNoText extends Text implements VerifyListener, FocusListener {

	private StringBuffer realText = new StringBuffer();

	private boolean modifying;


	public CreditCardCheckNoText(Composite parent, int style) {
		super(parent, style);

		addVerifyListener(this);
		addFocusListener(this);
	}


	@Override
	public void setText(String string) {
		realText.delete(0, realText.length());
		realText.append(string);

		StringBuffer sb = new StringBuffer(string);
		
		for (int i = 0; i < sb.length(); i++) {
			sb.replace(i, i + 1, "*");
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


	public String getText() {
		return realText.toString();
	}


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
			if (!e.text.matches("[0-9]*")) {
//				System.out.println("NOT INSERTING BECAUSE NO NUMBER");
				getDisplay().beep();
				e.doit = false;
				return;
			}
			else if (e.end > e.start) {
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
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	public void focusGained(FocusEvent e) {
		// Do nothing
	}

	/**
	 * When the user leaves the text widget after entering a number, we set the text anew programmatically, because by that measure the
	 * digits get replaced by asterisks.
	 */
	public void focusLost(FocusEvent e) {
		setText(getText());
	}

}
