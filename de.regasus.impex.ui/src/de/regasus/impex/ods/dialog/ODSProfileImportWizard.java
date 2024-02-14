package de.regasus.impex.ods.dialog;

import org.eclipse.jface.wizard.IWizardPage;

import com.lambdalogic.messeinfo.impex.ODSProfileImportHelper;
import com.lambdalogic.messeinfo.profile.ProfileLabel;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;

public class ODSProfileImportWizard extends ODSImportWizard {

	public static String language;

	public static String path;

	public static boolean isDubCheckLastname;

	public static boolean isDubCheckFistname;

	public static boolean isDubCheckEmail;

	public static boolean isDubCheckMainCity;

	public static int maxErrors;

	public static String customFieldListValueSeparator;


	private ODSProfileImportWizardPage wizardPage;


	@Override
	public void addPages() {
		setWindowTitle(ImpexI18N.ODSProfileImportWizard_Title);

		wizardPage = new ODSProfileImportWizardPage();

		addPage(wizardPage);
	}


	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return
			currentPage == wizardPage &&
			wizardPage.isPageComplete();
	}


	@Override
	public boolean performFinish() {

		try {
			ODSProfileImportHelper odsProfileImportHelper = new ODSProfileImportHelper(
				path,
				language
			);

			odsProfileImportHelper.setCheckDuplicateLastName(isDubCheckLastname);
			odsProfileImportHelper.setCheckDuplicateFirstName(isDubCheckFistname);
			odsProfileImportHelper.setCheckDuplicateEmail(isDubCheckEmail);
			odsProfileImportHelper.setCheckDuplicateMainCity(isDubCheckMainCity);
			odsProfileImportHelper.setCustomFieldListValueSeparator(customFieldListValueSeparator);

			ODSAbstractPersonImportJob job = new ODSAbstractPersonImportJob(
				odsProfileImportHelper,
				maxErrors,
				getShell(),
				ImpexI18N.ODSProfileImportWizard_Title,
				ProfileLabel.Profiles.getString()
			);

			job.setUser(true);
			job.schedule();


		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}

		return true;
	}

}
