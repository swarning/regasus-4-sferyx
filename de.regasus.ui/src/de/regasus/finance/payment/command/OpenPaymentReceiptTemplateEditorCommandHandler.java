package de.regasus.finance.payment.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.rcp.Activator;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.payment.editor.PaymentReceiptTemplateEditor;
import de.regasus.finance.payment.editor.PaymentReceiptTemplateEditorInput;

public class OpenPaymentReceiptTemplateEditorCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
				new PaymentReceiptTemplateEditorInput(),
				PaymentReceiptTemplateEditor.ID
			);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t);
		}

		return null;
	}

}
