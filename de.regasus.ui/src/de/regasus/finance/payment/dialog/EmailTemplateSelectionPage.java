package de.regasus.finance.payment.dialog;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateComparator;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailTemplateModel;
import de.regasus.email.template.EmailTemplateSearchTable;
import de.regasus.finance.ICurrencyAmountProvider;
import de.regasus.ui.Activator;

/**
 * A WizardPage to select an Email Template.
 * Wizards that add this WizardPage must implement {@link ICurrencyAmountProvider}.
 * This is necessary, because the presented list of Email Templates depends on the the amount.
 * If the amount is positive, this page shows Email Templates with the
 * {@link EmailTemplateSystemRole#PAYMENT_RECEIVED}. Other wise Email Templates with
 * {@link EmailTemplateSystemRole#REFUND_ISSUED} are shown.
 */
public class EmailTemplateSelectionPage extends WizardPage {

	public static final String DIALOG_SETTING_KEY_SEND_PAYMENT_EMAIL = "sendPaymentEmail";
	public static final String DIALOG_SETTING_KEY_EMAIL_PAYMENT_TEMPLATE_ID = "paymentEmailTemplateID";

	public static final String DIALOG_SETTING_KEY_SEND_REFUND_EMAIL = "sendRefundEmail";
	public static final String DIALOG_SETTING_KEY_EMAIL_REFUND_TEMPLATE_ID = "refundEmailTemplateID";


	public static final String NAME = "EmailTemplateSelectionPage";

	// *************************************************************************
	// * Attributes
	// *

	private Long eventPK;
	private EmailTemplate selectedTemplate;

	private EmailTemplateModel emailTemplateModel;

	private boolean refund;


	// *************************************************************************
	// * Widgets
	// *

	private EmailTemplateSearchTable emailTemplateTable;
	private Button sendEmailCheckbox;

	private IDialogSettings dialogSettings;

	// *************************************************************************
	// * Constructor
	// *

	/**
	 * Create a {@link WizardPage} to select an {@link EmailTemplate}.
	 *
	 * The list of email templates contains either all email templates with the
	 * {@link EmailTemplateSystemRole} {@link EmailTemplateSystemRole#PAYMENT_RECEIVED} or
	 * {@link EmailTemplateSystemRole#REFUND_ISSUED}. Which one is chosen depends on the value of
	 * the parameter refund. If refund is true, templates with {@link EmailTemplateSystemRole#REFUND_ISSUED}
	 * are shown. If refund is false, the {@link EmailTemplateSystemRole} depends on the value of
	 * the entered amount! Only if it is positive templates with {@link EmailTemplateSystemRole#PAYMENT_RECEIVED}
	 * are shown. If it is negative, the user is entering a refund manually and templates with
	 * {@link EmailTemplateSystemRole#REFUND_ISSUED} are shown. That's why the list of {@link EmailTemplate}s
	 * is determined later in {@link #setVisible(boolean)}.
	 *
	 * @param eventPK
	 * @param refund
	 */
	public EmailTemplateSelectionPage(Long eventPK, boolean refund) {
		super(NAME);
		setTitle(I18N.EmailTemplateSelectionPage_title);
		setDescription(I18N.EmailTemplateSelectionPage_desc);

		dialogSettings = Activator.getDefault().getDialogSettings();

		this.eventPK = eventPK;
		this.refund = refund;

		emailTemplateModel = EmailTemplateModel.getInstance();
	}

	// *************************************************************************
	// * Overridden/implemented WizardPage-Methods
	// *

	@Override
	public void createControl(Composite parent) {
		Composite pageComposite = new Composite(parent, SWT.NONE);

		pageComposite.setLayout(new GridLayout(1, false));

		sendEmailCheckbox = new Button(pageComposite, SWT.CHECK | SWT.WRAP);
		sendEmailCheckbox.setText(I18N.EmailTemplateSelectionPage_sendEmailCheckbox_text);
		sendEmailCheckbox.setToolTipText(I18N.EmailTemplateSelectionPage_sendEmailCheckbox_tooltip);

		Composite tableComposite = new Composite(pageComposite, SWT.BORDER);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);

		final Table table = new Table(tableComposite, SelectionMode.SINGLE_SELECTION.getSwtStyle());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(nameTableColumn, new ColumnWeightData(100));
		nameTableColumn.setText(EmailLabel.EmailTemplate.getString());

		final TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(languageTableColumn, new ColumnWeightData(25));
		languageTableColumn.setText( Person.LANGUAGE_CODE.getLabel() );

		final TableColumn webidTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(webidTableColumn, new ColumnWeightData(25));
		webidTableColumn.setText(I18N.WebId);

		emailTemplateTable = new EmailTemplateSearchTable(table);


		// observe table
		emailTemplateTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// save selected EmailTemplate in DialogSettings
				ISelection selection = emailTemplateTable.getViewer().getSelection();
				if (selection.isEmpty() ) {
					selectedTemplate = null;
					dialogSettings.put(DIALOG_SETTING_KEY_EMAIL_PAYMENT_TEMPLATE_ID, (String) null);
				}
				else {
					selectedTemplate = (EmailTemplate) SelectionHelper.getUniqueSelected(selection);
					dialogSettings.put(
						DIALOG_SETTING_KEY_EMAIL_PAYMENT_TEMPLATE_ID,
						selectedTemplate.getID().longValue()
					);
				}


