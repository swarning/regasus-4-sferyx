package de.regasus.finance.paymentsystem.view;

import java.lang.invoke.MethodHandles;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.paymentsystem.editor.PaymentSystemSetupEditor;
import de.regasus.finance.paymentsystem.editor.PaymentSystemSetupEditorInput;

public class CreatePaymentSystemSetupForEasyCheckoutAction extends AbstractAction {

	public static final String ID = MethodHandles.lookup().lookupClass().getName();

	private final IWorkbenchWindow window;


	public CreatePaymentSystemSetupForEasyCheckoutAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(FinanceI18N.PaymentSystemSetup_Action_Create_EasyCheckout_Text);
		setToolTipText(FinanceI18N.PaymentSystemSetup_Action_Create_EasyCheckout_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.CREATE
		));
	}


	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		PaymentSystemSetupEditorInput editorInput = PaymentSystemSetupEditorInput.buildCreateEasyCheckoutInstance();
		try {
			page.openEditor(editorInput, PaymentSystemSetupEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
