package de.regasus.finance.export.ui;

import org.eclipse.osgi.util.NLS;

public class FinanceExportI18N extends NLS {
	public static final String BUNDLE_NAME = "de.regasus.finance.export.ui.i18n-finance-export-ui"; 
	
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, FinanceExportI18N.class);
	}

	
	public static String KeyNotDefined;
	public static String FinanceExportClassNotFound;
	public static String FinanceExportClassCastError;


	private FinanceExportI18N() {
	}

}
