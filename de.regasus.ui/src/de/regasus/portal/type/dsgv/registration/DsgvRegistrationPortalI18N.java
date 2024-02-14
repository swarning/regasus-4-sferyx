package de.regasus.portal.type.dsgv.registration;

import org.eclipse.osgi.util.NLS;

public class DsgvRegistrationPortalI18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.portal.type.dsgv.registration.i18n-dsgv-registration-portal";


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, DsgvRegistrationPortalI18N.class);
	}


	public static String RegistrationGroup;
	public static String RegistrationNewParticipant;
	public static String RegistrationWithPersonalLink;
	public static String RegistrationWithVigenere2Code;
	public static String RegistrationWithLastNameAndVigenere2Code;
	public static String RegistrationWithLastNameAndParticipantNumber;
	public static String RegistrationWithProfile;

	public static String AtLeastOneRegistrationTypeMustBeSelected;

	public static String DefaultParticipantType;

	public static String BookingGroup;
	public static String ShowProgrammeBookings;
	public static String ShowStreams;
	public static String ShowHotel;

	public static String CompanionCount;
	public static String CompanionParticipantTypeEqualToMainParticipant;
	public static String CompanionDefaultParticipantType;
	public static String CompanionWithProgrammeBookings;

	public static String FileExplanation;

	public static String UrlAfterLogout;
	public static String UrlAfterLogoutDescription;


	private DsgvRegistrationPortalI18N() {
	}

}
