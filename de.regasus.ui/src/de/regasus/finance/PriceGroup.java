package de.regasus.finance;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.InvoiceConfigParameterSet;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.costcenter.combo.CostCenterCombo;
import de.regasus.finance.costunit.combo.CostUnitCombo;
import de.regasus.finance.currency.combo.CurrencyCombo;
import de.regasus.finance.impersonalaccount.combo.ImpersonalAccountCombo;
import de.regasus.finance.invoicenumberrange.combo.InvoiceNoRangeCombo;
import de.regasus.ui.Activator;

public class PriceGroup extends Group {

	// the entity
	private PriceVO priceVO;

	private ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private DecimalNumberText amountNumberText;
	private CurrencyCombo currencyCombo;
	private Button grossButton;
	private Button netButton;

	private DecimalNumberText taxRateNumberText;
	private Label grossAmountValueLabel;
	private Label netAmountValueLabel;
	private Label taxAmountValueLabel;
	private InvoiceNoRangeCombo invoiceNoRangeCombo;
	private ImpersonalAccountCombo impersonalAccountCombo;
	private ImpersonalAccountCombo impersonalAccountTaxCombo;
	private CostCenterCombo costCenterCombo;
	private CostUnitCombo costUnitCombo;

	// GridData needed as fields because their heightHint is changed in setMinimize();
	private GridData priceLabelGridData;
	private GridData amountNumberTextGridData;
	private GridData currencyComboGridData;
	private GridData grossNetButtonGridData;
	private GridData vatLabelGridData;
	private GridData taxAmountLabelGridData;
	private GridData taxRateNumberTextGridData;
	private GridData grossAmountLabelGridData;
	private GridData grossAmountValueLabelGridData;
	private GridData netAmountLabelGridData;
	private GridData netAmountValueLabelGridData;
	private GridData invoiceNumberRangeLabelGridData;
	private GridData invoiceNoRangeComboGridData;
	private GridData impersonalAccountLabelGridData;
	private GridData impersonalAccountComboGridData;
	private GridData impersonalAccountTaxLabelGridData;
	private GridData impersonalAccountTaxComboGridData;
	private GridData costCenterLabelGridData;
	private GridData costCenter1ComboGridData;
	private GridData costUnitLabelGridData;
	private GridData costCenter2ComboGridData;

	private PriceVO tmpPriceVO = new PriceVO();

	/**
	 * Indicates that syncWidgetsToEntity is in progress.
	 * If this is the case, ModifyListener and SelectionListener do not need to execute their code.
	 */
	private boolean sync = false;

	private boolean withInvoice = false;
	private boolean withCostCenter1 = false;
	private boolean withCostCenter2 = false;
	private boolean withImpersonalAccount = false;


	/**
	 * ModifyListener for updating the displayed gross/net amounts.
	 */
	private ModifyListener amountModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			try {
				/* Do not fire if the source is a radio button that has been deselected.
				 * In this case there will be another event for the button that has been selected.
				 * There is no "deselection" event without a corresponding "selection" event.
				 */
				if (!sync && !ModifySupport.isDeselectedRadioButton(event)) {
					refreshAmounts();
				}
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};

	private SelectionListener amountSelectionListener = new ModifyListenerAdapter(amountModifyListener);


	public PriceGroup(Composite parent, int style) throws Exception {
		this(parent, style, null);
	}


	public PriceGroup(Composite parent, int style, Long eventPK) throws Exception {
		this(parent, style, eventPK, InvoiceLabel.Price.getString());
	}


	public PriceGroup(Composite parent, int style, Long eventPK, String label) throws Exception {
		super(parent, style);


		ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventPK);
		withInvoice = configParameterSet.getEvent().getInvoice().isVisible();
		InvoiceConfigParameterSet invoiceDetails = configParameterSet.getInvoiceDetails();
		withCostCenter1 = invoiceDetails.getCostCenter1().isVisible();
		withCostCenter2 = invoiceDetails.getCostCenter2().isVisible();
		withImpersonalAccount = invoiceDetails.getImpersonalAccount().isVisible();

		setText(label);
		GridLayout layout = new GridLayout(5, false);

		setLayout(layout);

		// ****************************************************************************
		// * Line 1: Additional Widgets
		// *

		createAdditionalWidget();

		// *
		// * Line 1: Additional Widgets
		// ****************************************************************************

		// ****************************************************************************
		// * Line 2: Amount (left) and Net Amount (right)
		// *

