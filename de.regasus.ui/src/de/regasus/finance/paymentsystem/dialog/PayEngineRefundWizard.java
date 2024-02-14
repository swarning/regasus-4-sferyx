package de.regasus.finance.paymentsystem.dialog;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.CurrencyAmount;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailTemplateModel;
import de.regasus.finance.ICurrencyAmountProvider;
import de.regasus.finance.payment.dialog.EmailTemplateSelectionPage;
import de.regasus.ui.Activator;

public class PayEngineRefundWizard extends Wizard
implements IPageChangedListener, ICurrencyAmountProvider {


	// WizardPages
	private RefundAmountPage refundAmountPage;
	private EmailTemplateSelectionPage emailTemplateSelectionPage;
	private PayEngineRefundTransactionPage payEngineRefundTransactionPage;

	// Other attributes
	private BigDecimal maxRefundableAmount;

	private PaymentVO paymentVO;

	private Long payID;


	// **************************************************************************
	// * Constructors
	// *

	public PayEngineRefundWizard(BigDecimal maxRefundableAmount, PaymentVO paymentVO, Long payID) {
		this.maxRefundableAmount = maxRefundableAmount;
		this.paymentVO = paymentVO;
		this.payID = payID;
	}


	@Override
	public CurrencyAmount getCurrencyAmount() {
		return refundAmountPage.getRefundAmount();
	}


	@Override
	public void addPages() {
		// RefundAmountPage
		refundAmountPage = new RefundAmountPage(maxRefundableAmount, paymentVO);
		addPage(refundAmountPage);

		// Add EmailTemplateSelectionPage if any EmailTemplate with EmailTemplateSystemRole.REFUND_ISSUED exist
		Long eventPK = paymentVO.getEventPK();
		List<EmailTemplate> emailTemplates = null;
		try {
			// check if any EmailTemplate with EmailTemplateSystemRole.REFUND_ISSUED exist
			emailTemplates = EmailTemplateModel.getInstance().getEmailTemplateSearchDataByEvent(
				eventPK,
				EmailTemplateSystemRole.REFUND_ISSUED
			);

			if (notEmpty(emailTemplates)) {
				emailTemplateSelectionPage = new EmailTemplateSelectionPage(eventPK, true /*refund*/);
				addPage(emailTemplateSelectionPage);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}


		// PayEngineRefundTransactionPage
		payEngineRefundTransactionPage = new PayEngineRefundTransactionPage();
		addPage(payEngineRefundTransactionPage);
	}


	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		super.setContainer(wizardContainer);

		if (wizardContainer instanceof WizardDialog) {
			WizardDialog wizardDialog = (WizardDialog) wizardContainer;
			wizardDialog.addPageChangedListener(this);
		}
	}


	@Override
	public String getWindowTitle() {
		return InvoiceLabel.Refund.getString();
	}


	/**
	 * Can only finish if the second page is shown, so that the user is reminded to enter the additional data.
	 */
	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return
			currentPage == payEngineRefundTransactionPage &&
			payEngineRefundTransactionPage.isPageComplete();
	}


	@Override
	public boolean performFinish() {
		return true;
	}


	public Point getPreferredSize() {
		return new Point(700, 600);
	}


	@Override
	public void pageChanged(PageChangedEvent event) {
		System.out.println("PayEngineRefundWizard.pageChanged()");
		try {
			Object selectedPage = event.getSelectedPage();

			if (selectedPage == payEngineRefundTransactionPage) {
				BigDecimal refundAmount = refundAmountPage.getRefundAmount().getAmount();

				Long emailTemplateID = null;
				if (emailTemplateSelectionPage != null) {
					emailTemplateID = emailTemplateSelectionPage.getSelectedEmailTemplateID();
				}

				payEngineRefundTransactionPage.init(
					refundAmount,
					payID,
					paymentVO,
					emailTemplateID
				);
			}
		}
    	catch (Exception e) {
    		RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    	}
	}

}
