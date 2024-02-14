package de.regasus.report;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.report.ui.Activator;



public class IconRegistry {

	
	public static ImageRegistry imageRegistry;

	public static Image getImage(String key) {

		if (imageRegistry == null) {
			SWTHelper.syncExecDisplayThread(new Runnable(){
				public void run() {
					imageRegistry = JFaceResources.getImageRegistry();					
				}});
		}
		
		Image image = imageRegistry.get(key);
		if (image == null) {
			imageRegistry.put(key, Activator.getImageDescriptor(key));
			image = imageRegistry.get(key);
			if (image == null) System.err.println("Kein Image für Key " + key + " in Plugin " + Activator.PLUGIN_ID);
		}
		return image;
	}
}
