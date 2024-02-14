package de.regasus.programme.cancelterm.editor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.CancelationTermVO;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
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
 * The user can edit the "percent" value, then the price amount gets changed, and the user can edit also the price
 * amount, then the percent gets changed. The original logic is found in the class CancelationTermEditorPane of the
 * Swing client.
 * <p>
 * TODO: Complete the update of price/percent when the user changes and leaves the according input field.
 *
 * @author manfred
 *
 */
public class ProgrammeCancelationTermComposite extends Composite {

	// **************************************************************************
	// * Entities
	// *

	protected ProgrammeCancelationTermVO cancelationTermVO;

	protected ProgrammeOfferingVO offeringVO;

	// *
	// * Entities
	// **************************************************************************

	// **************************************************************************
	// * Widgets
	// *

	protected DateTimeComposite startDateText;

	protected DateTimeComposite endDateText;

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


	public ProgrammeCancelationTermComposite(Composite parent, int style, Long eventPK) throws Exception {
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

		// Prozent

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

		priceGroup = new PriceGroup(this, SWT.NONE, eventPK);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.horizontalSpan = 3;
		priceGroup.setLayoutData(layoutData);

		priceGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				syncPercentWithPrice();
			}
		});

		// disable widgets whose values must not change, because they are inherited from the offering
		priceGroup.setEnabledCurrencyCombo(false);
		priceGroup.setEnabledGrossButton(false);
		priceGroup.setEnabledNetButton(false);

		priceGroup.addModifyListener(modifySupport);


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
				if (syncStartDate)
					return;
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
			boolean syncEndDate = false;

			@Override
			public void modifyText(ModifyEvent e) {
				if (syncEndDate)
					return;
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
	public void setCancelationTermVO(ProgrammeCancelationTermVO cancelationTermVO) {
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
					if (offeringVO.isGross()) {
						// get brutto amounts
						cancellationFee = priceGroup.getAmountGross();
						offeringAmount = offeringVO.getAmountGross();
					}
					else {
						// get netto amounts
						cancellationFee = priceGroup.getAmountNetto();
						offeringAmount = offeringVO.getAmountNet();
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
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
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
					BigDecimal percentValue = percentNumberText.getValue();

					if (percentValue != null) {
						BigDecimal amount = BigDecimal.ZERO;
						if (offeringVO.isGross()) {
							amount = offeringVO.getAmountGross();
						}
						else {
							amount = offeringVO.getAmountNet();
						}

						amount = amount.multiply(percentValue);
						amount = amount.divide(NumberHelper.BD_100);

						priceGroup.setAmount(amount);
					}
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
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
	public void setOfferingVO(ProgrammeOfferingVO offeringVO) {
		this.offeringVO = offeringVO;

		// copy values of currency and brutto from offering
		priceGroup.setCurrency(offeringVO.getCurrency());
		priceGroup.setGross(offeringVO.isGross());
		priceGroup.refreshAmounts();
	}

}
