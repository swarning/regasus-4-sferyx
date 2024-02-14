package de.regasus.onlineform.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.regasus.Field;

import de.regasus.onlineform.OnlineFormI18N;

/**
 * A composite showing three radio buttons labelled with "Required", "Optional" and "Invisible" (with i18n); it is used to show and
 * edit the value of a {@link Field} variable which in turn serves for configuring a form field of the registration form.
 * <p>
 * @author manfred
 * 
 */
public class FieldConfigComposite extends Composite {

	private Button invisibleButton;

	private Button optionalButton;

	private Button requiredButton;

	private boolean requiredAndNotEditable;

	public void addSelectionListener(SelectionListener selectionListener) {
		invisibleButton.addSelectionListener(selectionListener);
		optionalButton.addSelectionListener(selectionListener);
		requiredButton.addSelectionListener(selectionListener);
		
	}
	
	public FieldConfigComposite(Composite parent, int style) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);

		requiredButton = new Button(this, SWT.RADIO);
		requiredButton.setText(OnlineFormI18N.Required);

		optionalButton = new Button(this, SWT.RADIO);
		optionalButton.setText(OnlineFormI18N.Optional);

		invisibleButton = new Button(this, SWT.RADIO);
		invisibleButton.setText(OnlineFormI18N.Invisible);

	}


	public void deselectAll() {
		invisibleButton.setSelection(false);
		requiredButton.setSelection(false);
		optionalButton.setSelection(false);
	}


	public void setRequiredAndNotEditable() {
		this.requiredAndNotEditable = true;
		invisibleButton.setEnabled(false);
		optionalButton.setEnabled(false);
		

		deselectAll();
		requiredButton.setSelection(true);
	}

	public void setEditable() {
		this.requiredAndNotEditable = false;
		invisibleButton.setEnabled(true);
		optionalButton.setEnabled(true);
	}

	
	public void setField(Field field) {
		if (requiredAndNotEditable) {
			return;
		}
		
		
		deselectAll();
		if (field == null) {
			optionalButton.setSelection(true);
		}
		else {
			switch (field) {
			case INV:
				invisibleButton.setSelection(true);
				break;
			case OPT:
				optionalButton.setSelection(true);
				break;
			case REQ:
				requiredButton.setSelection(true);
				break;
			}
		}
	}


	public Field getField() {
		if (requiredAndNotEditable) {
			return Field.REQ;
		}
		
		if (invisibleButton.getSelection()) {
			return Field.INV;
		}
		if (optionalButton.getSelection()) {
			return Field.OPT;
		}
		if (requiredButton.getSelection()) {
			return Field.REQ;
		}
		return null;
	}

}
