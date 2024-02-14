package de.regasus.participant.editor.finance;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.ReminderState;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.CommandButtonFactory;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.finance.InvoiceNoRangeModel;
import de.regasus.finance.invoice.command.CloseInvoiceCommandHandler;
import de.regasus.finance.invoice.command.ShowInvoiceCommandHandler;
import de.regasus.ui.Activator;

public class InvoiceDetailsComposite extends ScrolledComposite {

	private static FormatHelper fh = FormatHelper.getDefaultLocaleInstance();

	private InvoiceVO invoiceVO;

	private Composite content;

	private Label headerLabel;

	private Text recipientText;
	private Text salutationText;
	private Text amountText;
	private Text salestaxText;
	private Text invoiceNumberText;
	private Text invoiceDateText;
	private Text paymentTermsText;
	private Text printedDateText;
	private Text remindLevelText;
	private Text nextReminderText;
	private Text reminderDataText;
	private Text customerAccountNoText;

	private Button showInvoiceButton;
	private Button closeInvoiceButton;



	private GridData getDefaultLabelGridData() {
		return new GridData(SWT.RIGHT, SWT.TOP, false, false);
	}


	private GridData getDefaultTextGridData() {
		return new GridData(SWT.FILL, SWT.TOP, true, false);
	}


	public InvoiceDetailsComposite(Composite parent, int style) {
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
			// text of headerLabel is set in setInvoiceVO(...)
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

		// Recipient Address Label
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(UtilI18N.Recipient + ":");
			label.setLayoutData(getDefaultLabelGridData());

			recipientText = new Text(content, SWT.MULTI);
			SWTHelper.disableTextWidget(recipientText);
			recipientText.setLayoutData(getDefaultTextGridData());
		}

		// Salutation
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(Person.SALUTATION.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			salutationText = new Text(content, SWT.MULTI);
			SWTHelper.disableTextWidget(salutationText);
			salutationText.setLayoutData(getDefaultTextGridData());
		}

		// Amount
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.Amount.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			amountText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(amountText);
			amountText.setLayoutData(getDefaultTextGridData());
		}

		// Tax Amount
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.SalesTax.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			salestaxText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(salestaxText);
			salestaxText.setLayoutData(getDefaultTextGridData());
		}

		// Invoice Number
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.InvoiceNo.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			invoiceNumberText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(invoiceNumberText);
			invoiceNumberText.setLayoutData(getDefaultTextGridData());
		}

		// Invoice Date
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(UtilI18N.Closed + ":");
			label.setLayoutData(getDefaultLabelGridData());

			invoiceDateText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(invoiceDateText);
			invoiceDateText.setLayoutData(getDefaultTextGridData());
		}

		// Printed
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(UtilI18N.Printed + ":");
			label.setLayoutData(getDefaultLabelGridData());

			printedDateText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(printedDateText);
			printedDateText.setLayoutData(getDefaultTextGridData());
		}

		// Payment Terms
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.Invoice_PaymentTerms.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			paymentTermsText = new Text(content, SWT.MULTI);
			SWTHelper.disableTextWidget(paymentTermsText);
			paymentTermsText.setLayoutData(getDefaultTextGridData());
		}

		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.ReminderState.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			remindLevelText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(remindLevelText);
			remindLevelText.setLayoutData(getDefaultTextGridData());
		}

		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.NextReminder.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			nextReminderText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(nextReminderText);
			nextReminderText.setLayoutData(getDefaultTextGridData());
		}

		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.ReminderDates.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			reminderDataText = new Text(content, SWT.MULTI);
			SWTHelper.disableTextWidget(reminderDataText);
			reminderDataText.setLayoutData(getDefaultTextGridData());
		}

