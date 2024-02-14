package de.regasus.email.template.dialog;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.interfaces.IEmailTemplateManager;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailI18N;
import de.regasus.email.EmailTemplateModel;
import de.regasus.event.dialog.EventWizardPage;
import de.regasus.ui.Activator;


/**
 * Wizard used to copy an Email Template.
 *
 * This class contains also the business logic to copy the Email Template though there is the according
 * method {@link IEmailTemplateManager#copyEmailTemplate(Long, Long)}.
 */
public class CopyEmailTemplateOtherEventWizard extends Wizard {

	DuplicationModePage duplicationModePage;

	EventWizardPage targetEventWizardPage;

	List<EmailTemplate> emailTemplateList;


	// **************************************************************************
	// * Constructors
	// *

	public CopyEmailTemplateOtherEventWizard(List<EmailTemplate> emailTemplateList) {
		this.emailTemplateList = emailTemplateList;
	}


	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void addPages() {
		Long eventPK = emailTemplateList.get(0).getEventPK();
		boolean templateHasEvent = (eventPK != null);

		duplicationModePage = new DuplicationModePage(templateHasEvent);
		addPage(duplicationModePage);

		try {
			targetEventWizardPage = new EventWizardPage();
			targetEventWizardPage.setDescription(EmailI18N.SelectEventToCopyEmailTemplateInto);

			// In case the email template belongs to an event, don't offer it for selection
			if (templateHasEvent) {
				targetEventWizardPage.setHiddenEventPKs( createArrayList(eventPK) );
			}

			addPage(targetEventWizardPage);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public boolean performFinish() {
		CopyEmailTemplateListRunnable copyEmailTemplateListRunnable = new CopyEmailTemplateListRunnable();
		Shell shell = getShell();
		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(shell);
		try {
			progressMonitorDialog.run(
				false,	// fork
				true,	// cancelable
				copyEmailTemplateListRunnable
			);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return true;
	}


	@Override
	public String getWindowTitle() {
		return UtilI18N.CopyVerb;
	}


	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage == duplicationModePage) {
			DuplicationMode mode = duplicationModePage.getDuplicateMode();
			return mode == DuplicationMode.SAME_EVENT || mode == DuplicationMode.NO_EVENT;
		}
		else if (currentPage == targetEventWizardPage) {
			return targetEventWizardPage.isPageComplete();
		}

		return false;
	}



	class CopyEmailTemplateListRunnable implements IRunnableWithProgress {

		@Override
		public void run(IProgressMonitor monitor) {
			// Prepare duplicate
			monitor.beginTask(UtilI18N.CopyVerb, emailTemplateList.size());
			try {
				EmailTemplateModel emailTemplateModel = EmailTemplateModel.getInstance();

				for (EmailTemplate emailTemplate : emailTemplateList) {

					Long targetEventPK = null;
					switch ( duplicationModePage.getDuplicateMode() ) {
						case NO_EVENT:
							break;
						case SAME_EVENT:
							targetEventPK = emailTemplate.getEventPK();
							break;
						case OTHER_EVENT:
							targetEventPK = targetEventWizardPage.getEventId();
							break;
					}

					emailTemplateModel.copyEmailTemplate(emailTemplate.getID(), targetEventPK);

					if (monitor.isCanceled()) {
						monitor.done();
						break;
					}
					monitor.worked(1);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
			monitor.done();
		}
	}

}
