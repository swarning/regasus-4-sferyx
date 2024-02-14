package de.regasus.finance.paymentsystem.view;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.PaymentSystemSetup;
import de.regasus.finance.paymentsystem.editor.PaymentSystemSetupEditor;
import de.regasus.finance.paymentsystem.editor.PaymentSystemSetupEditorInput;
import de.regasus.ui.Activator;

public class EditPaymentSystemSetupAction extends AbstractAction implements ISelectionListener {

	public static final String ID = MethodHandles.lookup().lookupClass().getName();

	private final IWorkbenchWindow window;

	private Long paymentSystemSetupPK = null;


	public EditPaymentSystemSetupAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);

		this.window = window;
		setId(ID);
		setText(FinanceI18N.PaymentSystemSetup_Action_Edit_Text);
		setToolTipText(FinanceI18N.PaymentSystemSetup_Action_Edit_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
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
		if (paymentSystemSetupPK != null) {
			IWorkbenchPage page = window.getActivePage();
			PaymentSystemSetupEditorInput editorInput = PaymentSystemSetupEditorInput.buildEditInstance(paymentSystemSetupPK);
			try {
				page.openEditor(editorInput, PaymentSystemSetupEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		paymentSystemSetupPK = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object selectedObject = selection.getFirstElement();
				if (selectedObject instanceof PaymentSystemSetup) {
					paymentSystemSetupPK = ((PaymentSystemSetup) selectedObject).getId();
				}
			}
		}
		setEnabled(paymentSystemSetupPK != null);
	}

}
