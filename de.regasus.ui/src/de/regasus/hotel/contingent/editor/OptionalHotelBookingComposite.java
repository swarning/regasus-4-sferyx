package de.regasus.hotel.contingent.editor;

import static com.lambdalogic.util.DateHelper.addDays;
import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.OptionalHotelBookingVO;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class OptionalHotelBookingComposite extends Group {

	/**
	 * The entity
	 */
	private OptionalHotelBookingVO optionalHotelBookingVO;

	/**
	 * Modify support
	 */
	private ModifySupport modifySupport = new ModifySupport(this);


	private HotelContingentEditorCapacityComposite hotelContingentEditorCapacityComposite;


	/*** widgets ***/

	private Text nameText;
	private NullableSpinner countSpinner;
	private Text noteText;
	private DateComposite arrivalDateComposite;
	private DateComposite departureDateComposite;
	private DateComposite expirationDateComposite;
	private Button expirationEnableCheckButton;
	private Label currentlyBlockedValueLabel;
	private Button removeButton;


	private ModifyListener hotelContingentEditorCapacityCompositeModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			validateArrivalDeparture();
		}
	};


	/**
	 * @param parent
	 * @param style
	 * @param hotelContingentEditorCapacityComposite
	 *  related HotelContingentEditorCapacityComposite
	 */
	public OptionalHotelBookingComposite(
		Composite parent,
		int style,
		final HotelContingentEditorCapacityComposite hotelContingentEditorCapacityComposite
	) {
		super(parent, style);

		// set related HotelContingentEditorCapacityComposite
		if (hotelContingentEditorCapacityComposite == null) {
			throw new IllegalArgumentException("Parameter 'hotelContingentEditorCapacityComposite' must not be null.");
		}
		this.hotelContingentEditorCapacityComposite = hotelContingentEditorCapacityComposite;


		// start observing related HotelContingentEditorCapacityComposite
		hotelContingentEditorCapacityComposite.addModifyListener(hotelContingentEditorCapacityCompositeModifyListener);

		// stop observing related HotelContingentEditorCapacityComposite on dispose
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (hotelContingentEditorCapacityComposite != null) {
					hotelContingentEditorCapacityComposite.removeModifyListener(hotelContingentEditorCapacityCompositeModifyListener);
				}
			}
		});


		/*** create widgets ***/
		try {
			createPartControl();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Create widgets.
	 * @throws Exception
	 */
	protected void createPartControl() throws Exception {
		final int COLUMNS = 8;
		setLayout(new GridLayout(COLUMNS, false));


		/* Row 1
		 */

		// Name
		{
			Label label = new Label(this, SWT.RIGHT);
			GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			label.setLayoutData(layoutData);
			label.setText(UtilI18N.Name);
		}
		{
			nameText = new Text(this, SWT.BORDER);
    		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMNS - 3, 1);
    		nameText.setLayoutData(layoutData);

    		nameText.addModifyListener(modifySupport);
		}

		// Count
		{
			Label label = new Label(this, SWT.RIGHT);
			GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			label.setLayoutData(layoutData);
			label.setText(HotelLabel.OptionalHotelBooking_Count.getString());
		}
		{
			countSpinner = new NullableSpinner(this, SWT.NONE);
			countSpinner.setMinimum(OptionalHotelBookingVO.MIN_COUNT);
			countSpinner.setMaximum(OptionalHotelBookingVO.MAX_COUNT);
			countSpinner.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			WidgetSizer.setWidth(countSpinner);
			countSpinner.setValue(0);

			countSpinner.addModifyListener(modifySupport);
		}


		/* Row 2
		 */

		{
			Label label = new Label(this, SWT.RIGHT);
			GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			label.setLayoutData(layoutData);
			label.setText(UtilI18N.Note);
		}

		{
			noteText = new Text(this, SWT.BORDER);
    		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMNS - 1, 1);
    		noteText.setLayoutData(layoutData);

    		noteText.addModifyListener(modifySupport);
		}


		/* Row 3
		 */

		// arrival
		{
			Label label = new Label(this, SWT.RIGHT);
			GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			label.setLayoutData(layoutData);
			label.setText(HotelLabel.OptionalHotelBooking_Arrival.getString());
		}

		{
			arrivalDateComposite = new DateComposite(this, SWT.BORDER);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			arrivalDateComposite.setLayoutData(layoutData);

			arrivalDateComposite.addModifyListener(modifySupport);
			arrivalDateComposite.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent event) {
					validateArrivalDeparture();
				}
			});
		}


		// departure
		{
			Label label = new Label(this, SWT.RIGHT);
			GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			label.setLayoutData(layoutData);
			label.setText(HotelLabel.OptionalHotelBooking_Departure.getString());
		}

		{
			departureDateComposite = new DateComposite(this, SWT.BORDER);
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMNS - 3, 1);
			departureDateComposite.setLayoutData(layoutData);

			departureDateComposite.addModifyListener(modifySupport);
			departureDateComposite.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent event) {
					validateArrivalDeparture();
				}
			});
		}


		/* Row 4
		 */

		// Expiration Time
		{
			Label label = new Label(this, SWT.NONE);
			GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			label.setLayoutData(layoutData);
    		label.setText(HotelLabel.OptionalHotelBooking_Expiration.getString());
    		label.setToolTipText(HotelLabel.OptionalHotelBooking_Expiration_tooltip.getString());
		}

    	{
    		expirationDateComposite = new DateComposite(this, SWT.BORDER);
    		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    		expirationDateComposite.setLayoutData(layoutData);

    		expirationDateComposite.addModifyListener(modifySupport);
    	}


		new Label(this, SWT.NONE);

		// Enable Expiration
		expirationEnableCheckButton = new Button(this, SWT.CHECK);
		expirationEnableCheckButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		expirationEnableCheckButton.setText(HotelLabel.OptionalHotelBooking_Enable_Expiration.getString());

		expirationEnableCheckButton.addSelectionListener(modifySupport);


		// Currently blocked
		Label currentlyBlockedLabel = new Label(this, SWT.NONE);
		currentlyBlockedLabel.setText(I18N.OptionalHotelBookingComposite_CurrentlyBlocked + ":");
		{
    		GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
    		layoutData.horizontalIndent = 20;
			currentlyBlockedLabel.setLayoutData(layoutData);
		}

		currentlyBlockedValueLabel = new Label(this, SWT.NONE);
		currentlyBlockedValueLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMNS - 5, 1));


		// Button Row
		{
			Label label1to5 = new Label(this, SWT.NONE);
    		label1to5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 5, 1));

    		// use label6 to expand column 6 (layout trick), otherwise column 8 would get the space
    		Label label6 = new Label(this, SWT.NONE);
    		label6.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

    		removeButton = new Button(this, SWT.PUSH);
    		removeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
    		removeButton.setText(UtilI18N.Remove);
    		/* There is no SelectionListener for removeButton here.
    		 * OptionalHotelBookingManagementComposite is observing to removeButton directly,
    		 * see addRemoveListener(SelectionListener).
    		 * When removebutton is selected, OptionalHotelBookingManagementComposite will destroy this
    		 * OptionalHotelBookingComposite.
    		 */
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


	/**
	 * Add {@link SelectionListener} that will be notified when an item is removed.
	 * @param selectionListener
	 */
	public void addRemoveListener(SelectionListener selectionListener) {
		removeButton.addSelectionListener(selectionListener);
	}


	private void syncWidgetsToEntity() {
		if (optionalHotelBookingVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						/*** copy values from entity to widgets ***/

						nameText.setText( avoidNull(optionalHotelBookingVO.getName()) );
						countSpinner.setValue( optionalHotelBookingVO.getCount() );
						noteText.setText( avoidNull(optionalHotelBookingVO.getNote()) );
						arrivalDateComposite.setLocalDate( TypeHelper.toLocalDate(optionalHotelBookingVO.getArrival()) );
						departureDateComposite.setLocalDate( TypeHelper.toLocalDate(optionalHotelBookingVO.getDeparture()) );
						expirationDateComposite.setLocalDate( TypeHelper.toLocalDate(optionalHotelBookingVO.getExpiration()) );
						expirationEnableCheckButton.setSelection( optionalHotelBookingVO.isExpirationEnabled() );

						if (optionalHotelBookingVO.isBlocking()) {
							currentlyBlockedValueLabel.setText(UtilI18N.Yes);
						}
						else {
							currentlyBlockedValueLabel.setText(UtilI18N.No);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	/**
	 * Copy values from widgets to entity.
	 */
	public void syncEntityToWidgets() {
		if (optionalHotelBookingVO != null) {
    		optionalHotelBookingVO.setName(nameText.getText());
    		optionalHotelBookingVO.setCount(countSpinner.getValueAsInteger());
    		optionalHotelBookingVO.setNote(noteText.getText());
    		optionalHotelBookingVO.setArrival( arrivalDateComposite.getI18NDate());
    		optionalHotelBookingVO.setDeparture( departureDateComposite.getI18NDate() );
    		optionalHotelBookingVO.setExpiration( expirationDateComposite.getI18NDate() );
    		optionalHotelBookingVO.setExpirationEnabled(expirationEnableCheckButton.getSelection());
		}
	}


	/**
	 * Copy values from widgets to entity and return it.
	 * @return
	 */
	public OptionalHotelBookingVO getOptionalHotelBookingVO() {
		syncEntityToWidgets();
		return optionalHotelBookingVO;
	}


	/**
	 * Set entity and copy its values to widgets.
	 * @param optionalHotelBookingVO
	 * @throws Exception
	 */
	public void setOptionalHotelBookingVO(OptionalHotelBookingVO optionalHotelBookingVO) {
		this.optionalHotelBookingVO = optionalHotelBookingVO;
		syncWidgetsToEntity();
	}


	// *************************************************************************
	// * Internal methods
	// *

	private Date getFirstDay() {
		Date firstDay = hotelContingentEditorCapacityComposite.getFirstDayDate();
		return firstDay;
	}


	private Date getLastDay() {
		Date lastDay = hotelContingentEditorCapacityComposite.getLastDayDate();
		return lastDay;
	}


	private void validateArrivalDeparture() {
		Date newArrivalDate = arrivalDateComposite.getDate();
		Date newDepartureDate = departureDateComposite.getDate();

		boolean validArrivalDate = true;
		boolean validDepartureDate= true;


		Date firstDay = getFirstDay();
		Date lastDay = getLastDay();


		if (firstDay != null && lastDay != null) {
    		Date lastDayPlus1 = addDays(lastDay, 1);


    		// check arrivalDate
    		if (newArrivalDate == null ||
    			newArrivalDate.before(firstDay) || !newArrivalDate.before(lastDayPlus1)
    		) {
    			validArrivalDate = false;
    		}

    		// check departureDate
    		if (newDepartureDate == null ||
    			!newDepartureDate.after(getFirstDay()) || newDepartureDate.after(lastDayPlus1)
    		) {
    			validDepartureDate = false;
    		}

    		// check relation between arrivalDate and departureDate
    		if ( (validArrivalDate || validDepartureDate) &&
    			 newArrivalDate != null &&
    			 newDepartureDate != null &&
    			 !newDepartureDate.after(newArrivalDate)
    		) {
    			validArrivalDate = false;
    			validDepartureDate = false;
    		}
		}
		else {
			validArrivalDate = false;
			validDepartureDate = false;
		}

		// get colors
		Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		Color red = getDisplay().getSystemColor(SWT.COLOR_RED);

		// set color of arrivalDate
		if (validArrivalDate) {
			arrivalDateComposite.setForeground(black);
		}
		else {
			arrivalDateComposite.setForeground(red);
		}

		// set color of departureDate
		if (validDepartureDate) {
			departureDateComposite.setForeground(black);
		}
		else {
			departureDateComposite.setForeground(red);
		}
	}

	// *
	// * Internal methods
	// *************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	/* Uncomment to make this Composite totally invisible if it shall be hidden.
	 * The parent OptionalHotelBookingManagementComposite sets the heightHint of the GridData
	 * to 0 to hide the Composite. This minimizes the Composite but there is still a horizontal
	 * line visible what is the border of this Composite.
	 * During layout this Composite is asked for its preferred size by calling computeSize().
	 * Returning a a Point with a height of 0 makes this COmposite totally invisible.
	 * However, the GridLayout will still assign the
	 */
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point size = super.computeSize(wHint, hHint, changed);

		Object layoutData = getLayoutData();
		if (layoutData != null && layoutData instanceof GridData) {
    		GridData gridData = (GridData) getLayoutData();
    		if (gridData.heightHint == 0) {
    			size.y = 0;
    		}
		}

		return size;
	}

}
