package de.regasus.portal.dialog;

import java.util.Objects;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.Activator;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.dialog.EventWizardPage;
import de.regasus.portal.CopyPortalMissingCustomFieldBehaviour;
import de.regasus.portal.CopyPortalMissingParticipantTypeBehaviour;
import de.regasus.portal.CopyPortalMissingProgrammePointBehaviour;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;


public class CopyPortalWizard extends Wizard {

	private Portal sourcePortal;

	private EventWizardPage targetEventPage;
	private CopyPortalSettingsWizardPage settingsPage;


	public CopyPortalWizard(Long sourcePortalPK) throws Exception {
		Objects.requireNonNull(sourcePortalPK);

		sourcePortal = PortalModel.getInstance().getPortal(sourcePortalPK);
		if (sourcePortal == null) {
			throw new ErrorMessageException("Portal with PK " + sourcePortalPK + " does not exist.");
		}
	}


	@Override
	public void addPages() {
		String title = I18N.CopyPortalWizard_Title;
		String description = I18N.CopyPortalWizard_Description;
		Long initialEventPK = sourcePortal.getEventId();

		if (initialEventPK != null) {
    		targetEventPage = new EventWizardPage();
    		targetEventPage.setTitle(title);
    		targetEventPage.setDescription(description);
    		targetEventPage.setInitiallySelectedEventPK(initialEventPK);

    		targetEventPage.addModifyListener(eventPageModifyListener);
    		addPage(targetEventPage);
		}

		settingsPage = new CopyPortalSettingsWizardPage();
		addPage(settingsPage);
	}


	private ModifyListener eventPageModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			Long sourceEventPK = sourcePortal.getEventId();		// not null
			Long targetEventPK = targetEventPage.getEventId();	// can be null

			boolean equalEvents = sourceEventPK.equals(targetEventPK);
			settingsPage.showWidgetsForDifferentEvents( ! equalEvents );
		}
	};


	@Override
	public boolean performFinish() {
		// determine settings
		Long targetEventId = null;
		if (targetEventPage != null) {
			targetEventId = targetEventPage.getEventId();
		}

		String newPortalMnemonic = settingsPage.getMnemonic();
		boolean copyPhotos = settingsPage.isCopyPhotos();
		CopyPortalMissingParticipantTypeBehaviour missingParticipantTypeBehaviour = settingsPage.getMissingParticipantTypeBehaviour();
		CopyPortalMissingCustomFieldBehaviour missingCustomFieldBehaviour = settingsPage.getMissingCustomFieldBehaviour();
		CopyPortalMissingProgrammePointBehaviour missingProgrammePointBehaviour = settingsPage.getMissingProgrammePointBehaviour();

		try {
			PortalModel.getInstance().copy(
				sourcePortal.getId(),
				targetEventId,
				newPortalMnemonic,
				copyPhotos,
				missingParticipantTypeBehaviour,
				missingCustomFieldBehaviour,
				missingProgrammePointBehaviour
			);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return false;
		}

		return true;
	}


	@Override
	public String getWindowTitle() {
		return I18N.CopyPortalWizard_WindowTitle;
	}


	public Point getPreferredSize() {
		return new Point(700, 600);
	}

}