		// amount and currency
		{
    		Label priceLabel = new Label(this, SWT.NONE);
    		priceLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    		priceLabel.setLayoutData(priceLabelGridData);
    		priceLabel.setText(InvoiceLabel.Amount.getString());
    		SWTHelper.makeBold(priceLabel);

    		amountNumberText = new DecimalNumberText(this, SWT.BORDER);
    		amountNumberText.setFractionDigits(2);
    		amountNumberText.setValue(0.0);
    		amountNumberText.setNullAllowed(false);

    		amountNumberTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
    		amountNumberText.setLayoutData(amountNumberTextGridData);
    		SWTHelper.makeBold(amountNumberText);

    		currencyCombo = new CurrencyCombo(this, SWT.NONE);
    		currencyComboGridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
    		currencyCombo.setLayoutData(currencyComboGridData);
    		SWTHelper.makeBold(currencyCombo.getCombo());

    		amountNumberText.addModifyListener(modifySupport);
    		currencyCombo.addModifyListener(modifySupport);

    		amountNumberText.addModifyListener(amountModifyListener);
    		currencyCombo.addModifyListener(amountModifyListener);
		}

		// net amount
		{
    		Label netAmountLabel = new Label(this, SWT.NONE);
    		netAmountLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
    		netAmountLabel.setLayoutData(netAmountLabelGridData);
    		netAmountLabel.setText(InvoiceLabel.NetAmount.getString());

    		netAmountValueLabel = new Label(this, SWT.NONE);
    		netAmountValueLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
    		netAmountValueLabel.setLayoutData(netAmountValueLabelGridData);
		}

		// *
		// * Line 2: Amount (left) and Net Amount (right)
		// ****************************************************************************

		// ****************************************************************************
		// * Line 3: Tax Rate
		// *

		// tax rate and price type
		{
			// tax rate label
			Label vatLabel = new Label(this, SWT.NONE);
			vatLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			vatLabel.setLayoutData(vatLabelGridData);
			vatLabel.setText(InvoiceLabel.SalesTax.getString());


    		// tax rate number text
    		taxRateNumberText = new DecimalNumberText(this, SWT.BORDER);
    		taxRateNumberText.setFractionDigits(1);
    		taxRateNumberText.setNullAllowed(false);
    		taxRateNumberText.setShowPercent(true);
    		taxRateNumberText.setMaxValue(100);
    		taxRateNumberText.setMinValue(0);
    		taxRateNumberText.setValue(0.0);
    		taxRateNumberTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
    		taxRateNumberText.setLayoutData(taxRateNumberTextGridData);

    		taxRateNumberText.addModifyListener(modifySupport);
    		taxRateNumberText.addModifyListener(amountModifyListener);


    		// Price type
    		final Composite priceTypeComposite = new Composite(this, SWT.NONE);
    		priceTypeComposite.setLayout(new GridLayout(2, true));
    		grossNetButtonGridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
    		priceTypeComposite.setLayoutData(grossNetButtonGridData);


    		grossButton = new Button(priceTypeComposite, SWT.RADIO);
    		grossButton.setText(InvoiceLabel.gross.getString());
    		grossButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

    		netButton = new Button(priceTypeComposite, SWT.RADIO);
    		netButton.setText(InvoiceLabel.net.getString());
    		netButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

    		/* Every Button of a group of radio buttons has to be observed!
    		 * In some environments events are fired for the selected and the deselected Button. In other
    		 * environments only the Event for the selected Button is fired.
    		 * However, the amountSelectionListener reacts only on the Event fired by the Button that has been selected.
    		 */
    		grossButton.addSelectionListener(modifySupport);
    		grossButton.addSelectionListener(amountSelectionListener);
    		netButton.addSelectionListener(modifySupport);
    		netButton.addSelectionListener(amountSelectionListener);
		}


		// tax rate
		{
    		Label taxAmountLabel = new Label(this, SWT.NONE);
    		vatLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    		taxAmountLabel.setLayoutData(vatLabelGridData);
    		taxAmountLabel.setText(InvoiceLabel.TaxAmount.getString());

    		taxAmountValueLabel = new Label(this, SWT.NONE);
    		taxAmountLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    		taxAmountValueLabel.setLayoutData(taxAmountLabelGridData);
		}

		// *
		// * Line 3: Tax Rate
		// ****************************************************************************

		// ****************************************************************************
		// * Line 4: Separators
		// *

