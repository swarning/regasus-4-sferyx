package com.lambdalogic.util.rcp.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.DigitsOnlyVerifyListener;

public class NullableSpinner extends Composite {

	private Long value = null;

	private boolean nullable = true;

	private long minimum = Long.MIN_VALUE;

	private long maximum = Long.MAX_VALUE;

	/**
	 * The value which is set if increment or decrement is called while value is null.
	 */
	private Long startValue;

	private int increment = 1;

	private int pageIncrement = 10;

	// Widgets
	private Text text;

	private Button upButton;

	private Button downButton;

	private boolean ignoreModifyEvent = false;


	/**
	 * Determines the style parameter for different platforms.
	 *
	 * @param style
	 * @return
	 */
	private static int getStyle(int style) {
		// if style doesn't contain infos about horizontal alignment, set it to RIGHT
		if ((style & (SWT.LEFT | SWT.CENTER | SWT.RIGHT)) == 0) {
			style = style | SWT.RIGHT;
		}

		return style;
	}

	public NullableSpinner(Composite parent, int style) {
		this(parent, style, false /*required*/);
	}


	/**
	 * Create the composite
	 *
	 * @param parent
	 * @param style
	 */
	public NullableSpinner(Composite parent, int style, boolean required) {
		super(parent, getStyle(style & ~SWT.BORDER)); // suppress ugly double border
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		text = new Text(this, SWT.BORDER | SWT.RIGHT);
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.stateMask == 0) {
					if (e.keyCode == SWT.ARROW_UP) {
						increment(increment);
						e.doit = false;
					}
					else if (e.keyCode == SWT.ARROW_DOWN) {
						decrement(increment);
						e.doit = false;
					}
					else if (e.keyCode == SWT.PAGE_UP) {
						increment(pageIncrement);
						e.doit = false;
					}
					else if (e.keyCode == SWT.PAGE_DOWN) {
						decrement(pageIncrement);
						e.doit = false;
					}
				}
			}
		});
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
		text.addVerifyListener(DigitsOnlyVerifyListener.getInstance());
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!ignoreModifyEvent) {
					boolean correctionDone = false;

					final String s = text.getText();
					if (s == null || s.length() == 0) {
						if (nullable) {
							value = null;
						}
						else {
							value = getMinimum();
							if (value == null) {
								value = 0L;
							}
							correctionDone = true;
						}
					}
					else {
						long tmpValue = Long.valueOf(s);

						// If entered value too large: set max value
						if (tmpValue > getMaximum()) {
							tmpValue = getMaximum();
							correctionDone = true;
						}
						// If entered value too small: set min value
						else if (tmpValue < getMinimum()) {
							tmpValue = getMinimum();
							correctionDone = true;
						}

						value = tmpValue;
					}

					if (correctionDone) {
						ignoreModifyEvent = true;
						text.setText(String.valueOf(value));
						ignoreModifyEvent = false;
					}
				}
			}
		});

		if (required) {
			SWTHelper.makeBold(text);
		}

		upButton = new Button(this, SWT.ARROW);
		upButton.setLayoutData(new GridData(SWT.DEFAULT, 10));
		upButton.setText("button");
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				increment(increment);
			}
		});

		downButton = new Button(this, SWT.ARROW | SWT.DOWN);
		downButton.setLayoutData(new GridData(SWT.DEFAULT, 10));
		downButton.setText("button");
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				decrement(increment);
			}
		});
		//
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	private void increment(long incValue) {
		if (value == null) {
			if (startValue != null) {
				value = startValue;
			}
			else {
				value = calcStartValue();
			}
		}
		else if (value + incValue <= maximum) {
			value += incValue;
		}

		text.setText(String.valueOf(value));
		text.selectAll();
	}


	private void decrement(long decValue) {
		if (value == null) {
			if (startValue != null) {
				value = startValue;
			}
			else {
				value = calcStartValue();
			}
		}
		else if (value - decValue >= minimum) {
			value -= decValue;
		}

		text.setText(String.valueOf(value));
		text.selectAll();
	}


	protected long calcStartValue() {
		// just in case that maximum is smaller than minimum: swap both values
		if (maximum < minimum) {
			long tmp = minimum;
			minimum = maximum;
			maximum = tmp;
		}

		long start = 0;
		if (maximum < 0) {
			// if maximum is negative then minimum is even "more negative", so maximum is the value most next to 0
			start = maximum;
		}
		else if (minimum > 0) {
			// if minimum is positive then maximum is even "more positive", so minimum is the value most next to 0
			start = minimum;
		}

		return start;
	}


	/**
	 * Delegates the added key listener to inner text widget, so that eg a search
	 * can be started by the Enter key (MIRCP-1895)
	 */
	@Override
	public void addKeyListener(KeyListener keyListener) {
		text.addKeyListener(keyListener);
	}


	@Override
	public void removeKeyListener(KeyListener keyListener) {
		text.removeKeyListener(keyListener);
	}


	// **************************************************************************
	// * Getter / Setter
	// *

	public Long getValue() {
		return value;
	}


	public Integer getValueAsInteger() {
		Integer i = null;
		if (value != null) {
			i = value.intValue();
		}
		return i;
	}


	public void setValue(Integer value) {
		Long valueAsLong = null;
		if (value != null) {
			valueAsLong = Long.valueOf(value.longValue());
		}

		setValue(valueAsLong);
	}


	public void setValue(Long newValue) {
		if (newValue == null) {
			if (nullable) {
				value = null;
				text.setText("");
			}
			else {
				throw new IllegalArgumentException("The value must not be null.");
			}
		}
		else if (newValue < minimum) {
			throw new IllegalArgumentException("The value (" + newValue
				+ ") must be greater or equal than the property minimum (" + minimum + ").");
		}
		else if (newValue > maximum) {
			throw new IllegalArgumentException("The value (" + newValue
				+ ") must be less or equal than the property maximum (" + maximum + ").");
		}
		else {
			value = newValue;
			text.setText(String.valueOf(value));
		}
	}


	public boolean isNullable() {
		return nullable;
	}


	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}


	public long getMinimum() {
		return minimum;
	}


	public void setMinimum(Long minimum) {
		if (minimum == null) {
			minimum = Long.MIN_VALUE;
		}
		
		if (minimum > maximum) {
			throw new IllegalArgumentException("The property minimum (" + minimum
				+ ") must be less or equal than the property maximum (" + maximum + ").");
		}

		this.minimum = minimum;
	}

	
	public void setMinimum(Integer minimum) {
		if (minimum == null) {
			minimum = Integer.MIN_VALUE;
		}

		setMinimum( Long.valueOf(minimum.longValue()) );
	}
	

	public long getMaximum() {
		return maximum;
	}


	public void setMaximum(Long maximum) {
		if (maximum == null) {
			maximum = Long.MAX_VALUE;
		}

		if (maximum < minimum) {
			throw new IllegalArgumentException("The property maximum (" + maximum
				+ ") must be greater or equal than the property minimum (" + minimum + ").");
		}
		this.maximum = maximum;
	}

	
	public void setMaximum(Integer maximum) {
		if (maximum == null) {
			maximum = Integer.MAX_VALUE;
		}

		setMaximum( Long.valueOf(maximum.longValue()) );
	}


	public void setMinimumAndMaximum(Long minimum, Long maximum) {
		if (minimum == null) {
			minimum = Long.MIN_VALUE;
		}
		
		if (maximum == null) {
			maximum = Long.MAX_VALUE;
		}
		
		
		if (maximum < minimum) {
			throw new IllegalArgumentException("The property maximum (" + maximum
				+ ") must be greater or equal than the property minimum (" + minimum + ").");
		}
		this.minimum = minimum;
		this.maximum = maximum;
	}
	
	
	public void setMinimumAndMaximum(Integer minimum, Integer maximum) {
		if (minimum == null) {
			minimum = Integer.MIN_VALUE;
		}
		
		if (maximum == null) {
			maximum = Integer.MAX_VALUE;
		}
		
		setMinimumAndMaximum(
			Long.valueOf(minimum.longValue()), 
			Long.valueOf(maximum.longValue())
		);
	}


	public Long getStartValue() {
		return startValue;
	}


	public void setStartValue(Long startValue) {
		this.startValue = startValue;
	}


	public int getIncrement() {
		return increment;
	}


	public void setIncrement(int increment) {
		this.increment = increment;
	}


	public int getPageIncrement() {
		return pageIncrement;
	}


	public void setPageIncrement(int pageIncrement) {
		this.pageIncrement = pageIncrement;
	}


	// *
	// * Getter / Setter
	// **************************************************************************

	public void addModifyListener(ModifyListener listener) {
		text.addModifyListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		text.removeModifyListener(listener);
	}


