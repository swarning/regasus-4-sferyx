package com.lambdalogic.util.rcp.validation;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

import com.lambdalogic.util.CharValidator;

public class VerifyAdapter implements VerifyListener {

	private CharValidator charValidator;


	public VerifyAdapter(CharValidator charValidator) {
		this.charValidator = Objects.requireNonNull(charValidator);
	}


	@Override
	public void verifyText(VerifyEvent e) {
		// allows cut (CTRL + x)
		if (e.text.isEmpty()) {
			e.doit = true;
		}
		else if (
			e.keyCode == SWT.ARROW_LEFT ||
			e.keyCode == SWT.ARROW_RIGHT ||
			e.keyCode == SWT.BS ||
			e.keyCode == SWT.DEL ||
			e.keyCode == SWT.CTRL ||
			e.keyCode == SWT.SHIFT
		) {
			e.doit = true;
		}
		else {
			boolean allow = false;
			for (int i = 0; i < e.text.length(); i++) {
				char c = e.text.charAt(i);
				allow = charValidator.isValid(c);
				if (!allow) {
					break;
				}
			}
			e.doit = allow;
		}
	}

}
