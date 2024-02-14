package de.regasus.participant.editor.hotelbooking;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.math.BigDecimal;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.ArrivalInfo;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingPaymentCondition;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.SmokerType;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.IconRegistry;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.hotel.booking.combo.HotelBookingPaymentConditionCombo;
import de.regasus.hotel.booking.dialog.HotelBookingInfoForGuestGroup;
import de.regasus.hotel.booking.dialog.HotelBookingInfoForHotelGroup;
import de.regasus.hotel.offering.combo.HotelOfferingCombo;
import de.regasus.ui.Activator;


public class HotelBookingDetailsDialog extends TitleAreaDialog implements ModifyListener {

	private boolean dirty = false;

	private HotelBookingCVO hotelBookingCVO;

	// Widgets

//	private I18NText infoI18Ntext;
	private DateComposite arrivalDate;
	private DateComposite departureDate;
	private HotelOfferingCombo hotelOfferingCombo;
	private HotelBookingPaymentConditionCombo hotelBookingPaymentConditionCombo;
	private DecimalNumberText depositAmountNumberText;
	private HotelBookingInfoForHotelGroup infoForHotelGroup;
	private HotelBookingInfoForGuestGroup infoForGuestGroup;


	public HotelBookingDetailsDialog(Shell parentShell, HotelBookingCVO hotelBookingCVO) {
		super(parentShell);
		setShellStyle(getShellStyle()  | SWT.RESIZE );
		this.hotelBookingCVO = hotelBookingCVO;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(UtilI18N.Details);
		setMessage( hotelBookingCVO.getLabelForHotelContingent() );

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		try {
			Composite mainComposite = new Composite(dialogArea, SWT.NONE);
			mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

			final int NUM_COLS = 2;
			mainComposite.setLayout(new GridLayout(NUM_COLS, true));

			FormatHelper fh = FormatHelper.getDefaultLocaleInstance();
			HotelBookingVO bookingVO = hotelBookingCVO.getBookingVO();


			// Info Button
			Button infoButton = buildInfoButton(mainComposite);
			GridDataFactory.swtDefaults()
				.align(SWT.RIGHT, SWT.CENTER)
				.span(NUM_COLS, 1)
				.applyTo(infoButton);


			// *************************************************************************
			// * Left column - general booking details
			// *

			Group leftComposite = new Group(mainComposite, SWT.NONE);
			leftComposite.setText(I18N.BookingDetails);


			GridData leftGridData = new GridData(GridData.FILL_BOTH);
			leftGridData.verticalSpan = 2;
			leftComposite.setLayoutData(leftGridData);
			leftComposite.setLayout(new GridLayout(4, false));

			createEntry(leftComposite, I18N.BookedAt, fh.formatDateTime(bookingVO.getBookingDate()));
			createEntry(leftComposite, I18N.CanceledAt, fh.formatDateTime(bookingVO.getCancelationDate()));
			createEntry(leftComposite, UtilI18N.CreateDateTime, fh.formatDateTime(bookingVO.getNewTime()));
			createEntry(leftComposite, UtilI18N.CreateUser, bookingVO.getNewDisplayUserStr());
			createEntry(leftComposite, UtilI18N.EditDateTime, fh.formatDateTime(bookingVO.getEditTime()));
			createEntry(leftComposite, UtilI18N.EditUser, bookingVO.getEditDisplayUserStr());


			// horizontal line
			Label separatorLabel = new Label(leftComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
			separatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));


			// arrival
			createLabel(leftComposite, HotelLabel.HotelBooking_Arrival);

			arrivalDate = new DateComposite(leftComposite, SWT.BORDER);
			arrivalDate.setLocalDate( TypeHelper.toLocalDate(hotelBookingCVO.getArrival()) );
			arrivalDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			arrivalDate.addModifyListener(this);

			Label arrivalDateEditTimeLabel = new Label(leftComposite, SWT.RIGHT);
			arrivalDateEditTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			arrivalDateEditTimeLabel.setText(UtilI18N.EditDateTime + ":");

			Label arrivalDateEditTimeValue = new Label(leftComposite, SWT.NONE);
			arrivalDateEditTimeValue.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
			arrivalDateEditTimeValue.setText(fh.formatDateTime(bookingVO.getArrivalEditTime()));


			// departure
			createLabel(leftComposite, HotelLabel.HotelBooking_Departure);

			departureDate = new DateComposite(leftComposite, SWT.BORDER);
			departureDate.setLocalDate( TypeHelper.toLocalDate(hotelBookingCVO.getDeparture()) );
			departureDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			departureDate.addModifyListener(this);


			Label departureDateEditTimeLabel = new Label(leftComposite, SWT.RIGHT);
			departureDateEditTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			departureDateEditTimeLabel.setText(UtilI18N.EditDateTime + ":");

			Label departureDateEditTimeValue = new Label(leftComposite, SWT.NONE);
			departureDateEditTimeValue.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
			departureDateEditTimeValue.setText(fh.formatDateTime(bookingVO.getDepartureEditTime()));


			// Selection of hotelOffering
			createLabel(leftComposite, HotelLabel.HotelOffering);

			Long hotelContingentPK = hotelBookingCVO.getHotelOfferingCVO().getHotelContingent().getPK();
			hotelOfferingCombo = new HotelOfferingCombo(leftComposite, SWT.NONE, hotelContingentPK);
			hotelOfferingCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			hotelOfferingCombo.setHotelOfferingPK(hotelBookingCVO.getHotelOfferingCVO().getPK());
			hotelOfferingCombo.addModifyListener(this);


			// Hotel Booking Payment Condition
			createLabel(leftComposite, HotelLabel.HotelBookingPaymentCondition);
			{
				/* HotelOfferingVO is used to limit the values in the HotelBookingPaymentConditionCombo to the values
				 * that are allowed in the hotel offering. Because these values could habe changed
				 * since the hotel booking was loaded, take the most actual version from the
				 * HotelOfferingModel.
				 */
				HotelOfferingVO hotelOfferingVO = hotelBookingCVO.getHotelOfferingCVO().getHotelOfferingVO();
				hotelOfferingVO = HotelOfferingModel.getInstance().getHotelOfferingVO(hotelOfferingVO.getID());

    			hotelBookingPaymentConditionCombo = new HotelBookingPaymentConditionCombo(leftComposite, SWT.READ_ONLY, hotelOfferingVO);
    			hotelBookingPaymentConditionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
    			hotelBookingPaymentConditionCombo.setEntity(hotelBookingCVO.getVO().getPaymentCondition());
    			hotelBookingPaymentConditionCombo.addModifyListener(this);
			}


			// deposit amount
			createLabel(leftComposite, HotelLabel.HotelBooking_Deposit);
			{
				depositAmountNumberText = new DecimalNumberText(leftComposite, SWT.BORDER);
				depositAmountNumberText.setFractionDigits(2);
				depositAmountNumberText.setNullAllowed(HotelOfferingVO.NULL_ALLOWED_DEPOSIT);
				depositAmountNumberText.setShowPercent(false);
				depositAmountNumberText.setMinValue(BigDecimal.ZERO);
				GridData depositAmountGridData = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1);
				depositAmountNumberText.setLayoutData(depositAmountGridData);
				depositAmountNumberText.addModifyListener(this);
				if (hotelBookingCVO.getVO().getPaymentCondition() == HotelBookingPaymentCondition.DEPOSIT) {
					depositAmountNumberText.setValue(hotelBookingCVO.getVO().getLodgePriceVO().getAmount());
				}
			}


