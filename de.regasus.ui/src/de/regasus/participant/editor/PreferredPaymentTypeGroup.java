package de.regasus.participant.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.PreferredPaymentTypeConfigParameterSet;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.PreferredPaymentTypeRadio;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class PreferredPaymentTypeGroup extends Group {

	/**
	 * The entity
	 */
	private Participant participant;

	/**
	 * Modify support
	 */
	private ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private PreferredPaymentTypeRadio preferredProgrammePaymentTypeButton;
	private PreferredPaymentTypeRadio preferredHotelPaymentTypeButton;


	// Flags
	private boolean showPreferredProgrammePaymentType = true;
	private boolean showPreferredHotelPaymentType = true;


	public PreferredPaymentTypeGroup(Composite parent, int style) {
		this(parent, style, null);
	}


	public PreferredPaymentTypeGroup(
		Composite parent,
		int style,
		PreferredPaymentTypeConfigParameterSet configParameterSet
	) {
		super(parent, style);

		try {
    		if (configParameterSet != null) {
    			showPreferredProgrammePaymentType = configParameterSet.getProgrammeBooking().isVisible();
    			showPreferredHotelPaymentType = configParameterSet.getHotelBooking().isVisible();
    		}

			createPartControl();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void createPartControl() throws Exception {
		setText( ParticipantLabel.PreferredPaymentTypes.getString() );

		setLayout( new GridLayout(2, false) );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER);
		GridDataFactory buttonGridDataFactory = GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER);


		if (showPreferredProgrammePaymentType) {
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ParticipantLabel.ProgrammeBookings.getString() );

			preferredProgrammePaymentTypeButton = new PreferredPaymentTypeRadio(this, SWT.NONE);
			buttonGridDataFactory.applyTo(preferredProgrammePaymentTypeButton);
			preferredProgrammePaymentTypeButton.addModifyListener(modifySupport);
		}

		if (showPreferredHotelPaymentType) {
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText( ParticipantLabel.HotelBookings.getString() );

			preferredHotelPaymentTypeButton = new PreferredPaymentTypeRadio(this, SWT.NONE);
			buttonGridDataFactory.applyTo(preferredHotelPaymentTypeButton);
			preferredHotelPaymentTypeButton.addModifyListener(modifySupport);
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


	/**
	 * Copy values from entity to widgets.
	 */
	private void syncWidgetsToEntity() {
		if (participant != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						/*** copy values from entity to widgets ***/

						if (showPreferredProgrammePaymentType) {
							preferredProgrammePaymentTypeButton.setValue( participant.getPreferredProgrammePaymentType() );
						}
						if (showPreferredHotelPaymentType) {
							preferredHotelPaymentTypeButton.setValue( participant.getPreferredHotelPaymentType() );
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
		if (participant != null) {
			if (showPreferredProgrammePaymentType) {
				participant.setPreferredProgrammePaymentType( preferredProgrammePaymentTypeButton.getValue() );
			}
			if (showPreferredHotelPaymentType) {
				participant.setPreferredHotelPaymentType( preferredHotelPaymentTypeButton.getValue() );
			}
		}
	}


	/**
	 * Set entity and copy its values to widgets.
	 * @param communication
	 */
	public void setParticipant(Participant participant) {
		this.participant = participant;
		syncWidgetsToEntity();
	}


	@Override
	public void setEnabled (boolean enabled) {
		if (showPreferredProgrammePaymentType) {
			preferredProgrammePaymentTypeButton.setEnabled(enabled);
		}
		if (showPreferredHotelPaymentType) {
			preferredHotelPaymentTypeButton.setEnabled(enabled);
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
