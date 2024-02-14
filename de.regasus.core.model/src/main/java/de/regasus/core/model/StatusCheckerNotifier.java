package de.regasus.core.model;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import de.regasus.core.ServerModel;

/**
 * A runnable called periodically by the {@link ServerModel} to look up and notify all classes that are 
 * configured as extensions of the extension point de.regasus.core.model.statusChecker.
 */
public class StatusCheckerNotifier implements Runnable {

	private static final String EXTENSION_POINT_FULL_ID = "de.regasus.core.model.statusChecker";
	
	private static DateFormat dateTimeFormat = DateFormat.getTimeInstance(DateFormat.LONG);

	
	public void run() {
		String time = dateTimeFormat.format(new Date());
		
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_FULL_ID);
		if (elements != null && elements.length > 0) {
			for (IConfigurationElement element : elements) {
				try {
//					System.out.println(time + ": Creating and notifiying statusChecker of " + element.getContributor().getName());
					StatusChecker statusChecker = (StatusChecker) element.createExecutableExtension("class");
					statusChecker.checkStatus();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			} 
		}
		else {
			System.out.println(time + ": No contribution element found for " + EXTENSION_POINT_FULL_ID);
		}

	}
}
