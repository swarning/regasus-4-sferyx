package de.regasus.participant.editor.finance;

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

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.ImpersonalAccountVO;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.ui.Activator;

public class InvoicePositionDetailsComposite extends ScrolledComposite {

	private static FormatHelper fh = FormatHelper.getDefaultLocaleInstance();

	private InvoicePositionVO invoicePositionVO;


	private Text descriptionText;
	private Text amountBruttoText;
	private Text amountNettoText;
	private Text amountTaxText;
	private Text taxRateText;
	private Text amountOpenText;
	private Text costCenter1Text;
	private Text costCenter2Text;
	private Text impersonalAccountNoText;
	private Text impersonalAccountNoTaxText;


	private Composite content;


	private GridData getDefaultLabelGridData() {
		return new GridData(SWT.RIGHT, SWT.TOP, false, false);
	}

	private GridData getDefaultTextGridData() {
		return new GridData(SWT.LEFT, SWT.TOP, true, false);
	}


	public InvoicePositionDetailsComposite(Composite parent, int style) {
		super(parent, style | SWT.V_SCROLL);

		setExpandHorizontal(true);
		setExpandVertical(true);

		content = new Composite(this, SWT.NONE);
		content.setLayout(new GridLayout(2, false));

		if (SystemHelper.isMacOSX()) {
			/* Reset the color of the backgroupd.
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
			Label headerLabel = new Label(content, SWT.NONE);
			headerLabel.setText(InvoiceLabel.InvoicePosition.getString());
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

		// Description
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(UtilI18N.Description + ":");
			label.setLayoutData(getDefaultLabelGridData());

			descriptionText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(descriptionText);
			descriptionText.setLayoutData(getDefaultTextGridData());
		}

		// Amount (brutto)
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.Amount.getString() + " (" + InvoiceLabel.gross.getString() + "):");
			label.setLayoutData(getDefaultLabelGridData());

			amountBruttoText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(amountBruttoText);
			amountBruttoText.setLayoutData(getDefaultTextGridData());
		}

		// Amount (netto)
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.Amount.getString() + " (" + InvoiceLabel.net.getString() + "):");
			label.setLayoutData(getDefaultLabelGridData());

			amountNettoText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(amountNettoText);
			amountNettoText.setLayoutData(getDefaultTextGridData());
		}

		// Amount (tax)
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.SalesTax.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			amountTaxText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(amountTaxText);
			amountTaxText.setLayoutData(getDefaultTextGridData());
		}

		// Tax Rate
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.SalesTaxRate.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			taxRateText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(taxRateText);
			taxRateText.setLayoutData(getDefaultTextGridData());
		}

		// Amount (open)
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(ParticipantLabel.OpenAmount.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			amountOpenText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(amountOpenText);
			amountOpenText.setLayoutData(getDefaultTextGridData());
		}

		// Cost Center 1
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.CostCenter.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			costCenter1Text = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(costCenter1Text);
			costCenter1Text.setLayoutData(getDefaultTextGridData());
		}

		// Cost Center 2
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.CostUnit.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			costCenter2Text = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(costCenter2Text);
			costCenter2Text.setLayoutData(getDefaultTextGridData());
		}

		// Impersonal Account Number
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.ImpersonalAccount.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			impersonalAccountNoText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(impersonalAccountNoText);
			impersonalAccountNoText.setLayoutData(getDefaultTextGridData());
		}

		// Impersonal Account Number Tax
		{
			Label label = new Label(content, SWT.NONE);
			label.setText(InvoiceLabel.ImpersonalAccountForSalesTax.getString() + ":");
			label.setLayoutData(getDefaultLabelGridData());

			impersonalAccountNoTaxText = new Text(content, SWT.NONE);
			SWTHelper.disableTextWidget(impersonalAccountNoTaxText);
			impersonalAccountNoTaxText.setLayoutData(getDefaultTextGridData());
		}



		setContent(content);
		Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		content.setSize(point);
		setMinSize(point);
	}


	public void setInvoicePositionVO(InvoicePositionVO invoicePositionVO) {
		try {
			this.invoicePositionVO = invoicePositionVO;

			descriptionText.setText(StringHelper.avoidNull(invoicePositionVO.getDescription()));
			amountBruttoText.setText(AccountancyUIHelper.format(invoicePositionVO.getAmountGrossAsCurrencyAmount()));
			amountNettoText.setText(AccountancyUIHelper.format(invoicePositionVO.getAmountNetAsCurrencyAmount()));
			amountTaxText.setText(AccountancyUIHelper.format(invoicePositionVO.getAmountTaxAsCurrencyAmount()));
			taxRateText.setText(fh.format(invoicePositionVO.getTaxRate()) + " %");
			amountOpenText.setText(AccountancyUIHelper.format(invoicePositionVO.getAmountOpenAsCurrencyAmount()));
			costCenter1Text.setText(StringHelper.avoidNull(invoicePositionVO.getCostCenter1()));
			costCenter2Text.setText(StringHelper.avoidNull(invoicePositionVO.getCostCenter2()));

			String impAccStr = "";
			if (invoicePositionVO.getImpersonalAccountNo() != null) {
				impAccStr = ImpersonalAccountVO.NUMBER_FORMAT.format( invoicePositionVO.getImpersonalAccountNo() );
			}
			impersonalAccountNoText.setText(impAccStr);

			String impAccTaxStr = "";
			if (invoicePositionVO.getImpersonalAccountNoTax() != null) {
				impAccTaxStr = ImpersonalAccountVO.NUMBER_FORMAT.format( invoicePositionVO.getImpersonalAccountNoTax() );
			}
			impersonalAccountNoTaxText.setText(impAccTaxStr);

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
			InvoiceLabel.Booking.getString() + " (" + UtilI18N.ID + ")",
			InvoiceLabel.InvoiceType.getString(),
			InvoiceLabel.InvoicePositionType.getString() + " (" + UtilI18N.ID + ")",
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(invoicePositionVO.getID()),
			(invoicePositionVO.getBookingPK() != null ? String.valueOf(invoicePositionVO.getBookingPK()) : ""),
			(invoicePositionVO.isGross() ? InvoiceLabel.gross.toString() : InvoiceLabel.net.toString()),
			(invoicePositionVO.getBookingType() != null ? invoicePositionVO.getBookingType() : ""),
			formatHelper.formatDateTime(invoicePositionVO.getNewTime()),
			invoicePositionVO.getNewDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getShell(),
			InvoiceLabel.InvoicePosition.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}
