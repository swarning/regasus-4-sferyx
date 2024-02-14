package de.regasus.portal.type.react.hotelspeaker;

import java.util.Objects;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
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

import de.regasus.portal.Portal;
import de.regasus.portal.type.react.hotelspeaker.ReactHotelSpeakerPortalConfig;
import de.regasus.portal.type.standard.registration.StandardRegistrationPortalI18N;

public class RegistrationSettingsGroup extends EntityGroup<ReactHotelSpeakerPortalConfig> {

	private final int COL_COUNT = 2;


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


	public RegistrationSettingsGroup(Composite parent, int style, Portal portal)
	throws Exception {
		super(parent, style, Objects.requireNonNull(portal));

		setText(ReactSpeakerHotelPortalI18N.RegistrationGroup);
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
						ReactSpeakerHotelPortalI18N.AtLeastOneRegistrationTypeMustBeSelected
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

		GridDataFactory checkboxGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.span(COL_COUNT, 1)
			.grab(true, false);

		// Row 1
		registrationWithPersonalLinkButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithPersonalLinkButton);
		registrationWithPersonalLinkButton.setText(StandardRegistrationPortalI18N.RegistrationWithPersonalLink);
		registrationWithPersonalLinkButton.addSelectionListener(modifySupport);
		registrationWithPersonalLinkButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 2
		SWTHelper.horizontalLine(parent);

		// Row 3
		registrationWithVigenereCodeButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithVigenereCodeButton);
		registrationWithVigenereCodeButton.setText(StandardRegistrationPortalI18N.RegistrationWithVigenere2Code);
		registrationWithVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 4
		registrationWithEmail1AndVigenereCodeButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithEmail1AndVigenereCodeButton);
		registrationWithEmail1AndVigenereCodeButton.setText(StandardRegistrationPortalI18N.RegistrationWithEmail1AndVigenere2Code);
		registrationWithEmail1AndVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithEmail1AndVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 5
		registrationWithLastNameAndVigenereCodeButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithLastNameAndVigenereCodeButton);
		registrationWithLastNameAndVigenereCodeButton.setText(StandardRegistrationPortalI18N.RegistrationWithLastNameAndVigenere2Code);
		registrationWithLastNameAndVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithLastNameAndVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 6
		registrationWithLastNameAndParticipantNumberButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithLastNameAndParticipantNumberButton);
		registrationWithLastNameAndParticipantNumberButton.setText(StandardRegistrationPortalI18N.RegistrationWithLastNameAndParticipantNumber);
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
