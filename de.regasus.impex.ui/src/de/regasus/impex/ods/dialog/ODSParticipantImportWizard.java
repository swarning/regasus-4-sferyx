package de.regasus.impex.ods.dialog;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import com.lambdalogic.messeinfo.impex.ODSParticipantImportHelper;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.dialog.EventWizardPage;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;


public class ODSParticipantImportWizard extends ODSImportWizard {

	private static Long eventPK;

	public static String language;

	public static Long participantStatePK;

	public static Long participantTypePK;

	public static Long groupManagerTypePK;

	public static String path;

	public static boolean isDubCheckLastname;

	public static boolean isDubCheckFistname;

	public static boolean isDubCheckEmail;

	public static boolean isDubCheckMainCity;

	public static String customFieldListValueSeparator;

	public static int maxErrors;


	private EventWizardPage eventPage;
	private ODSParticipantImportWizardPage wizardPage;


	@Override
	public void addPages() {
		setWindowTitle(ImpexI18N.ODSParticipantImportWizard_Title);

		eventPage = new EventWizardPage();
		eventPage.setTitle( ParticipantLabel.Event.getString() );
		eventPage.setDescription(ImpexI18N.ODSImportWizard_eventPageDescription);
		eventPage.setInitiallySelectedEventPK(eventPK);

		eventPage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setEventPK( eventPage.getEventId() );
			}
		});


		wizardPage = new ODSParticipantImportWizardPage();

		addPage(eventPage);
		addPage(wizardPage);
	}


	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return
			currentPage == wizardPage &&
			eventPage.isPageComplete() &&
			wizardPage.isPageComplete();
	}


	@Override
	public boolean performFinish() {

		try {
			ODSParticipantImportHelper odsParticipantImportHelper = new ODSParticipantImportHelper(
				path,
				language,
				getEventPK(),
				participantStatePK,
				participantTypePK,
				groupManagerTypePK
			);

			odsParticipantImportHelper.setCheckDuplicateLastName(isDubCheckLastname);
			odsParticipantImportHelper.setCheckDuplicateFirstName(isDubCheckFistname);
			odsParticipantImportHelper.setCheckDuplicateEmail(isDubCheckEmail);
			odsParticipantImportHelper.setCheckDuplicateMainCity(isDubCheckMainCity);
			odsParticipantImportHelper.setCustomFieldListValueSeparator(customFieldListValueSeparator);

			ODSAbstractPersonImportJob job = new ODSAbstractPersonImportJob(
				odsParticipantImportHelper,
				maxErrors, getShell(),
				ImpexI18N.ODSParticipantImportWizard_Title,
				ParticipantLabel.Participants.getString()
			);

			job.setUser(true);
			job.schedule();
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return true;
	}


	public static Long getEventPK() {
		return eventPK;
	}


	public static void setEventPK(Long eventPK) {
		ODSParticipantImportWizard.eventPK = eventPK;
	}

}
