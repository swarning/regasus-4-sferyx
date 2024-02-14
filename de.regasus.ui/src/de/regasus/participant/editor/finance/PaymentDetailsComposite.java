package de.regasus.participant.editor.finance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.ImpersonalAccountVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.CommandButtonFactory;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.PaymentType;
import de.regasus.finance.payment.command.CancelPaymentCommandHandler;
import de.regasus.finance.payment.command.EditPaymentCommandHandler;
import de.regasus.finance.payment.command.SendPaymentConfirmationEmailHandler;
import de.regasus.finance.payment.command.ShowPaymentReceiptCommandHandler;
import de.regasus.ui.Activator;

public class PaymentDetailsComposite extends ScrolledComposite {

	private static FormatHelper fh = FormatHelper.getDefaultLocaleInstance();

	private PaymentVO paymentVO;

	private Composite content;

	private Label headerLabel;

	private Text newTimeText;
	private Text bookingDateText;
	private Text amountText;
	private Text descriptionText;
	private Text cancellationDateText;
	private Text debitorText;
	private Text creditorText;
	private Text paymentTypeText;


	private Composite typeDetailsStackComposite;

	private StackLayout stackLayout;

	private PaymentDetailsCashComposite cashComposite;
	private PaymentDetailsChequeComposite chequeComposite;
	private PaymentDetailsCreditCardComposite creditCardComposite;
	private PaymentDetailsTransferComposite transferComposite;

	private PaymentDetailsPayEngineGroup payEngineGroup;
	private GridData payEngineGroupLayoutData;

	private PaymentDetailsEasyCheckoutGroup easyCheckoutGroup;
	private GridData easyCheckoutGroupLayoutData;

	private Button editPaymentButton;
	private Button cancelPaymentButton;
	private Button showPaymentReceiptButton;
	private Button sendPaymentReceiptByEmailButton;

	private GridData typeDetailsStackCompositeLayoutData;

	private Text paymentSystemText;


	public static GridData getDefaultLabelGridData() {
		return new GridData(SWT.RIGHT, SWT.TOP, false, false);
	}


	public static GridData getDefaultTextGridData() {
		return new GridData(SWT.FILL, SWT.TOP, true, false);
	}


