package de.regasus.hotel.cancelterm.editor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.CancelationTermVO;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.util.NumberHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.PriceGroup;
import de.regasus.ui.Activator;

/**
 * The user can edit the "percent" value, then the price amount gets changed, and the user can edit
 * also the price amount, then the percent gets changed.
 */
public class HotelCancelationTermComposite extends Composite {

	// **************************************************************************
	// * Entities
	// *

	protected HotelCancelationTermVO cancelationTermVO;

	protected HotelOfferingVO offeringVO;

	// *
	// * Entities
	// **************************************************************************

	// **************************************************************************
	// * Widgets
	// *

	protected DateTimeComposite startDateText;

	protected DateTimeComposite endDateText;

	protected Button pricePerNightButton;
	protected Button flatPriceButton;

	protected DecimalNumberText percentNumberText;

	protected PriceGroup priceGroup;

	// *
	// * Widgets
	// **************************************************************************

	private ModifySupport modifySupport = new ModifySupport(this);

	/**
	 * Is true while synchronizing the percent value with the price or vice versa.
	 */
	private boolean inPercentPriceSync = false;


	public HotelCancelationTermComposite(Composite parent, int style, Long eventPK) throws Exception {
		super(parent, style);

		setLayout(new GridLayout(3, false));

		// Eckdaten

		// Start Date
		Label startDateLabel = new Label(this, SWT.RIGHT);

		startDateLabel.setText(InvoiceLabel.StartDate.getString());
		startDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		startDateText = new DateTimeComposite(this, SWT.BORDER);
		startDateText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		startDateText.addModifyListener(modifySupport);


		// End Date
		Label endDateLabel = new Label(this, SWT.RIGHT);
		endDateLabel.setText(InvoiceLabel.EndDate.getString());
		endDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		endDateText = new DateTimeComposite(this, SWT.BORDER);
		endDateText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		endDateText.addModifyListener(modifySupport);


		// Price per Night
		{
			Label priceCalculationLabel = new Label(this, SWT.RIGHT);
			priceCalculationLabel.setText(HotelLabel.HotelCancelationTerm_PriceCalculation.getString());
			priceCalculationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

			Composite pricePerNightComposite = new Composite(this, SWT.NONE);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			layoutData.horizontalSpan = 2;
			pricePerNightComposite.setLayoutData(layoutData);
			pricePerNightComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

			pricePerNightButton = new Button(pricePerNightComposite, SWT.RADIO);
			pricePerNightButton.setText(HotelLabel.HotelCancelationTerm_PricePerNight.getString());

			pricePerNightButton.addSelectionListener(modifySupport);

			flatPriceButton = new Button(pricePerNightComposite, SWT.RADIO);
			flatPriceButton.setText(HotelLabel.HotelCancelationTerm_FlatPrice.getString());

			flatPriceButton.addSelectionListener(modifySupport);
		}


		// Percent

		Label percentLabel = new Label(this, SWT.RIGHT);
		percentLabel.setText(InvoiceLabel.Percent.getString());
		percentLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		percentNumberText = new DecimalNumberText(this, SWT.BORDER);
		percentNumberText.setFractionDigits(1);
		percentNumberText.setNullAllowed(true);
		percentNumberText.setShowPercent(true);
		// percentNumberText.setMaxValue(100);
		// percentNumberText.setMinValue(0);
		percentNumberText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		percentNumberText.setMaxValue(1000);
		WidgetSizer.setWidth(percentNumberText);
		percentNumberText.setMaxValue(null);

		new Label(this, SWT.RIGHT); // Dummy

		// Price
		{
			priceGroup = new PriceGroup(this, SWT.NONE, eventPK);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			layoutData.horizontalSpan = 3;
			priceGroup.setLayoutData(layoutData);

			priceGroup.addModifyListener(modifySupport);
		}


		// disable widgets whose values must not change, because they are inherited from the offering
		priceGroup.setEnabledCurrencyCombo(false);
		priceGroup.setEnabledGrossButton(false);
		priceGroup.setEnabledNetButton(false);


		// add ModifyListeners
		priceGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				syncPercentWithPrice();
			}
		});

		percentNumberText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				syncPriceWithPercent();
			}
		});


		startDateText.addModifyListener(new ModifyListener() {
			boolean syncStartDate = false;

			@Override
			public void modifyText(ModifyEvent e) {
				// avoid setting startDate after endDate

				if (syncStartDate) {
					// stop if this event is caused by setDate() in this method
					return;
				}

				Date startDate = startDateText.getDate();
				Date endDate = endDateText.getDate();
				if (endDate != null && startDate != null && startDate.after(endDate)) {
					syncStartDate = true;
					Display.getCurrent().beep();
					startDateText.setDate(endDate);
					syncStartDate = false;
				}
			}
		});

		endDateText.addModifyListener(new ModifyListener() {
			// avoid setting endDate before startDate
			boolean syncEndDate = false;

			@Override
			public void modifyText(ModifyEvent e) {
				if (syncEndDate) {
					// stop if this event is caused by setDate() in this method
					return;
				}

				Date startDate = startDateText.getDate();
				Date endDate = endDateText.getDate();
				if (endDate != null && startDate != null && endDate.before(startDate)) {
					syncEndDate = true;
					Display.getCurrent().beep();
					endDateText.setDate(startDate);
					syncEndDate = false;
				}
			}
		});
	}


	/**
	 * @return the cancelationTermVO
	 */
	public CancelationTermVO getCancelationTermVO() {
		return cancelationTermVO;
	}


	/**
	 * @param cancelationTermVO
	 *            the cancelationTermVO to set
	 */
	public void setCancelationTermVO(HotelCancelationTermVO cancelationTermVO) {
		this.cancelationTermVO = cancelationTermVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					startDateText.setDate(cancelationTermVO.getStartTime());
					endDateText.setDate(cancelationTermVO.getEndTime());
					pricePerNightButton.setSelection(cancelationTermVO.isPricePerNight());
					flatPriceButton.setSelection(!cancelationTermVO.isPricePerNight());
					priceGroup.setPriceVO(getPriceVO());
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	public void syncEntityToWidgets() {
		cancelationTermVO.setStartTime(startDateText.getDate());
		cancelationTermVO.setEndTime(endDateText.getDate());
		cancelationTermVO.setPricePerNight(pricePerNightButton.getSelection());

		priceGroup.syncEntityToWidgets();
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	private void syncPercentWithPrice() {
		if (!inPercentPriceSync) {
			inPercentPriceSync = true;

			try {
				PriceVO priceVO = getPriceVO();
				if (priceVO != null && offeringVO != null) {
					BigDecimal cancellationFee = BigDecimal.ZERO;
					BigDecimal offeringAmount = BigDecimal.ZERO;

					if (offeringVO.getLodgePriceVO().isGross()) {
						// get brutto amounts
						cancellationFee = priceGroup.getAmountGross();

						offeringAmount = offeringVO.getCurrencyAmountGross().getAmount();
					}
					else {
						// get netto amounts
						cancellationFee = priceGroup.getAmountNetto();

						offeringAmount = offeringVO.getCurrencyAmountNet().getAmount();
					}

					if (cancellationFee.signum() != 0 && offeringAmount.signum() != 0) {
						BigDecimal percentValue = cancellationFee.multiply(NumberHelper.BD_100);
						percentValue = percentValue.divide(
							offeringAmount,			// divisor
							1,						// scale
							RoundingMode.HALF_UP	// roundingMode
						);

						// the value will be rounded according to the precision value of the DecimalNumberText widget
						percentNumberText.setValue(percentValue);
					}
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
			finally {
				inPercentPriceSync = false;
			}
		}
	}


	private void syncPriceWithPercent() {
		if (!inPercentPriceSync) {
			inPercentPriceSync = true;

			try {
				PriceVO priceVO = getPriceVO();
				if (priceVO != null && offeringVO != null) {
					PriceVO offeringPriceVO = offeringVO.getLodgePriceVO();

					BigDecimal percentValue = percentNumberText.getValue();

					if (percentValue != null) {
						BigDecimal dblPercentValue = percentValue;

						BigDecimal amount = BigDecimal.ZERO;
						if (offeringPriceVO.isGross()) {
							amount = offeringVO.getCurrencyAmountGross().getAmount();
						}
						else {
							amount = offeringVO.getCurrencyAmountNet().getAmount();
						}

						amount = amount.multiply(dblPercentValue);
						amount = amount.divide(NumberHelper.BD_100);

						priceGroup.setAmount(amount);
						priceGroup.setTaxRate(offeringPriceVO.getTaxRate());
						priceGroup.setGross(offeringPriceVO.isGross());
						priceGroup.setCurrency(offeringPriceVO.getCurrency());
					}
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
			finally {
				inPercentPriceSync = false;
			}
		}
	}


	/**
	 * @return the priceVO
	 */
	public PriceVO getPriceVO() {
		PriceVO priceVO = null;
		if (cancelationTermVO != null) {
			priceVO = cancelationTermVO.getPriceVO();
		}
		return priceVO;
	}


	/**
	 * @param offeringVO
	 *            the offeringVO to set
	 */
	public void setOfferingVO(HotelOfferingVO offeringVO) {
		this.offeringVO = offeringVO;

		// copy values of currency and brutto from offering
		priceGroup.setCurrency(offeringVO.getCurrency());
		priceGroup.setGross(offeringVO.isGross());
		priceGroup.refreshAmounts();
	}

}
