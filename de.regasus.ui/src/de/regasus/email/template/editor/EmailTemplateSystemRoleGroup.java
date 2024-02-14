package de.regasus.email.template.editor;

import static com.lambdalogic.messeinfo.email.EmailTemplateSystemRole.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.IconRegistry;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.email.template.EmailTemplateSystemRoleHelper;


public class EmailTemplateSystemRoleGroup extends Group {

	// *************************************************************************
	// * Widgets
	// *

	// Buttons for all EmailTemplates
	private Button noneButton;

	// Buttons for purposes for event specific EmailTemplates
	private Button invitationButton;
	private Button confirmationButton;
	private Button changeConfirmButton;
	private Button cancelConfirmButton;
	private Button letterOfInvitationButton;
	private Button newsletterConfirmButton;
	private Button paymentReceivedButton;
	private Button refundIssuedButton;
	private Button withPaymentReceiptButton;
	private Button easyCheckoutButton;
	private Button upcomingStreamButton;
	private Button certificateButton;

	// Buttons for purposes for global EmailTemplates
	private Button profileCreatedButton;
	private Button emailVerificationButton;
	private Button forgotPasswordButton;

	// *************************************************************************
	// * Other Attributes
	// *

	private Long eventId;

	/**
	 * The listeners that are notified when the user makes changes in this group.
	 */
	private ModifySupport modifySupport = new ModifySupport(this);

	private EmailTemplate emailTemplate;

	// *************************************************************************
	// * Constructor
	// *

	/**
	 * Group used in EmailTemplateEditor to show the purpose  (EmailTemplateSystemRole)of an EmailTemplate.
	 * @param parent
	 * @param style
	 * @param eventSpecific
	 *  Define whether the EmailTemplate belongs to an Event. The list of available purposes depends on this setting.
	 * @throws Exception
	 */
	public EmailTemplateSystemRoleGroup(Composite parent, int style, Long eventId) throws Exception {
		super(parent, style);

		this.eventId = eventId;

		setText(EmailLabel.EmailTemplateSystemRole.getString());

		setLayout(new GridLayout(4, false));

		noneButton = createRadioButton("blank.png", EmailLabel.EmailTemplateSystemRole_NULL.getString());
		noneButton.setToolTipText(EmailLabel.EmailTemplateSystemRole_desc_NULL.getString());

		createDummyLabel(this);

		// Create different Buttons for different System Roles depending on whether the Email Template is Event specific.
		if (eventId != null) {

			// create Buttons for System Roles for Event specific Email Templates

			/* |-------------------------|-------------------------|-------------------------|-------------------------|
			 * | Button                                            | Dummy Label                                       |
			 * |-------------------------|-------------------------|-------------------------|-------------------------|
			 */

			invitationButton = createRadioButton(INVITATION);
			invitationButton.setToolTipText(INVITATION.getDescription());
			createDummyLabel(this);

			confirmationButton = createRadioButton(CONFIRMATION);
			confirmationButton.setToolTipText(CONFIRMATION.getDescription());
			createDummyLabel(this);

			changeConfirmButton = createRadioButton(CHANGE_CONFIRMATION);
			changeConfirmButton.setToolTipText(CHANGE_CONFIRMATION.getDescription());
			createDummyLabel(this);

			cancelConfirmButton = createRadioButton(CONFIRM_CANCEL);
			cancelConfirmButton.setToolTipText(CONFIRM_CANCEL.getDescription());
			createDummyLabel(this);

			letterOfInvitationButton = createRadioButton(LETTER_OF_INVITATION);
			letterOfInvitationButton.setToolTipText(LETTER_OF_INVITATION.getDescription());
			createDummyLabel(this);

			newsletterConfirmButton = createRadioButton(NEWSLETTER_CONFIRM);
			newsletterConfirmButton.setToolTipText(NEWSLETTER_CONFIRM.getDescription());
			createDummyLabel(this);


			/* |-------------------------|-------------------------|-------------------------|-------------------------|
			 * | paymentReceivedButton                             |withPaymentReceiptButton | withPaymentReceiptLabel |
			 * |-------------------------|-------------------------|                         |                         |
			 * | refundIssuedButton                                |                         |                         |
			 * |-------------------------|-------------------------|-------------------------|-------------------------|
			 */
			paymentReceivedButton = createRadioButton(PAYMENT_RECEIVED);
			paymentReceivedButton.setToolTipText(PAYMENT_RECEIVED.getDescription());

			withPaymentReceiptButton = new Button(this, SWT.CHECK);
			withPaymentReceiptButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
			withPaymentReceiptButton.addSelectionListener(modifySupport);

			Label withPaymentReceiptLabel = new Label(this, SWT.WRAP | SWT.LEFT);
			withPaymentReceiptLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2));
			withPaymentReceiptLabel.setText(EmailLabel.WithPaymentReceipt.getString());

