package de.regasus.users.user.dialog;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.auth.api.ACLObject;
import de.regasus.users.UsersAdministrationHelper;
import de.regasus.users.UsersI18N;

public class SelectACLObjectPage extends WizardPage implements ISelectionChangedListener {
	
	public static final String NAME = "SelectACLObjectPage";
	private ListViewer listViewer;

	protected SelectACLObjectPage() {
		super(NAME);

		setTitle(UsersI18N.SelectRight);
		
	}

	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		
		setControl(container);

		List list = new List(container, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		listViewer = new ListViewer(list);
		
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new ACLObjectLabelProvider());
		listViewer.setInput(UsersAdministrationHelper.getACLObjectsSortedByCurrentLocale());
		
		listViewer.addSelectionChangedListener(this);
		
		setPageComplete(false);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		boolean somethingSelected = ! listViewer.getSelection().isEmpty();
		
		SelectConstraintTypePage constraintTypePage = (SelectConstraintTypePage) getWizard().getPage(SelectConstraintTypePage.NAME);
		constraintTypePage.setDirty(true);
		
		setPageComplete(somethingSelected);
	}
	
	public ACLObject getSelectedACLObject() {
		return SelectionHelper.getUniqueSelected(listViewer.getSelection());
	}
	
	@Override
	public IWizardPage getNextPage() {
		
		ACLObject aclObject = getSelectedACLObject();
		if (aclObject != null && aclObject.constraintTypes != null && aclObject.constraintTypes.length > 0) {
			return getWizard().getPage(SelectConstraintTypePage.NAME);
		} else {
			return getWizard().getPage(SetCrudRightsAndPriorityPage.NAME);
		}
	}
}
