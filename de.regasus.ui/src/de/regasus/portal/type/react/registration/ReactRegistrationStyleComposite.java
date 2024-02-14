package de.regasus.portal.type.react.registration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.ZipHelper;

import de.regasus.portal.type.standard.StandardStyleComposite;


/**
 * Special Composite for ReactGroupStyleProvider.
 * However, it uses sub-Composites from de.regasus.portal.type.standard.
 */
public class ReactRegistrationStyleComposite extends StandardStyleComposite {
	
	public ReactRegistrationStyleComposite(Composite parent) {
		super(parent);
	}

	/**
	 * Copy the resources that are necessary to compile the LESS file to the directory <code>dir</code>.
	 * The resources are stored in <code>less-resources.zip</code> which is contained in the entity-JAR.
	 * @param dir
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Override
	protected void copyLessResources(File dir) throws ClassNotFoundException, IOException {
		log("Copy resources from JAR to " + dir.getAbsolutePath());
		Class<?> styleTemplateClass = pageLayout.getStyleTemplateClass();
		if (styleTemplateClass != null) {
			InputStream inputStream = null;
			if (ReactRegistrationStyleProvider.STANDARD_STYLE.getStyleTemplateFileName().equals(pageLayout.getStyleTemplateFileName())) {
				inputStream = styleTemplateClass.getResourceAsStream(STANDARD_LESS_RESOURCES_FILE_NAME);
			}
			else if (ReactRegistrationStyleProvider.CLASSIC_STYLE.getStyleTemplateFileName().equals(pageLayout.getStyleTemplateFileName())) {
				inputStream = styleTemplateClass.getResourceAsStream(CLASSIC_LESS_RESOURCES_FILE_NAME);
			}
			if (inputStream != null) {
				ZipHelper.unzip(inputStream, dir);
			}
		}
		log("Resources successfully copied");
	}

}