		// tmp placeholder
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		{
    		Label separator = new Label(this, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
    		GridData separatorGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
    		separator.setLayoutData(separatorGridData);
    		Label separator2 = new Label(this, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
    		GridData separator2GridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
    		separator2.setLayoutData(separator2GridData);
		}

		// *
		// * Line 4: Separators
		// ****************************************************************************

		// ****************************************************************************
		// * Line 5: Gross Amount
		// *

		// placeholder
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		// gross amount
		{
        	Label grossAmountLabel = new Label(this, SWT.NONE);
        	grossAmountLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        	grossAmountLabel.setLayoutData(grossAmountLabelGridData);
        	grossAmountLabel.setText(InvoiceLabel.GrossAmount.getString());

        	grossAmountValueLabel = new Label(this, SWT.NONE);
        	grossAmountValueLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        	grossAmountValueLabel.setLayoutData(grossAmountValueLabelGridData);
		}

		// *
		// * Line 5: Gross Amount
		// ****************************************************************************


		// invoice number range
		if (withInvoice) {

			// ****************************************************************************
			// * Line 6: Invoice Number Range
			// *

    		Label invoiceNumberRangeLabel = new Label(this, SWT.NONE);
    		invoiceNumberRangeLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    		invoiceNumberRangeLabel.setLayoutData(invoiceNumberRangeLabelGridData);
    		invoiceNumberRangeLabel.setText(InvoiceLabel.InvoiceNoRange.getString());

    		invoiceNoRangeCombo = new InvoiceNoRangeCombo(this, SWT.NONE, eventPK);
    		invoiceNoRangeComboGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
    		invoiceNoRangeCombo.setLayoutData(invoiceNoRangeComboGridData);

    		new Label(this, SWT.NONE);
    		new Label(this, SWT.NONE);

    		invoiceNoRangeCombo.addModifyListener(modifySupport);

    		// *
    		// * Line 6: Invoice Number Range
    		// ****************************************************************************

    		// ****************************************************************************
    		// * Line 7: Impersonal Accounts
    		// *

    		// impersonal accounts
    		if (withImpersonalAccount) {
    			Label impersonalAccountLabel = new Label(this, SWT.NONE);
    			impersonalAccountLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    			impersonalAccountLabel.setLayoutData(impersonalAccountLabelGridData);
    			impersonalAccountLabel.setText(InvoiceLabel.ImpersonalAccount.getString());

    			impersonalAccountCombo = new ImpersonalAccountCombo(this, SWT.NONE);
    			impersonalAccountComboGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
    			impersonalAccountCombo.setLayoutData(impersonalAccountComboGridData);

    			Label impersonalAccountTaxLabel = new Label(this, SWT.NONE);
    			impersonalAccountTaxLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    			impersonalAccountTaxLabel.setLayoutData(impersonalAccountTaxLabelGridData);
    			impersonalAccountTaxLabel.setText(InvoiceLabel.ImpersonalAccountForSalesTax.getString());

    			impersonalAccountTaxCombo = new ImpersonalAccountCombo(this, SWT.NONE);
    			impersonalAccountTaxComboGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
    			impersonalAccountTaxCombo.setLayoutData(impersonalAccountTaxComboGridData);

    			impersonalAccountCombo.addModifyListener(modifySupport);
    			impersonalAccountTaxCombo.addModifyListener(modifySupport);
    		}

    		// *
    		// * Line 7: Impersonal Accounts
    		// ****************************************************************************

    		// ****************************************************************************
    		// * Line 8: Cost Centers
    		// *

    		if (withCostCenter1 || withCostCenter2) {

    			// cost center 1
    			if (withCostCenter1) {
        			Label costCenterLabel = new Label(this, SWT.NONE);
        			costCenterLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        			costCenterLabel.setLayoutData(costCenterLabelGridData);
        			costCenterLabel.setText(InvoiceLabel.CostCenter.getString());

        			costCenterCombo = new CostCenterCombo(this, SWT.NONE);
        			costCenter1ComboGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        			costCenterCombo.setLayoutData(costCenter1ComboGridData);

    				costCenterCombo.addModifyListener(modifySupport);
    			}
    			else {
    	    		new Label(this, SWT.NONE);
    	    		new Label(this, SWT.NONE);
    	    		new Label(this, SWT.NONE);
    			}

    			// cost center 2
    			if (withCostCenter2) {
        			Label costUnitLabel = new Label(this, SWT.NONE);
        			costUnitLabelGridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        			costUnitLabel.setLayoutData(costUnitLabelGridData);
        			costUnitLabel.setText(InvoiceLabel.CostUnit.getString());

        			costUnitCombo = new CostUnitCombo(this, SWT.NONE);
        			costCenter2ComboGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        			costUnitCombo.setLayoutData(costCenter2ComboGridData);

    				costUnitCombo.addModifyListener(modifySupport);
    			}
    			else {
    	    		new Label(this, SWT.NONE);
    	    		new Label(this, SWT.NONE);
    			}
    		}

    		// *
    		// * Line 8: Cost Centers
    		// ****************************************************************************
		}


		// add modifySupport as ModifyListener
		addModifyListenerToAdditionalWidgets(modifySupport, modifySupport);
	}


	/**
	 * Minimize or restore all widgets.
	 * When the widgets are minimized they are not only invisible, they even don't take any space.
	 * This is done by setting their heightHint to -1.
	 *
	 * @param minimize
	 */
	public void setMinimize(boolean minimize) {
		int heightHint = minimize ? -1 : 0;

		setAdditionalWidgetHeight(heightHint);

		priceLabelGridData.heightHint = heightHint;
		amountNumberTextGridData.heightHint = heightHint;
		currencyComboGridData.heightHint = heightHint;
		grossNetButtonGridData.heightHint = heightHint;
		vatLabelGridData.heightHint = heightHint;
		taxRateNumberTextGridData.heightHint = heightHint;
		grossAmountLabelGridData.heightHint = heightHint;
		grossAmountValueLabelGridData.heightHint = heightHint;
		netAmountLabelGridData.heightHint = heightHint;
		netAmountValueLabelGridData.heightHint = heightHint;

		if (withInvoice) {
    		invoiceNumberRangeLabelGridData.heightHint = heightHint;
    		invoiceNoRangeComboGridData.heightHint = heightHint;

    		if (withImpersonalAccount) {
        		impersonalAccountLabelGridData.heightHint = heightHint;
        		impersonalAccountComboGridData.heightHint = heightHint;
        		impersonalAccountTaxLabelGridData.heightHint = heightHint;
        		impersonalAccountTaxComboGridData.heightHint = heightHint;
    		}

    		if (withCostCenter1) {
        		costCenterLabelGridData.heightHint = heightHint;
        		costCenter1ComboGridData.heightHint = heightHint;
    		}

    		if (withCostCenter2) {
        		costUnitLabelGridData.heightHint = heightHint;
        		costCenter2ComboGridData.heightHint = heightHint;
    		}
		}

		layout();
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


	@Override
	public void addFocusListener(FocusListener listener){
		amountNumberText.addFocusListener(listener);
	}


	private void syncWidgetsToEntity() {
		if (priceVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						sync = true;

						syncAdditionalWidgetToEntity();

						amountNumberText.setValue(priceVO.getAmount());
						currencyCombo.setCurrencyCode(priceVO.getCurrency());
						netButton.setSelection(priceVO.isNet());
						grossButton.setSelection(priceVO.isGross());
						taxRateNumberText.setValue(priceVO.getTaxRate());

						if (withInvoice) {
    						invoiceNoRangeCombo.setInvoiceNoRangeByPK(priceVO.getInvoiceNoRangePK());
    						if (withImpersonalAccount) {
        						impersonalAccountCombo.setImpersonalAccountPK(priceVO.getImpersonalAccountNo());
        						impersonalAccountTaxCombo.setImpersonalAccountPK(priceVO.getImpersonalAccountNoTax());
    						}
    						if (withCostCenter1) {
    							costCenterCombo.setCostCenter1(priceVO.getCostCenter1());
    						}
    						if (withCostCenter2) {
    							costUnitCombo.setCostCenter2(priceVO.getCostCenter2());
    						}
						}
						refreshAmounts();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						sync = false;
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (priceVO != null) {
			syncEntityToAdditionalWidget();

			boolean isGross = grossButton.getSelection();
			priceVO.setAmount(amountNumberText.getValue(), isGross);
			priceVO.setCurrency(currencyCombo.getCurrencyCode());

			// taxRate
			BigDecimal taxValue = taxRateNumberText.getValue();
			if (taxValue == null) {
				taxValue = BigDecimal.ZERO;
			}
			priceVO.setTaxRate(taxValue);

			if (withInvoice) {
    			priceVO.setInvoiceNoRangePK(invoiceNoRangeCombo.getInvoiceNoRangePK());
    			if (withImpersonalAccount) {
        			priceVO.setImpersonalAccountNo(impersonalAccountCombo.getImpersonalAccountNo());
        			priceVO.setImpersonalAccountNoTax(impersonalAccountTaxCombo.getImpersonalAccountNo());
    			}
    			if (withCostCenter1) {
    				priceVO.setCostCenter1(costCenterCombo.getCostCenter1());
    			}
    			if (withCostCenter2) {
    				priceVO.setCostCenter2(costUnitCombo.getCostCenter2());
    			}
			}
		}
	}


	public void setPriceVO(PriceVO priceVO) {
		this.priceVO = priceVO;
		syncWidgetsToEntity();
	}


	public PriceVO getPriceVO() {
		return priceVO;
	}


	public void refreshAmounts() {
		// init a temporary PriceVO with the current values of the widgets

		BigDecimal amount = amountNumberText.getValue();
		if (amount == null) {
			amount = BigDecimal.ZERO;
		}
		tmpPriceVO.setAmount(amount, grossButton.getSelection());

		String currency = currencyCombo.getCurrencyCode();
		tmpPriceVO.setCurrency(currency);

		BigDecimal taxValue = taxRateNumberText.getValue();
		if (taxValue == null) {
			taxValue = BigDecimal.ZERO;
		}
		tmpPriceVO.setTaxRate(taxValue);



		grossAmountValueLabel.setText(tmpPriceVO.getCurrencyAmountGross().format(false, false));
		netAmountValueLabel.setText(tmpPriceVO.getCurrencyAmountNet().format(false, false));
		taxAmountValueLabel.setText(tmpPriceVO.getCurrencyAmountTax().format(false, false));

		layout();
	}


	public BigDecimal getAmount() {
		BigDecimal value = amountNumberText.getValue();
		if (value == null) {
			value = BigDecimal.ZERO;
		}
		return value;
	}


	public BigDecimal getAmountGross() {
		boolean isGross = grossButton.getSelection();

		BigDecimal amount = amountNumberText.getValue();
		if (amount == null) {
			amount = BigDecimal.ZERO;
		}
		tmpPriceVO.setAmount(amount, isGross);

		// taxRate
		BigDecimal taxValue = taxRateNumberText.getValue();
		if (taxValue == null) {
			taxValue = BigDecimal.ZERO;
		}
		tmpPriceVO.setTaxRate(taxValue);

		return tmpPriceVO.getAmountGross();
	}


	public BigDecimal getAmountNetto() {
		boolean isGross = grossButton.getSelection();

		BigDecimal amount = amountNumberText.getValue();
		if (amount == null) {
			amount = BigDecimal.ZERO;
		}
		tmpPriceVO.setAmount(amount, isGross);

		// taxRate
		BigDecimal taxValue = taxRateNumberText.getValue();
		if (taxValue == null) {
			taxValue = BigDecimal.ZERO;
		}
		tmpPriceVO.setTaxRate(taxValue);

		return tmpPriceVO.getAmountNet();
	}


	public void setAmount(BigDecimal amount) {
		amountNumberText.setValue(amount);
	}


	public void setTaxRate(BigDecimal taxRate) {
		taxRateNumberText.setValue(taxRate);
	}


	public void setGross(boolean gross) {
		grossButton.setSelection(gross);
		netButton.setSelection(!gross);
	}


	public void setCurrency(String currency) {
		currencyCombo.setCurrencyCode(currency);
	}

	public String getCurrency() {
		return currencyCombo.getCurrencyCode();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	public void setEnabledCurrencyCombo(boolean enabled) {
		currencyCombo.setEnabled(enabled);
	}


	public void setEnabledGrossButton(boolean enabled) {
		grossButton.setEnabled(enabled);
	}


	public void setEnabledNetButton(boolean enabled) {
		netButton.setEnabled(enabled);
	}


	public boolean isGross() {
		return grossButton.getSelection();
	}


	// **************************************************************************
	// * Method that should be overwritten in subclasses to add additional widgets.
	// *

	/**
	 * This method should be overwritten in subclasses to add additional widgets.
	 * The widgets have to be added into this Group that has a GridLayout with 5 columns.
	 * All 5 columns have to be filled! Otherwise the layout is destroyed!
	 */
	protected void createAdditionalWidget() {
	}


	/**
	 * This method should be overwritten in subclasses to set the height of additional widgets.
	 */
	protected void setAdditionalWidgetHeight(int heightHint) {
	}


	/**
	 * This method should be overwritten in subclasses to add the additional widgets either to the
	 * ModifyListener or the SelectionListener.
	 */
	protected void addModifyListenerToAdditionalWidgets(
		ModifyListener modifyListener,
		SelectionListener selectionListener
	) {
	}


	/**
	 * This method should be overwritten in subclasses to copy data from the additional widgets
	 * to the entity.
	 */
	protected void syncAdditionalWidgetToEntity() {
	}


	/**
	 * This method should be overwritten in subclasses to copy data from the entity
	 * to the additional widgets.
	 */
	protected void syncEntityToAdditionalWidget() {
	}

	// *
	// * Method that should be overwritten in subclasses to add additional widgets.
	// **************************************************************************

}