				setPageComplete(isPageComplete());
			}
		});


		// observe checkbox
		sendEmailCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// enable/disable table according to sendEmailCheckbox
				emailTemplateTable.setEnabled(sendEmailCheckbox.getSelection());

				if (!sendEmailCheckbox.getSelection()) {
					// remove any selection in table
					StructuredSelection selection = new StructuredSelection();
					emailTemplateTable.getViewer().setSelection(selection);
				}

				// save value of sendEmailCheckbox in DialogSettings
				dialogSettings.put(DIALOG_SETTING_KEY_SEND_PAYMENT_EMAIL, sendEmailCheckbox.getSelection());


				setPageComplete(isPageComplete());
			}
		});

		setControl(pageComposite);
	}


	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		// load data of table when this page gets visible
		if (visible) {
			try {
				EmailTemplateSystemRole role;
				if (refund) {
					role = EmailTemplateSystemRole.REFUND_ISSUED;
				}
				else {
					/* If the wizard has been opened to enter a payment, the payment is actually a
					 * refund if the entered amount is negative. So, determine the entered amount
					 * from the Wizard.
					 * The Wizard must implement ICurrencyAmountProvider.
					 */
					IWizard wizard = getWizard();
					if (! (wizard instanceof ICurrencyAmountProvider)) {
						throw new RuntimeException("EmailTemplateSelectionPage must belong to a Wizard that implements ICurrencyAmountProvider.");
					}

					ICurrencyAmountProvider currencyAmountProvider = (ICurrencyAmountProvider) getWizard();
					CurrencyAmount currencyAmount = currencyAmountProvider.getCurrencyAmount();

					if (currencyAmount.getAmount().signum() > 0) {
						role = EmailTemplateSystemRole.PAYMENT_RECEIVED;
					}
					else {
						role = EmailTemplateSystemRole.REFUND_ISSUED;
					}
				}


				// get data
				List<EmailTemplate> emailTemplateSearchDataList = emailTemplateModel.getEmailTemplateSearchDataByEvent(
					eventPK,
					role
				);

				// sort by language and name (sorting by 2 columns is not supported by SimpleTable)
				emailTemplateSearchDataList = createArrayList(emailTemplateSearchDataList);
				Collections.sort(emailTemplateSearchDataList, EmailTemplateComparator.getInstance());


				// set data
				emailTemplateTable.setInput(emailTemplateSearchDataList);


				// Restore previous DialogSettings

				// restore value of sendEmailCheckbox
				// If no DialogSettings present, returns default value 'false', which is OK
				boolean sendEmailCheckboxSelection;
				if (role == EmailTemplateSystemRole.PAYMENT_RECEIVED) {
					sendEmailCheckboxSelection = dialogSettings.getBoolean(DIALOG_SETTING_KEY_SEND_PAYMENT_EMAIL);
				}
				else {
					sendEmailCheckboxSelection = dialogSettings.getBoolean(DIALOG_SETTING_KEY_SEND_REFUND_EMAIL);
				}
				sendEmailCheckbox.setSelection(sendEmailCheckboxSelection);

				// restore selected EmailTemplate
				String emailTemplateIdString;
				if (role == EmailTemplateSystemRole.PAYMENT_RECEIVED) {
					emailTemplateIdString = dialogSettings.get(DIALOG_SETTING_KEY_EMAIL_PAYMENT_TEMPLATE_ID);
				}
				else {
					emailTemplateIdString = dialogSettings.get(DIALOG_SETTING_KEY_EMAIL_REFUND_TEMPLATE_ID);
				}

				if (StringHelper.isNotEmpty(emailTemplateIdString)) {
					try {
						Long emailTemplateID = TypeHelper.toLong(emailTemplateIdString);

						EmailTemplate emailTemplate = emailTemplateModel.getEmailTemplate(emailTemplateID);
						if (emailTemplate != null) {
							// select EmailTemplate in Table
							// if EmailTemplate does not exist in Table, nothing will be selected
							StructuredSelection selection = new StructuredSelection(emailTemplate);
							emailTemplateTable.getViewer().setSelection(selection, true);
						}
					}
					catch (EntityNotFoundException e) {
						System.out.println( e.getMessage() );
					}
					catch (Exception e) {
						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					}
				}

				// update buttons without going over container
				emailTemplateTable.setEnabled(sendEmailCheckbox.getSelection());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	public boolean isPageComplete() {
		boolean complete = true;

		if (sendEmailCheckbox.getSelection()) {
			ISelection selection = emailTemplateTable.getViewer().getSelection();
			if (selection.isEmpty()) {
				complete = false;
			}
			else {
				selectedTemplate = (EmailTemplate) SelectionHelper.getUniqueSelected(selection);
			}
		}
		return complete;
	}


	// *************************************************************************
	// * getters and setters
	// *

	public boolean isSendEmail() {
		return sendEmailCheckbox.getSelection();
	}


	public EmailTemplate getSelectedEmailTemplate() {
		return selectedTemplate;
	}


	public Long getSelectedEmailTemplateID() {
		Long emailTemplateID = null;
		if (selectedTemplate != null) {
			emailTemplateID = selectedTemplate.getID();
		}
		return emailTemplateID;
	}

}
