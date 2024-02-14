package de.regasus.event.editor;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.EventConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.InvoiceConfigParameterSet;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PriceDefaultsVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.costcenter.combo.CostCenterCombo;
import de.regasus.finance.costunit.combo.CostUnitCombo;
import de.regasus.finance.currency.combo.CurrencyCombo;
import de.regasus.finance.impersonalaccount.combo.ImpersonalAccountCombo;
import de.regasus.finance.invoicenumberrange.combo.InvoiceNoRangeCombo;
import de.regasus.ui.Activator;

public class PriceDefaultsGroup extends Group {

	// the entity
	private PriceDefaultsVO priceDefaultsVO;
	private Long eventID;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private CurrencyCombo currencyCombo;
	private Button bruttoButton;
	private Button nettoButton;
	private DecimalNumberText taxRateNumberText;
	private InvoiceNoRangeCombo invoiceNoRangeCombo;
	private ImpersonalAccountCombo impersonalAccountCombo;
	private ImpersonalAccountCombo impersonalAccountTaxCombo;
	private CostCenterCombo costCenter1Combo;
	private CostUnitCombo costCenter2Combo;

	// *
	// * Widgets
	// **************************************************************************

	// Flags
	private boolean showInvoiceNoRange = true;
	private boolean showImpersonalAccounts = true;
	private boolean showCostCenter1 = true;
	private boolean showCostCenter2 = true;


	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public PriceDefaultsGroup(
		Composite parent,
		int style,
		ConfigParameterSet configParameterSet
	)
	throws Exception {
		super(parent, style);

		if (configParameterSet != null) {
			EventConfigParameterSet eventConfigParameterSet = configParameterSet.getEvent();

    		if (eventConfigParameterSet.getInvoice().isVisible()) {
    			InvoiceConfigParameterSet invoiceConfigParameterSet = configParameterSet.getInvoiceDetails();

    			showInvoiceNoRange = true;
    			showImpersonalAccounts = invoiceConfigParameterSet.getImpersonalAccount().isVisible();
    			showCostCenter1 = invoiceConfigParameterSet.getCostCenter1().isVisible();
    			showCostCenter2 = invoiceConfigParameterSet.getCostCenter2().isVisible();
    		}
    		else {
    			showInvoiceNoRange = false;
    			showImpersonalAccounts = false;
    			showCostCenter1 = false;
    			showCostCenter2 = false;
    		}
		}

		setLayout(new GridLayout(1, false));

		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		setLayout(new GridLayout(2, false));

		{
			Label currencyLabel = new Label(this, SWT.NONE);
			currencyLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			currencyLabel.setText(InvoiceLabel.Currency.getString());

			currencyCombo = new CurrencyCombo(this, SWT.BORDER);
			currencyCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			currencyCombo.addModifyListener(modifySupport);
		}
		{
			Label priceTypeLabel = new Label(this, SWT.NONE);
			priceTypeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			priceTypeLabel.setText(InvoiceLabel.PriceType.getString());

			Composite priceTypeComposite = new Composite(this, SWT.NONE);
			priceTypeComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

			bruttoButton = new Button(priceTypeComposite, SWT.RADIO);
			bruttoButton.setText(InvoiceLabel.gross.getString());

			bruttoButton.addSelectionListener(modifySupport);

			nettoButton = new Button(priceTypeComposite, SWT.RADIO);
			nettoButton.setText(InvoiceLabel.net.getString());

			nettoButton.addSelectionListener(modifySupport);
		}
		{
			Label taxRateLabel = new Label(this, SWT.NONE);
			taxRateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			taxRateLabel.setText(InvoiceLabel.SalesTax.getString());

			taxRateNumberText = new DecimalNumberText(this, SWT.BORDER);
			taxRateNumberText.setFractionDigits(1);
			taxRateNumberText.setNullAllowed(true);
			taxRateNumberText.setShowPercent(true);
			taxRateNumberText.setMaxValue(100);
			taxRateNumberText.setMinValue(0);
			taxRateNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			taxRateNumberText.addModifyListener(modifySupport);
		}

		if (showInvoiceNoRange) {
			Label invoiceNoRangeLabel = new Label(this, SWT.NONE);
			invoiceNoRangeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			invoiceNoRangeLabel.setText(InvoiceLabel.InvoiceNoRange.getString());

			if (eventID != null) {
				invoiceNoRangeCombo = new InvoiceNoRangeCombo(this, SWT.NONE, eventID);
			}
			else {
				invoiceNoRangeCombo = new InvoiceNoRangeCombo(this, SWT.NONE, true);
				invoiceNoRangeCombo.setEnabled(false);
			}
			invoiceNoRangeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			invoiceNoRangeCombo.addModifyListener(modifySupport);
		}

		if (showImpersonalAccounts) {
			Label impersonalAccountLabel = new Label(this, SWT.NONE);
			impersonalAccountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			impersonalAccountLabel.setText(InvoiceLabel.ImpersonalAccount.getString());

			impersonalAccountCombo = new ImpersonalAccountCombo(this, SWT.NONE);
			impersonalAccountCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			impersonalAccountCombo.addModifyListener(modifySupport);

			Label impersonalAccountTaxLabel = new Label(this, SWT.NONE);
			impersonalAccountTaxLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			impersonalAccountTaxLabel.setText(InvoiceLabel.ImpersonalAccountForSalesTax.getString());

			impersonalAccountTaxCombo = new ImpersonalAccountCombo(this, SWT.NONE);
			impersonalAccountTaxCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			impersonalAccountTaxCombo.addModifyListener(modifySupport);
		}

		if (showCostCenter1) {
			Label costCenter1Label = new Label(this, SWT.NONE);
			costCenter1Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			costCenter1Label.setText(InvoiceLabel.CostCenter.getString());

			costCenter1Combo = new CostCenterCombo(this, SWT.NONE);
			costCenter1Combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			costCenter1Combo.addModifyListener(modifySupport);
		}

		if (showCostCenter2) {
			Label costCenter2Label = new Label(this, SWT.NONE);
			costCenter2Label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			costCenter2Label.setText(InvoiceLabel.CostUnit.getString());

			costCenter2Combo = new CostUnitCombo(this, SWT.NONE);
			costCenter2Combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			costCenter2Combo.addModifyListener(modifySupport);
		}
	}

	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

