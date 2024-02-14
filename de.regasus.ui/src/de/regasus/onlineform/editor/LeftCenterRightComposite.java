package de.regasus.onlineform.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * A composite showing three radio buttons labelled "Left", "Center" and "Right" (with i18n); used for determining
 * banner alignment
 * 
 * @author manfred
 * 
 */
public class LeftCenterRightComposite extends Composite {

	private Button leftButton;

	private Button centerButton;

	private Button rightButton;
	
	private Button responsiveButton;
	
	private boolean withResponsive;

	public void addSelectionListener(SelectionListener listener) {
		leftButton.addSelectionListener(listener);
		centerButton.addSelectionListener(listener);
		rightButton.addSelectionListener(listener);
		if (withResponsive) {
			responsiveButton.addSelectionListener(listener);
		}
	}

	public LeftCenterRightComposite(Composite parent, int style) {
		this(parent, style, true /*withResponsive*/);
	}
	
	
	public LeftCenterRightComposite(Composite parent, int style, boolean withResponsive) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout(4, true);
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);

		leftButton = new Button(this, SWT.RADIO);
		leftButton.setText(de.regasus.onlineform.OnlineFormI18N.Left);

		centerButton = new Button(this, SWT.RADIO);
		centerButton.setText(de.regasus.onlineform.OnlineFormI18N.Center);

		rightButton = new Button(this, SWT.RADIO);
		rightButton.setText(de.regasus.onlineform.OnlineFormI18N.Right);
		
		this.withResponsive = withResponsive;
		
		if (withResponsive) {
			responsiveButton = new Button(this, SWT.RADIO);
			responsiveButton.setText(de.regasus.onlineform.OnlineFormI18N.Responsive);
			responsiveButton.setSelection(true);
		}
	}


	public void reset() {
		leftButton.setSelection(false);
		centerButton.setSelection(false);
		rightButton.setSelection(false);
		if (withResponsive) {
			responsiveButton.setSelection(true);
		}
	}


	public String getAlignment() {
		String result = null;
		if (leftButton.getSelection()) {
			result = "l";
		}
		else if (rightButton.getSelection()) {
			result = "r";
		}
		else if (centerButton.getSelection()) {
			result = "c";
		}
		else if (withResponsive && responsiveButton.getSelection()) {
			result = "s";
		}
		
		return result;
	}


	public void setAlignment(String s) {
		if (s != null) {
			leftButton.setSelection("l".equals(s));
			rightButton.setSelection("r".equals(s));
			centerButton.setSelection("c".equals(s));
			if (withResponsive) {
				responsiveButton.setSelection("s".equals(s));
			}
		}
		else { // null value may come from DB
			if (withResponsive) {
				responsiveButton.setSelection(true);
			}
			else {
				centerButton.setSelection(true);
			}
		}
		
	}

}
