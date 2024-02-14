package de.regasus.finance.payment.dialog;

import static com.lambdalogic.util.rcp.widget.SWTHelper.createLabel;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.datetime.DateComposite;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.impersonalaccount.combo.ImpersonalAccountCombo;
import de.regasus.ui.Activator;

public class EditPaymentDialog extends TitleAreaDialog {

	private AccountancyModel accountancyModel;
	private PaymentVO paymentVO;

	// widgets
	private DateComposite bookingDate;
	private ImpersonalAccountCombo impersonalAccountCombo;
	private Text descriptionText;


    /**
     * Create the dialog
     * @param parentShell
     */
    public EditPaymentDialog(Shell parentShell, PaymentVO paymentVO) {
    	super(parentShell);

    	this.paymentVO = paymentVO;

    	accountancyModel = AccountancyModel.getInstance();
    }


    @Override
    public void create() {
    	super.create();

    	// set title and message after the dialog has been opened
		setTitle(I18N.EditPaymentDialog_title);
		setMessage(I18N.EditPaymentDialog_message);
    }


    @Override
    protected boolean isResizable() {
    	return true;
    }


    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

        try {
			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


			container.setLayout(new GridLayout(2, false) );


			// Booking Date
			{
				createLabel(container, InvoiceLabel.BookingDate.getString());

				bookingDate = new DateComposite(container, SWT.BORDER);
				bookingDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

				// set data
				bookingDate.setLocalDate( TypeHelper.toLocalDate(paymentVO.getBookingDate()) );
			}


			// Credit/Impersonal Account
			{
				Label creditorLabel = createLabel(
					container,
					InvoiceLabel.Creditor.getString() + " (" + InvoiceLabel.FinanceAccount.getString() + ")"
				);
				creditorLabel.setToolTipText(I18N.CreatePaymentAmountPage_CreditorTooltip);

				impersonalAccountCombo = new ImpersonalAccountCombo(container, SWT.NONE);
				impersonalAccountCombo.setOnlyFinanceAcounts(true);
				impersonalAccountCombo.setWithEmptyElement(true);

				impersonalAccountCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

				// set data
				impersonalAccountCombo.setImpersonalAccountPK( paymentVO.getCreditAccountNo() );
			}


			// Description
			{
				createLabel(container, UtilI18N.Description);

    			descriptionText = new Text(container, SWT.BORDER);
    			descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    			descriptionText.setTextLimit(PaymentVO.MAX_LENGTH_DESCRIPTION);

				// set data
    			descriptionText.setText( StringHelper.avoidNull(paymentVO.getDescription()) );
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

    	return area;
    }


    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }


    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
    	return new Point(580, 400);
    }


    @Override
    protected void buttonPressed(int buttonId) {
    	if (buttonId == OK) {
    		try {
    			// copy values from widgets to paymentVO
    			paymentVO.setBookingDate( bookingDate.getDate() );
    			paymentVO.setCreditAccountNo( impersonalAccountCombo.getImpersonalAccountNo() );
    			paymentVO.setDescription( StringHelper.trim(descriptionText.getText()) );

    			// Update payment via Model
				accountancyModel.updatePayment(paymentVO);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
    	}

    	super.buttonPressed(buttonId);
    }

}
