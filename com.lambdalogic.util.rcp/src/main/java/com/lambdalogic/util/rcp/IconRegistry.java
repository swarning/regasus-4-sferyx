package com.lambdalogic.util.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

public class IconRegistry {

	public static ImageRegistry imageRegistry = JFaceResources.getImageRegistry();


	public static Image getImage(String key) {
		Image image = imageRegistry.get(key);
		if (image == null) {
			imageRegistry.put(key, Activator.getImageDescriptor(key));
			image = imageRegistry.get(key);
		}
		return image;
	}

	public static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor descriptor = imageRegistry.getDescriptor(key);
		if (descriptor == null) {
			getImage(key);
			return imageRegistry.getDescriptor(key);
		} else {
			return descriptor;
		}
	}


	public static Image getLanguageIcon(String language) {
		Image image = IconRegistry.getImage("/icons/lang/" + language + ".png");
		return image;
	}
}
