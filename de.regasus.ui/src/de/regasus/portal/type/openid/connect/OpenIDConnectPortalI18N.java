package de.regasus.portal.type.openid.connect;

import org.eclipse.osgi.util.NLS;

public class OpenIDConnectPortalI18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.portal.type.openid.connect.i18n-openid-connect-portal";


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, OpenIDConnectPortalI18N.class);
	}


	public static String AuthenticationGroup;
	public static String AuthenticateAsParticipant;
	public static String AuthenticateAsProfile;
	public static String AuthenticateAsRegasusUser;

	public static String AtLeastOneAuthenticationTypeMustBeSelected;

	public static String ExternalSystemsGroup;


	private OpenIDConnectPortalI18N() {
	}

}
