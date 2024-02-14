package de.regasus.common.country.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.common.country.editor.CountryEditor;
import de.regasus.common.country.editor.CountryEditorInput;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;

public class CreateCountryAction
extends Action 
implements ActionFactory.IWorkbenchAction {

	public static final String ID = "de.regasus.core.ui.country.CreateCountryAction"; 

	private final IWorkbenchWindow window;
	
	
	public CreateCountryAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.CreateCountryAction_Text);
		setToolTipText(I18N.CreateCountryAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}
	

	public void dispose() {
	}

	
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		CountryEditorInput editorInput = new CountryEditorInput();
		try {
			page.openEditor(editorInput, CountryEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
