package de.regasus.finance.currency.view;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.currency.editor.CurrencyEditor;
import de.regasus.finance.currency.editor.CurrencyEditorInput;

public class CreateCurrencyAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.CreateCurrencyAction"; 

	private final IWorkbenchWindow window;
	
	
	public CreateCurrencyAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(FinanceI18N.Currency_Action_Create_Text);
		setToolTipText(FinanceI18N.Currency_Action_Create_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}

	
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		CurrencyEditorInput editorInput = new CurrencyEditorInput();
		try {
			page.openEditor(editorInput, CurrencyEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
