package de.regasus.common.dialog;

import java.math.BigDecimal;
import java.util.Date;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.InvoiceConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PriceDefaultsVO;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.costcenter.combo.CostCenterCombo;
import de.regasus.finance.costunit.combo.CostUnitCombo;
import de.regasus.finance.currency.combo.CurrencyCombo;
import de.regasus.finance.impersonalaccount.combo.ImpersonalAccountCombo;
import de.regasus.finance.invoicenumberrange.combo.InvoiceNoRangeCombo;
import de.regasus.ui.Activator;


public class CreateCancelationTermsDatesAndAmountPage extends WizardPage {

	public static final String NAME = "CreateCancelationTermsDatesAndAmountPage";


	protected boolean showPricePerNight;
	private EventVO eventVO;
	private Long eventID;


	protected DateTimeComposite startDateTimeComposite;

	protected DateTimeComposite endDateTimeComposite;

	protected Button pricePerNightButton;

	protected Button flatPriceButton;

	protected DecimalNumberText percentNumberText;

	private Button amountButton;

	private Button percentButton;

	private DecimalNumberText amountNumberText;

	private CurrencyCombo currencyCombo;

	private Button bruttoButton;

	private Button nettoButton;

	private DecimalNumberText taxRateNumberText;
	private InvoiceNoRangeCombo invoiceNoRangeCombo;
	private ImpersonalAccountCombo impersonalAccountCombo;
	private ImpersonalAccountCombo impersonalAccountTaxCombo;
	private CostCenterCombo costCenter1Combo;
	private CostUnitCombo costCenter2Combo;

	// result of configuration
	private boolean withInvoice = false;
	private boolean withCostCenter1 = false;
	private boolean withCostCenter2 = false;
	private boolean withImpersonalAccount = false;


