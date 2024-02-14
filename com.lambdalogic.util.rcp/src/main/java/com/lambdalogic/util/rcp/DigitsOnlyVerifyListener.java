package com.lambdalogic.util.rcp;

import static com.lambdalogic.util.StringHelper.*;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class DigitsOnlyVerifyListener implements VerifyListener {
	
	private static DigitsOnlyVerifyListener instance = null;
	
	
	public static DigitsOnlyVerifyListener getInstance() {
		if (instance == null) {
			instance = new DigitsOnlyVerifyListener();
		}
		return instance;
	}
	

	public void verifyText(final VerifyEvent e) {
		final char[] input = e.text.toCharArray();
	    	    
		for (int i = 0; i < input.length; i++) {
			if ( ! Character.isDigit(input[i])) {
				// MIRCP-2315 --> Negative values in Copy Event dialog
				// since Character.isDigit only validate positive integers 
				// to validate negative integers :- accepts a single '-' value as a valid input
				// at index = '0' and the input.length must be greater than 1 ('-' with out a number is invalid)
				if (
					i==0 
					&& input[i] == HYPHEN_MINUS 
					&& input.length > 1
				){
					continue;
				}
				e.doit = false;
				break;
			}
		}
	}

}
