package de.regasus.email.template.editor;

import java.util.List;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.ReminderState;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.invoice.combo.ReminderStateCombo;
import de.regasus.finance.invoicenumberrange.combo.InvoiceNoRangeCombo;
import de.regasus.ui.Activator;

/**
 * A composite used in the {@link EmailTemplateEditor} to control what dynamic attachments are to be added.
 */
public class InvoicesGroup extends Group {

	// *************************************************************************
	// * Widgets
	// *

	private Button withOpenInvoicesButton;

	private Button withRemindersButton;

	private ReminderStateCombo reminderStateCombo;

	private Button withClosedInvoicesButton;

	private Button ignoreInvoicesWithAmountZeroButton;

	private InvoiceNoRangeCombo invoiceNoRangeCombo;


	// *************************************************************************
	// * Other Attributes
	// *

	private EmailTemplate emailTemplate;


	/**
	 * The listeners that are notified when the user makes changes in this group.
	 */
	private ModifySupport modifySupport = new ModifySupport(this);


	// *************************************************************************
	// * Constructor
	// *

	public InvoicesGroup(Composite parent, int style, Long eventId) throws Exception {
		super(parent, style);

		Objects.requireNonNull(eventId);

		setLayout(new GridLayout(4, false));
		setText(InvoiceLabel.Invoices.getString());
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// With open invoices
		withOpenInvoicesButton = new Button(this, SWT.CHECK);
		withOpenInvoicesButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		withOpenInvoicesButton.addSelectionListener(modifySupport);

		Label withOpenInvoicesLabel = new Label(this, SWT.WRAP | SWT.LEFT);
		withOpenInvoicesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		withOpenInvoicesLabel.setText(EmailLabel.WithOpenInvoices.getString());

		// With closed invoices
		withClosedInvoicesButton = new Button(this, SWT.CHECK);
		withClosedInvoicesButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		withClosedInvoicesButton.addSelectionListener(modifySupport);

		Label withClosedInvoicesLabel = new Label(this, SWT.WRAP | SWT.LEFT);
		withClosedInvoicesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		withClosedInvoicesLabel.setText(EmailLabel.WithClosedInvoices.getString());

		// Ignore invoices with amount zero
		ignoreInvoicesWithAmountZeroButton = new Button(this, SWT.CHECK);
		ignoreInvoicesWithAmountZeroButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		ignoreInvoicesWithAmountZeroButton.addSelectionListener(modifySupport);

		Label ignoreInvoicesWithAmountZeroLabel = new Label(this, SWT.WRAP | SWT.LEFT);
		ignoreInvoicesWithAmountZeroLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		ignoreInvoicesWithAmountZeroLabel.setText(EmailLabel.IgnoreInvoicesWithAmountZeroLabel.getString());

		// With reminders of a certain state
		withRemindersButton = new Button(this, SWT.CHECK);
		withRemindersButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		withRemindersButton.addSelectionListener(modifySupport);


		// Reminder State
		Label withRemindersLabel = new Label(this, SWT.WRAP | SWT.LEFT);
		withRemindersLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		withRemindersLabel.setText(EmailLabel.WithRemindersOfState.getString());

		List<ReminderState> reminderStates = CollectionsHelper.createArrayList(
			ReminderState.LEVEL1,
			ReminderState.LEVEL2,
			ReminderState.LEVEL3,
			ReminderState.LEVEL4,
			ReminderState.LEVEL5
		);
		reminderStateCombo = new ReminderStateCombo(this, SWT.READ_ONLY, reminderStates);
		reminderStateCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		reminderStateCombo.addModifyListener(modifySupport);
		reminderStateCombo.setEntity(ReminderState.LEVEL1);


		// Invoice number range (Rechnungsnummernkreis)
		Label invoiceNumberRangeLabel = new Label(this, SWT.NONE);
		invoiceNumberRangeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		invoiceNumberRangeLabel.setText(InvoiceLabel.InvoiceNoRange.getString());

		invoiceNoRangeCombo = new InvoiceNoRangeCombo(this, SWT.NONE, eventId);
		invoiceNoRangeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		invoiceNoRangeCombo.setToolTipText(EmailLabel.IfSetOnlyInvoicesFromThatRangeWillBeSent.getString());

		invoiceNoRangeCombo.addModifyListener(modifySupport);

		/**
		 * When at least one of the buttons is selected that invoices are to be attached (whether open
		 * or closed doesn't matter), the user may also select an invoice number range and a file format.
		 */
		modifySupport.addListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateStates();
			}
		});
	}


	/**
	 * Stores the widgets' contents to the entity.
	 */
	public void syncEntityToWidgets(EmailTemplate emailTemplate) {
		emailTemplate.setWithOpenInvoices(withOpenInvoicesButton.getSelection());
		emailTemplate.setWithClosedInvoices(withClosedInvoicesButton.getSelection());
		emailTemplate.setIgnoreInvoicesWithAmountZero(ignoreInvoicesWithAmountZeroButton.getSelection());
		emailTemplate.setWithReminders(withRemindersButton.getSelection());

		if (withRemindersButton.getSelection()) {
			emailTemplate.setReminderState(reminderStateCombo.getEntity());
		}
		else {
			emailTemplate.setReminderState(null);
		}

		InvoiceNoRangeCVO invoiceNoRangeVO = invoiceNoRangeCombo.getEntity();
		emailTemplate.setInvoiceNoRangePK(invoiceNoRangeVO != null ? invoiceNoRangeVO.getVO().getID() : null);
	}


	public void setEmailTemplate(EmailTemplate emailTemplate) {
		this.emailTemplate = emailTemplate;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (emailTemplate != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// Invoices
						withClosedInvoicesButton.setSelection(emailTemplate.isWithClosedInvoices());
						withOpenInvoicesButton.setSelection(emailTemplate.isWithOpenInvoices());
						ignoreInvoicesWithAmountZeroButton.setSelection(emailTemplate.isIgnoreInvoicesWithAmountZero());
						withRemindersButton.setSelection(emailTemplate.isWithReminders());
						if (emailTemplate.getReminderState() != null) {
							reminderStateCombo.setEntity(emailTemplate.getReminderState());
						}

						Long rangePK = emailTemplate.getInvoiceNoRangePK();
						invoiceNoRangeCombo.setInvoiceNoRangeByPK(rangePK != null ? rangePK : null);

						updateStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void updateStates() {
		boolean withReminders = withRemindersButton.getSelection();
		reminderStateCombo.setEnabled(withReminders);

		boolean withAnyInvoice =
			   withClosedInvoicesButton.getSelection()
			|| withOpenInvoicesButton.getSelection();

		invoiceNoRangeCombo.setEnabled(withAnyInvoice || withReminders);
		ignoreInvoicesWithAmountZeroButton.setEnabled(withAnyInvoice);
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
