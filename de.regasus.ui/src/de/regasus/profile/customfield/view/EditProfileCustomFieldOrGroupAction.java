package de.regasus.profile.customfield.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.customfield.editor.ProfileCustomFieldEditor;
import de.regasus.profile.customfield.editor.ProfileCustomFieldEditorInput;
import de.regasus.profile.customfield.editor.ProfileCustomFieldGroupEditor;
import de.regasus.profile.customfield.editor.ProfileCustomFieldGroupEditorInput;

class EditProfileCustomFieldOrGroupAction extends AbstractAction implements ISelectionListener {

	private final IWorkbenchWindow window;

	private TreeNode<?> node;

	EditProfileCustomFieldOrGroupAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);

		this.window = window;
		setId(getClass().getName());
		setText(UtilI18N.Edit);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.EDIT));

		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	@Override
	public void run() {
		try {
			if (node instanceof ProfileCustomFieldGroupTreeNode) {
				ProfileCustomFieldGroup customFieldGroup = ((ProfileCustomFieldGroupTreeNode) node).getValue();
				ProfileCustomFieldGroupEditorInput editorInput =
					ProfileCustomFieldGroupEditorInput.getEditInstance(customFieldGroup.getID());
				window.getActivePage().openEditor(editorInput, ProfileCustomFieldGroupEditor.ID);
			}
			else if (node instanceof ProfileCustomFieldTreeNode) {
				ProfileCustomField customField = ((ProfileCustomFieldTreeNode) node).getValue();
				ProfileCustomFieldEditorInput editorInput =
					ProfileCustomFieldEditorInput.getEditInstance(customField.getID());
				window.getActivePage().openEditor(editorInput, ProfileCustomFieldEditor.ID);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		boolean enabled = false;

		if (part instanceof ProfileCustomFieldTreeView) {
			node = SelectionHelper.getUniqueSelected(incoming);
			if (node != null &&
				(	node instanceof ProfileCustomFieldGroupTreeNode ||
					node instanceof ProfileCustomFieldTreeNode
				)
			) {
				enabled = true;
			}
			setToolTipText(I18N.EventMasterDataEditAction_ToolTip_Generic);
		}

		setEnabled(enabled);
	}
}
