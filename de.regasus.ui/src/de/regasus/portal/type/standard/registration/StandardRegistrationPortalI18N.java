package de.regasus.portal.type.standard.registration;

import org.eclipse.osgi.util.NLS;

public class StandardRegistrationPortalI18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.portal.type.standard.registration.i18n-standard-registration-portal";


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, StandardRegistrationPortalI18N.class);
	}


	public static String VisibilityGroup;

	public static String RegistrationGroup;
	public static String RegistrationNewParticipant;
	public static String RegistrationWithPersonalLink;
	public static String RegistrationWithVigenere2Code;
	public static String RegistrationWithEmail1AndVigenere2Code;
	public static String RegistrationWithLastNameAndVigenere2Code;
	public static String RegistrationWithLastNameAndParticipantNumber;
	public static String RegistrationWithProfile;

	public static String AtLeastOneRegistrationTypeMustBeSelected;

	public static String DefaultParticipantType;
	public static String DefineParticipantTypeFromRegistrationPp;

	public static String BookingGroup;
	public static String PaymentGroup;

	public static String CompanionParticipantTypeEqualToMainParticipant;
	public static String CompanionDefaultParticipantType;

	public static String ExternalSystemsGroup;
	public static String GoogleGroup;

	private StandardRegistrationPortalI18N() {
	}

}