	private void syncWidgetsToEntity() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					currencyCombo.setCurrencyCode(priceDefaultsVO.getCurrency());
					nettoButton.setSelection(!priceDefaultsVO.isGross());
					bruttoButton.setSelection(priceDefaultsVO.isGross());
					taxRateNumberText.setValue(priceDefaultsVO.getTaxRate());

					if (showInvoiceNoRange) {
						invoiceNoRangeCombo.setInvoiceNoRangeByPK(priceDefaultsVO.getInvoiceNoRangePK());
					}

					if (showImpersonalAccounts) {
						impersonalAccountCombo.setImpersonalAccountPK(priceDefaultsVO.getImpersonalAccountNo());
						impersonalAccountTaxCombo.setImpersonalAccountPK(priceDefaultsVO.getImpersonalAccountNoTax());
					}

					if (showCostCenter1) {
						costCenter1Combo.setCostCenter1(priceDefaultsVO.getCostCenter1());
					}

					if (showCostCenter2) {
						costCenter2Combo.setCostCenter2(priceDefaultsVO.getCostCenter2());
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	public void syncEntityToWidgets() {
		if (priceDefaultsVO != null) {
			priceDefaultsVO.setCurrency(currencyCombo.getCurrencyCode());

			priceDefaultsVO.setGross(bruttoButton.getSelection());

			// taxRate
			BigDecimal taxValue = taxRateNumberText.getValue();
			if (taxValue == null) {
				taxValue = BigDecimal.ZERO;
			}

			priceDefaultsVO.setTaxRate(taxValue);

			if (showInvoiceNoRange) {
				priceDefaultsVO.setInvoiceNoRangePK(invoiceNoRangeCombo.getInvoiceNoRangePK());
			}

			if (showImpersonalAccounts) {
    			priceDefaultsVO.setImpersonalAccountNo(impersonalAccountCombo.getImpersonalAccountNo());
    			priceDefaultsVO.setImpersonalAccountNoTax(impersonalAccountTaxCombo.getImpersonalAccountNo());
			}

			if (showCostCenter1) {
				priceDefaultsVO.setCostCenter1(costCenter1Combo.getCostCenter1());
			}

			if (showCostCenter2) {
				priceDefaultsVO.setCostCenter2(costCenter2Combo.getCostCenter2());
			}
		}
	}


	public void setPriceDefaultsVO(PriceDefaultsVO priceDefaultsVO, Long eventID) {
		this.priceDefaultsVO = priceDefaultsVO;
		this.eventID = eventID;

		if (showInvoiceNoRange) {
			// set eventPK only if it is not null, otherwise all InvoiceNoRanges would be loaded
			if (eventID != null) {
				invoiceNoRangeCombo.setEventPK(eventID);
			}

			invoiceNoRangeCombo.setEnabled(eventID != null);
		}

		syncWidgetsToEntity();
	}


	public void copyTo(PriceDefaultsGroup other) {
		other.currencyCombo.setCurrencyCode(currencyCombo.getCurrencyCode());
		other.nettoButton.setSelection(nettoButton.getSelection());
		other.bruttoButton.setSelection(bruttoButton.getSelection());
		other.taxRateNumberText.setValue(taxRateNumberText.getValue());

		if (showInvoiceNoRange) {
			other.invoiceNoRangeCombo.setInvoiceNoRangeByPK(invoiceNoRangeCombo.getInvoiceNoRangePK());
		}

		if (showImpersonalAccounts) {
    		other.impersonalAccountCombo.setImpersonalAccountPK(impersonalAccountCombo.getImpersonalAccountNo());
    		other.impersonalAccountTaxCombo.setImpersonalAccountPK(impersonalAccountTaxCombo.getImpersonalAccountNo());
		}

		if (showCostCenter1) {
			other.costCenter1Combo.setCostCenter1(costCenter1Combo.getCostCenter1());
		}

		if (showCostCenter2) {
			other.costCenter2Combo.setCostCenter2(costCenter2Combo.getCostCenter2());
		}

		// fire ModifyEvent
		modifySupport.fire(this);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
