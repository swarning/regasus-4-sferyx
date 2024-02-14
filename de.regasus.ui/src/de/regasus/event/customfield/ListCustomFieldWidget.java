package de.regasus.event.customfield;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import com.lambdalogic.messeinfo.contact.CustomField;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;

public class ListCustomFieldWidget extends AbstractCustomFieldListWidget {

	private List list;

	/**
	 *  Stores the indices of all previous (validly) selected items, as i cannot
	 *  programmatically find out which row was selected to de-select it in case
	 *  it was one row to much.
	 */
	private int[] selectionIndices = new int[0];


	public ListCustomFieldWidget(Composite parent, CustomField field) {
		super(parent, field);

		setLayout(new FillLayout(SWT.VERTICAL));

		list = new List(this, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);

		// MIRCP-1327: If max has a value of e.g. 3, the user should
		// not be able to select more than 3 values.
		final Integer max = field.getMax();
		if (max != null) {
			list.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					// If too many items are selected, reverse to previous selection
					if (list.getSelectionCount() > max.intValue()) {
						list.setSelection(selectionIndices);
					}

					//  Stores the indices of validly selected items
					selectionIndices = list.getSelectionIndices();
				}
			});
		}

		for (int i = 0; i < count; i++) {
			list.add(getLabel(i));
		}
	}


	@Override
	protected void setIndicesToSelect(java.util.List<Integer> indicesToSelect) {
		for (int i = 0; i < count; i++) {
			list.deselect(i);
		}

		for (Integer integer : indicesToSelect) {
			list.select(integer.intValue());
		}
	}


	@Override
	public java.util.List<Integer> getSelectedIndices() {
		int[] selectionIndices = list.getSelectionIndices();
		java.util.List<Integer> selectedIndices = new ArrayList<>(selectionIndices.length);
		for (int index : selectionIndices) {
			selectedIndices.add(index);
		}
		return selectedIndices;
	}


	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		SelectionListener listenerAdapter = new ModifyListenerAdapter(modifyListener);
		list.addSelectionListener(listenerAdapter);
	}


	@Override
	public boolean isGrabHorizontalSpace() {
		return true;
	}

}
