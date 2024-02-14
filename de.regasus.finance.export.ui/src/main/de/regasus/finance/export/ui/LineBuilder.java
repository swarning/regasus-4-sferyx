package de.regasus.finance.export.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class LineBuilder {
	
	private static char SEPARATOR = ';';
	
	
	// Using an own instance to have control of decimal character and sign
	private static NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);
	static {
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(2);
		numberFormat.setGroupingUsed(false);
	}

	
	private StringBuilder sb;
	private boolean empty = true;
	
	
	public LineBuilder() {
		sb = new StringBuilder();
	}
	

	public void reset() {
		sb.setLength(0);
		empty = true;
	}


	public String build() {
		return sb.toString();
	}
	
	
	protected void separate() {
		if (!empty) {
			sb.append(SEPARATOR);
		}
		else {
			empty = false;
		}
	}
	
	
	public void addField() {
		separate();
	}
	
	
	public void addField(int i) {
		separate();
		
		sb.append(i);
	}
	
	
	public void addField(Integer i) {
		separate();
		
		if (i != null) {
			sb.append(i.intValue());
		}
	}
	
	
	public void addField(String s) {
		separate();
		
		if (s != null) {
			sb.append("\"");
			sb.append(s);
			sb.append("\"");
		}
	}
	
	
	public void addAbsoluteAmount(BigDecimal amount) {
		separate();
		
		if (amount != null) {
			BigDecimal amountAbsoluteValue = amount.abs();
			String betragOhneVorzeichen = numberFormat.format(amountAbsoluteValue);
			sb.append(betragOhneVorzeichen);
		}
	} 


	@Override
	public String toString() {
		return build();
	}
	
}
