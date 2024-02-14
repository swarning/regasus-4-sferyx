package de.regasus.portal.type.openid.connect;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

public class AuthenticationSettingsGroup extends EntityGroup<OpenIDConnectPortalConfig> {

	private final int COL_COUNT = 1;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private Button authenticateAsParticipantButton;
	private Button authenticateAsProfileButton;

	// *
	// * Widgets
	// **************************************************************************


	public AuthenticationSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(OpenIDConnectPortalI18N.AuthenticationGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		// Row 1
		authenticateAsParticipantButton = new Button(parent, SWT.RADIO);
		authenticateAsParticipantButton.setText(OpenIDConnectPortalI18N.AuthenticateAsParticipant);
		authenticateAsParticipantButton.addSelectionListener(modifySupport);

		// Row 2
		authenticateAsProfileButton = new Button(parent, SWT.RADIO);
		authenticateAsProfileButton.setText(OpenIDConnectPortalI18N.AuthenticateAsProfile);
		authenticateAsProfileButton.addSelectionListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		authenticateAsParticipantButton.setSelection( entity.getAuthenticationType() == AuthenticationType.AS_PARTICIPANT );
		authenticateAsProfileButton.setSelection( entity.getAuthenticationType() == AuthenticationType.AS_PROFILE );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if (authenticateAsParticipantButton.getSelection()) {
				entity.setAuthenticationType(AuthenticationType.AS_PARTICIPANT);
			}
			else if (authenticateAsProfileButton.getSelection()) {
				entity.setAuthenticationType(AuthenticationType.AS_PROFILE);
			}
		}
	}

}