			// open amount
			BigDecimal openAmount = hotelBookingCVO.getOpenAmount();
			if (openAmount == null) {
				openAmount = BigDecimal.ZERO;
			}
			String currency = hotelBookingCVO.getCurrency();
			String openAmountString = new CurrencyAmount(openAmount, currency).format(false, false);

			createEntry(leftComposite, ParticipantLabel.OpenAmount.getString(), openAmountString);

			// *
			// * Left column - general booking details
			// *************************************************************************

			// *************************************************************************
			// * Top-Right area
			// *

			infoForHotelGroup = new HotelBookingInfoForHotelGroup(mainComposite, SWT.NONE, true/*showEditTime*/);
			infoForHotelGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

			infoForHotelGroup.setArrivalInfo( bookingVO.getArrivalInfo() );
			infoForHotelGroup.setArrivalNote( bookingVO.getArrivalNote() );
			infoForHotelGroup.setTwinRoom( bookingVO.isTwinRoom() );
			infoForHotelGroup.setSmokerType( bookingVO.getSmokerType() );
			infoForHotelGroup.setHotelInfo( bookingVO.getHotelInfo() );
			infoForHotelGroup.setHotelPaymentInfo( bookingVO.getHotelPaymentInfo() );
			infoForHotelGroup.setAdditionalGuests( bookingVO.getAdditionalGuests() );
			infoForHotelGroup.setEditTime( bookingVO.getHotelInfosEditTime() );

