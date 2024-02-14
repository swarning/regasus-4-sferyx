package de.regasus.users.user.dialog;

import java.util.ArrayList;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.auth.api.ACLObject;
import de.regasus.auth.api.ACLObjectDefinitions;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.users.UsersAdministrationHelper;
import de.regasus.users.UsersI18N;
import de.regasus.users.ui.Activator;

public class SelectConstraintTypePage extends WizardPage {

	public static final String NAME = "SelectConstraintTypePage";

	private Button noConstraintType;

	private ArrayList<Button> constaintTypeButtons = new ArrayList<Button>(5);

	private String constraintType;

	private Composite container;

	private boolean dirty;


	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				if (!ModifySupport.isDeselectedRadioButton(event)) {
					constraintType = (String) event.widget.getData();
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	protected SelectConstraintTypePage() {
		super(NAME);

		setTitle(UsersI18N.SelectConstraintType);
	}


	@Override
	public void createControl(Composite parent) {

		container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));

		setControl(container);

		noConstraintType = new Button(container, SWT.RADIO);
		noConstraintType.setText(UsersI18N.NoConstraintType);
		noConstraintType.setSelection(true);
		noConstraintType.addSelectionListener(selectionListener);
	}


	/**
	 * When this page is made visible, show buttons for the constaint types of the right selected on the previous page
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible && dirty) {

			// Is set to true when another ACL object is selected
			dirty = false;

			// First reset the page to initial state:
			// No constraint type specific buttons
			// and no constraint type selected
			for (Button button : constaintTypeButtons) {
				button.dispose();
			}
			constaintTypeButtons.clear();

			constraintType = null;
			noConstraintType.setSelection(true);

			// Now find which right is selected on the previous page
			// an add buttons for the respective constraint types
			SelectACLObjectPage selectACLObjectPage = (SelectACLObjectPage) getWizard().getPage(SelectACLObjectPage.NAME);

			ACLObject aclObject = selectACLObjectPage.getSelectedACLObject();
			if (aclObject.constraintTypes != null) {
				for (String constaintType : aclObject.constraintTypes) {
					createButtonForConstraintType(container, constaintType);
				}
				container.layout();
			}
		}
		super.setVisible(visible);
	}


	private void createButtonForConstraintType(Composite container, String type) {
		Button constraintTypeButton = new Button(container, SWT.RADIO);
		constraintTypeButton.setText(UsersAdministrationHelper.getLabelForConstraintType(type));
		constraintTypeButton.setData(type);
		constraintTypeButton.addSelectionListener(selectionListener);
		constraintTypeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		constaintTypeButtons.add(constraintTypeButton);
	}


	public String getConstraintType() {
		return constraintType;
	}


	@Override
	public IWizardPage getNextPage() {
		if (ACLObjectDefinitions.CONSTRAINT_TYPE_EVENT.equals(constraintType)) {
			return getWizard().getPage(SelectEventConstraintPage.NAME);
		}
		else if (ACLObjectDefinitions.CONSTRAINT_TYPE_REPORT.equals(constraintType)) {
			return getWizard().getPage(SelectReportConstraintPage.NAME);
		}
		else if (ACLObjectDefinitions.CONSTRAINT_TYPE_PROGRAMME_POINT.equals(constraintType)) {
			return getWizard().getPage(SelectEventConstraintPage.NAME);
		}
		else if (ACLObjectDefinitions.CONSTRAINT_TYPE_HOTEL.equals(constraintType)) {
			return getWizard().getPage(SelectEventConstraintPage.NAME);
		}
		else if (ACLObjectDefinitions.CONSTRAINT_TYPE_HOTEL_CONTINGENT.equals(constraintType)) {
			return getWizard().getPage(SelectEventConstraintPage.NAME);
		}
		else {
			return getWizard().getPage(SetCrudRightsAndPriorityPage.NAME);
		}
	}


	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

}
