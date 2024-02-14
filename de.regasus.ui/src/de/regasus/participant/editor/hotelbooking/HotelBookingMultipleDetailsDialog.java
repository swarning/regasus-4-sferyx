package de.regasus.participant.editor.hotelbooking;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.ArrivalInfo;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingChangeFlags;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingPaymentCondition;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.SmokerType;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.DeepEnablingComposite;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.hotel.booking.combo.HotelBookingPaymentConditionCombo;
import de.regasus.hotel.offering.combo.HotelOfferingCombo;
import de.regasus.ui.Activator;

/**
 * A dialog that shows a subset of the attributes of the "regular" {@link HotelBookingDetailsDialog},
 * each together with a checkbox that must be selected to enable the editing of the attribute and
 * to let it change on the server.
 */
public class HotelBookingMultipleDetailsDialog extends TitleAreaDialog {

	private static final String LABEL = "label";


	// *************************************************************************
	// * Widgets
	// *

	private I18NText infoI18Ntext;

	private DateComposite arrivalDateComposite;

	private DateComposite departureDateComposite;

	private MultiLineText hotelInfoText;

	private MultiLineText hotelPaymentInfoText;

	private MultiLineText additionalGuestsText;

	private HotelOfferingCombo hotelOfferingCombo;

	private HotelBookingPaymentConditionCombo hotelBookingPaymentConditionCombo;

	private DecimalNumberText depositAmountNumberText;

	private Button earlyArrivalRadioButton;

	private Button lateArrivalRadioButton;

	private Text arrivalNoteText;

	private Button nonSmokerRadioButton;

	private Button smokerRadioButton;

	private Button unknkownSmokerRadioButton;

	private Button twinRoomCheckBox;

	private Button changeGuestInfoCheckBox;

	private Button changeHotelOfferingCheckbox;

	private Button changePaymentConditionCheckbox;

	private Button changeDepositAmountCheckbox;

	private Button changeArrivalCheckbox;

	private Button changeDepartureCheckbox;

	private Button changeArrivalInfoCheckbox;

	private Button changeArrivalNoteCheckbox;

	private Button changeTwinRoomCheckbox;

	private Button changeSmokerCheckbox;

	private Button changeHotelInfoCheckbox;

	private Button changeHotelPaymentInfoCheckbox;

	private Button changeAdditionalGuestsCheckbox;

	// *
	// * Widgets
	// *************************************************************************

	/**
	 * List of hotel bookings to change.
	 */
	private List<HotelBookingCVO> bookingCVOs;

	/**
	 * Stores if all hotel bookings belong to the same contingent.
	 * Some widgets are only available in that case.
	 */
	private boolean differentContingents;


	public HotelBookingMultipleDetailsDialog(Shell parentShell, List<HotelBookingCVO> bookingCVOs) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.bookingCVOs = bookingCVOs;

		// init differentContingents
		differentContingents = false;
		Long hotelContingentPK = bookingCVOs.get(0).getHotelBookingVO().getHotelContingentPK();
		for (HotelBookingCVO hotelBookingCVO : bookingCVOs) {
			Long hcPK = hotelBookingCVO.getHotelBookingVO().getHotelContingentPK();
			if (! hotelContingentPK.equals(hcPK)) {
				differentContingents = true;
				break;
			}
		}
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(I18N.ChangeOfMultipleBookings);

		String message = I18N.ChangeOfMultipleBookingsMessage;
		if (differentContingents) {
			message += " " + I18N.SelectedBookingsNotInSameContingent;
		}
		setMessage(message);


		Composite area = (Composite) super.createDialogArea(parent);

		try {
			Composite innerArea = new Composite(area, SWT.NONE);
			innerArea.setLayoutData(new GridData(GridData.FILL_BOTH));
			innerArea.setLayout(new GridLayout(2, false));

			buildBookingDetailsGroup(innerArea);
			buildInfoForHotelGroup(innerArea);
			buildInfoForGuestGroup(innerArea);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return area;
	}


