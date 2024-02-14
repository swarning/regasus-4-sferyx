package de.regasus.anonymize.dialog;

import static de.regasus.LookupService.getAnonymizeMgr;

import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantCorrespondenceModel;
import de.regasus.participant.ParticipantFileModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.dialog.ParticipantSelectionWizardPage;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.dialog.ProfileSelectionWizardPage;
import de.regasus.ui.Activator;

public class AnonymizeWizard extends Wizard {

	// wizard pages
	private ProfileSelectionWizardPage profileSelectionWizardPage;
	private AnonymizeSelectedProfilesAndParticipants1WizardPage selectedProfilesAndParticipants1WizardPage;
	private ParticipantSelectionWizardPage participantSelectionWizardPage;
	private AnonymizeSelectedProfilesAndParticipants2WizardPage selectedProfilesAndParticipants2WizardPage;


	public AnonymizeWizard() {
	}


	@Override
	public void addPages() {
		// Select Profiles
		profileSelectionWizardPage = new ProfileSelectionWizardPage(SelectionMode.MULTI_OPTIONAL_SELECTION);
		profileSelectionWizardPage.setDescription(I18N.AnonymizeWizard_ProfileSelectionWizardPage_Description);
		addPage(profileSelectionWizardPage);

		// Show selected Profiles and associated Participants
		selectedProfilesAndParticipants1WizardPage = new AnonymizeSelectedProfilesAndParticipants1WizardPage();
		addPage(selectedProfilesAndParticipants1WizardPage);

		// Select Participants
		participantSelectionWizardPage = new AnonymizeParticipantSelectionWizardPage();
		addPage(participantSelectionWizardPage);

		// Show selected Profiles and Participants incl. associated Profiles and Participants
		selectedProfilesAndParticipants2WizardPage = new AnonymizeSelectedProfilesAndParticipants2WizardPage();
		addPage(selectedProfilesAndParticipants2WizardPage);
	}


	@Override
	public boolean performFinish() {
		List<Long> profilePKs = selectedProfilesAndParticipants2WizardPage.getCheckedProfilePKs();
		List<Long> participantPKs = selectedProfilesAndParticipants2WizardPage.getCheckedParticipantPKs();

		try {
			MessageDialog messageDialog = new MessageDialog(
	            getShell(),
	            I18N.AnonymizeWizard_PrintDocumentDialogTitle,
	            null,								// dialogTitleImage
	            I18N.AnonymizeWizard_PrintDocumentDialogMessage,
	            MessageDialog.QUESTION_WITH_CANCEL,	// dialogImageType
	            new String[]{						// dialogButtonLabels
	                IDialogConstants.YES_LABEL,
	                IDialogConstants.NO_LABEL,
	                IDialogConstants.CANCEL_LABEL
	            },
	            0
			);
			int dialogResult = messageDialog.open();

			if (dialogResult == 0) {
				// yes: print Anonymize Document
				printAnonymizeDocument();
			}
			else if (dialogResult == 1) {
				// no: don't print Anonymize Document
			}
			else {
				// cancel: don't print and don't anonymize
				return false;
			}


			getAnonymizeMgr().anonymize(profilePKs, participantPKs);

			// refresh models
			ProfileModel.getInstance().refresh(profilePKs);
			// there is no ProfileCorrespondenceModel, because the Correspondence is part of Profile
			// even there is a ProfileFileModel, we don't need to refresh it, because Profile get deleted and their editors closed

			ParticipantModel.getInstance().refresh(participantPKs);
			// ParticipantHistoryModel refreshes itself automatically, cause is is observing ParticipantModel
			ParticipantCorrespondenceModel.getInstance().refreshForeignKeys(participantPKs);
			ParticipantFileModel.getInstance().refreshForeignKeys(participantPKs);

			return true;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return false;
		}
	}


	public void printAnonymizeDocument() {
		List<Long> profilePKs = selectedProfilesAndParticipants2WizardPage.getCheckedProfilePKs();
		List<Long> participantPKs = selectedProfilesAndParticipants2WizardPage.getCheckedParticipantPKs();

		try {
			DocumentContainer anonymizeDocument = getAnonymizeMgr().getAnonymizeDocument(
				profilePKs,
				participantPKs,
				Locale.getDefault().getLanguage()
			);
			anonymizeDocument.print();
			// call anonymizeDocument.open() to open the document during debug
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Point getPreferredSize() {
		return new Point(1024 - 100, 768 - 100);
	}

}
