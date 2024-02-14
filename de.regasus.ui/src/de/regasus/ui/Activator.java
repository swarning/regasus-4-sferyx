package de.regasus.ui;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.regasus.core.error.RegasusErrorHandler;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "de.regasus.ui";

	// The shared instance
	private static Activator plugin;

	public Activator() {
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;


		// Trigger preference initialization via extension point org.eclipse.core.runtime.preferences
		// See also: https://lambdalogic.atlassian.net/wiki/x/AwATsg
		getPreferenceStore().getString("dummy");


		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					// get ImageRegistry
					ImageRegistry imageRegistry = getDefault().getImageRegistry();

					// create ImageDescriptors and put into ImageRegistry
					imageRegistry.put("next", getImageDescriptor("icons/next.png"));
					imageRegistry.put("down", getImageDescriptor("icons/down.png"));
					imageRegistry.put("flagRed", getImageDescriptor("icons/flag_red.png"));
					imageRegistry.put("flagGreen", getImageDescriptor("icons/flag_green.png"));
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

		printWorkspaceDir();
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}


	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}


	private void printWorkspaceDir() {
		URL url = Platform.getInstanceLocation().getURL();
		System.out.println("Workspace: " + url);
	}

}
