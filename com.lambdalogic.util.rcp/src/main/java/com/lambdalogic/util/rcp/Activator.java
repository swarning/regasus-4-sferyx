package com.lambdalogic.util.rcp;

import java.lang.reflect.Field;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.lambdalogic.util.SystemHelper;

/**
 * The activator class controls the plug-in life cycle. At start up, the font registry is initialized.
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.lambdalogic.util.rcp";

	public static final String DEFAULT_FONT_BOLD = "DEFAULT_FONT_BOLD";

	public static final String DEFAULT_FONT_BOLD_UNDER = "DEFAULT_FONT_BOLD_UNDER";

	public static final String BIG_FONT = "BIG_FONT";

	public static final String BARCODE_FONT = "BARCODE_FONT";

	public static final String SOURCE_CODE_FONT = "SOURCE_CODE_FONT";


	// The shared instance
	private static Activator plugin;

	private boolean isFontRegistryInitialized = false;

	/**
	 * The constructor
	 */
	public Activator() {
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Trigger preference initialization via extension point org.eclipse.core.runtime.preferences
		// See also: https://lambdalogic.atlassian.net/wiki/x/AwATsg
		getPreferenceStore().getString("dummy");
	}


	/*
	 * (non-Javadoc)
	 *
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
	 * The first time someone asks for the image registry, this initialization method is called by the superclass.
	 *
	 * Loading and Access to the images happens through the fields in the Images-Class, whose names must correspond to
	 * an existing icon file.
	 */
	@Override
	public void initializeImageRegistry(ImageRegistry imageRegistry) {

		Field[] declaredFields = Images.class.getDeclaredFields();
		for (Field field : declaredFields) {
			try {
				String value = (String) field.get(null);
				ImageDescriptor id = Activator.imageDescriptorFromPlugin(PLUGIN_ID, "icons/" + value);
				imageRegistry.put(value, id);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}

		}
	}


	public Font getFontFromRegistry(String fontKey) {
		if (! isFontRegistryInitialized) {
			initializeFontRegistry();
			isFontRegistryInitialized = true;
		}
		return JFaceResources.getFontRegistry().get(fontKey);
	}



	public Image findImageForExtension(String extension) {
		// If image already in Registry, return it
		ImageRegistry imageRegistry = getImageRegistry();
		Image image = imageRegistry.get(extension);
		if (image != null) {
			return image;
		}

		// Find program (and image data) based on extension
		Program program = Program.findProgram(extension);
		ImageData imageData = (program == null ? null : program.getImageData());
		if (imageData != null) {
			// If image data is present, create image and store in registry
			image = new Image(Display.getDefault(), imageData);
			imageRegistry.put(extension, image);
		}
		return image;
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


	/*
	 * ================================================================================================================
	 * Private helper methods
	 */

	/**
	 * Puts bold and underlined fonts in the FontRegistry, e.g. for use in the GridTreeViewer.
	 */
	private void initializeFontRegistry() {

		// Get Standard JFace-FontResources
		FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		FontData[] defaultFontData = fontRegistry.getFontData(JFaceResources.DEFAULT_FONT);


		// Build a Bold font
		FontData[] boldFontData = copy(defaultFontData);
		for (FontData fontData : boldFontData) {
			fontData.setStyle(SWT.BOLD);
		}
		fontRegistry.put(DEFAULT_FONT_BOLD, boldFontData);


		// Build a bold underlined font (may not work on some OS)
		FontData[] sumFontData = copy(defaultFontData);
		for (FontData fontData : sumFontData) {
			fontData.setStyle(SWT.BOLD);

			// Since the attribute fontData.data.lfUnderline may not be available, use reflection to set it to 1
			if (SystemHelper.isWindows()) {
				try {
					Field dataField = FontData.class.getField("data");
					Object dataObject = dataField.get(fontData);
					Field underlineField = dataField.getType().getField("lfUnderline");
					underlineField.set(dataObject, Byte.valueOf((byte)1));
				}
				catch (Throwable t) {
					System.err.println("Could not define underlined font");
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(t);
				}
			}
		}
		fontRegistry.put(DEFAULT_FONT_BOLD_UNDER, sumFontData);


		// Build a Big font
		FontData[] bigFontData = copy(defaultFontData);
		for (FontData fontData : bigFontData) {
			int height = fontData.getHeight();
			fontData.setHeight(height + 3);
			fontData.setStyle(SWT.BOLD);
		}
		fontRegistry.put(BIG_FONT, bigFontData);

		// Build a Barcode font
		FontData barcodeFontData = new FontData("CODE-128-EH", 24, SWT.NORMAL);
		fontRegistry.put(BARCODE_FONT, new FontData[] { barcodeFontData });


		// Build font for XML code
		FontData xmlFontData = new FontData("Courier New", 12, SWT.NORMAL);
		fontRegistry.put(SOURCE_CODE_FONT, new FontData[] { xmlFontData });
	}


	private FontData[] copy(FontData[] fd) {
		FontData[] copiedFd = new FontData[fd.length];
		for (int i = 0; i < fd.length; i++) {
			copiedFd[i] = new FontData(fd[i].getName(), fd[i].getHeight(), fd[i].getStyle());
		}
		return copiedFd;
	}

}
