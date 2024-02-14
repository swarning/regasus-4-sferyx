package de.regasus.profile.role.view;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.role.editor.ProfileRoleEditor;
import de.regasus.profile.role.editor.ProfileRoleEditorInput;

public class CreateProfileRoleAction extends AbstractAction {
	
	public static final String ID = "de.regasus.profile.action.CreateProfileRoleAction"; 
	
	private final IWorkbenchWindow window;

	public CreateProfileRoleAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.CreateProfileRoleAction_Text);
		setToolTipText(I18N.CreateProfileRoleAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}
	
	
	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		ProfileRoleEditorInput editorInput = new ProfileRoleEditorInput();
		try {
			page.openEditor(editorInput, ProfileRoleEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
