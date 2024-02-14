package de.regasus.impex.eivfobi.command;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static de.regasus.LookupService.*;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.EIVTeilnehmer;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.participant.data.ParticipantVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.CustomWizardDialog;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.error.ErrorHandler.ErrorLevel;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.EventSelectionHelper;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.eivfobi.dialog.EIVFoBiCreateWizard;
import de.regasus.impex.ui.Activator;


public class EIVFoBiCreateHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		if (PlatformUI.getWorkbench().saveAllEditors(true)) {
			try {
				EIVFoBiCreateWizard wizard = new EIVFoBiCreateWizard();

				// init wizard with selected event
				Long eventID = EventSelectionHelper.getEventID(event);
				if (eventID != null) {
					wizard.setEventPK(eventID);

					CustomWizardDialog dialog = new CustomWizardDialog(shell, wizard);
					dialog.setFinishButtonText(UtilI18N.Create);
					int returnCode = dialog.open();

					if (returnCode == CustomWizardDialog.OK) {
						List<ParticipantSearchData> selectedParticipants = wizard.getSelectedParticipants();
						Long programmePointPK = wizard.getProgrammePointPK();
						createEviFobi(shell, selectedParticipants, programmePointPK);
					}
				}
				else {
					System.err.println("EIVFoBiCreateHandler terminated, because it requires that exactly 1 Event has been selected.");
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		return null;
	}


	private void createEviFobi(Shell shell, List<ParticipantSearchData> selectedParticipants, Long programmePointPK) {
		try {
			int createdParticipantCount = 0;

			if (notEmpty(selectedParticipants)) {
				// remove Participants without cmeNumber
				List<Long> pkList = ParticipantSearchData.getPKs(selectedParticipants);
				List<ParticipantVO> participantVOs = getParticipantMgr().getParticipantVOs(pkList);

				for (Iterator<ParticipantVO> it = participantVOs.iterator(); it.hasNext();) {
					ParticipantVO participantVO = it.next();
					if (StringHelper.isEmpty(participantVO.getCmeNo())) {
						it.remove();
					}
				}


				if (notEmpty(participantVOs)) {
					// create EIV for selected participants
					PARTICIPANT_LOOP: for (ParticipantVO participantVO : participantVOs) {
						String cmeNo = participantVO.getCmeNo();

						// skip this step if there is already existing EIV-Fobi with the cmeNo and programmePointPK
						List<EIVTeilnehmer> eivTeilnehmerList = getEIVTeilnehmerDAO().findByCmeNumber(cmeNo, false /*onlyWithoutLead*/);
						if (notEmpty(eivTeilnehmerList)) {
							for (EIVTeilnehmer eivTeilnehmer : eivTeilnehmerList) {
								if (programmePointPK.equals(eivTeilnehmer.getProgrammePointPK())) {
									continue PARTICIPANT_LOOP;
								}
							}
						}

						// build EIVTeilnehmer
						EIVTeilnehmer eivTeilnehmer = new EIVTeilnehmer();
						eivTeilnehmer.setRecorded(new Date());
						eivTeilnehmer.setProgrammePointPK(programmePointPK);
						eivTeilnehmer.setEFN(cmeNo);

						// create EIVTeilnehmer
						getEIVTeilnehmerDAO().create(eivTeilnehmer);
						createdParticipantCount++;
					}
				}
			}

			String message = ImpexI18N.EIVFoBiCreateWizardMsg;
			message = message.replace("<count>", String.valueOf(createdParticipantCount));
			MessageDialog.openInformation(
				shell,
				UtilI18N.Info,
				message
			);
		}
		catch (ErrorMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e, ErrorLevel.USER);
		}
	}

}
