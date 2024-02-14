package de.regasus.event.customfield;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;

public class RadioButtonsCustomFieldWidget extends AbstractCustomFieldSingleListWidget {

	private Button[] buttons;


	public RadioButtonsCustomFieldWidget(Composite parent, CustomField field) {
		super(parent, field);

		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.wrap = true;
		rowLayout.pack = false; // all widgets are the same size
		setLayout(rowLayout);

		buttons = new Button[count];

		for (int i = 0; i < count; i++) {
			buttons[i] = new Button(this, SWT.RADIO);
			String label = getLabel(i);
			label = StringHelper.shorten(label, 50);
			buttons[i].setText(label);
		}
	}


	@Override
	protected void setIndexToSelect(Integer indexToSelect) {
		for (int i = 0; i < count; i++) {
			boolean selected = indexToSelect != null && indexToSelect== i;
			buttons[i].setSelection(selected);
		}
	}


	@Override
	protected Integer getSelectedIndex() {
		Integer index = null;
		for (int i = 0; i < count; i++) {
			if (buttons[i].getSelection()) {
				index = i;
				break;
			}
		}
		return index;
	}


	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		SelectionListener listenerAdapter = new ModifyListenerAdapter(modifyListener);

		for (int i = 0; i < count; i++) {
			buttons[i].addSelectionListener(listenerAdapter);
		}
	}


	/**
	 * Only set focus in selected radio button, because if none is yet selected,
	 * the first one gets selected (even if never has answered that
	 * question in online form) and also fires a selection event and makes the
	 * editor dirty. (MIRCP-1115)
	 */
	@Override
	public boolean setFocus() {
		boolean focusSet = false;

		for (int i = 0; i < count; i++) {
			if (buttons[i].getSelection()) {
				focusSet = buttons[i].setFocus();
				break;
			}
		}

		if (!focusSet) {
			/* On MacOSX the focus can be set to the first button without selecting it
			 * On WIndows the button would be selected.
			 * On Linux, I don't know.
			 */
    		if (SystemHelper.isMacOSX()) {
    			focusSet = buttons[0].setFocus();
    		}
		}

		return focusSet;
	}


	@Override
	public boolean isGrabHorizontalSpace() {
		return true;
	}

}
