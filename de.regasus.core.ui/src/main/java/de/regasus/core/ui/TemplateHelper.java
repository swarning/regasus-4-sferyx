package de.regasus.core.ui;

import static de.regasus.LookupService.*;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;


public class TemplateHelper {

	public static File editTemplate(de.regasus.common.File template) {
		// check if path to OpenOffice executable is configured
		File tmpFile = null;
		if ( FileHelper.isValidOpenOfficePath() ) {
			try {
				/* Load DataStoreVO with content by PK.
				 * Direct access is fine here, because:
				 * - no data is changed
				 * - the requested data is not in any cache
				 */

				String fileName = template.getExternalFileName();

				// create tmp directory
				String baseFileName = FileHelper.getNameWithoutExtension(fileName);
				File tmpDir = FileHelper.createTempDirectory(baseFileName);
				tmpDir.deleteOnExit();

				// reload File with content from server
				template = getFileMgr().read( template.getId() );
				if (template != null) {
    				// save content to File with original name in tmp directory
    				tmpFile = FileHelper.saveTo(tmpDir, fileName, template.getContent());

    				FileHelper.open(tmpFile);
				}
				else {
					String msg = CoreI18N.FileNotFound_Message.replace("<fileName>", fileName);
					MessageDialog.openError(null, CoreI18N.FileNotFound_Title, msg);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, TemplateHelper.class.getName(), e);
			}
		}
		else {
			MessageDialog.openWarning(
				Display.getDefault().getActiveShell(),
				UtilI18N.Warning,
				de.regasus.core.ui.CoreI18N.OpenOfficePreference_NoProperPathConfiguration
			);
		}

		return tmpFile;
	}


	public static File downloadTemplate(de.regasus.common.File template) {
		File targetFile = null;

		// Make sure extFileName ends with extension
		File externalPath = new File( template.getExternalPath() );
		File dir = externalPath.getParentFile();

		// Open Dialog with originalFile's name and path (if exists)
		FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
		if (dir != null && dir.exists()) {
			fileDialog.setFilterPath( dir.getPath() );
		}
		fileDialog.setFileName( template.getExternalFileName() );

		final String targetPath = fileDialog.open();

		// If dialog was not cancelled, fetch contents from server and save in file
		if (targetPath != null) {
			try {
				// save content to File with original name in tmp directory
				targetFile = new File(targetPath);

				// reload File with content from server
				template = getFileMgr().read( template.getId() );
				if (template != null) {
    				// save content to File with original name in tmp directory
    				FileHelper.saveTo(targetFile, template.getContent());
				}
				else {
					String msg = CoreI18N.FileNotFound_Message.replace("<fileName>", targetFile.getName());
					MessageDialog.openError(null, CoreI18N.FileNotFound_Title, msg);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, TemplateHelper.class.getName(), e);
			}
		}

		return targetFile;
	}


	@Deprecated
	public static File editTemplate(DataStoreVO dataStoreVO) {
		// check if path to OpenOffice executable is configured
		File file = null;
		if ( FileHelper.isValidOpenOfficePath() ) {
			try {
				/* Load DataStoreVO with content by PK.
				 * Direct access is fine here, because:
				 * - no data is changed
				 * - the requested data is not in any cache
				 */
				DataStoreVO contentDataStoreVO = getDataStoreMgr().getDataStoreVO(dataStoreVO.getID(), true);
				byte[] content = contentDataStoreVO.getContentUncompressed();

				String extFileName = dataStoreVO.getExtFileName();

				// create tmp directory
				String baseFileName = FileHelper.getNameWithoutExtension(extFileName);
				File tmpDir = FileHelper.createTempDirectory(baseFileName);
				tmpDir.deleteOnExit();

				// save content to File with original name in tmp directory
				String fileName = FileHelper.getName(extFileName);
				file = FileHelper.saveTo(tmpDir, fileName, content);

				FileHelper.open(file);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, TemplateHelper.class.getName(), e);
			}
		}
		else {
			MessageDialog.openWarning(
				Display.getDefault().getActiveShell(),
				UtilI18N.Warning,
				de.regasus.core.ui.CoreI18N.OpenOfficePreference_NoProperPathConfiguration
			);
		}

		return file;
	}


	@Deprecated
	public static void downloadTemplate(final DataStoreVO dataStoreVO) {
		// Make sure extFileName ends with extension
		String extFileName = dataStoreVO.getExtFileName();
		String extension = dataStoreVO.getExtension();
		if (!extFileName.endsWith(extension) && extension != null) {
			extFileName += "." + extension;
		}
		File originalFile = new File(extFileName);

		// Open Dialog with originalFile's name and path (if exists)
		FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
		File dir = originalFile.getParentFile();
		if (dir != null && dir.exists()) {
			fileDialog.setFilterPath(dir.getPath());
		}
		fileDialog.setFileName(originalFile.getName());
		final String saveFileName = fileDialog.open();

		// If dialog was not cancelled, fetch contents from server and save in file
		if (saveFileName != null) {

			try {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {
						try {
							/* Load DataStoreVO with content by PK.
							 * Direct access is fine here, because:
							 * - no data is changed
							 * - the requested data is not in any cache
							 */
							DataStoreVO contentDataStoreVO = getDataStoreMgr().getDataStoreVO(dataStoreVO.getID(), true);

							FileHelper.writeFile(new File(saveFileName), contentDataStoreVO.getContentUncompressed());
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}

					}
				});
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, TemplateHelper.class.getName(), e);
			}

		}
	}

}
