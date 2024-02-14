package de.regasus;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

import de.regasus.ui.Activator;


public class IconRegistry {

	public static ImageRegistry imageRegistry = JFaceResources.getImageRegistry();


	public static Image getImage(String key) {
		Image image = imageRegistry.get(key);
		if (image == null) {
			imageRegistry.put(key, Activator.getImageDescriptor(key));
			image = imageRegistry.get(key);
			if (image == null) {
				System.err.println("No image for key " + key + " in Plugin " + Activator.PLUGIN_ID);
			}
		}
		return image;
	}


	public static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor descriptor = imageRegistry.getDescriptor(key);
		if (descriptor == null) {
			getImage(key);
			return imageRegistry.getDescriptor(key);
		}
		else {
			return descriptor;
		}
	}

}
