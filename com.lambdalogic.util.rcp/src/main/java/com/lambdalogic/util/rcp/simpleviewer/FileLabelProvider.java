package com.lambdalogic.util.rcp.simpleviewer;

import java.io.File;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.Activator;

/**
 * A label provider to be used in simple one-column {@link TableViewer}s that shall show a list of files with their
 * respective program icon.
 * 
 * @author manfred
 * 
 */
public class FileLabelProvider extends LabelProvider {

	/**
	 * Returns the name for file elements, or the default text for others.
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof File) {
			return ((File) element).getName();
		}
		return super.getText(element);
	}


	/**
	 * Returns an image based on the extension of the file, obtained from associated {@link Program}, cached in an
	 * {@link ImageRegistry} so that we don't obtain too many images and also don't have to care for disposal.
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof File) {
			String name = ((File) element).getName();
			
			String extension = FileHelper.getExtension(name);
			if (extension != null){
				return Activator.getDefault().findImageForExtension(extension);
			}
		}
		return null;
	}
}
