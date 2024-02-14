package de.regasus.finance.creditcardtype.view;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.creditcardtype.editor.CreditCardTypeEditor;
import de.regasus.finance.creditcardtype.editor.CreditCardTypeEditorInput;
import de.regasus.ui.Activator;

public class CreateCreditCardTypeAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.CreateCreditCardTypeAction"; 

	private final IWorkbenchWindow window;
	
	
	public CreateCreditCardTypeAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(de.regasus.finance.FinanceI18N.CreditCardType_Action_Create_Text);
		setToolTipText(de.regasus.finance.FinanceI18N.CreditCardType_Action_Create_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}

	
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		CreditCardTypeEditorInput editorInput = new CreditCardTypeEditorInput();
		try {
			page.openEditor(editorInput, CreditCardTypeEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
