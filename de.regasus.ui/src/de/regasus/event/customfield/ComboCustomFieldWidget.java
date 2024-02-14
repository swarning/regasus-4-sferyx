package de.regasus.event.customfield;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CustomField;

public class ComboCustomFieldWidget extends AbstractCustomFieldSingleListWidget {

	private Combo combo;


	public ComboCustomFieldWidget(Composite parent, CustomField field) {
		super(parent, field);

		setLayout(new FillLayout(SWT.VERTICAL));

		combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);

		combo.add("");
		for (int i = 0; i < count; i++) {
			String label = getLabel(i);
			combo.add(label);
		}
	}


	@Override
	protected void setIndexToSelect(Integer indexToSelect) {
		int index = 0;
		if (indexToSelect != null) {
			index = indexToSelect.intValue() + 1;
		}
		combo.select(index);
	}


	@Override
	public Integer getSelectedIndex() {
		Integer index = null;

		int selectionIndex = combo.getSelectionIndex();
		if (selectionIndex > 0) {
			index = selectionIndex - 1;
		}

		return index;
	}


	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		combo.addModifyListener(modifyListener);
	}


	@Override
	public boolean isGrabHorizontalSpace() {
		return true;
	}

}
