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

import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroupLocation;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.customfield.editor.ProfileCustomFieldGroupEditor;
import de.regasus.profile.customfield.editor.ProfileCustomFieldGroupEditorInput;
import de.regasus.ui.Activator;

public class CreateProfileCustomFieldGroupAction extends AbstractAction implements ISelectionListener {

	private final IWorkbenchWindow window;
	private ProfileCustomFieldGroupLocation location;


	public CreateProfileCustomFieldGroupAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(getClass().getName());
		setText(I18N.CreateProfileCustomFieldGroup);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			"icons/customfield_group_add.png"));
		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		ProfileCustomFieldGroupEditorInput editorInput = ProfileCustomFieldGroupEditorInput.getCreateInstance(location);
		try {
			page.openEditor(editorInput, ProfileCustomFieldGroupEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		this.location = null;
		if (part instanceof ProfileCustomFieldTreeView && selection instanceof IStructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;

			Object element = structuredSelection.getFirstElement();
			if (element instanceof ProfileCustomFieldGroupLocationTreeNode) {
				this.location = ((ProfileCustomFieldGroupLocationTreeNode) element).getValue();
			}
		}
		setEnabled(location != null);
	}

}
