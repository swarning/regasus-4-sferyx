package de.regasus.onlineform.admin.dialog;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.event.dialog.EventWizardPage;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.onlineform.admin.ui.Activator;
import de.regasus.onlineform.editor.RegistrationFormConfigEditor;
import de.regasus.onlineform.editor.RegistrationFormConfigEditorInput;
import de.regasus.participant.ParticipantTypeModel;

public class CopyRegistrationFormConfigWizard extends Wizard {

	private CopyRegistrationFormWizardOptionsPage optionsPage;

	private EventWizardPage eventPage;

	private RegistrationFormConfig config;

	private RegistrationFormConfig copiedConfig;

	private RegistrationFormConfigModel registrationFormConfigModel = RegistrationFormConfigModel.getInstance();

	private EventModel eventModel = EventModel.getInstance();
	private ParticipantTypeModel participantTypeModel = ParticipantTypeModel.getInstance();


	public CopyRegistrationFormConfigWizard(RegistrationFormConfig config) throws Exception {
		this.config = config;
	}


	@Override
	public void addPages() {
		Long eventPK = config.getEventPK();

		eventPage = new EventWizardPage();
		eventPage.setTitle( ParticipantLabel.Event.getString() );
		eventPage.setDescription( ParticipantLabel.Event.getString());
		eventPage.setInitiallySelectedEventPK(eventPK);


		addPage(eventPage);
		eventPage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				try {
					String eventMnemonic = null;
					Long eventPK = eventPage.getEventId();
					if (eventPK != null) {
						EventVO eventVO = eventModel.getEventVO(eventPK);
						eventMnemonic = eventVO.getMnemonic();
					}
					optionsPage.setWebId(eventMnemonic);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

		optionsPage = new CopyRegistrationFormWizardOptionsPage();
		addPage(optionsPage);

		// MIRCP-2371 - Handle missing Participant Types when copying a Registration Form Config
		// Give to the optionsPage all participant types that are referred to by the config
		Set<Long> referredParticipantTypePKList = CollectionsHelper.createHashSet(20);

		// add default Particiant Type
		Long defaultParticipantTypePK = config.getDefaultParticipantTypePK();
		if (defaultParticipantTypePK != null) {
			referredParticipantTypePKList.add(defaultParticipantTypePK);
		}

		/* Ignore config.getHiddenParticipantTypesList(), because if those are missing, they won't
		 * appear anyhow.
		 */

		optionsPage.setReferredParticipantTypes(referredParticipantTypePKList);
	}


	@Override
	public boolean performFinish() {

		final Long targetEventPK = eventPage.getEventId();
		final String webId = optionsPage.getWebId();
		final boolean copyUploadedFiles = optionsPage.isCopyUploadedFiles();
		final boolean copyEmailTemplates = optionsPage.isCopyEmailTemplates();
		final boolean copyBookingRules = optionsPage.isCopyBookingRules();

		// Handle missing Participant Types when copying a Registration Form Config (MIRCP-2371)
		BusyCursorHelper.busyCursorWhile(new Runnable() {
			@Override
			public void run() {
				try {
					if (optionsPage.isAdditionOfParticipantTypesNeeded()) {
						List<Long> requiredTargetParticipantTypePKlist = optionsPage.getRequiredTargetParticipantTypePKlist();
						participantTypeModel.setEventParticipantTypes(targetEventPK, requiredTargetParticipantTypePKlist);
					}

					copiedConfig = registrationFormConfigModel.copyRegistrationFormConfig(
						config.getId(),
						targetEventPK,
						webId,
						copyUploadedFiles,
						copyEmailTemplates,
						copyBookingRules
					);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}

			}
		});

		if (copiedConfig != null) {
			try {
				RegistrationFormConfigEditorInput input = new RegistrationFormConfigEditorInput(copiedConfig);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, RegistrationFormConfigEditor.ID);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

		}
		return copiedConfig != null;
	}


	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage() == optionsPage && optionsPage.isPageComplete();
	}


	@Override
	public String getWindowTitle() {
		return UtilI18N.CopyVerb;
	}


	public Point getPreferredSize() {
		return new Point(500, 400);
	}

}
