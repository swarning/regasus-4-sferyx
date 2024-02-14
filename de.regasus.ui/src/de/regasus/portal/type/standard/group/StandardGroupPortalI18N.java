package de.regasus.portal.type.standard.group;

import org.eclipse.osgi.util.NLS;

public class StandardGroupPortalI18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.portal.type.standard.group.i18n-standard-group-portal";


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, StandardGroupPortalI18N.class);
	}


	public static String StartPageGroup;

	public static String RegistrationGroup;
	public static String RegistrationWithPersonalLink;
	public static String RegistrationWithVigenere2Code;
	public static String RegistrationWithEmail1AndVigenere2Code;
	public static String RegistrationWithLastNameAndVigenere2Code;
	public static String RegistrationWithLastNameAndParticipantNumber;
	public static String RegistrationWithProfile;

	public static String AtLeastOneRegistrationTypeMustBeSelected;

	public static String DefaultParticipantType;

	public static String BookingGroup;


	private StandardGroupPortalI18N() {
	}

}
