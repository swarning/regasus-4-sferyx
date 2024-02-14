package de.regasus.finance.paymentsystem.dialog;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.exception.WarnMessageException;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.CustomWizardDialog;
import com.lambdalogic.util.rcp.ICustomWizard;

import de.regasus.I18N;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.PaymentSystemSetup;
import de.regasus.finance.PaymentSystemSetupModel;
import de.regasus.finance.payengine.PayEngineSetup;

public class PayEngineAliasWizard extends Wizard implements ICustomWizard, IPageChangedListener {

	private CustomWizardDialog customWizardDialog;

	// WizardPages
	private PayEngineBrowserPage payEngineBrowserPage;


	private Participant participant;


	// **************************************************************************
	// * Constructors
	// *

	protected PayEngineAliasWizard() {
	}


	public static PayEngineAliasWizard getInstance(
		Participant participant,
		EventVO eventVO
	)
	throws Exception {
		Long paymentSystemSetupPK = eventVO.getPaymentSystemSetupPK();
		if (paymentSystemSetupPK == null) {
			throw new WarnMessageException(I18N.PayEngine_NoSetupMessage);
		}

		PaymentSystemSetup paymentSystemSetup = PaymentSystemSetupModel.getInstance().getPaymentSystemSetup(paymentSystemSetupPK);
		if (paymentSystemSetup.getPaymentSystem() != PaymentSystem.PAYENGINE) {
			throw new WarnMessageException("The Payment System Setup does not define a PayEngine Setup!");
		}

		PayEngineSetup payEngineSetup = paymentSystemSetup.getPayEngineSetup();
		if ( !payEngineSetup.isComplete() ) {
			throw new WarnMessageException(I18N.PayEngine_IncompleteSetupMessage);
		}


		PayEngineAliasWizard wizard = new PayEngineAliasWizard();
		wizard.participant = participant;

		return wizard;
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


	@Override
	public void addPages() {
		// PayEngineBrowserPage
		payEngineBrowserPage = new PayEngineBrowserPage();
		addPage(payEngineBrowserPage);
	}


	@Override
	public void pageChanged(PageChangedEvent event) {
		Object selectedPage = event.getSelectedPage();

		if (selectedPage == payEngineBrowserPage) {
			payEngineBrowserPage.startAlias(participant);
		}
	}


	@Override
	public String getWindowTitle() {
		return I18N.PayEngineAliasWizard_WindowTitle;
	}


	/**
	 * Can only finish if the second page is shown, so that the user is reminded to enter the additional data.
	 */
	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return currentPage == payEngineBrowserPage;
	}


	@Override
	public boolean performFinish() {
		return true;
	}


	@Override
	public Point getPreferredSize() {
		return new Point(900, 700);
	}

}
