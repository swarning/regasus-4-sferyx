package de.regasus.event.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

//REFERENCE
public class EventBadgeGroup extends Group {

	// the entity
	private EventVO eventVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private Button badgeDemandsRegistrationButton;
	private Button badgeDemandsFullPaymentAsBenefitRecipientButton;
	private Button badgeDemandsFullPaymentAsInvoiceRecipientButton;

	// *
	// * Widgets
	// **************************************************************************


	public EventBadgeGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		createWidgets();
	}


	private void createWidgets() throws Exception {
		setLayout(new GridLayout(1, false));
		setText(I18N.EventEditor_BadgeSettings);


		{
			badgeDemandsRegistrationButton = new Button(this, SWT.CHECK);
			badgeDemandsRegistrationButton.setText(ParticipantLabel.Event_BadgeDemandsRegistration.getString());

			badgeDemandsRegistrationButton.addSelectionListener(modifySupport);
		}

		{
			badgeDemandsFullPaymentAsBenefitRecipientButton = new Button(this, SWT.CHECK);
			badgeDemandsFullPaymentAsBenefitRecipientButton.setText(ParticipantLabel.Event_BadgeDemandsFullPaymentAsBenefitRecipient.getString());

			badgeDemandsFullPaymentAsBenefitRecipientButton.addSelectionListener(modifySupport);
		}

		{
			badgeDemandsFullPaymentAsInvoiceRecipientButton = new Button(this, SWT.CHECK);
			badgeDemandsFullPaymentAsInvoiceRecipientButton.setText(ParticipantLabel.Event_BadgeDemandsFullPaymentAsInvoiceRecipient.getString());

			badgeDemandsFullPaymentAsInvoiceRecipientButton.addSelectionListener(modifySupport);
		}
	}


	public void setEvent(EventVO eventVO) {
		this.eventVO = eventVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						badgeDemandsRegistrationButton.setSelection( eventVO.isBadgeDemandsRegistration() );
						badgeDemandsFullPaymentAsBenefitRecipientButton.setSelection( eventVO.isBadgeDemandsFullPaymentAsBenefitRecipient() );
						badgeDemandsFullPaymentAsInvoiceRecipientButton.setSelection( eventVO.isBadgeDemandsFullPaymentAsInvoiceRecipient() );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null) {
			eventVO.setBadgeDemandsRegistration( badgeDemandsRegistrationButton.getSelection() );
			eventVO.setBadgeDemandsFullPaymentAsBenefitRecipient( badgeDemandsFullPaymentAsBenefitRecipientButton.getSelection() );
			eventVO.setBadgeDemandsFullPaymentAsInvoiceRecipient( badgeDemandsFullPaymentAsInvoiceRecipientButton.getSelection() );
		}
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
