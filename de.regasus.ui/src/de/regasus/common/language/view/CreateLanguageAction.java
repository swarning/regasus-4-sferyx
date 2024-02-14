package de.regasus.common.language.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.common.language.editor.LanguageEditor;
import de.regasus.common.language.editor.LanguageEditorInput;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;

public class CreateLanguageAction
extends Action 
implements ActionFactory.IWorkbenchAction {

	public static final String ID = "de.regasus.core.ui.language.CreateLanguageAction"; 

	private final IWorkbenchWindow window;
	
	
	public CreateLanguageAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.CreateLanguageAction_Text);
		setToolTipText(I18N.CreateLanguageAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}
	

	public void dispose() {
	}

	
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		LanguageEditorInput editorInput = new LanguageEditorInput();
		try {
			page.openEditor(editorInput, LanguageEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
