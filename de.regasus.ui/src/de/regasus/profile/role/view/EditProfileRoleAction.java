package de.regasus.profile.role.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.profile.ProfileRole;
import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.I18N;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.role.editor.ProfileRoleEditor;
import de.regasus.profile.role.editor.ProfileRoleEditorInput;

public class EditProfileRoleAction  extends AbstractAction implements ISelectionListener {
	
	public static final String ID = "de.regasus.profile.action.EditProfileRoleAction";

	private final IWorkbenchWindow window;
	
	private Long profileRoleID = null;
	
	
	public EditProfileRoleAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		setId(ID);
		setText(I18N.EditProfileRoleAction_Text);
		setToolTipText(I18N.EditProfileRoleAction_ToolTip);
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
		if (profileRoleID != null) {
			IWorkbenchPage page = window.getActivePage();
			ProfileRoleEditorInput editorInput = new ProfileRoleEditorInput(profileRoleID);
			try {
				page.openEditor(editorInput, ProfileRoleEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		super.run();
	}
	
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		profileRoleID = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object selectedObject = selection.getFirstElement();
				if (selectedObject instanceof ProfileRole) {
					profileRoleID = ((ProfileRole) selectedObject).getID();
				}
			}
		}
		setEnabled(profileRoleID != null);
	}

}
