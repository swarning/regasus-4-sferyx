/**
 * CreateParticipantStateAction.java
 * Created on 16.04.2012
 */
package de.regasus.participant.state.view;

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
import de.regasus.participant.state.editor.ParticipantStateEditor;
import de.regasus.participant.state.editor.ParticipantStateEditorInput;

/**
 * @author huuloi
 *
 */
public class CreateParticipantStateAction 
extends Action
implements ActionFactory.IWorkbenchAction {

	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.participantState.CreateParticipantStateAction"; 
	
	private final IWorkbenchWindow window;
	
	public CreateParticipantStateAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.CreateParticipantStateAction_Text);
		setToolTipText(I18N.CreateParticipantStateAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}
	
	public void dispose() {
	}
	
	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		ParticipantStateEditorInput editorInput = new ParticipantStateEditorInput();
		try {
			page.openEditor(editorInput, ParticipantStateEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
}
