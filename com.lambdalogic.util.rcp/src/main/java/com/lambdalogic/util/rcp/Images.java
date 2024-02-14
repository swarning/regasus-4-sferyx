package com.lambdalogic.util.rcp;

import org.eclipse.swt.graphics.Image;

/**
 * All constants (must) belong to an image file in the icons directory.
 * Via reflection, the files are loaded when the ImageRegistry is accessed the 
 * first time. Access to the images is via e.g. <code>Images.get(Images.SUM)</code>
 *  
 * @author manfred
 *
 */
public class Images {

	public final static String UNKNOWN = "Unknown.png";

	public final static String UNKNOWN_DEFAULT = "UnknownDefault.png";

	public final static String KNOWN = "Known.png";

	public final static String LANGUAGES = "Languages.png";
	
	public final static String SUM = "sum.png";
	
	public final static String EURO = "euro.png";
	
	public final static String CALENDAR = "calendar.png";
	
	public final static String CLOCK = "clock.png";
	
	public final static String OK = "tick.png";
	
	public final static String CANCEL = "cross.png";
	
	public static Image get(String name) {
		return Activator.getDefault().getImageRegistry().get(name);
	}

}
