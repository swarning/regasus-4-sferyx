package de.regasus.participant.editor.hotelbooking;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.HotelCostCoverage;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.hotel.booking.dialog.CreateHotelBookingDialog;
import de.regasus.hotel.offering.dialog.SelectHotelOfferingWizardDialog;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

public class HotelCostCoverageGroup extends EntityGroup<Participant> {

	private final int COL_COUNT = 6;

	/**
	 * There is no single widget representing the Hotel Offering ID.
	 * Therefore it is stored here.
	 */
	private Long offeringId;

	/**
	 * HotelID of the Offering.
	 */
	private Long hotelId;


	/*
	 * GridLayout with 6 column:
	 *
	 * |                    |              |                    |      |         |      |
	 * | "Number of nights" | Spinner      | "Status"           | Text |                |
	 * | "Hotel"            | Text         | "Contingent"       | Text                  |
	 * | "Offering"         | Text         | "Number of guests" | Text | "Price" | Text |
	 * |--------------------------------------------------------------------------------|
	 * | Select Offering    | Remove Offering                   |      |         |      |
	 *
	 */

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	// widgets
	private NullableSpinner numberOfNightsSpinner;
	private Text statusText;

	// widgets that represent the Hotel Offering
	private Text hotelNameText;
	private Text contingentNameText;
	private Text offeringDescText;
	private Text guestCountText;
	private Text priceText;

	// Buttons
	private Button selectOfferingButton;
	private Button removeOfferingButton;
	private Button createBookingButton;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public HotelCostCoverageGroup(Composite parent, int style)
	throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
		super(parent, style);

		setText( Participant.HOTEL_COST_COVERAGE.getString() );
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		/*
		 * Row 1
		 */

		// numberOfNights
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( HotelCostCoverage.NUMBER_OF_NIGHTS.getLabel() );
    		label.setToolTipText( HotelCostCoverage.NUMBER_OF_NIGHTS.getDescription() );

