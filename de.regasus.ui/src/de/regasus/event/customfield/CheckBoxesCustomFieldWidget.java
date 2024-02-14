package de.regasus.event.customfield;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;

public class CheckBoxesCustomFieldWidget extends AbstractCustomFieldListWidget {

	private Button[] buttons;
	private Integer max;

	public CheckBoxesCustomFieldWidget(Composite parent, CustomField field) {
		super(parent, field);

//		setLayout(new FillLayout(SWT.VERTICAL));

		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.wrap = true;
		rowLayout.pack = false; // all widgets are the same size
		setLayout(rowLayout);

		buttons = new Button[count];

		for (int i = 0; i < count; i++) {
			buttons[i] = new  Button(this, SWT.CHECK);
			String label = getLabel(i);
			label = StringHelper.shorten(label, 100);
			buttons[i].setText(label);
		}

		// MIRCP-1327: If max has a value of e.g. 3, the user should 
		// not be able to select more than 3 values.
		max = field.getMax();
		if (max != null) {
			addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					handleMax();
				}
			});
		}
	}
	
	
	private void handleMax() {
		if (max != null) {
    		int selectionCount = getSelectionCount();
    		if (selectionCount == max) {
    			disableNotSelectedButtons();
    		}
    		else {
    			enableAllButtons();
    		}
		}
	}
	
	
	private void disableNotSelectedButtons() {
		for (Button button : buttons) {
			if (!button.getSelection()) {
				button.setEnabled(false);
			}
		}
	}
	
	
	private void enableAllButtons() {
		for (Button button : buttons) {
			button.setEnabled(true);
		}
	}

	
	private int getSelectionCount() {
		int count = 0;
		for (Button button : buttons) {
			if (button.getSelection()) {
				count++;
			}
		}
		return count;
	}
	

	@Override
	protected void setIndicesToSelect(java.util.List<Integer> indicesToSelect) {
		for (int i = 0; i < count; i++) {
			buttons[i].setSelection(false);
		}

		for (Integer integer : indicesToSelect) {
			buttons[integer].setSelection(true);
		}
		
		handleMax();
	}


	@Override
	public List<Integer> getSelectedIndices() {
		List<Integer> selectedIndices = new ArrayList<Integer>(count);
		for (int i = 0; i < count; i++) {
			if (buttons[i].getSelection()) {
				selectedIndices.add(i);
			}
		}
		return selectedIndices;
	}


	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		SelectionListener listenerAdapter = new ModifyListenerAdapter(modifyListener);

		for (int i = 0; i < count; i++) {
			buttons[i].addSelectionListener(listenerAdapter);
		}
	}


	@Override
	public boolean isGrabHorizontalSpace() {
		return true;
	}

}
