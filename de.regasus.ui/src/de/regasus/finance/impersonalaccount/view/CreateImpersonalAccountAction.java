package de.regasus.finance.impersonalaccount.view;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.impersonalaccount.editor.ImpersonalAccountEditor;
import de.regasus.finance.impersonalaccount.editor.ImpersonalAccountEditorInput;
import de.regasus.ui.Activator;

public class CreateImpersonalAccountAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.CreateImpersonalAccountAction"; 

	private final IWorkbenchWindow window;
	
	
	public CreateImpersonalAccountAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(de.regasus.finance.FinanceI18N.ImpersonalAccount_Action_Create_Text);
		setToolTipText(de.regasus.finance.FinanceI18N.ImpersonalAccount_Action_Create_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}

	
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		ImpersonalAccountEditorInput editorInput = new ImpersonalAccountEditorInput();
		try {
			page.openEditor(editorInput, ImpersonalAccountEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
