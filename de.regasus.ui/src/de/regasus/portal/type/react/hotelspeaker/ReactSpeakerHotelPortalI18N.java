package de.regasus.portal.type.react.hotelspeaker;

import org.eclipse.osgi.util.NLS;

public class ReactSpeakerHotelPortalI18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.portal.type.standard.hotel.i18n-standard-hotel-portal";


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ReactSpeakerHotelPortalI18N.class);
	}


	public static String GeneralSettingsGroup;
	public static String RegistrationGroup;
	public static String RegistrationWithPersonalLink;
	public static String RegistrationWithVigenere2Code;
	public static String RegistrationWithEmail1AndVigenere2Code;
	public static String RegistrationWithLastNameAndVigenere2Code;
	public static String RegistrationWithLastNameAndParticipantNumber;
	public static String RegistrationWithProfile;

	public static String AtLeastOneRegistrationTypeMustBeSelected;

	public static String DefaultParticipantType;

	public static String PaymentGroup;


	private ReactSpeakerHotelPortalI18N() {
	}

}
