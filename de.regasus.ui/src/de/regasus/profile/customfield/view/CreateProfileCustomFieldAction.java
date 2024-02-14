package de.regasus.profile.customfield.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.customfield.editor.ProfileCustomFieldEditor;
import de.regasus.profile.customfield.editor.ProfileCustomFieldEditorInput;
import de.regasus.ui.Activator;

class CreateProfileCustomFieldAction extends AbstractAction implements ISelectionListener {

	private final IWorkbenchWindow window;

	private Long groupID;

	CreateProfileCustomFieldAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);

		this.window = window;
		setId(getClass().getName());
		setText(I18N.CreateProfileCustomField);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			"icons/customfield_add.png"
		));
		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();

		ProfileCustomFieldEditorInput editorInput = ProfileCustomFieldEditorInput.getCreateInstance(groupID);
		try {
			page.openEditor(editorInput, ProfileCustomFieldEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		this.groupID = null;
		if (part instanceof ProfileCustomFieldTreeView && selection instanceof IStructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;

			Object element = structuredSelection.getFirstElement();
			if (element instanceof ProfileCustomFieldGroupTreeNode) {
				groupID = ((ProfileCustomFieldGroupTreeNode) element).getProfileCustomFieldGroupID();
			}
		}
		// MIRCP-2635 - Error when creating ProfileCustomField without -Group
		setEnabled(groupID != null);
	}

}
