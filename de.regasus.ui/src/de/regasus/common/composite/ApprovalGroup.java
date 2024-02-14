package de.regasus.common.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.PersonConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.BooleanRadio;
import com.lambdalogic.util.rcp.widget.DoubleOptInStatusCombo;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class ApprovalGroup extends Group {

	/**
	 * The entity
	 */
	private Person person;

	/**
	 * Modify support
	 */
	private ModifySupport modifySupport = new ModifySupport(this);

	/*** Widgets ***/
	private BooleanRadio privacyButton;
	private BooleanRadio programmeConditionsButton;
	private BooleanRadio programmeCancelConditionsButton;
	private BooleanRadio hotelConditionsButton;
	private BooleanRadio hotelCancelConditionsButton;
	private DoubleOptInStatusCombo promotionCombo;


	// Flags
	private boolean showProgrammeConditions = true;
	private boolean showProgrammeCancelConditions = true;
	private boolean showHotelConditions = true;
	private boolean showHotelCancelConditions = true;
	private boolean showPromotion = true;


	public ApprovalGroup(Composite parent, int style) {
		this(parent, style, null);
	}


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public ApprovalGroup(
		Composite parent,
		int style,
		PersonConfigParameterSet configParameterSet
	) {
		super(parent, style);

		try {
    		if (configParameterSet != null) {
    			showProgrammeConditions = configParameterSet.getProgrammeConditionsAccepted().isVisible();
    			showProgrammeCancelConditions = configParameterSet.getProgrammeCancelConditionsAccepted().isVisible();
    			showHotelConditions = configParameterSet.getHotelConditionsAccepted().isVisible();
    			showHotelCancelConditions = configParameterSet.getHotelCancelConditionsAccepted().isVisible();
    			showPromotion = configParameterSet.getPromotion().isVisible();
    		}

			createPartControl();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void createPartControl() throws Exception {
		setText(ContactLabel.Approvals.getString());

		setLayout(new GridLayout(2, false));

		// privacy (always visible)
		{
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
			label.setText( Person.PRIVACY_ACCEPTED.getLabel() );
			label.setToolTipText( Person.PRIVACY_ACCEPTED.getDescription() );

			Composite composite = new Composite(this, SWT.NONE);
			GridLayout gridLayout = new GridLayout(1, false);
			gridLayout.marginHeight = 0;
			composite.setLayout( gridLayout );

			privacyButton = new BooleanRadio(composite, SWT.NONE);
			privacyButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			privacyButton.addSelectionListener(modifySupport);
		}

		if (showProgrammeConditions) {
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
			label.setText( Person.PROGRAMME_CONDITIONS_ACCEPTED.getLabel() );
			label.setToolTipText( Person.PROGRAMME_CONDITIONS_ACCEPTED.getDescription() );

			Composite composite = new Composite(this, SWT.NONE);
			GridLayout gridLayout = new GridLayout(1, false);
			gridLayout.marginHeight = 0;
			composite.setLayout( gridLayout );

			programmeConditionsButton = new BooleanRadio(composite, SWT.NONE);
			programmeConditionsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			programmeConditionsButton.addSelectionListener(modifySupport);
		}

		if (showProgrammeCancelConditions) {
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
			label.setText( Person.PROGRAMME_CANCEL_CONDITIONS_ACCEPTED.getLabel() );
			label.setToolTipText( Person.PROGRAMME_CANCEL_CONDITIONS_ACCEPTED.getDescription());

			Composite composite = new Composite(this, SWT.NONE);
			GridLayout gridLayout = new GridLayout(1, false);
			gridLayout.marginHeight = 0;
			composite.setLayout( gridLayout );

			programmeCancelConditionsButton = new BooleanRadio(composite, SWT.NONE);
			programmeCancelConditionsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			programmeCancelConditionsButton.addSelectionListener(modifySupport);
		}

		if (showHotelConditions) {
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
			label.setText( Person.HOTEL_CONDITIONS_ACCEPTED.getLabel() );
			label.setToolTipText( Person.HOTEL_CONDITIONS_ACCEPTED.getDescription() );

			Composite composite = new Composite(this, SWT.NONE);
			GridLayout gridLayout = new GridLayout(1, false);
			gridLayout.marginHeight = 0;
			composite.setLayout( gridLayout );

			hotelConditionsButton = new BooleanRadio(composite, SWT.NONE);
			hotelConditionsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			hotelConditionsButton.addSelectionListener(modifySupport);
		}

		if (showHotelCancelConditions) {
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
			label.setText( Person.HOTEL_CANCEL_CONDITIONS_ACCEPTED.getLabel() );
			label.setToolTipText( Person.HOTEL_CANCEL_CONDITIONS_ACCEPTED.getDescription() );

			Composite composite = new Composite(this, SWT.NONE);
			GridLayout gridLayout = new GridLayout(1, false);
			gridLayout.marginHeight = 0;
			composite.setLayout( gridLayout );

			hotelCancelConditionsButton = new BooleanRadio(composite, SWT.NONE);
			hotelCancelConditionsButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			hotelCancelConditionsButton.addSelectionListener(modifySupport);
		}

		if (showPromotion) {
			Label label = new Label(this, SWT.NONE);
			label.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false) );
			label.setText( Person.PROMOTION.getLabel());
			label.setToolTipText( Person.PROMOTION.getDescription() );

			Composite composite = new Composite(this, SWT.NONE);
			GridLayout gridLayout = new GridLayout(1, false);
			gridLayout.marginHeight = 0;
			composite.setLayout( gridLayout );

			promotionCombo = new DoubleOptInStatusCombo(composite, SWT.NONE);
			promotionCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			promotionCombo.addModifyListener(modifySupport);
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
		if (person != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						/*** copy values from entity to widgets ***/

						privacyButton.setValue( person.getPrivacyAccepted() );
						if (showProgrammeConditions) {
							programmeConditionsButton.setValue( person.getProgrammeConditionsAccepted() );
						}
						if (showProgrammeCancelConditions) {
							programmeCancelConditionsButton.setValue( person.getProgrammeCancelConditionsAccepted() );
						}
						if (showHotelConditions) {
							hotelConditionsButton.setValue( person.getHotelConditionsAccepted() );
						}
						if (showHotelCancelConditions) {
							hotelCancelConditionsButton.setValue( person.getHotelCancelConditionsAccepted() );
						}
						if (showPromotion) {
							promotionCombo.setDoubleOptInStatus( person.getPromotion() );
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
		if (person != null) {
			person.setPrivacyAccepted( privacyButton.getValue() );
			if (showProgrammeConditions) {
				person.setProgrammeConditionsAccepted( programmeConditionsButton.getValue() );
			}
			if (showProgrammeCancelConditions) {
				person.setProgrammeCancelConditionsAccepted( programmeCancelConditionsButton.getValue() );
			}
			if (showHotelConditions) {
				person.setHotelConditionsAccepted( hotelConditionsButton.getValue() );
			}
			if (showHotelCancelConditions) {
				person.setHotelCancelConditionsAccepted( hotelCancelConditionsButton.getValue() );
			}
			if (showPromotion) {
				person.setPromotion( promotionCombo.getDoubleOptInStatus() );
			}
		}
	}


	/**
	 * Set entity and copy its values to widgets.
	 * @param communication
	 */
	public void setPerson(Person person) {
		this.person = person;
		syncWidgetsToEntity();
	}


	@Override
	public void setEnabled (boolean enabled) {
		privacyButton.setEnabled(enabled);
		if (showProgrammeConditions) {
			programmeConditionsButton.setEnabled(enabled);
		}
		if (showProgrammeCancelConditions) {
			programmeCancelConditionsButton.setEnabled(enabled);
		}
		if (showHotelConditions) {
			hotelConditionsButton.setEnabled(enabled);
		}
		if (showHotelCancelConditions) {
			hotelCancelConditionsButton.setEnabled(enabled);
		}
		if (showPromotion) {
			promotionCombo.setEnabled(enabled);
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
