package de.regasus.finance.paymentsystem.dialog;

import static com.lambdalogic.util.rcp.widget.SWTHelper.createLabel;

import java.math.BigDecimal;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.CreditCardAlias;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.report.oo.OpenOfficeConstants;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;

import de.regasus.I18N;
import de.regasus.common.combo.OpenOfficeFormatCombo;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.currency.combo.CurrencyCombo;
import de.regasus.ui.Activator;

public class PayEngineStartPage extends WizardPage {

	public static final String NAME = "PayEngineStartPage";

	// Widgets
	private DecimalNumberText amountNumberText;
	private CurrencyCombo currencyCombo;
	private Text decriptionText;
	private OpenOfficeFormatCombo formatCombo;

	// parameters
	private String currency;
	private BigDecimal amount;
	private CreditCardAlias creditCardAlias;

	private Button dontUseAliasButton;
	private Button useAliasButton;


	public PayEngineStartPage(
		String currency,
		BigDecimal amount,
		Participant participant
	) {
		super(NAME);

		setTitle(I18N.PayEngineStartPage_Title);
		setMessage(I18N.PayEngineStartPage_Message);

		this.currency = currency;
		this.amount = amount;
		this.creditCardAlias = participant.getCreditCardAlias();
	}


	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		try {
			// First row: Amount and currency
			createLabel(composite, InvoiceLabel.Amount.getString());

			amountNumberText = new DecimalNumberText(composite, SWT.BORDER);
			amountNumberText.setFractionDigits(2);
			amountNumberText.setValue(amount);
			amountNumberText.setNullAllowed(false);

			amountNumberText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

			currencyCombo = new CurrencyCombo(composite, SWT.NONE);
			currencyCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			currencyCombo.setCurrencyCode(currency);

			// Second row: Description
			createLabel(composite, UtilI18N.Description);

			decriptionText = new Text(composite, SWT.BORDER);
			decriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

			// Third row: Payment Receipt Format
			Label paymentReceiptFormatLabel = new Label(composite, SWT.NONE);
			paymentReceiptFormatLabel.setText(I18N.PaymentReceiptFormat);

			formatCombo = new OpenOfficeFormatCombo(
				composite,
				SWT.READ_ONLY,
				OpenOfficeConstants.DOC_FORMAT_ODT // DocumentFormat of templates (only ODT is supported)
			);
			formatCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


			// 4th row: creditCardAlias
			if (creditCardAlias != null && creditCardAlias.isCompleteForPayEngine()) {
				Label ruler = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
				ruler.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

				Label aliasDescriptionLabel = new Label(composite, SWT.NONE);
				aliasDescriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
				aliasDescriptionLabel.setText(I18N.PayEngineStartPage_AliasDescription);


				dontUseAliasButton = new Button(composite, SWT.RADIO);
				dontUseAliasButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
				dontUseAliasButton.setText(I18N.PayEngineStartPage_DontUseAliasButton);

				useAliasButton = new Button(composite, SWT.RADIO);
				useAliasButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
				useAliasButton.setText(I18N.PayEngineStartPage_UseAliasButton + " (" + creditCardAlias.getMaskedNumber() + ")");
				useAliasButton.setSelection(true);
			}

			// set the focus
			amountNumberText.setFocus();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		setControl(composite);
	}


	public BigDecimal getAmount() {
		BigDecimal amount = amountNumberText.getValue();
		return amount;
	}


	@Override
	public String getDescription() {
		return StringHelper.trim(decriptionText.getText());
	}


	public String getCurrencyCode() {
		return currencyCombo.getCurrencyCode();
	}


	public String getPaymentReceiptFormat() {
		return formatCombo.getFormat().getFormatKey();
	}


	public CreditCardAlias getUsedCreditCardAlias() {
		CreditCardAlias usedCreditCardAlias = null;
		if (useAliasButton != null && useAliasButton.getSelection()) {
			usedCreditCardAlias = this.creditCardAlias;
		}
		return usedCreditCardAlias;
	}


	@Override
	public IWizardPage getNextPage() {
		return super.getNextPage();
	}

}
