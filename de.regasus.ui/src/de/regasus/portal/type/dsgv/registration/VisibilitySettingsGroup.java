package de.regasus.portal.type.dsgv.registration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.portal.type.standard.registration.StandardRegistrationPortalConfig;
import de.regasus.portal.type.standard.registration.StandardRegistrationPortalI18N;

public class VisibilitySettingsGroup extends EntityGroup<DsgvRegistrationPortalConfig> {

	private final int COL_COUNT = 1;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private Button showStartPageButton;
	private Button startPageRequiresPasswordButton;
	private Button withProgrammeBookingsButton;
	private Button withStreamsButton;
	private Button withHotelButton;

	// *
	// * Widgets
	// **************************************************************************


	public VisibilitySettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(StandardRegistrationPortalI18N.VisibilityGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		showStartPageButton = new Button(parent, SWT.CHECK);
		showStartPageButton.setText( StandardRegistrationPortalConfig.SHOW_START_PAGE.getString() );
		showStartPageButton.addSelectionListener(modifySupport);
		showStartPageButton.addListener(SWT.Selection, e -> refreshState());

		startPageRequiresPasswordButton = new Button(parent, SWT.CHECK);
		startPageRequiresPasswordButton.setText( StandardRegistrationPortalConfig.START_PAGE_REQUIRES_PASSWORD.getString() );
		startPageRequiresPasswordButton.addSelectionListener(modifySupport);

		withProgrammeBookingsButton = new Button(parent, SWT.CHECK);
		withProgrammeBookingsButton.setText(DsgvRegistrationPortalI18N.ShowProgrammeBookings);
		withProgrammeBookingsButton.addSelectionListener(modifySupport);

		withStreamsButton = new Button(parent, SWT.CHECK);
		withStreamsButton.setText(DsgvRegistrationPortalI18N.ShowStreams);
		withStreamsButton.addSelectionListener(modifySupport);

		withHotelButton = new Button(parent, SWT.CHECK);
		withHotelButton.setText(DsgvRegistrationPortalI18N.ShowHotel);
		withHotelButton.addSelectionListener(modifySupport);
	}


	private void refreshState() {
		startPageRequiresPasswordButton.setEnabled( showStartPageButton.getSelection() );
	}


	@Override
	protected void syncWidgetsToEntity() {
		showStartPageButton.setSelection( entity.isShowStartPage() );
		startPageRequiresPasswordButton.setSelection( entity.isStartPageRequiresPassword() );
		withProgrammeBookingsButton.setSelection( entity.isWithProgrammeBookings() );
		withStreamsButton.setSelection( entity.isWithStreams() );
		withHotelButton.setSelection( entity.isWithHotel() );

		refreshState();
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
    		entity.setShowStartPage( showStartPageButton.getSelection() );
    		entity.setStartPageRequiresPassword( startPageRequiresPasswordButton.getSelection() );
			entity.setWithProgrammeBookings( withProgrammeBookingsButton.getSelection() );
			entity.setWithStreams( withStreamsButton.getSelection() );
			entity.setWithHotel( withHotelButton.getSelection() );
		}
	}

}
