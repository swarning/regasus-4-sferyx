package de.regasus.report;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.ui.Activator;

public class ReportWizardFactory {

	/**
	 * The caller of this method obtained the name of the class to use from the
	 * report definition in the database. However, the class will probably be
	 * implemented in a different plugin. We thus search through all plugin.xml
	 * files which define wizard-classes till we find the one which defines
	 * the desired class, then we ask the plugin to actually create an instance
	 * of that class.
	 */
	public static IReportWizard getReportWizardInstance(String wizardClassName) {

		IReportWizard reportWizard = null;

		if (wizardClassName != null) {
			IExtensionRegistry er = Platform.getExtensionRegistry();
			IConfigurationElement[] configurationElements = er.getConfigurationElementsFor(Activator.PLUGIN_ID, "reportWizard");

			for (IConfigurationElement configurationElement : configurationElements) {
				
				if (configurationElement.getName().equals("wizard") &&
					wizardClassName.equals(configurationElement.getAttribute("class"))) {
					
					try {
						reportWizard = (IReportWizard) configurationElement.createExecutableExtension("class");
					}
					catch (CoreException e) {
						RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, ReportWizardFactory.class.getName(), e);
					} 
					break;
				}
			}
		}
		return reportWizard;
	}
}