	private void buildBookingDetailsGroup(Composite parent) throws Exception {
		Group group = new Group(parent, SWT.NONE);
		group.setText(I18N.BookingDetails);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		group.setLayout(new GridLayout(3, false));


		// Arrival
		changeArrivalCheckbox = createEnablingCheckbox(group);

		createLabel(group, HotelLabel.HotelBooking_Arrival, changeArrivalCheckbox);

		arrivalDateComposite = new DateComposite(group, SWT.BORDER);
		arrivalDateComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		arrivalDateComposite.setEnabled(false);
		changeArrivalCheckbox.setData(arrivalDateComposite);


		// Departure
		changeDepartureCheckbox = createEnablingCheckbox(group);
		createLabel(group, HotelLabel.HotelBooking_Departure, changeDepartureCheckbox);

		departureDateComposite = new DateComposite(group, SWT.BORDER);
		departureDateComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		departureDateComposite.setEnabled(false);
		changeDepartureCheckbox.setData(departureDateComposite);


		/***** Widgets that are only available if all bookings belong to the same contingent *****/

		if (! differentContingents) {
			/* calc initial amount type
			 * There is only an initial amount type if the amount type is the same in all bookings.
			 */
			HotelBookingPaymentCondition initialPaymentCondition = bookingCVOs.get(0).getHotelBookingVO().getPaymentCondition();
			for (HotelBookingCVO hbCVO : bookingCVOs) {
				HotelBookingPaymentCondition currentHotelBookingType = hbCVO.getHotelBookingVO().getPaymentCondition();
				if (!initialPaymentCondition.equals(currentHotelBookingType)) {
					initialPaymentCondition = null;
					break;
				}
			}

			/* calc initial deposit
			 * There is only an initial deposit if all bookings have the amount type deposit
			 * and the same lodge amount.
			 */
			BigDecimal initialDeposit = null;
			if (initialPaymentCondition != null && initialPaymentCondition == HotelBookingPaymentCondition.DEPOSIT) {
				initialDeposit = bookingCVOs.get(0).getHotelBookingVO().getLodgePriceVO().getAmount();

				for (HotelBookingCVO hbCVO : bookingCVOs) {
					BigDecimal currentLodgeAmount = hbCVO.getHotelBookingVO().getLodgePriceVO().getAmount();
					if (initialDeposit.compareTo(currentLodgeAmount) != 0) {
						initialDeposit = null;
						break;
					}
				}
			}



			{
				changeHotelOfferingCheckbox = createEnablingCheckbox(group);

				// Selection of hotelOffering
				createLabel(group, HotelLabel.HotelOffering, changeHotelOfferingCheckbox);

				hotelOfferingCombo = new HotelOfferingCombo(
					group,
					SWT.NONE,
					bookingCVOs.get(0).getVO().getHotelContingentPK()
				);
				hotelOfferingCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				hotelOfferingCombo.setEnabled(false);

				changeHotelOfferingCheckbox.setData(hotelOfferingCombo);
			}

			{
				changePaymentConditionCheckbox = createEnablingCheckbox(group);

				// Selection of HotelBookingPaymentCondition
				createLabel(group, HotelLabel.HotelBookingPaymentCondition, changePaymentConditionCheckbox);


				/* create a HotelOfferingVO that is only used to set the allowed Payment Conditions in the
				 * HotelBookingPaymentConditionCombo. These are the Payment Conditions that are allowed in all offerings.
				 */
				HotelOfferingVO hotelOfferingVO = new HotelOfferingVO();
				hotelOfferingVO.setPaymentConditionAbsorptionOfCosts(true);
				hotelOfferingVO.setPaymentConditionBookingAmount(true);
				hotelOfferingVO.setPaymentConditionDeposit(true);
				hotelOfferingVO.setPaymentConditionSelfPayPatient(true);
				for (HotelBookingCVO hbCVO : bookingCVOs) {
					HotelOfferingVO hoVO = hbCVO.getHotelOfferingCVO().getHotelOfferingVO();

					hotelOfferingVO.setPaymentConditionAbsorptionOfCosts(
						hotelOfferingVO.isPaymentConditionAbsorptionOfCosts() &&
						hoVO.isPaymentConditionAbsorptionOfCosts()
					);
					hotelOfferingVO.setPaymentConditionBookingAmount(
						hotelOfferingVO.isPaymentConditionBookingAmount() &&
						hoVO.isPaymentConditionBookingAmount()
					);
					hotelOfferingVO.setPaymentConditionDeposit(
						hotelOfferingVO.isPaymentConditionDeposit() &&
						hoVO.isPaymentConditionDeposit()
					);
					hotelOfferingVO.setPaymentConditionSelfPayPatient(
						hotelOfferingVO.isPaymentConditionSelfPayPatient() &&
						hoVO.isPaymentConditionSelfPayPatient()
					);
				}

				hotelBookingPaymentConditionCombo = new HotelBookingPaymentConditionCombo(
					group,
					SWT.READ_ONLY,
					hotelOfferingVO
				);

				hotelBookingPaymentConditionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    			hotelBookingPaymentConditionCombo.setEnabled(false);
    			hotelBookingPaymentConditionCombo.setEntity(initialPaymentCondition);
    			changePaymentConditionCheckbox.setData(hotelBookingPaymentConditionCombo);
			}

			{
				changeDepositAmountCheckbox = createEnablingCheckbox(group);

				// Input of deposit amount
				createLabel(group, HotelLabel.HotelBooking_Deposit, changeDepositAmountCheckbox);
				depositAmountNumberText = new DecimalNumberText(group, SWT.BORDER);
				depositAmountNumberText.setFractionDigits(2);
				depositAmountNumberText.setNullAllowed(HotelOfferingVO.NULL_ALLOWED_DEPOSIT);
				depositAmountNumberText.setShowPercent(false);
				depositAmountNumberText.setMinValue(BigDecimal.ZERO);
				GridData depositAmountGridData = new GridData(SWT.FILL, SWT.FILL, false, false);
				depositAmountNumberText.setLayoutData(depositAmountGridData);
				depositAmountNumberText.setValue(initialDeposit);
				depositAmountNumberText.setEnabled(false);
			}
		}
	}


