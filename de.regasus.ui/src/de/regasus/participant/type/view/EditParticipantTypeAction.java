package de.regasus.participant.type.view;

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

import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.I18N;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.event.ParticipantType;
import de.regasus.participant.type.editor.ParticipantTypeEditor;
import de.regasus.participant.type.editor.ParticipantTypeEditorInput;

public class EditParticipantTypeAction
extends Action
implements IWorkbenchAction, ISelectionListener {
	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.participantType.EditParticipantTypeAction"; 

	private final IWorkbenchWindow window;

	private Long participantTypePK = null;


	public EditParticipantTypeAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.EditParticipantTypeAction_Text);
		setToolTipText(I18N.EditParticipantTypeAction_ToolTip);
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


	@Override
	public void run() {
		if (participantTypePK != null) {
			IWorkbenchPage page = window.getActivePage();
			ParticipantTypeEditorInput editorInput = new ParticipantTypeEditorInput(participantTypePK);
			try {
				page.openEditor(editorInput, ParticipantTypeEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		participantTypePK = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object selectedObject = selection.getFirstElement();
				if (selectedObject instanceof ParticipantType) {
					participantTypePK = ((ParticipantType) selectedObject).getId();
				}
			}
		}
		setEnabled(participantTypePK != null);
	}

}
