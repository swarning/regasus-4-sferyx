package de.regasus.impex.eivfobi.dialog;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;

import de.regasus.event.dialog.EventWizardPage;
import de.regasus.impex.ImpexI18N;
import de.regasus.programme.programmepoint.dialog.ProgrammePointWizardPage;


public class EIVFoBiExportWizard extends Wizard {

	private Long eventPK;
	private Long programmePointPK;
	private Collection<Long> moveOffProgrammePointPKs;
	private File exportFile;
	private String email;
	private boolean onlyNotExported;
	private boolean markTransmitted;


	private EventWizardPage eventPage;
	private ProgrammePointWizardPage programmePointPage;
	private ProgrammePointWizardPage moveOffProgrammePointPage;
	private EIVFoBiExportWizardPage eivFoBiExportWizardPage;


	@Override
	public void addPages() {
		setWindowTitle(ImpexI18N.EIVFoBiExportWizard_Title);

		eventPage = new EventWizardPage();
		eventPage.setTitle( ParticipantLabel.Event.getString() );
		eventPage.setDescription(ImpexI18N.EIVFoBiExportWizard_eventPageDecription);
		eventPage.setInitiallySelectedEventPK(eventPK);

		eventPage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setEventPK( eventPage.getEventId() );
			}
		});


		programmePointPage = new ProgrammePointWizardPage(
			ParticipantLabel.ProgrammePoint.getString(), // title
			ImpexI18N.EIVFoBiExport_programmePointPageDecription, // description
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


		moveOffProgrammePointPage = new ProgrammePointWizardPage(
			ImpexI18N.EIVFoBiExport_moveOffProgrammePointPageTitle, // title
			ImpexI18N.EIVFoBiExport_moveOffProgrammePointPageDecription, // description
			eventPK,// eventPK
			null, 	// initialProgrammePointPKs
			true,	// multiSelection
			true	// allowNoSelection
		);
		moveOffProgrammePointPage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				List<ProgrammePointVO> moveOffProgrammePointVOs = moveOffProgrammePointPage.getProgrammePointVOs();
				if (moveOffProgrammePointVOs != null && !moveOffProgrammePointVOs.isEmpty()) {
					moveOffProgrammePointPKs = ProgrammePointVO.getPKs(moveOffProgrammePointVOs);
				}
				else {
					moveOffProgrammePointPKs = null;
				}
			}
		});


		eivFoBiExportWizardPage = new EIVFoBiExportWizardPage();

		addPage(eventPage);
		addPage(programmePointPage);
		addPage(moveOffProgrammePointPage);
		addPage(eivFoBiExportWizardPage);
	}


	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return
			currentPage == eivFoBiExportWizardPage &&
			eivFoBiExportWizardPage.isPageComplete();
	}


	@Override
	public boolean performFinish() {
		exportFile = eivFoBiExportWizardPage.getFile();
		email = eivFoBiExportWizardPage.getEmail();
		onlyNotExported = eivFoBiExportWizardPage.isOnlyNotExported();
		markTransmitted = eivFoBiExportWizardPage.isMarkTransmitted();

		return true;
	}


	public Long getEventPK() {
		return eventPK;
	}


	public void setEventPK(Long eventPK) {
		this.eventPK = eventPK;
		if (programmePointPage != null) {
			programmePointPage.setEventPK(eventPK);
		}
		if (moveOffProgrammePointPage != null) {
			moveOffProgrammePointPage.setEventPK(eventPK);
		}
	}


	public Long getProgrammePointPK() {
		return programmePointPK;
	}


	public Collection<Long> getMoveOffProgrammePointPKs() {
		return moveOffProgrammePointPKs;
	}


	public File getExportFile() {
		return exportFile;
	}


	public String getEmail() {
		return email;
	}


	public boolean isOnlyNotExported() {
		return onlyNotExported;
	}


	public boolean isMarkTransmitted() {
		return markTransmitted;
	}

}