	private void buildInfoForHotelGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(I18N.HotelBooking_InfoForHotel);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setLayout(new GridLayout(3, false));


		// Arrival Info
		changeArrivalInfoCheckbox = createEnablingCheckbox(group);

		createLabel(group, HotelLabel.HotelBooking_ArrivalInfo, changeArrivalInfoCheckbox);

		DeepEnablingComposite arrivalInfoButtons = new DeepEnablingComposite(group, SWT.NONE);
		arrivalInfoButtons.setLayout(new GridLayout(3, false));

		Button normalArrivalRadioButton = new Button(arrivalInfoButtons, SWT.RADIO);
		normalArrivalRadioButton.setText(HotelLabel.HotelBooking_ArrivalInfo_Normal.getString());

		earlyArrivalRadioButton = new Button(arrivalInfoButtons, SWT.RADIO);
		earlyArrivalRadioButton.setText(HotelLabel.HotelBooking_ArrivalInfo_Early.getString());

		lateArrivalRadioButton = new Button(arrivalInfoButtons, SWT.RADIO);
		lateArrivalRadioButton.setText(HotelLabel.HotelBooking_ArrivalInfo_Late.getString());

		changeArrivalInfoCheckbox.setData(arrivalInfoButtons);
		arrivalInfoButtons.setEnabled(false);


		// Arrival Time
		changeArrivalNoteCheckbox = createEnablingCheckbox(group);

		createLabel(group, HotelLabel.HotelBooking_ArrivalNote, changeArrivalNoteCheckbox);

		arrivalNoteText = new Text(group, SWT.NONE);

		changeArrivalNoteCheckbox.setData(arrivalNoteText);
		arrivalNoteText.setEnabled(false);



		// Twin Room
		changeTwinRoomCheckbox = createEnablingCheckbox(group);

		createLabel(group, HotelLabel.HotelBooking_TwinRoom, changeTwinRoomCheckbox);

		twinRoomCheckBox = new Button(group, SWT.CHECK);
		twinRoomCheckBox.setEnabled(false);
		changeTwinRoomCheckbox.setData(twinRoomCheckBox);


		// Smoker
		changeSmokerCheckbox = createEnablingCheckbox(group);

		createLabel(group, HotelLabel.SmokerInfo, changeSmokerCheckbox);