	public CreateCancelationTermsDatesAndAmountPage(EventVO eventVO, boolean showPricePerNight) {
		super(NAME);

		if (eventVO == null) {
			throw new IllegalArgumentException("Parameter 'eventVO' is null.");
		}

		this.eventVO = eventVO;
		this.showPricePerNight = showPricePerNight;

		eventID = eventVO.getID();

		try {
			ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventID);
			withInvoice = configParameterSet.getEvent().getInvoice().isVisible();
			InvoiceConfigParameterSet invoiceDetails = configParameterSet.getInvoiceDetails();
			if (withInvoice) {
				withCostCenter1 = invoiceDetails.getCostCenter1().isVisible();
				withCostCenter2 = invoiceDetails.getCostCenter2().isVisible();
				withImpersonalAccount = invoiceDetails.getImpersonalAccount().isVisible();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Shows three radio buttons to determine who should be the participant
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());

		setControl(container);

		final Composite controlComposite = new Composite(container, SWT.NONE);
		controlComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		controlComposite.setLayout(new GridLayout(4, false));


		try {
			// load default values of lodge price
			PriceDefaultsVO priceDefaultsVO = eventVO.getHotelLodgePriceDefaultsVO();



			// Start Date
			Label startDateLabel = new Label(controlComposite, SWT.RIGHT);
			startDateLabel.setText(InvoiceLabel.StartDate.getString());
			startDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

			startDateTimeComposite = new DateTimeComposite(controlComposite, SWT.BORDER);
			startDateTimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			WidgetSizer.setWidth(startDateTimeComposite);

			new Label(controlComposite, SWT.NONE); // dummy

			// End Date
			Label endDateLabel = new Label(controlComposite, SWT.RIGHT);
			endDateLabel.setText(InvoiceLabel.EndDate.getString());
			endDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

			endDateTimeComposite = new DateTimeComposite(controlComposite, SWT.BORDER);
			endDateTimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			WidgetSizer.setWidth(endDateTimeComposite);


			if (showPricePerNight) {
				{
					Label dummyRow = new Label(controlComposite, SWT.RIGHT); // DummyRow
					dummyRow.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 4, 1));
				}
				// Price per Night
				{
					Label priceCalculationLabel = new Label(controlComposite, SWT.RIGHT);
					priceCalculationLabel.setText(HotelLabel.HotelCancelationTerm_PriceCalculation.getString());
					priceCalculationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

					Composite pricePerNightComposite = new Composite(controlComposite, SWT.NONE);
					GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
					pricePerNightComposite.setLayoutData(layoutData);
					pricePerNightComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

					pricePerNightButton = new Button(pricePerNightComposite, SWT.RADIO);
					pricePerNightButton.setText(HotelLabel.HotelCancelationTerm_PricePerNight.getString());

					flatPriceButton = new Button(pricePerNightComposite, SWT.RADIO);
					flatPriceButton.setText(HotelLabel.HotelCancelationTerm_FlatPrice.getString());

					// set default value
					pricePerNightButton.setSelection(true);
					flatPriceButton.setSelection(false);
				}
			}
			{
				Label dummyRow = new Label(controlComposite, SWT.RIGHT); // DummyRow
				dummyRow.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 4, 1));
			}


			// Percent
			percentButton = new Button(controlComposite, SWT.RADIO);

			Label percentLabel = new Label(controlComposite, SWT.RIGHT);
			percentLabel.setText(InvoiceLabel.Percent.getString());
			percentLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

			percentNumberText = new DecimalNumberText(controlComposite, SWT.BORDER);
			percentNumberText.setFractionDigits(1);
			percentNumberText.setNullAllowed(false);
			percentNumberText.setShowPercent(true);
			percentNumberText.setMaxValue(100);
			percentNumberText.setMinValue(0);
			percentNumberText.setValue(0.0);

			percentNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			new Label(controlComposite, SWT.NONE); // dummy


			// amount
			amountButton = new Button(controlComposite, SWT.RADIO);

			Label amountLabel = new Label(controlComposite, SWT.RIGHT);
			amountLabel.setText(InvoiceLabel.Amount.getString());
			amountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

			amountNumberText = new DecimalNumberText(controlComposite, SWT.BORDER);
			amountNumberText.setFractionDigits(2);
			amountNumberText.setValue(0.0);
			amountNumberText.setNullAllowed(false);
			amountNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

			currencyCombo = new CurrencyCombo(controlComposite, SWT.NONE);
			currencyCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

			// set default value
			currencyCombo.setCurrencyCode(priceDefaultsVO.getCurrency());


			// price type (gross/net)
			Label priceTypeLabel = new Label(controlComposite, SWT.NONE);
			priceTypeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
			priceTypeLabel.setText(InvoiceLabel.PriceType.getString());

			final Composite composite = new Composite(controlComposite, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			composite.setLayout(new RowLayout(SWT.HORIZONTAL));

			bruttoButton = new Button(composite, SWT.RADIO);
			bruttoButton.setText(InvoiceLabel.gross.getString());

			nettoButton = new Button(composite, SWT.RADIO);
			nettoButton.setText(InvoiceLabel.net.getString());

			// set default values
			nettoButton.setSelection(!priceDefaultsVO.isGross());
			bruttoButton.setSelection(priceDefaultsVO.isGross());


			// tax rate
			Label taxRateLabel = new Label(controlComposite, SWT.RIGHT);
			taxRateLabel.setText(InvoiceLabel.SalesTax.getString());
			taxRateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

			taxRateNumberText = new DecimalNumberText(controlComposite, SWT.BORDER);
    		taxRateNumberText.setFractionDigits(1);
    		taxRateNumberText.setNullAllowed(false);
    		taxRateNumberText.setShowPercent(true);
    		taxRateNumberText.setMaxValue(100);
    		taxRateNumberText.setMinValue(0);
			taxRateNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

			// set default value
			taxRateNumberText.setValue(priceDefaultsVO.getTaxRate());

			new Label(controlComposite, SWT.NONE); // dummy


			// Invoice Number Range
			if (withInvoice) {
        		Label invoiceNumberRangeLabel = new Label(controlComposite, SWT.RIGHT);
        		invoiceNumberRangeLabel.setText(InvoiceLabel.InvoiceNoRange.getString());
        		invoiceNumberRangeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

        		invoiceNoRangeCombo = new InvoiceNoRangeCombo(controlComposite, SWT.NONE, eventID);
        		invoiceNoRangeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

    			// set default value
        		invoiceNoRangeCombo.setInvoiceNoRangeByPK(priceDefaultsVO.getInvoiceNoRangePK());

    			new Label(controlComposite, SWT.NONE); // dummy


    			if (withImpersonalAccount) {
        			// Impersonal Account
        			Label impersonalAccountLabel = new Label(controlComposite, SWT.RIGHT);
        			impersonalAccountLabel.setText(InvoiceLabel.ImpersonalAccount.getString());
        			impersonalAccountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

        			impersonalAccountCombo = new ImpersonalAccountCombo(controlComposite, SWT.NONE);
        			impersonalAccountCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        			new Label(controlComposite, SWT.NONE); // dummy


        			// Impersonal Account Tax
        			Label impersonalAccountTaxLabel = new Label(controlComposite, SWT.RIGHT);
        			impersonalAccountTaxLabel.setText(InvoiceLabel.ImpersonalAccountForSalesTax.getString());
        			impersonalAccountTaxLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

        			impersonalAccountTaxCombo = new ImpersonalAccountCombo(controlComposite, SWT.NONE);
        			impersonalAccountTaxCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        			// set default values
        			impersonalAccountCombo.setImpersonalAccountPK(priceDefaultsVO.getImpersonalAccountNo());
        			impersonalAccountTaxCombo.setImpersonalAccountPK(priceDefaultsVO.getImpersonalAccountNoTax());

        			new Label(controlComposite, SWT.NONE); // dummy
    			}

    			// cost center 1
    			if (withCostCenter1) {
        			Label costCenterLabel = new Label(controlComposite, SWT.NONE);
        			costCenterLabel.setText(InvoiceLabel.CostCenter.getString());
        			costCenterLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

        			costCenter1Combo = new CostCenterCombo(controlComposite, SWT.NONE);
        			costCenter1Combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        			// set default value
        			costCenter1Combo.setCostCenter1(priceDefaultsVO.getCostCenter1());

        			new Label(controlComposite, SWT.NONE); // dummy
    			}

    			// cost center 2
    			if (withCostCenter2) {
        			Label costUnitLabel = new Label(controlComposite, SWT.NONE);
        			costUnitLabel.setText(InvoiceLabel.CostUnit.getString());
        			costUnitLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

        			costCenter2Combo = new CostUnitCombo(controlComposite, SWT.NONE);
        			costCenter2Combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        			// set default value
        			costCenter2Combo.setCostCenter2(priceDefaultsVO.getCostCenter2());

        			new Label(controlComposite, SWT.NONE); // dummy
    			}
			}



			startDateTimeComposite.addModifyListener(new ModifyListener() {
				boolean syncStartDate = false;

				@Override
				public void modifyText(ModifyEvent e) {
					if (syncStartDate)
						return;
					Date startDate = startDateTimeComposite.getDate();
					Date endDate = endDateTimeComposite.getDate();
					if (endDate != null && startDate != null && startDate.after(endDate)) {
						syncStartDate = true;
						Display.getCurrent().beep();
						startDateTimeComposite.setDate(endDate);
						syncStartDate = false;
					}
				}
			});

			endDateTimeComposite.addModifyListener(new ModifyListener() {
				boolean syncEndDate = false;

				@Override
				public void modifyText(ModifyEvent e) {
					if (syncEndDate)
						return;
					Date startDate = startDateTimeComposite.getDate();
					Date endDate = endDateTimeComposite.getDate();
					if (endDate != null && startDate != null && endDate.before(startDate)) {
						syncEndDate = true;
						Display.getCurrent().beep();
						endDateTimeComposite.setDate(startDate);
						syncEndDate = false;
					}
				}
			});


			// handle radio buttons (percent and amount)
			SelectionAdapter selectionListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					try {
						if (!ModifySupport.isDeselectedRadioButton(event)) {
							adjustEditableStates();
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			};
			percentButton.addSelectionListener(selectionListener);
			amountButton.addSelectionListener(selectionListener);

			// set default selection (this won't call adjustEditableStates())
			percentButton.setSelection(true);
			adjustEditableStates();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		setPageComplete(true);
	}


	private void adjustEditableStates() {
		percentNumberText.setEnabled(percentButton.getSelection());

		amountNumberText.setEnabled(! percentButton.getSelection());
		currencyCombo.setEnabled(! percentButton.getSelection());
		bruttoButton.setEnabled(! percentButton.getSelection());
		nettoButton.setEnabled(! percentButton.getSelection());
	}


	public PriceVO getPrice() {
		PriceVO priceVO = new PriceVO();

		// set values that have to be set all the time
		priceVO.setTaxRate( getTaxRate() );
		priceVO.setInvoiceNoRangePK( getInvoiceNoRangePK() );
		priceVO.setImpersonalAccountNo( getImpersonalAccountNo() );
		priceVO.setImpersonalAccountNoTax( getImpersonalAccountNoTax() );
		priceVO.setCostCenter1( getCostCenter1() );
		priceVO.setCostCenter2( getCostCenter2() );

		if (amountButton.getSelection()) {
			priceVO.setAmount( getAmount(), isBrutto() );
			priceVO.setCurrency( getCurrency() );
		}

		return priceVO;
	}


	public Date getStartDate() {
		return startDateTimeComposite.getDate();
	}


	public Date getEndDate() {
		return endDateTimeComposite.getDate();
	}


	public BigDecimal getPercent() {
		BigDecimal percent = null;
		if (isPercentValue()) {
			percent = percentNumberText.getValue();
		}
		return percent;
	}


	public boolean isPercentValue() {
		return percentButton.getSelection();
	}


	public boolean isPricePerNight() {
		return pricePerNightButton != null && pricePerNightButton.getSelection();
	}


	private BigDecimal getAmount() {
		return amountNumberText.getValue();
	}


	private boolean isBrutto() {
		return bruttoButton.getSelection();
	}


	private String getCurrency() {
		return currencyCombo.getCurrencyCode();
	}


	private BigDecimal getTaxRate() {
		return taxRateNumberText.getValue();
	}


	private Long getInvoiceNoRangePK() {
		Long invoiceNoRangePK = null;
		if (invoiceNoRangeCombo != null) {
			invoiceNoRangePK = invoiceNoRangeCombo.getInvoiceNoRangePK();
		}
		return invoiceNoRangePK;
	}


	private Integer getImpersonalAccountNo() {
		Integer impersonalAccountNo = null;
		if (impersonalAccountCombo != null) {
			impersonalAccountNo = impersonalAccountCombo.getImpersonalAccountNo();
		}
		return impersonalAccountNo;
	}


	private Integer getImpersonalAccountNoTax() {
		Integer impersonalAccountNoTax = null;
		if (impersonalAccountTaxCombo != null) {
			impersonalAccountNoTax = impersonalAccountTaxCombo.getImpersonalAccountNo();
		}
		return impersonalAccountNoTax;
	}


	private Integer getCostCenter1() {
		Integer costCenter1 = null;
		if (costCenter1Combo != null) {
			costCenter1 = costCenter1Combo.getCostCenter1();
		}
		return costCenter1;
	}


	private Integer getCostCenter2() {
		Integer costCenter2 = null;
		if (costCenter2Combo != null) {
			costCenter2 = costCenter2Combo.getCostCenter2();
		}
		return costCenter2;
	}

}
