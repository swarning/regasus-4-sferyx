package de.regasus.common.country.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.common.Country;
import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.I18N;
import de.regasus.common.country.editor.CountryEditor;
import de.regasus.common.country.editor.CountryEditorInput;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;

public class EditCountryAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener {
	public static final String ID = "de.regasus.core.ui.country.EditCountryAction";

	private final IWorkbenchWindow window;

	private String countryId = null;


	public EditCountryAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.EditCountryAction_Text);
		setToolTipText(I18N.EditCountryAction_ToolTip);
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
		if (countryId != null) {
			IWorkbenchPage page = window.getActivePage();
			CountryEditorInput editorInput = new CountryEditorInput(countryId);
			try {
				page.openEditor(editorInput, CountryEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		countryId = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object selectedObject = selection.getFirstElement();
				if (selectedObject instanceof Country) {
					countryId = ((Country) selectedObject).getId();
				}
			}
		}
		setEnabled(countryId != null);
	}

}