//	public void addSelectionListener(SelectionListener listener) {
//		checkWidget ();
//		if (listener == null) {
//			SWT.error(SWT.ERROR_NULL_ARGUMENT);
//		}
//		TypedListener typedListener = new TypedListener(listener);
//		addListener (SWT.Selection,typedListener);
//		addListener (SWT.DefaultSelection,typedListener);
//	}
//
//
//	public void removeSelectionListener(SelectionListener listener) {
//		checkWidget ();
//		if (listener == null) {
//			SWT.error(SWT.ERROR_NULL_ARGUMENT);
//		}
//		if (text == null) return;
//		text.removeSelectionListener(listener);
//	}


	@Override
	public void setEnabled(boolean enabled) {
		text.setEnabled(enabled);
		upButton.setEnabled(enabled);
		downButton.setEnabled(enabled);
		super.setEnabled(enabled);
	}




	// @Override
	// public String getToolTipText() {
	// return text.getToolTipText();
	// }
	//
	//
	// @Override
	// public void setToolTipText(String value) {
	// text.setToolTipText(value);
	// upButton.setToolTipText(value);
	// downButton.setToolTipText(value);
	// }
	//
	//
	// @Override
	// public boolean getVisible() {
	// // TODO Auto-generated method stub
	// return super.getVisible();
	// }
	//
	//
	// @Override
	// public boolean isEnabled() {
	// // TODO Auto-generated method stub
	// return super.isEnabled();
	// }
	//
	//
	// @Override
	// public boolean isVisible() {
	// // TODO Auto-generated method stub
	// return super.isVisible();
	// }
	//
	//
	// @Override
	// public void setEnabled(boolean enabled) {
	// // TODO Auto-generated method stub
	// super.setEnabled(enabled);
	// }
	//
	//
	// @Override
	// public void setVisible(boolean visible) {
	// // TODO Auto-generated method stub
	// super.setVisible(visible);
	// }

}
