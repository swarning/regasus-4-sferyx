package de.regasus.portal.type.dsgv.registration;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

public class RegistrationSettingsGroup extends EntityGroup<DsgvRegistrationPortalConfig> {

	private final int COL_COUNT = 1;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private Button newParticipantRegistrationButton;
	private Button registrationWithPersonalLinkButton;
	private Button registrationWithVigenereCodeButton;
	private Button registrationWithLastNameAndVigenereCodeButton;
	private Button registrationWithLastNameAndParticipantNumberButton;
	private Button registrationWithProfileButton;

	// *
	// * Widgets
	// **************************************************************************


	public RegistrationSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(DsgvRegistrationPortalI18N.RegistrationGroup);
	}


	private SelectionListener registrationTypeSelectionListener;

	@Override
	protected void initialize(Object[] initValues) throws Exception {
		initRegistrationTypeSelectionListener();
	}


	private void initRegistrationTypeSelectionListener() {
		registrationTypeSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (    ! newParticipantRegistrationButton.getSelection()
					 && ! registrationWithPersonalLinkButton.getSelection()
					 && ! registrationWithVigenereCodeButton.getSelection()
					 && ! registrationWithLastNameAndVigenereCodeButton.getSelection()
					 && ! registrationWithLastNameAndParticipantNumberButton.getSelection()
					 && ! registrationWithProfileButton.getSelection()
				) {
					event.doit = false;

					/* Set selection of source Button to true, because 'event.doit = false' is not working.
					 * The only condition to reach this block is when the source button has been unchecked and all
					 * other Buttons are unchecked, too.
					 */
					((Button) event.getSource()).setSelection(true);

					MessageDialog.openInformation(
						getShell(),
						UtilI18N.Info,
						DsgvRegistrationPortalI18N.AtLeastOneRegistrationTypeMustBeSelected
					);
				}
				else  {
					/* Only one of the following registration modes may be true:
					 * - registrationWithVigenereCodeButton
					 * - registrationWithLastNameAndVigenereCodeButton
					 * - registrationWithLastNameAndParticipantNumberButton
					 * - registrationWithProfileButton
					 */
					Button button = (Button) event.getSource();

					if (button.getSelection()
						&& (
	    					   button == registrationWithVigenereCodeButton
	    					|| button == registrationWithLastNameAndVigenereCodeButton
	    					|| button == registrationWithLastNameAndParticipantNumberButton
	    					|| button == registrationWithProfileButton
						)
					) {
						registrationWithVigenereCodeButton.setSelection(false);
						registrationWithLastNameAndVigenereCodeButton.setSelection(false);
						registrationWithLastNameAndParticipantNumberButton.setSelection(false);
						registrationWithProfileButton.setSelection(false);

						button.setSelection(true);
					}
				}
			}
		};
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		// Row 1
		newParticipantRegistrationButton = new Button(parent, SWT.CHECK);
		newParticipantRegistrationButton.setText(DsgvRegistrationPortalI18N.RegistrationNewParticipant);
		newParticipantRegistrationButton.addSelectionListener(modifySupport);
		newParticipantRegistrationButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 2
		registrationWithPersonalLinkButton = new Button(parent, SWT.CHECK);
		registrationWithPersonalLinkButton.setText(DsgvRegistrationPortalI18N.RegistrationWithPersonalLink);
		registrationWithPersonalLinkButton.addSelectionListener(modifySupport);
		registrationWithPersonalLinkButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 3
		SWTHelper.horizontalLine(parent);

		// Row 4
		registrationWithVigenereCodeButton = new Button(parent, SWT.CHECK);
		registrationWithVigenereCodeButton.setText(DsgvRegistrationPortalI18N.RegistrationWithVigenere2Code);
		registrationWithVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 5
		registrationWithLastNameAndVigenereCodeButton = new Button(parent, SWT.CHECK);
		registrationWithLastNameAndVigenereCodeButton.setText(DsgvRegistrationPortalI18N.RegistrationWithLastNameAndVigenere2Code);
		registrationWithLastNameAndVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithLastNameAndVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 6
		registrationWithLastNameAndParticipantNumberButton = new Button(parent, SWT.CHECK);
		registrationWithLastNameAndParticipantNumberButton.setText(DsgvRegistrationPortalI18N.RegistrationWithLastNameAndParticipantNumber);
		registrationWithLastNameAndParticipantNumberButton.addSelectionListener(modifySupport);
		registrationWithLastNameAndParticipantNumberButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 7
		registrationWithProfileButton = new Button(parent, SWT.CHECK);
		registrationWithProfileButton.setText(DsgvRegistrationPortalI18N.RegistrationWithProfile);
		registrationWithProfileButton.addSelectionListener(modifySupport);
		registrationWithProfileButton.addSelectionListener(registrationTypeSelectionListener);
	}


	@Override
	protected void syncWidgetsToEntity() {
		newParticipantRegistrationButton.setSelection( entity.isNewParticipantRegistration() );
		registrationWithPersonalLinkButton.setSelection( entity.isRegistrationWithPersonalLink() );
		registrationWithVigenereCodeButton.setSelection( entity.isRegistrationWithVigenereCode() );
		registrationWithLastNameAndVigenereCodeButton.setSelection( entity.isRegistrationWithLastNameAndVigenereCode() );
		registrationWithLastNameAndParticipantNumberButton.setSelection( entity.isRegistrationWithLastNameAndParticipantNumber() );
		registrationWithProfileButton.setSelection(entity.isRegistrationWithProfile());
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setNewParticipantRegistration( newParticipantRegistrationButton.getSelection() );
			entity.setRegistrationWithPersonalLink( registrationWithPersonalLinkButton.getSelection() );
			entity.setRegistrationWithVigenereCode( registrationWithVigenereCodeButton.getSelection() );
			entity.setRegistrationWithLastNameAndVigenereCode( registrationWithLastNameAndVigenereCodeButton.getSelection() );
			entity.setRegistrationWithLastNameAndParticipantNumber( registrationWithLastNameAndParticipantNumberButton.getSelection() );
			entity.setRegistrationWithProfile( registrationWithProfileButton.getSelection() );
		}
	}

}
