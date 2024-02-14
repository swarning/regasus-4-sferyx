package de.regasus.finance.invoice.command;

import static de.regasus.LookupService.getInvoiceMgr;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.InvoiceMessage;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.ReminderState;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.error.ErrorHandler.ErrorLevel;

import de.regasus.I18N;
import de.regasus.common.dialog.FormatDialog;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.ui.Activator;


public class ShowReminderCommandHandler extends AbstractInvoiceCommandHandler {

	@Override
	public void execute(ExecutionEvent event, Collection<InvoiceVO> invoiceVOs) throws Exception {
		Shell shell = HandlerUtil.getActiveShell(event);

		/*
		 * The only supported formats for invoice (and reminder) templates are ODT and DOC.
		 * ODG is not supported yet.
		 * Reason:
		 * If the templates of the selected invoices have different formats, e.g. ODT and ODG,
		 * it is difficult to determine the available target formats.
		 * We would need the format of the templates to offer either the intersection of the
		 * supported target formats or ask the user for the target format of each template format.
		 */

		boolean allApplicable = isApplicable(invoiceVOs);
		if (allApplicable) {
			// Ask user for desired document format
			FormatDialog formatDialog = new FormatDialog(shell);
			int open = formatDialog.open();

			if (Window.OK == open) {
				String format = formatDialog.getFormat();
				List<Long> invoicePKs = AbstractVO.getPKs(invoiceVOs);

				// No usage of model because no change of entities
				try {
					List<DocumentContainer> reminderDocuments = getInvoiceMgr().getReminderDocuments(
						invoicePKs,
						format
					);

					for (DocumentContainer documentContainer : reminderDocuments) {
						/* save and open generated reminder file
						 * This code is referenced by
						 * https://lambdalogic.atlassian.net/wiki/pages/createpage.action?spaceKey=REGASUS&fromPageId=21987353
						 * Adapt the wiki document if this code is moved to another class or method.
						 */
						documentContainer.open();
					}
				}
				catch (ErrorMessageException e) {
					if (e.getErrorCode().equals(InvoiceMessage.MissingDocumentTemplateForReminder.name())) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e, ErrorLevel.USER);
					}
					else {
						throw e;
					}
				}
			}
		}
		else {
			// This actually shouldn't happen when the FinanceSourceProvider enables the commands correctly
			MessageDialog.openInformation(
				shell,
				UtilI18N.Hint,
				I18N.ActionNotApplicableForAnySelectedInvoice
			);
		}
	}


	public static boolean isApplicable(InvoiceVO invoiceVO) {
		boolean enable =
			invoiceVO.isInvoice() &&
			invoiceVO.getPrint() != null &&
			invoiceVO.getReminderState().ordinal() >= ReminderState.LEVEL1.ordinal() &&
			invoiceVO.getReminderState().ordinal() <= ReminderState.LEVEL5.ordinal() &&
			invoiceVO.isBalanced() == false;

		return enable;
	}


	public static boolean isApplicable(Collection<InvoiceVO> invoiceVOs) {
		boolean allApplicable = true;

		for (InvoiceVO invoiceVO : invoiceVOs) {
			if ( ! isApplicable(invoiceVO)) {
				allApplicable = false;
				break;
			}
		}

		return allApplicable;
	}

}
