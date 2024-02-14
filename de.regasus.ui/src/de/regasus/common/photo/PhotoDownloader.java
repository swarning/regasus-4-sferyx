package de.regasus.common.photo;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.CopyNameHelper;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.common.Photo;
import de.regasus.common.PhotoComparator;
import de.regasus.common.PhotoModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class PhotoDownloader {

	private Shell shell;
	private PhotoModel photoModel;
	private List<Photo> photos;


	/**
	 * Current order number.
	 * The file names of exported photos consist of an order number and the original file name.
	 */
	private int orderNumber;

	/**
	 * Length of the order prefix.
	 * The value depends of the total number of photos.
	 */
	private int orderPrefixLength;

	private String dirPath;

	private Set<String> existingNames = new HashSet<>();


	public PhotoDownloader(Shell shell, PhotoModel photoModel) {
		Objects.requireNonNull(shell);
		Objects.requireNonNull(photoModel);

		this.shell = shell;
		this.photoModel = photoModel;

		existingNames.clear();
	}


	public void download(List<Photo> photos) {
		this.photos = photos;


		if ( notEmpty(photos) ) {
			// order photos
			Collections.sort(photos, PhotoComparator.getInstance());

			orderNumber = 1;
			orderPrefixLength = String.valueOf( photos.size() ).length();

			// Open Save-as-Dialog
			DirectoryDialog directoryDialog = new DirectoryDialog(shell, SWT.SAVE);
			dirPath = directoryDialog.open();
			if (dirPath != null) {
				initExistingNames();

				downloadWithProgress();
			}
		}
	}


	private void initExistingNames() {
		File dir = new File(dirPath);
		String[] fileAndDirNames = dir.list();
		for (String name : fileAndDirNames) {
			String baseName = FileHelper.getNameWithoutExtension(name);
			existingNames.add(baseName);
		}
	}


	private void downloadWithProgress() {
		try {
			BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(UtilI18N.Working, photos.size());

					try {
        				for (Photo photo : photos) {
        					download(photo);
        					monitor.worked(1);

        					if ( monitor.isCanceled() ) {
    							return;
    						}
        				}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}

					monitor.done();
				}
			});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void download(Photo photo) throws Exception {
		byte[] content = photoModel.getPhotoContent( photo.getId() );
		String fileName = buildFileName(photo);
		File file = new File(dirPath, fileName);
		FileHelper.writeFile(file, content);
	}


	private StringBuilder sb = new StringBuilder(128);

	private String buildFileName(Photo photo) {

		String fileName = photo.getExternalPath();
		fileName = FileHelper.getName(fileName);

		String baseName = FileHelper.getNameWithoutExtension(fileName);
		String extension = FileHelper.getExtension(fileName);

		// prepend order prefix to baseName
		String orderPrefixStr = String.valueOf(orderNumber++);
		sb.setLength(0);
		while (sb.length() < orderPrefixLength - orderPrefixStr.length()) {
			sb.append('0');
		}
		sb.append(orderPrefixStr);
		sb.append('_');
		sb.append(baseName);
		baseName = sb.toString();


		baseName = CopyNameHelper.suggestNameForCopy(baseName, existingNames);
		existingNames.add(baseName);

		sb.setLength(0);
		fileName = sb.append(baseName).append(".").append(extension).toString();

		return fileName;
	}

}
