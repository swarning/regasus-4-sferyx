package de.regasus.finance.customeraccount.view;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.customeraccount.editor.CustomerAccountEditor;
import de.regasus.finance.customeraccount.editor.CustomerAccountEditorInput;
import de.regasus.ui.Activator;

public class CreateCustomerAccountAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.CreateCustomerAccountAction"; 

	private final IWorkbenchWindow window;
	
	
	public CreateCustomerAccountAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(de.regasus.finance.FinanceI18N.CustomerAccount_Action_Create_Text);
		setToolTipText(de.regasus.finance.FinanceI18N.CustomerAccount_Action_Create_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}

	
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		CustomerAccountEditorInput editorInput = new CustomerAccountEditorInput();
		try {
			page.openEditor(editorInput, CustomerAccountEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
