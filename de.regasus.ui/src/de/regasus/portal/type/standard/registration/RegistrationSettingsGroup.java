package de.regasus.portal.type.standard.registration;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.List;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.portal.Portal;

public class RegistrationSettingsGroup extends EntityGroup<StandardRegistrationPortalConfig> {

	private final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	private List<Language> languageList;


	// **************************************************************************
	// * Widgets
	// *

	private Button newParticipantRegistrationButton;
	private Button registrationWithPersonalLinkButton;
	private Button registrationWithVigenereCodeButton;
	private Button registrationWithEmail1AndVigenereCodeButton;
	private Button registrationWithLastNameAndVigenereCodeButton;
	private Button registrationWithLastNameAndParticipantNumberButton;
	private Button registrationWithProfileButton;
	private Text profilePortalUrlText;
	private I18NComposite<StandardRegistrationPortalConfig> i18nComposite;

	// *
	// * Widgets
	// **************************************************************************


	public RegistrationSettingsGroup(Composite parent, int style, Portal portal)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portal)
		);

		setText(StandardRegistrationPortalI18N.RegistrationGroup);
	}


	private SelectionListener registrationTypeSelectionListener;

	@Override
	protected void initialize(Object[] initValues) throws Exception {
		Portal portal = (Portal) initValues[0];

		// determine Portal languages
		List<String> languageCodes = portal.getLanguageList();
		this.languageList = LanguageModel.getInstance().getLanguages(languageCodes);

		initRegistrationTypeSelectionListener();
	}


	private void initRegistrationTypeSelectionListener() {
		registrationTypeSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (    ! newParticipantRegistrationButton.getSelection()
					 && ! registrationWithPersonalLinkButton.getSelection()
					 && ! registrationWithVigenereCodeButton.getSelection()
					 && ! registrationWithEmail1AndVigenereCodeButton.getSelection()
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
						StandardRegistrationPortalI18N.AtLeastOneRegistrationTypeMustBeSelected
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
	    					|| button == registrationWithProfileButton
						)
					) {
						registrationWithVigenereCodeButton.setSelection(false);
						registrationWithEmail1AndVigenereCodeButton.setSelection(false);
						registrationWithLastNameAndVigenereCodeButton.setSelection(false);
						registrationWithLastNameAndParticipantNumberButton.setSelection(false);
						registrationWithProfileButton.setSelection(false);

						button.setSelection(true);
					}
				}

				refreshState();
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

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory textGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		// Row 1
		newParticipantRegistrationButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(newParticipantRegistrationButton);
		newParticipantRegistrationButton.setText(StandardRegistrationPortalI18N.RegistrationNewParticipant);
		newParticipantRegistrationButton.addSelectionListener(modifySupport);
		newParticipantRegistrationButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 2
		registrationWithPersonalLinkButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithPersonalLinkButton);
		registrationWithPersonalLinkButton.setText(StandardRegistrationPortalI18N.RegistrationWithPersonalLink);
		registrationWithPersonalLinkButton.addSelectionListener(modifySupport);
		registrationWithPersonalLinkButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 3
		SWTHelper.horizontalLine(parent);

		// Row 4
		registrationWithVigenereCodeButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithVigenereCodeButton);
		registrationWithVigenereCodeButton.setText(StandardRegistrationPortalI18N.RegistrationWithVigenere2Code);
		registrationWithVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 5
		registrationWithEmail1AndVigenereCodeButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithEmail1AndVigenereCodeButton);
		registrationWithEmail1AndVigenereCodeButton.setText(StandardRegistrationPortalI18N.RegistrationWithEmail1AndVigenere2Code);
		registrationWithEmail1AndVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithEmail1AndVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 6
		registrationWithLastNameAndVigenereCodeButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithLastNameAndVigenereCodeButton);
		registrationWithLastNameAndVigenereCodeButton.setText(StandardRegistrationPortalI18N.RegistrationWithLastNameAndVigenere2Code);
		registrationWithLastNameAndVigenereCodeButton.addSelectionListener(modifySupport);
		registrationWithLastNameAndVigenereCodeButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 7
		registrationWithLastNameAndParticipantNumberButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithLastNameAndParticipantNumberButton);
		registrationWithLastNameAndParticipantNumberButton.setText(StandardRegistrationPortalI18N.RegistrationWithLastNameAndParticipantNumber);
		registrationWithLastNameAndParticipantNumberButton.addSelectionListener(modifySupport);
		registrationWithLastNameAndParticipantNumberButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 8
		registrationWithProfileButton = new Button(parent, SWT.CHECK);
		checkboxGridDataFactory.applyTo(registrationWithProfileButton);
		registrationWithProfileButton.setText(StandardRegistrationPortalI18N.RegistrationWithProfile);
		registrationWithProfileButton.addSelectionListener(modifySupport);
		registrationWithProfileButton.addSelectionListener(registrationTypeSelectionListener);

		// Row 9
		{
    		Label label = new Label(parent, SWT.NONE);
    		labelGridDataFactory.copy().indent(40, 0).applyTo(label);
    		label.setText( StandardRegistrationPortalConfig.PROFILE_PORTAL_URL.getString() );
//		    		label.setToolTipText(ReactRegistrationPortalI18N.ProfilePortalUrlDescription);

    		profilePortalUrlText = new Text(parent, SWT.BORDER);
    		textGridDataFactory.applyTo(profilePortalUrlText);
    		profilePortalUrlText.addModifyListener(modifySupport);
		}

		// Row 10
		{
    		i18nComposite = new I18NComposite<>(parent, SWT.BORDER, languageList, new RegistrationSettingsGroupI18NWidgetController());
    		GridDataFactory
    			.fillDefaults()
    			.grab(true, false)
    			.span(COL_COUNT, 1)
    			.indent(40, 0)
    			.applyTo(i18nComposite);
    		i18nComposite.addModifyListener(modifySupport);
		}
	}


	private void refreshState() {
		profilePortalUrlText.setEnabled( registrationWithProfileButton.getSelection() );
		i18nComposite.setEnabled( registrationWithProfileButton.getSelection() );
	}


	@Override
	protected void syncWidgetsToEntity() {
		newParticipantRegistrationButton.setSelection( entity.isNewParticipantRegistration() );
		registrationWithPersonalLinkButton.setSelection( entity.isRegistrationWithPersonalLink() );
		registrationWithVigenereCodeButton.setSelection( entity.isRegistrationWithVigenereCode() );
		registrationWithEmail1AndVigenereCodeButton.setSelection( entity.isRegistrationWithEmail1AndVigenereCode() );
		registrationWithLastNameAndVigenereCodeButton.setSelection( entity.isRegistrationWithLastNameAndVigenereCode() );
		registrationWithLastNameAndParticipantNumberButton.setSelection( entity.isRegistrationWithLastNameAndParticipantNumber() );
		registrationWithProfileButton.setSelection(entity.isRegistrationWithProfile());
		profilePortalUrlText.setText( avoidNull(entity.getProfilePortalUrl()) );
		i18nComposite.setEntity(entity);

		refreshState();
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setNewParticipantRegistration( newParticipantRegistrationButton.getSelection() );
			entity.setRegistrationWithPersonalLink( registrationWithPersonalLinkButton.getSelection() );
			entity.setRegistrationWithVigenereCode( registrationWithVigenereCodeButton.getSelection() );
			entity.setRegistrationWithEmail1AndVigenereCode( registrationWithEmail1AndVigenereCodeButton.getSelection() );
			entity.setRegistrationWithLastNameAndVigenereCode( registrationWithLastNameAndVigenereCodeButton.getSelection() );
			entity.setRegistrationWithLastNameAndParticipantNumber( registrationWithLastNameAndParticipantNumberButton.getSelection() );
			entity.setRegistrationWithProfile( registrationWithProfileButton.getSelection() );
			entity.setProfilePortalUrl( profilePortalUrlText.getText() );
			i18nComposite.syncEntityToWidgets();
		}
	}

}
