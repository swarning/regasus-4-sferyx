package de.regasus.participant.type.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.participant.type.editor.ParticipantTypeEditor;
import de.regasus.participant.type.editor.ParticipantTypeEditorInput;

public class CreateParticipantTypeAction
extends Action 
implements ActionFactory.IWorkbenchAction {

	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.participantType.CreateParticipantTypeAction"; 

	private final IWorkbenchWindow window;
	
	
	public CreateParticipantTypeAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.CreateParticipantTypeAction_Text);
		setToolTipText(I18N.CreateParticipantTypeAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}
	

	public void dispose() {
	}

	
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		ParticipantTypeEditorInput editorInput = new ParticipantTypeEditorInput();
		try {
			page.openEditor(editorInput, ParticipantTypeEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
