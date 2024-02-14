package de.regasus.finance.payment.dialog;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateComparator;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailTemplateModel;
import de.regasus.email.template.EmailTemplateSearchTable;
import de.regasus.ui.Activator;


public class SendPaymentConfirmationDialog extends TitleAreaDialog {

	public static final String DIALOG_SETTING_KEY_EMAIL_PAYMENT_TEMPLATE_ID =
		EmailTemplateSelectionPage.DIALOG_SETTING_KEY_EMAIL_PAYMENT_TEMPLATE_ID;

	public static final String DIALOG_SETTING_KEY_EMAIL_REFUND_TEMPLATE_ID =
		EmailTemplateSelectionPage.DIALOG_SETTING_KEY_EMAIL_REFUND_TEMPLATE_ID;


	private IDialogSettings dialogSettings;

	private EmailTemplateModel emailTemplateModel;

	private PaymentVO paymentVO;
	private Long emailTemplateID;

	private Button okButton;
	private EmailTemplateSearchTable emailTemplateTable;


	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public SendPaymentConfirmationDialog(Shell parentShell, PaymentVO paymentVO) {
		super(parentShell);

		this.paymentVO = paymentVO;

		dialogSettings = Activator.getDefault().getDialogSettings();

		emailTemplateModel = EmailTemplateModel.getInstance();
	}


	private boolean isPayment() {
		return paymentVO.getAmount().signum() > 0;
	}


	private boolean isRefund() {
		return paymentVO.getAmount().signum() < 0;
	}


	@Override
	public void create() {
		super.create();

		// set title and message after the dialog has been opened
		if (isPayment()) {
    		setTitle(I18N.SendPaymentConfirmationDialog_title);
    		setMessage(I18N.SendPaymentConfirmationDialog_message);
		}
		else if (isRefund()) {
    		setTitle(I18N.SendRefundConfirmationDialog_title);
    		setMessage(I18N.SendRefundConfirmationDialog_message);
		}
		else {
    		setTitle("No email confirmation possible");
    		setMessage("The amount of this payment is 0.00. So it is neither a real payment nor a refund.");
		}
	}


	@Override
	protected boolean isResizable() {
		return true;
	}


	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
	    Composite area = (Composite) super.createDialogArea(parent);
	    Composite container = new Composite(area, SWT.NONE);
	    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


		TableColumnLayout layout = new TableColumnLayout();
		container.setLayout(layout);

		final Table table = new Table(container, SelectionMode.SINGLE_SELECTION.getSwtStyle());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);


		// define TableColumns
		final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(nameTableColumn, new ColumnWeightData(100));
		nameTableColumn.setText(EmailLabel.EmailTemplate.getString());

		final TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(languageTableColumn, new ColumnWeightData(25));
		languageTableColumn.setText( Participant.LANGUAGE_CODE.getLabel() );

		final TableColumn webidTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(webidTableColumn, new ColumnWeightData(25));
		webidTableColumn.setText(I18N.WebId);


		emailTemplateTable = new EmailTemplateSearchTable(table);


		try {
			// get data
			EmailTemplateSystemRole role = EmailTemplateSystemRole.PAYMENT_RECEIVED;
			if (isRefund()) {
				role = EmailTemplateSystemRole.REFUND_ISSUED;
			}

			List<EmailTemplate> emailTemplateSearchDataList = emailTemplateModel.getEmailTemplateSearchDataByEvent(
				paymentVO.getEventPK(),
				role
			);


			// sort by language and name (sorting by 2 columns is not supported by SimpleTable)
			emailTemplateSearchDataList = createArrayList(emailTemplateSearchDataList);
			Collections.sort(emailTemplateSearchDataList, EmailTemplateComparator.getInstance());


			// set data
			emailTemplateTable.setInput(emailTemplateSearchDataList);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}


		// observe table
		emailTemplateTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// save selected EmailTemplate in DialogSettings
				ISelection selection = emailTemplateTable.getViewer().getSelection();

				emailTemplateID = null;
				if (!selection.isEmpty() ) {
					EmailTemplate selectedTemplate = (EmailTemplate) SelectionHelper.getUniqueSelected(selection);
					emailTemplateID = selectedTemplate.getID();
				}

				// okButton can be null, because it is created after the selection is restored
				if (okButton != null) {
					okButton.setEnabled(emailTemplateID != null);
				}

				if (emailTemplateID != null) {
					if (isPayment()) {
						dialogSettings.put(DIALOG_SETTING_KEY_EMAIL_PAYMENT_TEMPLATE_ID, emailTemplateID.longValue());
					}
					else {
						dialogSettings.put(DIALOG_SETTING_KEY_EMAIL_REFUND_TEMPLATE_ID, emailTemplateID.longValue());
					}
				}
				else {
					if (isPayment()) {
						dialogSettings.put(DIALOG_SETTING_KEY_EMAIL_PAYMENT_TEMPLATE_ID, (String) null);
					}
					else {
						dialogSettings.put(DIALOG_SETTING_KEY_EMAIL_REFUND_TEMPLATE_ID, (String) null);
					}
				}
			}
		});


		// restore selected EmailTemplate
		String emailTemplateIdString;
		if (isPayment()) {
			emailTemplateIdString = dialogSettings.get(DIALOG_SETTING_KEY_EMAIL_PAYMENT_TEMPLATE_ID);
		}
		else {
			emailTemplateIdString = dialogSettings.get(DIALOG_SETTING_KEY_EMAIL_REFUND_TEMPLATE_ID);
		}

		if (StringHelper.isNotEmpty(emailTemplateIdString)) {
			try {
				Long emailTemplateID = TypeHelper.toLong(emailTemplateIdString);

				EmailTemplate emailTemplate = emailTemplateModel.getEmailTemplate(emailTemplateID);
				if (emailTemplate != null) {
					// select EmailTemplate in Table
					// if EmailTemplate does not exist in Table, nothing will be selected
					StructuredSelection selection = new StructuredSelection(emailTemplate);
					emailTemplateTable.getViewer().setSelection(selection, true);
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}


		return area;
	}


	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(emailTemplateID != null);

		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(580, 400);
	}


	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == OK) {
			// MIRCP-2158 - Send email confirmations upon (manual and payengine) payments
			if (paymentVO != null && emailTemplateID != null) {
				SendPaymentConfirmationEmailHelper.sendPaymentConfirmationEmail(paymentVO, emailTemplateID);
			}
		}

		super.buttonPressed(buttonId);
	}

}
