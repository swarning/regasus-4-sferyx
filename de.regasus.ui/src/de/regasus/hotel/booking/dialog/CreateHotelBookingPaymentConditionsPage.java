package de.regasus.hotel.booking.dialog;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingPaymentCondition;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;

import de.regasus.I18N;

/**
 * A page in the wizard to create hotel bookings where the user can select the payment conditions from
 * (like booking, credit card, deposit, self-payment and credit card). Only those buttons are enabled
 * whose policy is allowed for that offering in the database.
 * <p>
 * See https://mi2.lambdalogic.de/jira/browse/MIRCP-84
 */

public class CreateHotelBookingPaymentConditionsPage extends WizardPage implements ModifyListener {

	public static final String NAME = "HotelPaymentConditionsPage";

	private Button bookingRadioButton;
	private Button selfPayPatientRadioButton;
	private Button absorptionOfCostsRadioButton;
	private Button depositRadioButton;
	
	private DecimalNumberText depositNumberText;
	private Label depositCurrencyLabel;

	private Map<HotelBookingPaymentCondition, Button> condition2ButtonMap = new TreeMap<HotelBookingPaymentCondition, Button>();

	private HotelBookingPaymentCondition hotelBookingPaymentCondition;


	public CreateHotelBookingPaymentConditionsPage() {
		super(NAME);

		setTitle(I18N.CreateHotelBookingPaymentConditionsPage_Title);
		setMessage(I18N.CreateHotelBookingPaymentConditionsPage_Message);
	}


	@Override
	public CreateHotelBookingWizard getWizard() {
		return (CreateHotelBookingWizard) super.getWizard();
	}


	@Override
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(new GridLayout(3, false));

		// Booking
		bookingRadioButton = createRadioButton(
			controlComposite,
			HotelBookingPaymentCondition.BOOKING_AMOUNT,
			false	// isDeposit
		);
		selfPayPatientRadioButton = createRadioButton(
			controlComposite,
			HotelBookingPaymentCondition.SELF_PAY_PATIENT,
			false	// isDeposit
		);
		absorptionOfCostsRadioButton = createRadioButton(
			controlComposite,
			HotelBookingPaymentCondition.ABSORPTION_OF_COSTS,
			false	// isDeposit
		);
		depositRadioButton = createRadioButton(
			controlComposite,
			HotelBookingPaymentCondition.DEPOSIT,
			true	// isDeposit
		);

		depositNumberText = new DecimalNumberText(controlComposite, SWT.BORDER);
		depositNumberText.setFractionDigits(2);
		depositNumberText.setNullAllowed(HotelOfferingVO.NULL_ALLOWED_DEPOSIT);
		depositNumberText.setMinValue(0);
		depositNumberText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// row 7, col 4
		depositCurrencyLabel = new Label(controlComposite, SWT.NONE);
		depositCurrencyLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// observe HotelOfferingsTablePage and react on changes of selected Hotel Offering
		getWizard().getHotelOfferingsTablePage().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				hotelOfferingChanged();
			}
		});

		// Never forget:
		setControl(controlComposite);
	}


	private void hotelOfferingChanged() {
		HotelOfferingsTablePage hotelOfferingsTablePage = getWizard().getHotelOfferingsTablePage();
		HotelOfferingCVO hotelOfferingCVO = hotelOfferingsTablePage.getBookedHotelOfferingCVO();

		// if no Hotel Offering is selected, the wizard cannot book anyway, no action is necessary
		if (hotelOfferingCVO != null) {
			setHotelOffering( hotelOfferingCVO.getVO() );
		}
	}


	public HotelBookingPaymentCondition getPaymentCondition() {
		return hotelBookingPaymentCondition;
	}


	public void setHotelOffering(HotelOfferingVO hotelOfferingVO) {
		bookingRadioButton.setEnabled(hotelOfferingVO.isPaymentConditionBookingAmount());
		selfPayPatientRadioButton.setEnabled(hotelOfferingVO.isPaymentConditionSelfPayPatient());
		absorptionOfCostsRadioButton.setEnabled(hotelOfferingVO.isPaymentConditionAbsorptionOfCosts());
		depositRadioButton.setEnabled(hotelOfferingVO.isPaymentConditionDeposit());

		bookingRadioButton.setSelection(false);
		selfPayPatientRadioButton.setSelection(false);
		absorptionOfCostsRadioButton.setSelection(false);
		depositRadioButton.setSelection(false);
		depositNumberText.setValue(hotelOfferingVO.getDeposit());
		String currency = hotelOfferingVO.getLodgePriceVO().getCurrency();
		currency = StringHelper.avoidNull(currency);
		depositCurrencyLabel.setText(currency);

		hotelBookingPaymentCondition = hotelOfferingVO.getDefaultPaymentCondition();
		Button button = condition2ButtonMap.get(hotelBookingPaymentCondition);
		button.setSelection(true);

		updateStates();
	}


	@Override
	public void modifyText(ModifyEvent e) {
		updateStates();
	}

	// *************************************************************************
	// * Private helper methods
	// *


	private Button createRadioButton(Composite composite, HotelBookingPaymentCondition paymentCondition, boolean isDeposit) {
		final Button radioButton = new Button(composite, SWT.RADIO);
		if (isDeposit) {
			radioButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		}
		else {
			radioButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
		}
		radioButton.setData(paymentCondition);
		radioButton.setText(HotelBookingVO.getLabelForHotelBookingPaymentCondition(paymentCondition).getString());
		
		radioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (radioButton.getSelection()) {
					CreateHotelBookingPaymentConditionsPage.this.hotelBookingPaymentCondition = 
						(HotelBookingPaymentCondition) radioButton.getData();
				}
				updateStates();
			}
		});
		
		condition2ButtonMap.put(paymentCondition, radioButton);

		return radioButton;
	}


	private void updateStates() {
		depositNumberText.setVisible(depositRadioButton.getSelection());
		depositCurrencyLabel.setVisible(depositRadioButton.getSelection());

		setPageComplete(isPageComplete());
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	public BigDecimal getDepositAmount() {
		return depositNumberText.getValue();
	}

}
