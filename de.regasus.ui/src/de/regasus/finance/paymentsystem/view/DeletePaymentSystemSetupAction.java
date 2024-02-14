package de.regasus.finance.paymentsystem.view;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.PaymentSystemSetup;
import de.regasus.finance.PaymentSystemSetupModel;
import de.regasus.finance.paymentsystem.editor.PaymentSystemSetupEditor;
import de.regasus.ui.Activator;

public class DeletePaymentSystemSetupAction extends AbstractAction implements ISelectionListener {

	public static final String ID = MethodHandles.lookup().lookupClass().getName();

	private final IWorkbenchWindow window;
	private List<PaymentSystemSetup> selectedPaymentSystemSetups = new ArrayList<>();



	public DeletePaymentSystemSetupAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);

		this.window = window;
		setId(ID);
		setText(FinanceI18N.PaymentSystemSetup_Action_Delete_Text);
		setToolTipText(FinanceI18N.PaymentSystemSetup_Action_Delete_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			IImageKeys.DELETE
		));

		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	@Override
	public void run() {
		if (!selectedPaymentSystemSetups.isEmpty()) {
			// Auftragsbestätigung
			boolean deleteOK = false;
			if (selectedPaymentSystemSetups.size() == 1) {
				final String title = UtilI18N.Question;
				String message = FinanceI18N.PaymentSystemSetup_Action_Delete_ConfirmationMessage;
				// Im Abfragetext den Namen des zu löschenden Landes einfügen
				final PaymentSystemSetup paymentSystemSetup = selectedPaymentSystemSetups.get(0);
				String name = paymentSystemSetup.getName();
				if (name == null) name = "";
				message = message.replaceFirst("<name>", name);
				// Dialog öffnen
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				final String title = UtilI18N.Question;
				String message = FinanceI18N.PaymentSystemSetup_Action_DeleteList_ConfirmationMessage;
				// Open the Dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}

			// If the user answered 'Yes' in the dialog
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {
						/* Iterate over copy, because selectedPayEngineSetups will be
						 * indirectly update while deleteting the entities via the model.
						 * After deleting there're no entities selected.
						 */
						for (PaymentSystemSetup paymentSystemSetup : new ArrayList<>(selectedPaymentSystemSetups)) {
							try {
								PaymentSystemSetupModel.getInstance().delete(paymentSystemSetup);
								// find editors and close them
								PaymentSystemSetupEditor.closeEditor( paymentSystemSetup.getId() );
							}
							catch (Throwable t) {
								String message = FinanceI18N.PaymentSystemSetup_Action_Delete_ErrorMessage;
								final String name = paymentSystemSetup.getName();
								message = message.replaceFirst("<name>", name);
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, message);
								return;
							}
						}
					}

				});
			}
		}
	}


	@Override
	@SuppressWarnings("rawtypes")
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedPaymentSystemSetups.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;

			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object selectedElement = it.next();
				if (selectedElement instanceof PaymentSystemSetup) {
					PaymentSystemSetup paymentSystemSetup = (PaymentSystemSetup) selectedElement;
					selectedPaymentSystemSetups.add(paymentSystemSetup);
				}
				else {
					selectedPaymentSystemSetups.clear();
					break;
				}
			}
		}
		setEnabled(!selectedPaymentSystemSetups.isEmpty());
	}

}
