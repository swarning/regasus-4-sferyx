package de.regasus.portal.type.standard.group;

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

public class RegistrationSettingsGroup extends EntityGroup<StandardGroupPortalConfig> {

	private final int COL_COUNT = 1;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private Button registrationWithPersonalLinkButton;
	private Button registrationWithVigenereCodeButton;
	private Button registrationWithEmail1AndVigenereCodeButton;
	private Button registrationWithLastNameAndVigenereCodeButton;
	private Button registrationWithLastNameAndParticipantNumberButton;

	// *
	// * Widgets
	// **************************************************************************


	public RegistrationSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(StandardGroupPortalI18N.RegistrationGroup);
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
				if (    ! registrationWithPersonalLinkButton.getSelection()
					 && ! registrationWithVigenereCodeButton.getSelection()
					 && ! registrationWithEmail1AndVigenereCodeButton.getSelection()
					 && ! registrationWithLastNameAndVigenereCodeButton.getSelection()
					 && ! registrationWithLastNameAndParticipantNumberButton.getSelection()
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
						StandardGroupPortalI18N.AtLeastOneRegistrationTypeMustBeSelected
					);
				}
				else  {
					/* Only one of the following registration modes may be true:
					 * - registrationWithVigenereCodeButton
					 * - registrationWithEmail1AndVigenereCodeButton
					 * - registrationWithLastNameAndVigenereCodeButton
					 * - registrationWithLastNameAndParticipantNumberButton
					 */
					Button button = (Button) event.getSource();

					if (button.getSelection()
						&& (
	    					   button == registrationWithVigenereCodeButton
	    					|| button == registrationWithEmail1AndVigenereCodeButton
	    					|| button == registrationWithLastNameAndVigenereCodeButton
	    					|| button == registrationWithLastNameAndParticipantNumberButton
						)
					) {
						registrationWithVigenereCodeButton.setSelection(false);
						registrationWithEmail1AndVigenereCodeButton.setSelection(false);
						registrationWithLastNameAndVigenereCodeButton.setSelection(false);
						registrationWithLastNameAndParticipantNumberButton.setSelection(false);

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
		registrationWithPersonalLinkButton = new Button(parent, SWT.CHECK);
		registrationWithPersonalLinkButton.setText(StandardGroupPortalI18N.RegistrationWithPersonalLink);
		registrationWithPersonalLinkButton.addSelectionListener(modifySupport);
		registrationWithPersonalLinkButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 2
		SWTHelper.horizontalLine(parent);

		// Row 3
		registrationWithVigenereCodeButton = new Button(parent, SWT.CHECK);
		registrationWithVigenereCodeButton.setText(StandardGroupPortalI18N.RegistrationWithVigenere2Code);
		registrationWithVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 4
		registrationWithEmail1AndVigenereCodeButton = new Button(parent, SWT.CHECK);
		registrationWithEmail1AndVigenereCodeButton.setText(StandardGroupPortalI18N.RegistrationWithEmail1AndVigenere2Code);
		registrationWithEmail1AndVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithEmail1AndVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 5
		registrationWithLastNameAndVigenereCodeButton = new Button(parent, SWT.CHECK);
		registrationWithLastNameAndVigenereCodeButton.setText(StandardGroupPortalI18N.RegistrationWithLastNameAndVigenere2Code);
		registrationWithLastNameAndVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithLastNameAndVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 6
		registrationWithLastNameAndParticipantNumberButton = new Button(parent, SWT.CHECK);
		registrationWithLastNameAndParticipantNumberButton.setText(StandardGroupPortalI18N.RegistrationWithLastNameAndParticipantNumber);
		registrationWithLastNameAndParticipantNumberButton.addSelectionListener(modifySupport);
		registrationWithLastNameAndParticipantNumberButton.addSelectionListener(registrationTypeSelectionListener);
	}


	@Override
	protected void syncWidgetsToEntity() {
		registrationWithPersonalLinkButton.setSelection( entity.isRegistrationWithPersonalLink() );
		registrationWithVigenereCodeButton.setSelection( entity.isRegistrationWithVigenereCode() );
		registrationWithEmail1AndVigenereCodeButton.setSelection( entity.isRegistrationWithEmail1AndVigenereCode() );
		registrationWithLastNameAndVigenereCodeButton.setSelection( entity.isRegistrationWithLastNameAndVigenereCode() );
		registrationWithLastNameAndParticipantNumberButton.setSelection( entity.isRegistrationWithLastNameAndParticipantNumber() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setRegistrationWithPersonalLink( registrationWithPersonalLinkButton.getSelection() );
			entity.setRegistrationWithVigenereCode( registrationWithVigenereCodeButton.getSelection() );
			entity.setRegistrationWithEmail1AndVigenereCode( registrationWithEmail1AndVigenereCodeButton.getSelection() );
			entity.setRegistrationWithLastNameAndVigenereCode( registrationWithLastNameAndVigenereCodeButton.getSelection() );
			entity.setRegistrationWithLastNameAndParticipantNumber( registrationWithLastNameAndParticipantNumberButton.getSelection() );
		}
	}

}
