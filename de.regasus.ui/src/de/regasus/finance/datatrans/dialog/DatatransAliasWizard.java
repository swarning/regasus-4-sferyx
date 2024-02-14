package de.regasus.finance.datatrans.dialog;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.CustomWizardDialog;
import com.lambdalogic.util.rcp.ICustomWizard;

import de.regasus.I18N;
import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

public class DatatransAliasWizard extends Wizard implements ICustomWizard, IPageChangedListener {

	private CustomWizardDialog customWizardDialog;

	// WizardPages
	private DatatransPage2 datatransPage2;


	private Participant participant;


	// **************************************************************************
	// * Constructors
	// *

	public DatatransAliasWizard(Participant participant) {
		this.participant = participant;
	}


	@Override
	public void setCustomWizardDialog(CustomWizardDialog customWizardDialog) {
		if (this.customWizardDialog != null) {
			this.customWizardDialog.removePageChangedListener(this);
		}

		this.customWizardDialog = customWizardDialog;
		if (this.customWizardDialog != null) {
			this.customWizardDialog.addPageChangedListener(this);
		}
	}

	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void addPages() {
		// DatatransPage2
		datatransPage2 = new DatatransPage2();
		addPage(datatransPage2);
	}


	@Override
	public String getWindowTitle() {
		return I18N.DatatransAliasWizard_WindowTitle;
	}


	/**
	 * Can only finish if the second page is shown, so that the user is reminded to enter the additional data.
	 */
	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return (currentPage == datatransPage2);
	}


	@Override
	public boolean performFinish() {
		return true;
	}


	@Override
	public Point getPreferredSize() {
		return new Point(900, 700);
	}


	@Override
	public void pageChanged(PageChangedEvent event) {
		Object selectedPage = event.getSelectedPage();
		if (selectedPage == datatransPage2) {
			startAliasRequest();
		}
	}


	private void startAliasRequest() {
		try {
			EventVO eventVO = EventModel.getInstance().getEventVO(participant.getEventId());

			// determine currency
			// 1.: Take the event's default currency for programme offerings
			String currency = eventVO.getProgPriceDefaultsVO().getCurrency();
			if (currency == null) {
				// 2.: Take default currency from properties
				currency = PropertyModel.getInstance().getDefaultCurrency();
			}
			if (currency == null) {
				// 3.: Take EUR
				currency = "EUR";
			}

			datatransPage2.startAlias(
				currency,
				participant,
				eventVO
			);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
