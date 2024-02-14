package de.regasus.core.ui;

import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
import de.regasus.core.ui.openoffice.pref.OpenOfficePreference;
import de.regasus.core.ui.openoffice.pref.OpenOfficePreferenceChangeListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.regasus.core.ui";

	// The shared instance
	private static Activator plugin;

	private IOfficeApplication localOfficeApplication;


	public Activator() {
	}


	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}


	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Trigger preference initialization via extension point org.eclipse.core.runtime.preferences
		// See also: https://lambdalogic.atlassian.net/wiki/x/AwATsg
		getPreferenceStore().getString("dummy");

		OpenOfficePreference.getInstance().getPreferenceStore().addPropertyChangeListener( new OpenOfficePreferenceChangeListener() );
	}


	@Override
	public void stop(BundleContext context) throws Exception {
		// Shutdown OOo if the user has no other open documents, eg in a separate OOo application window
		if (localOfficeApplication != null && localOfficeApplication.getDocumentService().getCurrentDocumentCount() == 0) {
			try {
				localOfficeApplication.deactivate();
				localOfficeApplication.dispose();
				localOfficeApplication = null;
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	// ----------------------------------------------------------------------------
	/**
	 * Returns local office application. The instance of the application will be managed by this plugin.
	 *
	 * @return local office application
	 *
	 * @author Andreas Bröker
	 * @param openofficePath
	 */
	public synchronized IOfficeApplication getManagedLocalOfficeApplication(String openofficePath) {
		if (localOfficeApplication == null) {
			HashMap<String, String> configuration = new HashMap<>(1);
			configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.LOCAL_APPLICATION);
			configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, openofficePath);
			try {
				localOfficeApplication = OfficeApplicationRuntime.getApplication(configuration);
			}
			catch (Throwable throwable) {
				// can not be - this code must work
				Platform.getLog(getBundle()).log(
					new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, throwable.getMessage(), throwable));
			}
		}
		return localOfficeApplication;
	}

}
