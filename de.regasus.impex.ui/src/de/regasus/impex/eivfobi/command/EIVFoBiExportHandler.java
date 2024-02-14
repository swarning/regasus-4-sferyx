package de.regasus.impex.eivfobi.command;

import static de.regasus.LookupService.*;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.CustomWizardDialog;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.error.ErrorHandler.ErrorLevel;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.EventSelectionHelper;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.eivfobi.dialog.EIVFoBiExportWizard;
import de.regasus.impex.ui.Activator;

public class EIVFoBiExportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);

		if (PlatformUI.getWorkbench().saveAllEditors(true)) {
			try {
				EIVFoBiExportWizard wizard = new EIVFoBiExportWizard();

				// init wizard with selected event
				Long eventID = EventSelectionHelper.getEventID(event);
				if (eventID != null) {
					wizard.setEventPK(eventID);

					CustomWizardDialog dialog = new CustomWizardDialog(shell, wizard);
					dialog.setFinishButtonText(UtilI18N.Save);

					int returnCode = dialog.open();
					if (returnCode == CustomWizardDialog.OK) {
						try {
							File file = wizard.getExportFile();

							String exportData = getEventMgr().exportEivTeilnehmer(
								wizard.getProgrammePointPK(),
								wizard.getMoveOffProgrammePointPKs(),
								wizard.getEmail(),
								wizard.isOnlyNotExported(),
								wizard.isMarkTransmitted()
							);

							if (StringHelper.isNotEmpty(exportData)) {
								FileHelper.writeFile(file, exportData.getBytes());

								String msg = ImpexI18N.EIVFoBiExportWizard_FinalMsg;
								msg = msg.replaceFirst("<file>", file.getAbsolutePath());

								MessageDialog.openInformation(
									shell,
									UtilI18N.Info,
									msg
								);
							}
							else {
								MessageDialog.openInformation(
									shell,
									UtilI18N.Info,
									ImpexI18N.EIVFoBiExportWizard_NoDataMsg
								);
							}
						}
						catch (ErrorMessageException e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e, ErrorLevel.USER);
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}
					}
					else {
						System.err.println("EIVFoBiExportHandler terminated, because it requires that exactly 1 Event has been selected.");
					}
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		return null;
	}

}