//		GridData reminderLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
//		reminderLayoutData.heightHint = 40;
//		reminderDataText.setLayoutData(reminderLayoutData);

		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.CustomerAccount.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			customerAccountNoText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(customerAccountNoText);
			customerAccountNoText.setLayoutData(getDefaultTextGridData());
		}


		// Button Composite
		{
			Composite buttonComposite = new Composite(content, SWT.NONE);
			buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			buttonComposite.setLayout(new GridLayout(2, true));

			GridData buttonLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);

			// show Invoice
			showInvoiceButton = CommandButtonFactory.createButton(buttonComposite, SWT.PUSH, ShowInvoiceCommandHandler.COMMAND_ID);
			showInvoiceButton.setLayoutData(buttonLayoutData);
			showInvoiceButton.setText(UtilI18N.Show);

			// close Invoice
			closeInvoiceButton = CommandButtonFactory.createButton(buttonComposite, SWT.PUSH, CloseInvoiceCommandHandler.COMMAND_ID);
			closeInvoiceButton.setLayoutData(buttonLayoutData);
			closeInvoiceButton.setText(UtilI18N.Close);
		}


		setContent(content);
		Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		content.setSize(point);
		setMinSize(point);
	}


	public void setInvoiceVO(InvoiceVO invoiceVO) {
		try {
			this.invoiceVO = invoiceVO;

			// set header
			if (invoiceVO.isInvoice()) {
				headerLabel.setText(InvoiceLabel.Invoice.getString());
			}
			else {
				headerLabel.setText(InvoiceLabel.Credit.getString());
			}

			recipientText.setText(StringHelper.avoidNull(invoiceVO.getAddressee()));


			String saluText = "";
			String saluToolTip = "";

			LanguageString salutation = invoiceVO.getSalutation();
			if (salutation != null) {
				StringBuilder salutationBuffer = new StringBuilder();
				Collection<String> languages = salutation.getLanguageCodes();
				for (String language : languages) {
					if (salutationBuffer.length() > 0) {
						salutationBuffer.append('\n');
					}
					salutationBuffer.append(salutation.getString(language));
				}
				saluText = salutation.getString();
				saluToolTip = salutationBuffer.toString();
			}

			salutationText.setText(saluText);
			salutationText.setToolTipText(saluToolTip);



			amountText.setText(AccountancyUIHelper.format(invoiceVO.getAmountGrossAsCurrencyAmount()));
			salestaxText.setText(AccountancyUIHelper.format(invoiceVO.getAmountTaxAsCurrencyAmount()));

			String invoiceNumber = "";
			if (invoiceVO.getNumber() != null) {
				if (invoiceVO.getNumberPrefix() != null) {
					invoiceNumber = invoiceVO.getNumberPrefix();
				}
				invoiceNumber += invoiceVO.getNumber();
			}
			invoiceNumberText.setText(invoiceNumber);

			invoiceDateText.setText(fh.formatDateTime(invoiceVO.getInvoiceDate()));
			printedDateText.setText(fh.formatDateTime(invoiceVO.getPrint()));
			paymentTermsText.setText(getPaymentTerms());
			remindLevelText.setText(StringHelper.toString(invoiceVO.getReminderState()));
			nextReminderText.setText(fh.formatDate(invoiceVO.getNextReminder()));
			reminderDataText.setText(getReminderDates(invoiceVO));
			customerAccountNoText.setText(StringHelper.toString(invoiceVO.getCustomerAccountNo()));

			content.layout();

			/*
			 * The buttons are set enabled depends on the command by creating them using the CommandButtonFactory.
			 * But the mechanism does not work in e4, so we need to enable the buttons here directly.
			 */
			showInvoiceButton.setEnabled(true);
			
			closeInvoiceButton.setEnabled(!invoiceVO.isClosed());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private String getPaymentTerms() {
		StringBuilder sb = new StringBuilder();

		if (invoiceVO.getPayTimeDate1() != null) {
			sb.append(fh.formatDate(invoiceVO.getPayTimeDate1()));
			sb.append(": ");
			sb.append(invoiceVO.getPayTimeAmount1());
			if (invoiceVO.getPayTimePercent1() != null) {
				sb.append(" (");
				sb.append(invoiceVO.getPayTimePercent1());
				sb.append("%)");
			}
		}

		if (invoiceVO.getPayTimeDate2() != null) {
			sb.append("\n");
			sb.append(fh.formatDate(invoiceVO.getPayTimeDate2()));
			sb.append(": ");
			sb.append(invoiceVO.getPayTimeAmount2());
			if (invoiceVO.getPayTimePercent2() != null) {
				sb.append(" (");
				sb.append(invoiceVO.getPayTimePercent2());
				sb.append("%)");
			}
		}

		if (invoiceVO.getPayTimeDate3() != null) {
			sb.append("\n");
			sb.append(fh.formatDate(invoiceVO.getPayTimeDate3()));
			sb.append(": ");
			sb.append(invoiceVO.getPayTimeAmount3());
			if (invoiceVO.getPayTimePercent3() != null) {
				sb.append(" (");
				sb.append(invoiceVO.getPayTimePercent3());
				sb.append("%)");
			}
		}

		return sb.toString();
	}

	/**
	 * This algorithm is taken from Swing-client's InvoicePaymentPane
	 */
	private String getReminderDates(InvoiceVO invoiceVO) {
		StringBuilder sb = new StringBuilder();
        if (invoiceVO != null) {
            // reminder 1
            if (invoiceVO.getReminder1() != null) {
                sb.append(ReminderState.LEVEL1.getString());
                sb.append(": ");
                sb.append(fh.formatDate(invoiceVO.getReminder1()));
            }

            // reminder 2
            if (invoiceVO.getReminder2() != null) {
                sb.append("\n");
                sb.append(ReminderState.LEVEL2.getString());
                sb.append(": ");
                sb.append(fh.formatDate(invoiceVO.getReminder2()));
            }

            // reminder 3
            if (invoiceVO.getReminder3() != null) {
                sb.append("\n");
                sb.append(ReminderState.LEVEL3.getString());
                sb.append(": ");
                sb.append(fh.formatDate(invoiceVO.getReminder3()));
            }

            // reminder 4
            if (invoiceVO.getReminder4() != null) {
                sb.append("\n");
                sb.append(ReminderState.LEVEL4.getString());
                sb.append(": ");
                sb.append(fh.formatDate(invoiceVO.getReminder4()));
            }

            // reminder 5
            if (invoiceVO.getReminder5() != null) {
                sb.append("\n");
                sb.append(ReminderState.LEVEL5.getString());
                sb.append(": ");
                sb.append(fh.formatDate(invoiceVO.getReminder5()));
            }
        }

        return sb.toString();
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	protected void openInfoDialog() {
		// get the name of the InvoiceNoRange

		String inrName = null;
		try {
			InvoiceNoRangeCVO invoiceNoRangeCVO = InvoiceNoRangeModel.getInstance().getInvoiceNoRangeCVO(invoiceVO.getInvoiceNoRangePK());
			inrName = invoiceNoRangeCVO.getVO().getName();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			InvoiceLabel.InvoiceNoRange.getString() + " (" + UtilI18N.ID + ")",
			InvoiceLabel.InvoiceNoRange.getString() + " (" + InvoiceLabel.InvoiceNoRange_Name.getString() + ")",
			InvoiceLabel.Invoice_ExportNumber.getString(),
			InvoiceLabel.InvoiceType.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser,
			InvoiceLabel.CloseUser.getString(),
			InvoiceLabel.PrintUser.getString()
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(invoiceVO.getID()),
			String.valueOf(invoiceVO.getInvoiceNoRangePK()),
			inrName,
			invoiceVO.getExportNumber() != null ? String.valueOf(invoiceVO.getExportNumber()) : "",
			(invoiceVO.isGross() ? InvoiceLabel.gross.toString() : InvoiceLabel.net.toString()),
			formatHelper.formatDateTime(invoiceVO.getNewTime()),
			invoiceVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(invoiceVO.getEditTime()),
			invoiceVO.getEditDisplayUserStr(),
			invoiceVO.getCloseDisplayUserStr(),
			invoiceVO.getPrintDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getShell(),
			InvoiceLabel.Invoice.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}
