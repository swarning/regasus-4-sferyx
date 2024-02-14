package de.regasus.push;

import org.eclipse.osgi.util.NLS;

public class PushI18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.push.i18n-push-ui";

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, PushI18N.class);
	}

	public static String EditPushSettingsEditor_TopText;
	public static String EditPushSettingsEditor_RemovePushSettingsQuestion;
	public static String EditPushSettingsEditor_TransferButtonText;
	public static String EditPushSettingsEditor_TransferDataQuestion;


	private PushI18N() {
	}

}
