package de.regasus.hotel.booking.dialog;

import static com.lambdalogic.util.StringHelper.avoidNull;
import static com.lambdalogic.util.rcp.widget.SWTHelper.prepareLabelText;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.ArrivalInfo;
import com.lambdalogic.messeinfo.hotel.data.SmokerType;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class HotelBookingInfoForHotelGroup extends Group {

	/**
	 * Modify support
	 */
	private ModifySupport modifySupport = new ModifySupport(this);


	private boolean showEditTime;


	/*** Widgets ***/

	private Button normalArrivalRadioButton;
	private Button earlyArrivalRadioButton;
	private Button lateArrivalRadioButton;

	private Text arrivalNoteText;

	private Button twinRoomCheckBox;

	private Button unknkownSmokerRadioButton;
	private Button nonSmokerRadioButton;
	private Button smokerRadioButton;

	private MultiLineText hotelInfoText;
	private MultiLineText hotelPaymentInfoText;
	private MultiLineText additionalGuestsText;

	private Label editTimeLabel;


	public HotelBookingInfoForHotelGroup(Composite parent, int style, boolean showEditTime) {
		super(parent, style);

		this.showEditTime = showEditTime;

		createPartControl();
	}


	/**
	 * Create widgets.
	 */
	protected void createPartControl() {
		try {
			setText(I18N.HotelBooking_InfoForHotel);

			setLayoutData(new GridData(GridData.FILL_BOTH));
			setLayout(new GridLayout(2, false));


			// ArrivalInfo
			createLabel(this, HotelLabel.HotelBooking_ArrivalInfo, null);

			Composite arrivalInfoButtons = new Composite(this, SWT.NONE);
			arrivalInfoButtons.setLayout(new GridLayout(3, false));

			normalArrivalRadioButton = new Button(arrivalInfoButtons, SWT.RADIO);
			normalArrivalRadioButton.setText(HotelLabel.HotelBooking_ArrivalInfo_Normal.getString());
			normalArrivalRadioButton.addSelectionListener(modifySupport);
			// default
			normalArrivalRadioButton.setSelection(true);

			earlyArrivalRadioButton = new Button(arrivalInfoButtons, SWT.RADIO);
			earlyArrivalRadioButton.setText(HotelLabel.HotelBooking_ArrivalInfo_Early.getString());
			earlyArrivalRadioButton.addSelectionListener(modifySupport);

			lateArrivalRadioButton = new Button(arrivalInfoButtons, SWT.RADIO);
			lateArrivalRadioButton.setText(HotelLabel.HotelBooking_ArrivalInfo_Late.getString());
			lateArrivalRadioButton.addSelectionListener(modifySupport);


			// ArrivalNote
			createLabel(this, HotelLabel.HotelBooking_ArrivalNote, HotelLabel.HotelBooking_ArrivalNote_Desc);

			arrivalNoteText = new Text(this, SWT.NONE);
			arrivalNoteText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			arrivalNoteText.addModifyListener(modifySupport);


			// Twin Room
			createLabel(this, HotelLabel.HotelBooking_TwinRoom, null);

			twinRoomCheckBox = new Button(this, SWT.CHECK);
			twinRoomCheckBox.addSelectionListener(modifySupport);


			// Smoker Info
			createLabel(this, HotelLabel.SmokerInfo, null);

			Composite smokerInfoButtons = new Composite(this, SWT.NONE);
			smokerInfoButtons.setLayout(new GridLayout(3, false));

			unknkownSmokerRadioButton = new Button(smokerInfoButtons, SWT.RADIO);
			unknkownSmokerRadioButton.setText(HotelLabel.SmokerTypeEmpty.getString());
			unknkownSmokerRadioButton.addSelectionListener(modifySupport);
			// default
			unknkownSmokerRadioButton.setSelection(true);

			nonSmokerRadioButton = new Button(smokerInfoButtons, SWT.RADIO);
			nonSmokerRadioButton.setText(SmokerType.NON_SMOKER.getString());
			nonSmokerRadioButton.addSelectionListener(modifySupport);

			smokerRadioButton = new Button(smokerInfoButtons, SWT.RADIO);
			smokerRadioButton.setText(SmokerType.SMOKER.getString());
			smokerRadioButton.addSelectionListener(modifySupport);


			// hotel info
			Label hotelInfoLabel = new Label(this, SWT.RIGHT);
			hotelInfoLabel.setText(I18N.HotelBooking_HotelInfo_General);
			GridData hotelInfoLabelGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			hotelInfoLabelGridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			hotelInfoLabel.setLayoutData(hotelInfoLabelGridData);

			hotelInfoText = new MultiLineText(this,  SWT.BORDER, false);
			hotelInfoText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			hotelInfoText.addModifyListener(modifySupport);


			// hotel payment info
			Label hotelPaymentInfoLabel = new Label(this, SWT.RIGHT);
			hotelPaymentInfoLabel.setText(I18N.HotelBooking_HotelInfo_Payment);
			GridData hotelPaymentInfoLabelGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			hotelPaymentInfoLabelGridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			hotelPaymentInfoLabel.setLayoutData(hotelPaymentInfoLabelGridData);

			hotelPaymentInfoText = new MultiLineText(this,  SWT.BORDER, false);
			hotelPaymentInfoText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			hotelPaymentInfoText.addModifyListener(modifySupport);


			// additional guests
			Label additionalGuestsLabel = new Label(this, SWT.RIGHT);
			additionalGuestsLabel.setText(HotelLabel.HotelBooking_AdditionalGuests.getString());
			GridData additionalGuestsLabelGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			additionalGuestsLabelGridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
			additionalGuestsLabel.setLayoutData(additionalGuestsLabelGridData);

			additionalGuestsText = new MultiLineText(this,  SWT.BORDER, false);
			additionalGuestsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			additionalGuestsText.addModifyListener(modifySupport);


			if (showEditTime) {
				Label label = new Label(this, SWT.RIGHT);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText(UtilI18N.EditDateTime + ":");

				editTimeLabel = new Label(this, SWT.NONE);
				editTimeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private Label createLabel(Composite comp, I18NString labelText, I18NString tooltipText) {
		Label label = new Label(comp, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText( prepareLabelText( labelText.getString() ) );
		if (tooltipText != null) {
			label.setToolTipText( prepareLabelText( tooltipText.getString() ) );
		}
		return label;
	}


	public ArrivalInfo getArrivalInfo() {
		ArrivalInfo arrivalInfo = null;

		if (earlyArrivalRadioButton.getSelection()) {
			arrivalInfo = ArrivalInfo.EARLY;
		}
		else if (lateArrivalRadioButton.getSelection()) {
			arrivalInfo = ArrivalInfo.LATE;
		}

		return arrivalInfo;
	}


	public void setArrivalInfo(ArrivalInfo arrivalInfo) {
		normalArrivalRadioButton.setSelection(arrivalInfo == null);
		earlyArrivalRadioButton.setSelection(arrivalInfo == ArrivalInfo.EARLY);
		lateArrivalRadioButton.setSelection(arrivalInfo == ArrivalInfo.LATE);
	}


	public String getArrivalNote() {
		return arrivalNoteText.getText();
	}


	public void setArrivalNote(String arrivalNote) {
		arrivalNoteText.setText( avoidNull(arrivalNote) );
	}


	public boolean isTwinRoom() {
		return twinRoomCheckBox.getSelection();
	}


	public void setTwinRoom(boolean twinRoom) {
		twinRoomCheckBox.setSelection(twinRoom);
	}


	public void setTwinRoomEnabled(boolean enabled) {
		twinRoomCheckBox.setEnabled(enabled);
	}


	public SmokerType getSmokerType() {
		SmokerType smokerType = null;

		if (smokerRadioButton.getSelection()) {
			smokerType = SmokerType.SMOKER;
		}
		else if (nonSmokerRadioButton.getSelection()) {
			smokerType = SmokerType.NON_SMOKER;
		}

		return smokerType;
	}


	public void setSmokerType(SmokerType smokerType) {
		unknkownSmokerRadioButton.setSelection(smokerType == null);
		smokerRadioButton.setSelection(smokerType == SmokerType.SMOKER);
		nonSmokerRadioButton.setSelection(smokerType == SmokerType.NON_SMOKER);
	}


	public String getHotelInfo() {
		return hotelInfoText.getText();
	}


	public void setHotelInfo(String hotelInfo) {
		hotelInfoText.setText( avoidNull(hotelInfo) );
	}


	public String getHotelPaymentInfo() {
		return hotelPaymentInfoText.getText();
	}


	public void setHotelPaymentInfo(String hotelPaymentInfo) {
		hotelPaymentInfoText.setText( avoidNull(hotelPaymentInfo) );
	}


	public String getAdditionalGuests() {
		return additionalGuestsText.getText();
	}


	public void setAdditionalGuests(String additionalGuests) {
		additionalGuestsText.setText( avoidNull(additionalGuests) );
	}


	public void setEditTime(Date editTime) {
		if (editTimeLabel != null) {
			String s = FormatHelper.getDefaultLocaleInstance().formatDateTime(editTime);
			editTimeLabel.setText(s);
		}
	}


	// **************************************************************************
	// * Modify support
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modify support
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
