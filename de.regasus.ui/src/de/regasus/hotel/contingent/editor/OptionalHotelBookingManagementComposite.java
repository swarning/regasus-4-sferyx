package de.regasus.hotel.contingent.editor;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.OptionalHotelBookingVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.ListComposite;
import com.lambdalogic.util.rcp.ListCompositeController;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;


public class OptionalHotelBookingManagementComposite
extends Composite
implements ListComposite<OptionalHotelBookingComposite> {

	// parent entity that contains the list of sub-entities managed by this Composite
	private HotelContingentCVO hotelContingentCVO;

	// support for ModifyEvents
	private ModifySupport modifySupport = new ModifySupport(this);

	// support for handling a List of sub-Composites
	private ListCompositeController<OptionalHotelBookingComposite> compositeListSupport =
		new ListCompositeController<OptionalHotelBookingComposite>(this);

	// ScrolledComposite to realize vertical scroll bars
	private ScrolledComposite scrollComposite;

	// parent Composite for sub-Composites
	private Composite contentComposite;

	private Button addButton;

	private Button showExpiredBookingsButton;

	private HotelContingentEditorCapacityComposite hotelContingentEditorCapacityComposite;


	public OptionalHotelBookingManagementComposite(
		final Composite parent,
		int style,
		HotelContingentEditorCapacityComposite hotelContingentEditorCapacityComposite
	)
	throws Exception {
		super(parent, style);

		this.hotelContingentEditorCapacityComposite = hotelContingentEditorCapacityComposite;

		this.setLayout(new GridLayout(1, false));

		createPartControl();
	}


	protected void createPartControl() throws Exception {
		// make the folders contentComposite scrollable
		scrollComposite = new ScrolledComposite(this, SWT.V_SCROLL );
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollComposite.setExpandVertical(true);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setShowFocusedControl(true);

		contentComposite = new Composite(scrollComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		// Set verticalSpacing to 0 to avoid giving that space to Composites with a height of 0.
		// Necessary because Composites are hidden by setting their height to 0.
		gridLayout.verticalSpacing = 0;
		contentComposite.setLayout(gridLayout);


		scrollComposite.setContent(contentComposite);
		scrollComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				refreshScrollbar();
			}
		});


		// horizontal line
		Label separatorLabel = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


		/*
		 * Buttons
		 */

		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		GridLayout buttonCompositeLayout = new GridLayout(2, false);
		buttonCompositeLayout.horizontalSpacing = 0;
		buttonCompositeLayout.verticalSpacing = 0;
		buttonCompositeLayout.marginWidth = 0;
		buttonCompositeLayout.marginHeight = 0;
		buttonComposite.setLayout(buttonCompositeLayout);


		// Button to hide/show Composites of expired bookings
		showExpiredBookingsButton = new Button(buttonComposite, SWT.CHECK);
		showExpiredBookingsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		showExpiredBookingsButton.setText(I18N.OptionalHotelBookingManagementComposite_ShowExpiredBookings);
		showExpiredBookingsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleExpiredBookings();
			}
		});


		// Button to add new Composites
		addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		addButton.setText(UtilI18N.Add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
    			addItem();
			}
		});


		// calling syncWidgetsToEntity() is not necessary, because this is not a LazyComposite
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.CompositeListSupport.CompositeFactory#createComposite()
	 */
	@Override
	public OptionalHotelBookingComposite createComposite() {
		final OptionalHotelBookingComposite composite = new OptionalHotelBookingComposite(
			contentComposite,
			SWT.None,
			hotelContingentEditorCapacityComposite
		);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		composite.addModifyListener(modifySupport);
		composite.addRemoveListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				compositeListSupport.removeComposite(composite);
			}
		});

		return composite;
	}


	private OptionalHotelBookingVO createEntity() throws Exception {
		OptionalHotelBookingVO entity = new OptionalHotelBookingVO();

		/* initialize arrival and departure
		 * The initial value for arrival is the beginning of the event.
		 * The initial value for departure is the end of the event or the following day if the end
		 * and the start of the event are equal.
		 */
		Long eventPK = hotelContingentCVO.getEventPK();
		EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
		I18NDate arrival = eventVO.getBeginDate();
		I18NDate departure = eventVO.getEndDate();

		if ( arrival.equals(departure) ) {
			departure = departure.plusDays(1);
		}

		entity.setArrival(arrival);
		entity.setDeparture(departure);

		return entity;
	}


	private void addItem() {
		try {
			// create Composite
			OptionalHotelBookingComposite composite = compositeListSupport.addComposite();

			// create entity
			OptionalHotelBookingVO entity = createEntity();

			// add entity to Composite
			composite.setOptionalHotelBookingVO(entity);

			// scroll to the end
			scrollComposite.setOrigin(0, Integer.MAX_VALUE);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
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
	 * Copy values from sub-entities to widgets of sub-Composites.
	 */
	private void syncWidgetsToEntity() {
		if (hotelContingentCVO != null && hotelContingentCVO.getPK() != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						// get sub-entity list
						List<OptionalHotelBookingVO> subEntityList = hotelContingentCVO.getOptionalHotelBookingVOs();
						if (subEntityList == null) {
							subEntityList = CollectionsHelper.createArrayList(0);
						}

						// set number of necessary Composites
						compositeListSupport.setSize(subEntityList.size());

						// set n sub-entities to n sub-Composites
						for (int i = 0; i < subEntityList.size(); i++) {
							// set sub-entity to sub-Composite
							compositeListSupport.getComposite(i).setOptionalHotelBookingVO( subEntityList.get(i) );
						}


						handleExpiredBookings();
						refreshScrollbar();
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
	 * Copy sub-entities from the sub-Composites to main entity.
	 */
	public void syncEntityToWidgets() {
		// get sub-entity list
		List<OptionalHotelBookingVO> subEntityList = hotelContingentCVO.getOptionalHotelBookingVOs();
		if (subEntityList == null) {
			subEntityList = CollectionsHelper.createArrayList(0);
		}

		// clear List
		subEntityList.clear();

		// add sub-entities from the sub-Composites to subEntityList
		for (OptionalHotelBookingComposite subComposite : compositeListSupport.getCompositeList()) {
			subEntityList.add(subComposite.getOptionalHotelBookingVO());
		}
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.CompositeListSupport.CompositeFactory#fireModifyEvent()
	 */
	@Override
	public void fireModifyEvent() {
		modifySupport.fire();
	}


	private void refreshScrollbar() {
		Rectangle clientArea = scrollComposite.getClientArea();
		scrollComposite.setMinSize(contentComposite.computeSize(clientArea.width, SWT.DEFAULT));
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.CompositeListSupport.CompositeFactory#refreshLayout()
	 */
	@Override
	public void refreshLayout() {
		layout(true, true);
		refreshScrollbar();
	}


	public HotelContingentCVO getHotelContingentCVO() {
		return hotelContingentCVO;
	}


	public void setHotelContingentCVO(HotelContingentCVO hotelContingentCVO) {
		this.hotelContingentCVO = hotelContingentCVO;
		syncWidgetsToEntity();
	}


	// *************************************************************************
	// * Internal methods
	// *

	/**
	 * Show or hide OptionalBookingComposites according to the state of the showExpiredBookingsButton
	 * and the OptionalHotelBookingVOs.
	 */
	private void handleExpiredBookings() {
		boolean showExpiredBookings = showExpiredBookingsButton.getSelection();

		for (OptionalHotelBookingComposite composite : compositeListSupport.getCompositeList()) {
			GridData gridData = (GridData) composite.getLayoutData();

			if (showExpiredBookings) {
				gridData.heightHint = SWT.DEFAULT;
			}
			else {
    			OptionalHotelBookingVO optionalHotelBookingVO = composite.getOptionalHotelBookingVO();
    			if (!optionalHotelBookingVO.isBlocking()) {
        			gridData.heightHint = 0;
    			}
    			else {
    				gridData.heightHint = SWT.DEFAULT;
    			}
			}
		}

		layout(true, true);
	}

	// *
	// * Internal methods
	// *************************************************************************

}
