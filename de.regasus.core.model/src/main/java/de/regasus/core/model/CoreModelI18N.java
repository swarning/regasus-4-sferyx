package de.regasus.core.model;

import org.eclipse.osgi.util.NLS;

public class CoreModelI18N extends NLS {
	public static final String BUNDLE_NAME = "de.regasus.core.model.i18n-core-model"; 
	
	public static String ServerModel_LoginFailedErrorTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CoreModelI18N.class);
	}

	
	public static String ServerModel_LoginFailedErrorMessage;


	private CoreModelI18N() {
	}
	
}