		DeepEnablingComposite smokerInfoButtons = new DeepEnablingComposite(group, SWT.NONE);
		smokerInfoButtons.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));
		smokerInfoButtons.setLayout(new GridLayout(3, false));

		nonSmokerRadioButton = new Button(smokerInfoButtons, SWT.RADIO);
		nonSmokerRadioButton.setText(SmokerType.NON_SMOKER.getString());

		smokerRadioButton = new Button(smokerInfoButtons, SWT.RADIO);
		smokerRadioButton.setText(SmokerType.SMOKER.getString());

		unknkownSmokerRadioButton = new Button(smokerInfoButtons, SWT.RADIO);
		unknkownSmokerRadioButton.setText(HotelLabel.SmokerTypeEmpty.getString());
		smokerInfoButtons.setEnabled(false);
		changeSmokerCheckbox.setData(smokerInfoButtons);


		// Hotel Info
		changeHotelInfoCheckbox = createEnablingCheckbox(group);
		GridData hotelInfoCheckBoxGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		hotelInfoCheckBoxGridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		changeHotelInfoCheckbox.setLayoutData(hotelInfoCheckBoxGridData);

		Label hotelInfoLabel = new Label(group, SWT.RIGHT);
		hotelInfoLabel.setText(I18N.HotelBooking_HotelInfo_General);
		GridData hotelInfoLabelGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		hotelInfoLabelGridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		hotelInfoLabel.setLayoutData(hotelInfoLabelGridData);
		hotelInfoLabel.setEnabled(false);

		hotelInfoText = new MultiLineText(group, SWT.BORDER, false);
		hotelInfoText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		hotelInfoText.setEnabled(false);
		changeHotelInfoCheckbox.setData(hotelInfoText);
		changeHotelInfoCheckbox.setData(LABEL, hotelInfoLabel);


		// Hotel Payment Info
		changeHotelPaymentInfoCheckbox = createEnablingCheckbox(group);
		GridData hotelPaymentInfoCheckBoxGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		hotelPaymentInfoCheckBoxGridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		changeHotelPaymentInfoCheckbox.setLayoutData(hotelPaymentInfoCheckBoxGridData);

		Label hotelPaymentInfoLabel = new Label(group, SWT.RIGHT);
		hotelPaymentInfoLabel.setText(I18N.HotelBooking_HotelInfo_Payment);
		GridData hotelPaymentInfoLabelGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		hotelPaymentInfoLabelGridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		hotelPaymentInfoLabel.setLayoutData(hotelPaymentInfoLabelGridData);
		hotelPaymentInfoLabel.setEnabled(false);

		hotelPaymentInfoText = new MultiLineText(group, SWT.BORDER, false);
		hotelPaymentInfoText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		hotelPaymentInfoText.setEnabled(false);
		changeHotelPaymentInfoCheckbox.setData(hotelPaymentInfoText);
		changeHotelPaymentInfoCheckbox.setData(LABEL, hotelPaymentInfoLabel);


		// Additional Guests
		changeAdditionalGuestsCheckbox = createEnablingCheckbox(group);
		GridData additionalGuestsCheckBoxGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		additionalGuestsCheckBoxGridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		changeAdditionalGuestsCheckbox.setLayoutData(additionalGuestsCheckBoxGridData);

		Label additionalGuestsLabel = new Label(group, SWT.RIGHT);
		additionalGuestsLabel.setText(HotelLabel.HotelBooking_AdditionalGuests.getString());
		GridData additionalGuestsLabelGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		additionalGuestsLabelGridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		additionalGuestsLabel.setLayoutData(additionalGuestsLabelGridData);
		additionalGuestsLabel.setEnabled(false);

		additionalGuestsText = new MultiLineText(group, SWT.BORDER, false);
		additionalGuestsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		additionalGuestsText.setEnabled(false);
		changeAdditionalGuestsCheckbox.setData(additionalGuestsText);
		changeAdditionalGuestsCheckbox.setData(LABEL, additionalGuestsLabel);
	}


	private void buildInfoForGuestGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(I18N.HotelBooking_InfoForGuest);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setLayout(new GridLayout(3, false));

		// Guest Info
		changeGuestInfoCheckBox = createEnablingCheckbox(group);
		GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		changeGuestInfoCheckBox.setLayoutData(gridData);

		// no label as long there is only this single field
//		Label infoLabel = new Label(group, SWT.RIGHT);
//		infoLabel.setText(HotelLabel.Common_GuestInfo.getString());
//		gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
//		gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
//		infoLabel.setLayoutData(gridData);
//		infoLabel.setEnabled(false);

		infoI18Ntext = new I18NText(group, SWT.MULTI, LanguageProvider.getInstance());
		infoI18Ntext.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		infoI18Ntext.setEnabled(false);
		changeGuestInfoCheckBox.setData(infoI18Ntext);
