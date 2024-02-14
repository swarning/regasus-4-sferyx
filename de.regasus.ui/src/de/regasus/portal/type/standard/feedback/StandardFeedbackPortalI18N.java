package de.regasus.portal.type.standard.feedback;

import org.eclipse.osgi.util.NLS;

public class StandardFeedbackPortalI18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.portal.type.standard.feedback.i18n-standard-feedback-portal";


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, StandardFeedbackPortalI18N.class);
	}


	public static String StartPageGroup;
	public static String ShowStartPage;
	public static String StartPageRequiresPassword;

	public static String Default;

	private StandardFeedbackPortalI18N() {
	}

}
