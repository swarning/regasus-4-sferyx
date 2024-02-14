package de.regasus.impex.scanndy.dialog;

import static com.lambdalogic.util.CollectionsHelper.empty;
import static de.regasus.LookupService.getLeadMgr;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.LeadDirection;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.dialog.EventWizardPage;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;
import de.regasus.programme.programmepoint.dialog.ProgrammePointWizardPage;

public class ScanndyDataImportWizard extends Wizard {

	private static Long eventPK;

	public static Long programmePointPK;



	private EventWizardPage eventPage;
	private ProgrammePointWizardPage programmePointPage;
	private ScanndyDataImportFileWizardPage filePage;


	@Override
	public void addPages() {
		setWindowTitle(ImpexI18N.ScanndyDataImportWizard_Title);

		eventPage = new EventWizardPage();
		eventPage.setTitle( ParticipantLabel.Event.getString() );
		eventPage.setDescription(ImpexI18N.ScanndyDataImportWizard_eventPageDecription);
		eventPage.setInitiallySelectedEventPK(eventPK);

		eventPage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setEventPK( eventPage.getEventId() );
			}
		});


		programmePointPage = new ProgrammePointWizardPage(
			ParticipantLabel.ProgrammePoint.getString(), // title
			ImpexI18N.ScanndyDataImportWizard_programmePointPageDecription, // description
			eventPK,// eventPK
			null, 	// initialProgrammePointPKs
			false,	// multiSelection
			false	// allowNoSelection
		);
		programmePointPage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ProgrammePointVO programmePointVO = programmePointPage.getProgrammePointVO();
				if (programmePointVO != null) {
					programmePointPK = programmePointVO.getID();
				}
				else {
					programmePointPK = null;
				}
			}
		});


		filePage = new ScanndyDataImportFileWizardPage(ScanndyDataImportWizard.class);

		addPage(eventPage);
		addPage(programmePointPage);
		addPage(filePage);
	}

	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return
			currentPage == filePage &&
			filePage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		boolean success = false;


		try {
			File file = filePage.getFile();
			byte[] scanndyData = FileHelper.readFile(file);

			String direction = filePage.getDirection();
			List<String> errorLines = getLeadMgr().importScanndyData(
				scanndyData,
				programmePointPK,
				LeadDirection.getDirection(direction),
				true,	// createEivTeilnehmer
				true	// createLead
			);

			success = empty(errorLines);

			if (success) {
				MessageDialog.openInformation(
					getShell(),
					UtilI18N.Info,
					ImpexI18N.ScanndyDataImportWizard_SuccessMsg
				);
			}
			else {
				// create the error file name
				String dirName = file.getParent();
				String dataFileName = file.getName();

				// letzten Punkt im Dateinamen suchen
				int lastDot = dataFileName.lastIndexOf(".");
				String errorFileName = dataFileName.substring(0, lastDot) + ".errors" + dataFileName.substring(lastDot);
				File errorFile = new File(dirName, errorFileName);

				FileHelper.writeStringCollectionAsFileLines(errorLines, errorFile);

				String msg = ImpexI18N.ScanndyDataImportWizard_ErrorMsg;
				msg = msg.replaceFirst("<ErrorFileName>", errorFileName);

				MessageDialog.openError(
					getShell(),
					UtilI18N.ErrorInfo,
					msg
				);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return success;
	}


	public static Long getEventPK() {
		return eventPK;
	}


	public void setEventPK(Long eventPK) {
		ScanndyDataImportWizard.eventPK = eventPK;
		if (programmePointPage != null) {
			programmePointPage.setEventPK(eventPK);
		}
	}

}
