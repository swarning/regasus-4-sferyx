package de.regasus.participant.dialog;

import static de.regasus.LookupService.getNoteMgr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.report.od.DocumentFormat;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

/**
 * Performs the actual printing of "Benachrichtigungen" as required by
 * https://mi2.lambdalogic.de/jira/browse/MIRCP-142.
 *
 * We loop through all participants and make individual server calls, althugh there is the
 * call overhead, but the benefit is that we
 * <ul>
 * <li>avoid out of memory exceptions on the server, and</li>
 * <li>allow to follow the progress (what participant's notifications are currently processed) and the cancellation<li>
 * </ul>
 *
 * @author manfred
 */
public class PrintNotificationsJob extends Job {

	private List<? extends IParticipant> participants;

	private List<Long> templatePKs;

	private DocumentFormat format;

	private boolean shouldPrint;

	private String printerName;

	private boolean markHotelPointBookingsAsConfirmed;

	private boolean markProgramPointBookingsAsConfirmed;


	public PrintNotificationsJob(
		List<? extends IParticipant> participants,
		List<Long> templatePKs,
		DocumentFormat format,
		boolean shouldPrint,
		String printerName,
		boolean markHotelPointBookingsAsConfirmed,
		boolean markProgramPointBookingsAsConfirmed
	) {
		super(I18N.PrintNotifications);

		this.participants = participants;
		this.templatePKs = templatePKs;
		this.format = format;
		this.shouldPrint = shouldPrint;
		this.printerName = printerName;
		this.markHotelPointBookingsAsConfirmed = markHotelPointBookingsAsConfirmed;
		this.markProgramPointBookingsAsConfirmed = markProgramPointBookingsAsConfirmed;
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (shouldPrint) {
			monitor.beginTask(I18N.PrintNotifications, participants.size());

			for (IParticipant participant : participants) {
				List<Long> oneRecipientPK = new ArrayList<>();
				oneRecipientPK.add(participant.getPK());

				monitor.subTask(participant.getName());

				try {
					List<DocumentContainer> documentList = getNoteMgr().getNoteDocument(
						oneRecipientPK,
						templatePKs,
						format.getFormatKey()
					);

					if (CollectionsHelper.notEmpty(documentList)) {
						for (DocumentContainer documentContainer : documentList) {
							/* save and open generated notification file
							 * This code is referenced by
							 * https://lambdalogic.atlassian.net/wiki/pages/createpage.action?spaceKey=REGASUS&fromPageId=21987353
							 * Adapt the wiki document if this code is moved to another class or method.
							 */
							documentContainer.print(printerName);
						}

						if (markHotelPointBookingsAsConfirmed || markProgramPointBookingsAsConfirmed) {
							ParticipantModel.getInstance().updateNoteTime(
								new Date(),
								oneRecipientPK,
								markProgramPointBookingsAsConfirmed,
								markHotelPointBookingsAsConfirmed
							);
						}

					}
					else {
						SWTHelper.showInfoDialog(UtilI18N.Info, I18N.NoRelevantNotifications);
					}

					monitor.worked(1);

					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
		else {
			monitor.beginTask(I18N.PrintNotifications, 3);

			// extract participantPKs
			List<Long> participantPKs = Participant.getIParticipantPKs(participants);

			try {
				List<DocumentContainer> documentList = getNoteMgr().getNoteDocument(
					participantPKs,
					templatePKs,
					format.getFormatKey()
				);

				monitor.worked(1);

				if (CollectionsHelper.notEmpty(documentList)) {

					for (DocumentContainer documentContainer : documentList) {
						/* save and open generated notification file
						 * This code is referenced by
						 * https://lambdalogic.atlassian.net/wiki/pages/createpage.action?spaceKey=REGASUS&fromPageId=21987353
						 * Adapt the wiki document if this code is moved to another class or method.
						 */
						documentContainer.open();
					}

					monitor.worked(1);

					if (markHotelPointBookingsAsConfirmed || markProgramPointBookingsAsConfirmed) {
						ParticipantModel.getInstance().updateNoteTime(
							new Date(),
							participantPKs,
							markProgramPointBookingsAsConfirmed,
							markHotelPointBookingsAsConfirmed
						);
					}
				}
				else {
					SWTHelper.showInfoDialog(UtilI18N.Info, I18N.NoRelevantNotifications);
				}
				monitor.worked(1);

				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return Status.OK_STATUS;
	}
}
