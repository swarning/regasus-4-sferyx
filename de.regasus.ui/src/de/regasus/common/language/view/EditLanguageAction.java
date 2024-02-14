package de.regasus.common.language.view;

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

import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.common.language.editor.LanguageEditor;
import de.regasus.common.language.editor.LanguageEditorInput;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;

public class EditLanguageAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener {
	public static final String ID = "de.regasus.core.ui.language.EditLanguageAction";

	private final IWorkbenchWindow window;

	private String languageCode = null;


	public EditLanguageAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.EditLanguageAction_Text);
		setToolTipText(I18N.EditLanguageAction_ToolTip);
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
		if (languageCode != null) {
			IWorkbenchPage page = window.getActivePage();
			LanguageEditorInput editorInput = new LanguageEditorInput(languageCode);
			try {
				page.openEditor(editorInput, LanguageEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		languageCode = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object selectedObject = selection.getFirstElement();

				if (selectedObject instanceof Language) {
					languageCode = ((Language) selectedObject).getId();
				}
			}
		}
		setEnabled(languageCode != null);
	}

}