//		changeGuestInfoCheckBox.setData(LABEL, infoLabel);
	}


	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(UtilI18N.Details);
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	@Override
	protected void okPressed() {

		HotelBookingChangeFlags flags = createFlags();

		// If any change needed, put all data possibly needed to a dummy hotel booking
		if (flags.isAnyChangeSet()) {

			try {
				HotelBookingVO hotelBookingVO = new HotelBookingVO();
				hotelBookingVO.setArrival( arrivalDateComposite.getI18NDate() );

				hotelBookingVO.setDeparture( departureDateComposite.getI18NDate() );
				hotelBookingVO.setInfo( infoI18Ntext.getLanguageString() );
				hotelBookingVO.setHotelInfo( hotelInfoText.getText() );
				hotelBookingVO.setHotelPaymentInfo( hotelPaymentInfoText.getText() );
				hotelBookingVO.setAdditionalGuests( additionalGuestsText.getText() );

				if (hotelOfferingCombo != null) {
					hotelBookingVO.setOfferingPK(hotelOfferingCombo.getEntity() != null ? hotelOfferingCombo
						.getEntity()
						.getPK() : null);
				}

				if (hotelBookingPaymentConditionCombo != null) {
					hotelBookingVO.setPaymentCondition(hotelBookingPaymentConditionCombo.getEntity());
				}

				BigDecimal newDepositAmount = null;
				if (depositAmountNumberText != null) {
					newDepositAmount = depositAmountNumberText.getValue();
				}


				ArrivalInfo newArrivalInfo = null;
				if (earlyArrivalRadioButton.getSelection()) {
					newArrivalInfo = ArrivalInfo.EARLY;
				}
				else if (lateArrivalRadioButton.getSelection()) {
					newArrivalInfo = ArrivalInfo.LATE;
				}
				hotelBookingVO.setArrivalInfo(newArrivalInfo);


				hotelBookingVO.setArrivalNote( arrivalNoteText.getText() );


				SmokerType newSmokerType = null;
				if (nonSmokerRadioButton.getSelection()) {
					newSmokerType = SmokerType.NON_SMOKER;
				}
				else if (smokerRadioButton.getSelection()) {
					newSmokerType = SmokerType.SMOKER;
				}
				hotelBookingVO.setSmokerType(newSmokerType);

				hotelBookingVO.setTwinRoom(twinRoomCheckBox.getSelection());

				// Hand the information to the server, which cares for the refresh event as well

				List<HotelBookingVO> hotelBookingVOs = AbstractCVO.getVOs(bookingCVOs);
				HotelBookingModel.getInstance().changeHotelBooking(
					flags,
					hotelBookingVO,
					hotelBookingVOs,
					newDepositAmount
				);

				super.okPressed();
			}
			catch (Exception e) {
				// When exception happened, don't close the dialog
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	private HotelBookingChangeFlags createFlags() {

		HotelBookingChangeFlags flags = new HotelBookingChangeFlags();
		flags.changeArrival = changeArrivalCheckbox.getSelection();
		flags.changeArrivalInfo = changeArrivalInfoCheckbox.getSelection();
		flags.changeArrivalNote = changeArrivalNoteCheckbox.getSelection();
		flags.changeDeparture = changeDepartureCheckbox.getSelection();
		flags.changeGuestInfo = changeGuestInfoCheckBox.getSelection();
		flags.changeHotelInfo = changeHotelInfoCheckbox.getSelection();
		flags.changeHotelPaymentInfo = changeHotelPaymentInfoCheckbox.getSelection();
		flags.changeAdditionalGuests = changeAdditionalGuestsCheckbox.getSelection();
		if (changeHotelOfferingCheckbox != null) {
			flags.changeHotelOffering = changeHotelOfferingCheckbox.getSelection();
		}
		if (changePaymentConditionCheckbox != null) {
			flags.changePaymentCondition = changePaymentConditionCheckbox.getSelection();
		}
		if (changeDepositAmountCheckbox != null) {
			flags.changeDepositAmount = changeDepositAmountCheckbox.getSelection();
		}
		flags.changeSmoker = changeSmokerCheckbox.getSelection();
		flags.changeTwinRoom = changeTwinRoomCheckbox.getSelection();
		return flags;
	}


	/**
	 * Creates a label that also gets associated to a checkbox to become enabled/disabled by it.
	 */
	private void createLabel(Composite comp, I18NString ls, Button checkbox) {
		Label label = new Label(comp, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText( SWTHelper.prepareLabelText(ls.getString()) );
		label.setEnabled(false);
		checkbox.setData(LABEL, label);
	}


	/**
	 * Creates a checkbox that enables/disables a control and a label (that
	 * are associated via the data-attributes) whenever it gets selected
	 * or deselected.
	 */
	private Button createEnablingCheckbox(Composite composite) {
		final Button checkBox = new Button(composite, SWT.CHECK);
		checkBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object data = checkBox.getData();
				if (data != null && data instanceof Control) {
					Control control = (Control) data;
					control.setEnabled(checkBox.getSelection());
				}
				data = checkBox.getData(LABEL);
				if (data != null && data instanceof Label) {
					Label label = (Label) data;
					label.setEnabled(checkBox.getSelection());
				}
				updateButtonStates();
			}
		});

		return checkBox;
	}


	protected void updateButtonStates() {
		HotelBookingChangeFlags flags = createFlags();

		// If any change needed, put all data possibly needed to a dummy hotel booking
		getButton(IDialogConstants.OK_ID).setEnabled(flags.isAnyChangeSet());

	}

}