    		numberOfNightsSpinner = new NullableSpinner(this, SWT.BORDER);
    		GridDataFactory.swtDefaults()
    			.align(SWT.LEFT,  SWT.CENTER)
    			.applyTo(numberOfNightsSpinner);
    		numberOfNightsSpinner.setMinimum( HotelCostCoverage.NUMBER_OF_NIGHTS.getMin().intValue() );
    		numberOfNightsSpinner.addModifyListener(modifySupport);
		}

		// statusText
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(UtilI18N.Status);

			statusText = new Text(this, SWT.BORDER);
			SWTHelper.disableTextWidget(statusText);
    		GridDataFactory.swtDefaults()
    			.align(SWT.FILL,  SWT.CENTER)
    			.span(COL_COUNT - 3, 1)
    			.applyTo(statusText);
		}

		/*
		 * Row 2
		 */

		// hotelName
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( HotelLabel.Hotel.getString() );


    		hotelNameText = new Text(this, SWT.BORDER);
    		SWTHelper.disableTextWidget(hotelNameText);
    		GridDataFactory.swtDefaults()
    			.align(SWT.FILL,  SWT.CENTER)
    			.grab(true, false)
    			.applyTo(hotelNameText);
		}

		modifySupport.addListener( new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateButtonsEnabled();
			}
		});

		// contingentName
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( HotelLabel.Contingent.getString() );

			contingentNameText = new Text(this, SWT.BORDER);
			SWTHelper.disableTextWidget(contingentNameText);
			GridDataFactory.swtDefaults()
    			.align(SWT.FILL,  SWT.CENTER)
    			.span(COL_COUNT - 3, 0)
    			.grab(true, false)
    			.applyTo(contingentNameText);
		}


		/*
		 * Row 3
		 */

		// offeringDesc
		{
    		Label label = new Label(this, SWT.NONE);
    		labelGridDataFactory.applyTo(label);
    		label.setText( HotelLabel.HotelOffering.getString() );

    		offeringDescText = new Text(this, SWT.BORDER);
    		SWTHelper.disableTextWidget(offeringDescText);
    		GridDataFactory.swtDefaults()
    			.align(SWT.FILL,  SWT.CENTER)
    			.grab(true, false)
    			.applyTo(offeringDescText);
		}

		// guestCount
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( HotelLabel.HotelBooking_GuestCount.getString() );

			guestCountText = new Text(this, SWT.BORDER);
			SWTHelper.disableTextWidget(guestCountText);
			GridDataFactory.swtDefaults()
    			.align(SWT.LEFT,  SWT.CENTER)
    			.applyTo(guestCountText);
		}

		// price
		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( InvoiceLabel.TotalPrice.getString() );

			priceText = new Text(this, SWT.BORDER);
			SWTHelper.disableTextWidget(priceText);
			GridDataFactory.swtDefaults()
    			.align(SWT.LEFT,  SWT.CENTER)
    			.applyTo(priceText);
		}


		/*
		 * Row 4
		 */

		SWTHelper.verticalSpace(this);


		/*
		 * Row 5
		 */

		Composite buttonComposite = new Composite(this, SWT.NONE);
		GridDataFactory.swtDefaults()
    		.align(SWT.FILL,  SWT.CENTER)
    		.span(COL_COUNT, 0)
    		.applyTo(buttonComposite);

		buttonComposite.setLayout( new GridLayout(3, true) );

		GridDataFactory buttonGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);

		// selectOfferingButton
		{
			selectOfferingButton = new Button(buttonComposite, SWT.PUSH);
			selectOfferingButton.setText(I18N.HotelCostCoverage_SelectOffering);
			buttonGridDataFactory.applyTo(selectOfferingButton);

			selectOfferingButton.addListener(SWT.Selection, e -> selectOffering());
		}

		// removeOfferingButton
		{
			removeOfferingButton = new Button(buttonComposite, SWT.PUSH);
			removeOfferingButton.setText(I18N.HotelCostCoverage_RemoveOffering);
			buttonGridDataFactory.applyTo(removeOfferingButton);

			removeOfferingButton.addListener(SWT.Selection, e -> setOfferingId(null));
		}

		// createBookingButton
		{
			createBookingButton = new Button(buttonComposite, SWT.PUSH);
			createBookingButton.setText(I18N.CreateBookings);
			buttonGridDataFactory.applyTo(createBookingButton);

			createBookingButton.addListener(SWT.Selection, e -> createBooking());
		}
	}


	private void selectOffering() {
		Shell shell = Display.getCurrent().getActiveShell();
		Long eventId = entity.getEventId();
		SelectHotelOfferingWizardDialog wizardDialog = SelectHotelOfferingWizardDialog.create(
			shell,
			eventId,
			hotelId,
			offeringId,
			I18N.HotelCostCoverage_SelectHotelDescription,
			I18N.HotelCostCoverage_SelectOfferingDescription
		);

		int returnCode = wizardDialog.open();
		if (returnCode == Window.OK) {
			setOfferingId( wizardDialog.getOfferingId() );
		}
	}


	private void setOfferingId(Long offeringId) {
		if ( !EqualsHelper.isEqual(this.offeringId, offeringId)) {
    		this.offeringId = offeringId;
    		this.hotelId = null;


    		// fill widgets that show the Hotel Offering

    		String hotelName = "";
    		String contingentName = "";
    		String offeringDesc = "";
    		String guestCount = "";
    		String price = "";

    		if (offeringId != null) {
    			try {
    				// get entities from models
    				HotelOfferingVO offeringVO = HotelOfferingModel.getInstance().getHotelOfferingVO(offeringId);
    				Long contingentId = offeringVO.getHotelContingentPK();
    				HotelContingentCVO contingentCVO = HotelContingentModel.getInstance().getHotelContingentCVO(contingentId);
    				hotelId = contingentCVO.getHotelPK();
    				Hotel hotel = HotelModel.getInstance().getHotel(hotelId);

    				// set String values
    				hotelName = hotel.getName1();
    				contingentName = contingentCVO.getHcName();

    				LanguageString offDescriptionLangStr = offeringVO.getDescription();
    				if (offDescriptionLangStr != null) {
    					offeringDesc = offDescriptionLangStr.getString();
    				}

    				guestCount = String.valueOf( offeringVO.getBedCount() );
    				price = offeringVO.getCurrencyAmountGross().format();
    			}
    			catch (Exception e) {
    				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    				offeringDesc = offeringDesc.toString();
    			}
    		}

    		hotelNameText.setText(hotelName);
    		contingentNameText.setText(contingentName);
    		offeringDescText.setText(offeringDesc);
    		guestCountText.setText(guestCount);
    		priceText.setText(price);

    		modifySupport.fire();
		}
	}


	private void createBooking() {
		 try {
			boolean editorSaveCkeckOK = ParticipantEditor.saveEditor( entity.getID() );
			if (editorSaveCkeckOK) {
				ParticipantSearchData psd = new ParticipantSearchData(entity);
				List<IParticipant> participantList = Collections.singletonList(psd);

				HotelCostCoverage hotelCostCoverage = entity.getHotelCostCoverage();
				CreateHotelBookingDialog.create(getShell(), participantList, hotelCostCoverage).open();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		HotelCostCoverage hotelCostCoverage = entity.getHotelCostCoverage();
		Long offeringId = hotelCostCoverage.getOfferingId();

		numberOfNightsSpinner.setValue( hotelCostCoverage.getNumberOfNights() );

		if ( !hotelCostCoverage.isDefined() ) {
			statusText.setText("");
		}
		else if ( hotelCostCoverage.isUsed() ) {
			statusText.setText(I18N.HotelCostCoverage_StatusText_Used);
		}
		else {
			statusText.setText(I18N.HotelCostCoverage_StatusText_NotUsed);
		}

		setOfferingId(offeringId);

		updateButtonsEnabled();
	}


	private void updateButtonsEnabled() {
		/* Enabled of Buttons depends on HotelCostCoverage.isUsed() which changes only when creating or cancelling
		 * a Hotel Booking. Both operations lead to a call of this method.
		 */
		boolean used = entity.getHotelCostCoverage().isUsed();
		numberOfNightsSpinner.setEnabled(!used);
		selectOfferingButton.setEnabled(!used);
		removeOfferingButton.setEnabled(!used && offeringId != null);

		/* The createBookingButton is only visible if the persisted values of numberOfNights and offeringId are not null
		 * and also the current values in the widgets. This is necessary, because the editor is saved before the
		 * booking dialog is opened what will persist the values in the widgets.
		 */
		createBookingButton.setEnabled(
			   !used
			&& numberOfNightsSpinner.getValue() != null
			&& offeringId != null
		);
	}


	@Override
	public void syncEntityToWidgets() {
		entity.getHotelCostCoverage().setNumberOfNights( numberOfNightsSpinner.getValueAsInteger() );
		entity.getHotelCostCoverage().setOfferingId(offeringId);
	}

}
