package de.regasus.profile.relationtype.view;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.relationtype.editor.ProfileRelationTypeEditor;
import de.regasus.profile.relationtype.editor.ProfileRelationTypeEditorInput;

public class CreateProfileRelationTypeAction extends AbstractAction {
	
	public static final String ID = "de.regasus.profile.action.CreateProfileRelationTypeAction"; 
	
	private final IWorkbenchWindow window;

	public CreateProfileRelationTypeAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.CreateProfileRelationTypeAction_Text);
		setToolTipText(I18N.CreateProfileRelationTypeAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}
	
	
	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		ProfileRelationTypeEditorInput editorInput = new ProfileRelationTypeEditorInput();
		try {
			page.openEditor(editorInput, ProfileRelationTypeEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