			infoForHotelGroup.addModifyListener(this);

			// *
			// * Top-Right area
			// *************************************************************************

			// *************************************************************************
			// * Bottom-Right area
			// *

			infoForGuestGroup = new HotelBookingInfoForGuestGroup(mainComposite, SWT.NONE, true/*showEditTime*/, bookingVO.getEventPK());
			infoForGuestGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

			infoForGuestGroup.setGuestInfo(bookingVO.getInfo());
			infoForGuestGroup.setEditTime(bookingVO.getInfoEditTime());

			infoForGuestGroup.addModifyListener(this);

			// *
			// * Bottom-Right area
			// *************************************************************************
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return dialogArea;
	}


	private Button buildInfoButton(Composite parent) {
		Button infoButton = new Button(parent, SWT.NONE);
		infoButton.setToolTipText(CoreI18N.InfoButtonToolTip);
		infoButton.setImage(IconRegistry.getImage(
			de.regasus.core.ui.IImageKeys.INFORMATION
		));

		infoButton.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openInfoDialog();
			}
		});

		return infoButton;
	}


	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf( hotelBookingCVO.getId() )
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getShell(),
			I18N.HotelBooking + ": " + UtilI18N.Info,
			labels,
			values
		);

		infoDialog.setSize(new Point(300, 120));

		infoDialog.open();
	}



	/**
	 * The Window Icon and Title are made identical to the icon and the tooltip of the button that opens this dialog.
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(UtilI18N.Details);
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	@Override
	protected void okPressed() {
		if (dirty) {
			// something has changed, need to update
			Long newHotelOfferingPK = hotelOfferingCombo.getEntity().getPK();
			HotelBookingPaymentCondition newPaymentCondition = hotelBookingPaymentConditionCombo.getEntity();
			BigDecimal newDepositAmount = depositAmountNumberText.getValue();
			I18NDate newArrival = arrivalDate.getI18NDate();
			I18NDate newDeparture = departureDate.getI18NDate();

			ArrivalInfo newArrivalInfo = infoForHotelGroup.getArrivalInfo();
			String newArrivalNote = infoForHotelGroup.getArrivalNote();
			boolean newTwinRoom = infoForHotelGroup.isTwinRoom();
			SmokerType newSmokerType = infoForHotelGroup.getSmokerType();
			String newHotelInfo = infoForHotelGroup.getHotelInfo();
			String newHotelPaymentInfo = infoForHotelGroup.getHotelPaymentInfo();
			String newAdditionalGuests = infoForHotelGroup.getAdditionalGuests();

			LanguageString newGuestInfo = infoForGuestGroup.getGuestInfo();


			try {
				HotelBookingModel.getInstance().changeHotelBooking(
					hotelBookingCVO.getVO(),
					newHotelOfferingPK,
					newPaymentCondition,
					newDepositAmount,
					newArrival,
					newDeparture,
					newHotelInfo,
					newHotelPaymentInfo,
					newAdditionalGuests,
					newArrivalInfo,
					newArrivalNote,
					newSmokerType,
					newTwinRoom,
					newGuestInfo
				);

				super.okPressed();
			}
			catch (Exception e) {
				// When exception happened, don't close the dialog
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		else {
			super.okPressed();
		}
	}


	private void createEntry(Composite composite, String leftText, String rightText) {
		Label label = new Label(composite, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText(leftText + ":");

		Label valueText = new Label(composite, SWT.NONE);
		valueText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		valueText.setText(avoidNull(rightText));
	}


	public boolean isDirty() {
		return dirty;
	}


	@Override
	public void modifyText(ModifyEvent e) {
		dirty = true;
	}


	private void createLabel(Composite comp, I18NString ls) {
		Label label = new Label(comp, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText( SWTHelper.prepareLabelText(ls.getString()) );
	}

}
