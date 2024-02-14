package de.regasus.profile.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.StringHelper;

/**
 * Shows a row for one profile that might be used as second person because it is an associated Profile
 */
public class RelatedProfileRow extends Composite {

	private Profile profile;
	private Link link;
	private Button button;
	
	
	
	public RelatedProfileRow(Composite parent) {
		super(parent, SWT.NONE);
		
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 2;
		setLayout(gridLayout);
		
		link = new Link(this, SWT.NONE);
		link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openEditor();
				}
				catch (PartInitException e1) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e1);
				}
			}
		});
		
		button = new Button(this, SWT.CHECK);
		button.setToolTipText(ContactLabel.secondPerson_description.getString());
		button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
	}

	
	public void addSelectionListener(SelectionListener selectionListener) {
		button.addSelectionListener(selectionListener);
	}
	
	
	public void setProfileAndRole(Profile profile, String role) {
		this.profile = profile;
		
		setLinkText(profile, role);
		button.setData(profile);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void openEditor() throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ProfileEditorInput editorInput = new ProfileEditorInput(profile.getID());
		page.openEditor(editorInput, ProfileEditor.ID);
	}


	public void updateForSecondPerson(Profile secondPerson) {
		if (secondPerson == null) {
			button.setSelection(false);
		}
		else {
			boolean sameSecondPerson = secondPerson.getID().equals(profile.getID());
			button.setSelection(sameSecondPerson);
		}
		
	}


	protected void setLinkText(Profile profile, String role) {
		StringBuilder linkText = new StringBuilder(200);
		
		linkText.append("<A>");
		linkText.append(profile.getName(true));
		if (StringHelper.isNotEmpty(role)) {
			linkText.append("(");
			linkText.append(role);
			linkText.append(")");
		}
		linkText.append("</A>");
		
		link.setText(linkText.toString());
	}


	public Profile getProfile() {
		return profile;
	}
	
}
