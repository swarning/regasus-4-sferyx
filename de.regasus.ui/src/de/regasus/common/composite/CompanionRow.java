package de.regasus.common.composite;

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
import com.lambdalogic.messeinfo.participant.Participant;

import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;

/**
 * Shows a row for one person that might be used as second person because it is a companion 
 * (or in future an associated Profile),
 */
public class CompanionRow extends Composite 
//implements CacheModelListener<Long>  
{

	private Participant companion;
	private Link link;
	private Button button;
	
	
	public CompanionRow(Composite parent) {
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
	
	
	public void setCompanion(Participant participant) {
		this.companion = participant;
		
		setLinkText(participant);
		button.setData(participant);
	}

	@Override
	public void dispose() {
//		participantModel.removeListener(this);
		super.dispose();
	}

	public void openEditor() throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ParticipantEditorInput editorInput = ParticipantEditorInput.getEditInstance(companion.getID());
		page.openEditor(editorInput, ParticipantEditor.ID);
	}


	public void updateForSecondPerson(Participant secondPerson) {
		if (secondPerson == null) {
			button.setSelection(false);
		}
		else {
			boolean sameSecondPerson = secondPerson.getID().equals(companion.getID());
			button.setSelection(sameSecondPerson);
		}
		
	}


	protected void setLinkText(Participant participant) {
		String linkText = "<A>" + participant.getName(true) + " (" + participant.getNumber() + ")</A>";
		link.setText(linkText);
	}


	public Participant getCompanion() {
		return companion;
	}
	
}
