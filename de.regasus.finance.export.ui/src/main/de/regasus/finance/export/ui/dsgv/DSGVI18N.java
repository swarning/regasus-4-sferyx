package de.regasus.finance.export.ui.dsgv;

import org.eclipse.osgi.util.NLS;

public class DSGVI18N extends NLS {

	private static final String BUNDLE_NAME = "de.regasus.finance.export.ui.dsgv.i18n-sapexport"; 


	public static String ExportDirDoesNotExistAndCouldNotBeCreated;
	public static String ExportFileSuccessfullyWritten;
	
	public static String NoConfiguredExportDir;

	public static String OpenExportedFile;

	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, DSGVI18N.class);
	}


	private DSGVI18N() {
	}
	
}
