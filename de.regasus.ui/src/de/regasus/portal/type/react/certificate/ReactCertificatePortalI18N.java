package de.regasus.portal.type.react.certificate;

import org.eclipse.osgi.util.NLS;

public class ReactCertificatePortalI18N extends NLS {
	
	public static final String BUNDLE_NAME = "de.regasus.portal.type.react.certificate.i18n-react-certificate-portal";
	
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ReactCertificatePortalI18N.class);
	}

	
	public static String AuthenticationGroup;
	public static String AuthenticationWithPersonalLink;
	public static String AuthenticationWithVigenereCode;
	public static String AuthenticationWithEmail1AndVigenereCode;
	public static String AuthenticationWithLastNameAndVigenereCode;
	public static String AuthenticationWithLastNameAndParticipantNumber;
	
	public static String AtLeastOneAuthenticationTypeMustBeSelected;
	
	
	public ReactCertificatePortalI18N() {
	}
	
}