			refundIssuedButton = createRadioButton(REFUND_ISSUED);
			refundIssuedButton.setToolTipText(REFUND_ISSUED.getDescription());

			easyCheckoutButton = createRadioButton(PAYMENT_LINK);
			easyCheckoutButton.setToolTipText(PAYMENT_LINK.getDescription());

			createDummyLabel(this);


			/* |-------------------------|-------------------------|-------------------------|-------------------------|
			 * | Button                                            | Dummy Label                                       |
			 * |-------------------------|-------------------------|-------------------------|-------------------------|
			 */

			upcomingStreamButton = createRadioButton(UPCOMING_STREAM);
			upcomingStreamButton.setToolTipText(UPCOMING_STREAM.getDescription());
			createDummyLabel(this);

			ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventId);
			if (   configParameterSet.getEvent().getCertificate().isVisible()
				&& configParameterSet.getEvent().getCertificate().getEmail().isVisible()
			) {
    			certificateButton = createRadioButton(CERTIFICATE);
    			certificateButton.setToolTipText(CERTIFICATE.getDescription());
    			createDummyLabel(this);
			}

			profileCreatedButton = createRadioButton(PROFILE_CREATED);
			profileCreatedButton.setToolTipText(PROFILE_CREATED.getDescription());
			createDummyLabel(this);

			emailVerificationButton = createRadioButton(EMAIL_VERIFICATION);
			emailVerificationButton.setToolTipText(EMAIL_VERIFICATION.getDescription());
			createDummyLabel(this);

			forgotPasswordButton = createRadioButton(FORGOT_PASSWORD);
			forgotPasswordButton.setToolTipText(FORGOT_PASSWORD.getDescription());
			createDummyLabel(this);


			// observe all radio buttons to update their states
			SelectionListener eventSpecificRadioButtonSelectionListener = new SelectionAdapter() {
				boolean ignore = false;

				@Override
				public void widgetSelected(SelectionEvent event) {
					try {
						if (!ignore && !ModifySupport.isDeselectedRadioButton(event)) {
							ignore = true;
							updateButtonStates();
						}
					}
					finally {
						ignore = false;
					}
				}
			};

			noneButton             .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			invitationButton       .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			confirmationButton     .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			changeConfirmButton    .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			cancelConfirmButton    .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			newsletterConfirmButton.addSelectionListener(eventSpecificRadioButtonSelectionListener);
			paymentReceivedButton  .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			refundIssuedButton     .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			easyCheckoutButton     .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			upcomingStreamButton   .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			if (certificateButton != null) {
				certificateButton  .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			}
			profileCreatedButton   .addSelectionListener(eventSpecificRadioButtonSelectionListener);
			emailVerificationButton.addSelectionListener(eventSpecificRadioButtonSelectionListener);
			forgotPasswordButton   .addSelectionListener(eventSpecificRadioButtonSelectionListener);
		}
		else {
			// create Buttons for System Roles for global (not Event specific) Email Templates

			profileCreatedButton = createRadioButton(PROFILE_CREATED);
			profileCreatedButton.setToolTipText(PROFILE_CREATED.getDescription());
			createDummyLabel(this);

			emailVerificationButton = createRadioButton(EMAIL_VERIFICATION);
			emailVerificationButton.setToolTipText(EMAIL_VERIFICATION.getDescription());
			createDummyLabel(this);

			forgotPasswordButton = createRadioButton(FORGOT_PASSWORD);
			forgotPasswordButton.setToolTipText(FORGOT_PASSWORD.getDescription());
			createDummyLabel(this);
		}
	}


	private Label createDummyLabel(Composite parent) {
		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		return label;
	}


	private Button createRadioButton(EmailTemplateSystemRole systemRole) {
		Button button = new Button(this, SWT.RADIO);
		button.setText( systemRole.getString() );
		button.setImage( EmailTemplateSystemRoleHelper.getImage(systemRole) );
		button.addSelectionListener(modifySupport);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		return button;
	}


	private Button createRadioButton(String icon, String label) {
		Button button = new Button(this, SWT.RADIO);
		button.setText(label);
		button.setImage(IconRegistry.getImage("icons/" + icon));
		button.addSelectionListener(modifySupport);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		return button;
	}


	private void updateButtonStates() {
		/* update state of withPaymentReceiptButton
		 * The paymentReceivedButton is only instantiated for event-specific Email Templates.
		 * The paymentReceivedButton shall be enabled if either the paymentReceivedButton or the refundIssuedButton
		 * are selected. Otherwise the withPaymentReceiptButton gets disabled and its value is set to false.
		 */
		if (eventId != null) {
			if (paymentReceivedButton.getSelection() || refundIssuedButton.getSelection()) {
				withPaymentReceiptButton.setEnabled(true);
			}
			else {
				withPaymentReceiptButton.setEnabled(false);
				withPaymentReceiptButton.setSelection(false);
			}
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	// **************************************************************************
	// * Sync and Modify
	// *

	/**
	 * Stores the widgets' contents to the entity.
	 * @param emailTemplate
	 */
	public void syncEntityToWidgets() {
		if (noneButton.getSelection()) {
			emailTemplate.setSystemRole(null);
			emailTemplate.setWithPaymentReceipt(false);
		}
		else if (eventId != null) {
			if (invitationButton.getSelection()) {
				emailTemplate.setSystemRole(INVITATION);
			}
			else if (confirmationButton.getSelection()) {
				emailTemplate.setSystemRole(CONFIRMATION);
			}
			else if (changeConfirmButton.getSelection()) {
				emailTemplate.setSystemRole(CHANGE_CONFIRMATION);
			}
			else if (cancelConfirmButton.getSelection()) {
				emailTemplate.setSystemRole(CONFIRM_CANCEL);
			}
			else if (letterOfInvitationButton.getSelection()) {
				emailTemplate.setSystemRole(LETTER_OF_INVITATION);
			}
			else if (newsletterConfirmButton.getSelection()) {
				emailTemplate.setSystemRole(NEWSLETTER_CONFIRM);
			}
			else if (paymentReceivedButton.getSelection()) {
				emailTemplate.setSystemRole(PAYMENT_RECEIVED);
			}
			else if (refundIssuedButton.getSelection()) {
				emailTemplate.setSystemRole(REFUND_ISSUED);
			}
			else if (easyCheckoutButton.getSelection()) {
				emailTemplate.setSystemRole(PAYMENT_LINK);
			}
			else if (upcomingStreamButton.getSelection()) {
				emailTemplate.setSystemRole(UPCOMING_STREAM);
			}
			else if (certificateButton != null && certificateButton.getSelection()) {
				emailTemplate.setSystemRole(CERTIFICATE);
			}
			else if (profileCreatedButton.getSelection()) {
				emailTemplate.setSystemRole(PROFILE_CREATED);
			}
			else if (emailVerificationButton.getSelection()) {
				emailTemplate.setSystemRole(EMAIL_VERIFICATION);
			}
			else if (forgotPasswordButton.getSelection()) {
				emailTemplate.setSystemRole(FORGOT_PASSWORD);
			}

			emailTemplate.setWithPaymentReceipt(withPaymentReceiptButton.getSelection());
		}
		else {
			if (profileCreatedButton.getSelection()) {
				emailTemplate.setSystemRole(PROFILE_CREATED);
			}
			else if (emailVerificationButton.getSelection()) {
				emailTemplate.setSystemRole(EMAIL_VERIFICATION);
			}
			else if (forgotPasswordButton.getSelection()) {
				emailTemplate.setSystemRole(FORGOT_PASSWORD);
			}

			emailTemplate.setWithPaymentReceipt(false);
		}
	}


	/**
	 * Show the entity's properties to the widgets
	 */
	private void syncWidgetsToEntity() {
		EmailTemplateSystemRole systemRole = emailTemplate.getSystemRole();
		noneButton.setSelection(systemRole == null);

		if (eventId != null) {
			invitationButton.setSelection(systemRole == INVITATION);
			confirmationButton.setSelection(systemRole == CONFIRMATION);
			changeConfirmButton.setSelection(systemRole == CHANGE_CONFIRMATION);
			cancelConfirmButton.setSelection(systemRole == CONFIRM_CANCEL);
			letterOfInvitationButton.setSelection(systemRole == EmailTemplateSystemRole.LETTER_OF_INVITATION);
			newsletterConfirmButton.setSelection(systemRole == NEWSLETTER_CONFIRM);

			paymentReceivedButton.setSelection(systemRole == PAYMENT_RECEIVED);
			refundIssuedButton.setSelection(systemRole == REFUND_ISSUED);
			withPaymentReceiptButton.setSelection(emailTemplate.isWithPaymentReceipt());
			easyCheckoutButton.setSelection(systemRole == PAYMENT_LINK);

			upcomingStreamButton.setSelection(systemRole == UPCOMING_STREAM);

			if (certificateButton != null) {
				certificateButton.setSelection(systemRole == CERTIFICATE);
			}

			profileCreatedButton.setSelection(systemRole == EmailTemplateSystemRole.PROFILE_CREATED);
			emailVerificationButton.setSelection(systemRole == EMAIL_VERIFICATION);
			forgotPasswordButton.setSelection(systemRole == FORGOT_PASSWORD);
		}
		else {
			profileCreatedButton.setSelection(systemRole == EmailTemplateSystemRole.PROFILE_CREATED);
			emailVerificationButton.setSelection(systemRole == EMAIL_VERIFICATION);
			forgotPasswordButton.setSelection(systemRole == FORGOT_PASSWORD);
		}

		updateButtonStates();
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Sync and Modify
	// **************************************************************************

	public void setEmailTemplate(EmailTemplate emailTemplate) {
		this.emailTemplate = emailTemplate;
		syncWidgetsToEntity();
	}

}
