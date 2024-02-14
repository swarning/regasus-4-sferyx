package de.regasus.portal.type.react.certificate;

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

public class AuthenticationGroup extends EntityGroup<ReactCertificatePortalConfig> {

	private final int COL_COUNT = 1;


	// **************************************************************************
	// * Widgets
	// *

	private Button authenticationWithPersonalLinkButton;
	private Button authenticationWithVigenereCodeButton;
	private Button authenticationWithEmail1AndVigenereCodeButton;
	private Button authenticationWithLastNameAndVigenereCodeButton;
	private Button authenticationWithLastNameAndParticipantNumberButton;

	// *
	// * Widgets
	// **************************************************************************

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public AuthenticationGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(ReactCertificatePortalI18N.AuthenticationGroup);
	}


	private SelectionListener authenticationTypeSelectionListener;


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		initAuthentificationTypeSelectionListener();
	}


	private void initAuthentificationTypeSelectionListener() {
		authenticationTypeSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (    ! authenticationWithPersonalLinkButton.getSelection()
					 && ! authenticationWithVigenereCodeButton.getSelection()
					 && ! authenticationWithEmail1AndVigenereCodeButton.getSelection()
					 && ! authenticationWithLastNameAndVigenereCodeButton.getSelection()
					 && ! authenticationWithLastNameAndParticipantNumberButton.getSelection()
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
						ReactCertificatePortalI18N.AtLeastOneAuthenticationTypeMustBeSelected
					);
				}
				else  {
					/* Only one of the following authentication modes may be true:
					 * - registrationWithVigenereCodeButton
					 * - registrationWithEmail1AndVigenereCodeButton
					 * - registrationWithLastNameAndVigenereCodeButton
					 * - registrationWithLastNameAndParticipantNumberButton
					 */
					Button button = (Button) event.getSource();

					if (button.getSelection()
						&& (
	    					   button == authenticationWithVigenereCodeButton
	    					|| button == authenticationWithEmail1AndVigenereCodeButton
	    					|| button == authenticationWithLastNameAndVigenereCodeButton
	    					|| button == authenticationWithLastNameAndParticipantNumberButton
						)
					) {
						authenticationWithVigenereCodeButton.setSelection(false);
						authenticationWithEmail1AndVigenereCodeButton.setSelection(false);
						authenticationWithLastNameAndVigenereCodeButton.setSelection(false);
						authenticationWithLastNameAndParticipantNumberButton.setSelection(false);

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
		authenticationWithPersonalLinkButton = new Button(parent, SWT.CHECK);
		authenticationWithPersonalLinkButton.setText(ReactCertificatePortalI18N.AuthenticationWithPersonalLink);
		authenticationWithPersonalLinkButton.addSelectionListener(modifySupport);
		authenticationWithPersonalLinkButton.addSelectionListener(authenticationTypeSelectionListener);

		// Row 2
		SWTHelper.horizontalLine(parent);

		// Row 3
		authenticationWithVigenereCodeButton = new Button(parent, SWT.CHECK);
		authenticationWithVigenereCodeButton.setText(ReactCertificatePortalI18N.AuthenticationWithVigenereCode);
		authenticationWithVigenereCodeButton.addSelectionListener(modifySupport);
		authenticationWithVigenereCodeButton.addSelectionListener(authenticationTypeSelectionListener);

		// Row 4
		authenticationWithEmail1AndVigenereCodeButton = new Button(parent, SWT.CHECK);
		authenticationWithEmail1AndVigenereCodeButton.setText(ReactCertificatePortalI18N.AuthenticationWithEmail1AndVigenereCode);
		authenticationWithEmail1AndVigenereCodeButton.addSelectionListener(modifySupport);
		authenticationWithEmail1AndVigenereCodeButton.addSelectionListener(authenticationTypeSelectionListener);

		// Row 5
		authenticationWithLastNameAndVigenereCodeButton = new Button(parent, SWT.CHECK);
		authenticationWithLastNameAndVigenereCodeButton.setText(ReactCertificatePortalI18N.AuthenticationWithLastNameAndVigenereCode);
		authenticationWithLastNameAndVigenereCodeButton.addSelectionListener(modifySupport);
		authenticationWithLastNameAndVigenereCodeButton.addSelectionListener(authenticationTypeSelectionListener);

		// Row 6
		authenticationWithLastNameAndParticipantNumberButton = new Button(parent, SWT.CHECK);
		authenticationWithLastNameAndParticipantNumberButton.setText(ReactCertificatePortalI18N.AuthenticationWithLastNameAndParticipantNumber);
		authenticationWithLastNameAndParticipantNumberButton.addSelectionListener(modifySupport);
		authenticationWithLastNameAndParticipantNumberButton.addSelectionListener(authenticationTypeSelectionListener);
	}


	@Override
	protected void syncWidgetsToEntity() {
		authenticationWithPersonalLinkButton.setSelection( entity.isAuthenticationWithPersonalLink() );
		authenticationWithVigenereCodeButton.setSelection( entity.isAuthenticationWithVigenereCode() );
		authenticationWithEmail1AndVigenereCodeButton.setSelection( entity.isAuthenticationWithEmail1AndVigenereCode() );
		authenticationWithLastNameAndVigenereCodeButton.setSelection( entity.isAuthenticationWithLastNameAndVigenereCode() );
		authenticationWithLastNameAndParticipantNumberButton.setSelection( entity.isAuthenticationWithLastNameAndParticipantNumber() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setAuthenticationWithPersonalLink( authenticationWithPersonalLinkButton.getSelection() );
			entity.setAuthenticationWithVigenereCode( authenticationWithVigenereCodeButton.getSelection() );
			entity.setAuthenticationWithEmail1AndVigenereCode( authenticationWithEmail1AndVigenereCodeButton.getSelection() );
			entity.setAuthenticationWithLastNameAndVigenereCode( authenticationWithLastNameAndVigenereCodeButton.getSelection() );
			entity.setAuthenticationWithLastNameAndParticipantNumber( authenticationWithLastNameAndParticipantNumberButton.getSelection() );
		}
	}
}
