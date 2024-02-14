/**
 * EditParticipantStateAction.java
 * Created on 16.04.2012
 */
package de.regasus.participant.state.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.ParticipantState;
import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.I18N;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.participant.state.editor.ParticipantStateEditor;
import de.regasus.participant.state.editor.ParticipantStateEditorInput;


public class EditParticipantStateAction
extends Action
implements IWorkbenchAction, ISelectionListener {

	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.participantState.EditParticipantStateAction";
	
	private final IWorkbenchWindow window;
	
	private Long participantStateID = null;
	
	public EditParticipantStateAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.EditParticipantStateAction_Text);
		setToolTipText(I18N.EditParticipantStateAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.EDIT
		));
		
		window.getSelectionService().addSelectionListener(this);
	}
	
	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	public void run() {
		if (participantStateID != null) {
			IWorkbenchPage page = window.getActivePage();
			ParticipantStateEditorInput editorInput = new ParticipantStateEditorInput(participantStateID);
			try {
				page.openEditor(editorInput, ParticipantStateEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		participantStateID = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			if (sselection.size() == 1) {
				Object seletedObject = sselection.getFirstElement();
				if (seletedObject instanceof ParticipantState) {
					participantStateID = ((ParticipantState)seletedObject).getID();
				}
			}
		}
		setEnabled(participantStateID != null);
	}

}