	public PaymentDetailsComposite(Composite parent, int style) {
		super(parent, style | SWT.V_SCROLL);

		setExpandHorizontal(true);
		setExpandVertical(true);

		content = new Composite(this, SWT.NONE);
		content.setLayout(new GridLayout(2, false));

		if (SystemHelper.isMacOSX()) {
			/* Reset the color of the background.
			 * Other wise there will be a little difference between the background color
			 * of the Composite and the Text widgets.
			 * The reason is yet unknown.
			 */
			Color bg = content.getParent().getBackground();
			Color newBackground = new Color(bg.getDevice(), bg.getRed(), bg.getGreen(), bg.getBlue());
			content.setBackground(newBackground);
		}

		// Header
		{
			headerLabel = new Label(content, SWT.NONE);
			// text of headerLabel is set in setPaymentVO(...)
			headerLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			Font font = com.lambdalogic.util.rcp.Activator.getDefault().getFontFromRegistry(com.lambdalogic.util.rcp.Activator.DEFAULT_FONT_BOLD);
			headerLabel.setFont(font);
		}

		// Info Button
		Button infoButton = new Button(content, SWT.PUSH);
		infoButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		infoButton.setToolTipText(de.regasus.core.ui.CoreI18N.InfoButtonToolTip);
		infoButton.setImage(IconRegistry.getImage(
			de.regasus.core.ui.IImageKeys.INFORMATION
		));
		infoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openInfoDialog();
			}
		});

		// newTime
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(UtilI18N.CreateDateTime + ":");
			label.setLayoutData(getDefaultLabelGridData());

			newTimeText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(newTimeText);
			newTimeText.setLayoutData(getDefaultTextGridData());
		}

		// bookingDate
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.BookingDate.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			bookingDateText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(bookingDateText);
			bookingDateText.setLayoutData(getDefaultTextGridData());
		}

		// Payment Amount
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.Amount.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			amountText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(amountText);
			amountText.setLayoutData(getDefaultTextGridData());
		}

		// Description
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(UtilI18N.Description + ":");
			label.setLayoutData(getDefaultLabelGridData());

			descriptionText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(descriptionText);
			descriptionText.setLayoutData(getDefaultTextGridData());
		}

		// Cancelation Date
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(ParticipantLabel.CancellationDate.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			cancellationDateText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(cancellationDateText);
			cancellationDateText.setLayoutData(getDefaultTextGridData());
		}


		// Customer Account
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(
				InvoiceLabel.Debitor.getString() + " (" + InvoiceLabel.CustomerAccount.getString() + "): "
			);
			label.setLayoutData(getDefaultLabelGridData());

			debitorText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(debitorText);
			debitorText.setLayoutData(getDefaultTextGridData());
		}


		// Impersonal Account
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(
				InvoiceLabel.Creditor.getString() + " (" + InvoiceLabel.FinanceAccount.getString() + "):"
			);
			label.setLayoutData(getDefaultLabelGridData());

			creditorText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(creditorText);
			creditorText.setLayoutData(getDefaultTextGridData());
		}


		// Payment Type
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.PaymentType.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			paymentTypeText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(paymentTypeText);
			paymentTypeText.setLayoutData(getDefaultTextGridData());
		}


		// Payment System
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.PaymentSystem.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			paymentSystemText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(paymentSystemText);
			paymentSystemText.setLayoutData(getDefaultTextGridData());
		}

		// Details for Payment Type
		typeDetailsStackComposite = new Group(content, SWT.NONE);
		typeDetailsStackCompositeLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		typeDetailsStackComposite.setLayoutData(typeDetailsStackCompositeLayoutData);
		stackLayout = new StackLayout();
		typeDetailsStackComposite.setLayout(stackLayout);

		creditCardComposite = new PaymentDetailsCreditCardComposite(typeDetailsStackComposite, SWT.NONE);
		transferComposite = new PaymentDetailsTransferComposite(typeDetailsStackComposite, SWT.NONE);
		chequeComposite = new PaymentDetailsChequeComposite(typeDetailsStackComposite, SWT.NONE);
		cashComposite = new PaymentDetailsCashComposite(typeDetailsStackComposite, SWT.NONE);

		// Details for Payment System (currently only PayEngine and EasyCheckout)
		payEngineGroup = new PaymentDetailsPayEngineGroup(content, SWT.NONE);
		payEngineGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		payEngineGroup.setLayoutData(payEngineGroupLayoutData);

		easyCheckoutGroup = new PaymentDetailsEasyCheckoutGroup(content, SWT.NONE);
		easyCheckoutGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		easyCheckoutGroup.setLayoutData(easyCheckoutGroupLayoutData);


		// Button Composite
		{
			Composite buttonComposite = new Composite(content, SWT.NONE);
			buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			buttonComposite.setLayout(new GridLayout(2, true));

			GridData buttonLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);

			// Edit Payment
			editPaymentButton = CommandButtonFactory.createButton(buttonComposite, SWT.PUSH, EditPaymentCommandHandler.COMMAND_ID);
			editPaymentButton.setText(UtilI18N.Edit);
			editPaymentButton.setLayoutData(buttonLayoutData);

			// Cancel Payment
			cancelPaymentButton = CommandButtonFactory.createButton(buttonComposite, SWT.PUSH, CancelPaymentCommandHandler.COMMAND_ID);
			cancelPaymentButton.setText(I18N.PaymentDetailsComposite_CancelPayment);
			cancelPaymentButton.setLayoutData(buttonLayoutData);

			// Show Payment Receipt/Confimation
			showPaymentReceiptButton = CommandButtonFactory.createButton(buttonComposite, SWT.PUSH, ShowPaymentReceiptCommandHandler.COMMAND_ID);
			showPaymentReceiptButton.setLayoutData(buttonLayoutData);

			// Send Payment Receipt/Confirmation by email
			sendPaymentReceiptByEmailButton = CommandButtonFactory.createButton(buttonComposite, SWT.PUSH, SendPaymentConfirmationEmailHandler.COMMAND_ID);
			// text and toolTipText are set in setPaymentVO(...)
			sendPaymentReceiptByEmailButton.setLayoutData(buttonLayoutData);
		}

		setContent(content);
		Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		content.setSize(point);
		setMinSize(point);
	}


	private void updateButtonStates() {
		boolean visible = paymentVO != null && !paymentVO.isClearing();
		editPaymentButton.setVisible(visible);
		cancelPaymentButton.setVisible(visible);
		showPaymentReceiptButton.setVisible(visible);
		sendPaymentReceiptByEmailButton.setVisible(visible);

		/*
		 * The buttons are set enabled depends on the command by creating them using the CommandButtonFactory.
		 * But the mechanism does not work in e4, so we need to enable the buttons here directly.
		 */
		boolean enabled = paymentVO != null && !paymentVO.isCanceled();
		editPaymentButton.setEnabled(enabled);
		cancelPaymentButton.setEnabled(enabled);
		showPaymentReceiptButton.setEnabled(enabled);
		sendPaymentReceiptByEmailButton.setEnabled(enabled);
	}


	public void setPaymentVO(PaymentVO paymentVO) {
		try {
			this.paymentVO = paymentVO;

			// set header
			if (paymentVO.isClearing()) {
				headerLabel.setText(InvoiceLabel.Clearing.getString());
			}
			else if (paymentVO.isRefund()) {
				headerLabel.setText(InvoiceLabel.OutgoingPayment.getString());
			}
			else {
				headerLabel.setText(InvoiceLabel.IncomingPayment.getString());
			}

			// set text of sendPaymentReceiptByEmailButton
			if (paymentVO.isRefund()) {
				sendPaymentReceiptByEmailButton.setText(I18N.PaymentDetailsComposite_SendRefundReceiptByEmail);
				sendPaymentReceiptByEmailButton.setToolTipText(I18N.PaymentDetailsComposite_SendRefundReceiptByEmail_tooltip);
			}
			else {
				sendPaymentReceiptByEmailButton.setText(I18N.PaymentDetailsComposite_SendPaymentReceiptByEmail);
				sendPaymentReceiptByEmailButton.setToolTipText(I18N.PaymentDetailsComposite_SendPaymentReceiptByEmail_tooltip);
			}

			newTimeText.setText(fh.formatDateTime(paymentVO.getNewTime()));
			bookingDateText.setText(fh.formatDate(paymentVO.getBookingDate()));
			amountText.setText(AccountancyUIHelper.format(paymentVO.getCurrencyAmount()));
			descriptionText.setText(StringHelper.avoidNull(paymentVO.getDescription()));
			cancellationDateText.setText(fh.formatDateTime(paymentVO.getCancelationDate()));
			debitorText.setText( StringHelper.avoidNull(paymentVO.getCustomerAccountNo()) );

			String creditorStr = "";
			if (paymentVO.getCreditAccountNo() != null) {
				creditorStr = ImpersonalAccountVO.NUMBER_FORMAT.format( paymentVO.getCreditAccountNo() );
			}
			creditorText.setText(creditorStr);

			paymentTypeText.setText(paymentVO.getType().getString());

			PaymentSystem paymentSystem = paymentVO.getPaymentSystem();
			paymentSystemText.setText(paymentSystem != null ? paymentSystem.getString() : "");

			// Decide which payment type details to show (if any)
			Composite newTopComposite = null;
			if (paymentVO.getCreditCardVO() != null &&
				paymentVO.getType() == PaymentType.CREDIT_CARD
			) {
				creditCardComposite.setPaymentVO(paymentVO);
				newTopComposite = creditCardComposite;
			}
			else if (paymentVO.getBankVO() != null &&
					! paymentVO.getBankVO().isEmpty() && (
					paymentVO.getType() == PaymentType.DEBIT ||
					paymentVO.getType() == PaymentType.TRANSFER ||
					paymentVO.getType() == PaymentType.ECMAESTRO
				)
			) {
				transferComposite.setPaymentVO(paymentVO);
				newTopComposite = transferComposite;
			}
			else if (paymentVO.getType() == PaymentType.CASH) {
				cashComposite.setPaymentVO(paymentVO);
				newTopComposite = cashComposite;
			}
			else if (paymentVO.getType() == PaymentType.CHEQUE) {
				chequeComposite.setPaymentVO(paymentVO);
				newTopComposite = chequeComposite;
			}

			if (newTopComposite != null) {
				typeDetailsStackCompositeLayoutData.exclude = false;
				typeDetailsStackComposite.setVisible(true);

				stackLayout.topControl = newTopComposite;
				typeDetailsStackComposite.layout();
			}
			else {
				typeDetailsStackCompositeLayoutData.exclude = true;
				typeDetailsStackComposite.setVisible(false);
			}

			// Decide whether payment system details to show (currently only PayEngine)
			payEngineGroupLayoutData.exclude = true;
			payEngineGroup.setVisible(false);

			easyCheckoutGroupLayoutData.exclude = true;
			easyCheckoutGroup.setVisible(false);

			if (paymentSystem == PaymentSystem.PAYENGINE) {
				payEngineGroupLayoutData.exclude = false;
				payEngineGroup.setVisible(true);
				payEngineGroup.setPaymentVO(paymentVO);
			}
			else if (paymentSystem == PaymentSystem.EASY_CHECKOUT) {
				easyCheckoutGroupLayoutData.exclude = false;
				easyCheckoutGroup.setVisible(true);
				easyCheckoutGroup.setPaymentVO(paymentVO);
			}


			updateButtonStates();

			content.layout();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser,
			InvoiceLabel.Payment_ExportNumber.getString(),
			InvoiceLabel.Payment_CancelExportNumber.getString(),
			InvoiceLabel.Payment_DocumentNumber.getString()
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(paymentVO.getID()),
			formatHelper.formatDateTime(paymentVO.getNewTime()),
			paymentVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(paymentVO.getEditTime()),
			paymentVO.getEditDisplayUserStr(),
			paymentVO.getExportNumber() != null ? String.valueOf(paymentVO.getExportNumber()) : "",
			paymentVO.getCancelExportNumber() != null ? String.valueOf(paymentVO.getCancelExportNumber()) : "",
			paymentVO.getDocumentNo()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getShell(),
			InvoiceLabel.Payment.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}
