package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.PersonConfigParameter;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class ApprovalConfigGroup extends Group {

	// the entity
	private PersonConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private PersonConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets promotionWidgets;
	private FieldConfigWidgets programmeConditionsWidgets;
	private FieldConfigWidgets programmeCancelConditionsWidgets;
	private FieldConfigWidgets hotelConditionsWidgets;
	private FieldConfigWidgets hotelCancelConditionsWidgets;


	public ApprovalConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false));
		setText(ContactLabel.Approvals.getString());

		// Widgets

		promotionWidgets = new FieldConfigWidgets(this, Person.PROMOTION.getString());
		programmeConditionsWidgets = new FieldConfigWidgets(this, ContactLabel.programmeConditions.getString());
		programmeCancelConditionsWidgets = new FieldConfigWidgets(this, ContactLabel.programmeCancelConditions.getString());
		hotelConditionsWidgets = new FieldConfigWidgets(this, ContactLabel.hotelConditions.getString());
		hotelCancelConditionsWidgets = new FieldConfigWidgets(this, ContactLabel.hotelCancelConditions.getString());
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(PersonConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		updateEnabledStatus();
	}


	private void updateEnabledStatus() {
		/* visibility of widgets depends on
		 * - enable-state of the Group
		 * - the setting of corresponding Admin-Config
		 */

		/* Use getEnabled() instead of isEnabled(), because isEnabled() returns only true if the
		 * Control and all its parent controls are enabled, whereas the result of getEnabled()
		 * relates only to the Control itself.
		 * For some reason, isEnbaled() returns false.
		 */
		boolean enabled = getEnabled();

		promotionWidgets.setEnabled(enabled && adminConfigParameter.getPromotionConfigParameter().isVisible());
		programmeConditionsWidgets.setEnabled(enabled && adminConfigParameter.getProgrammeConditionsAcceptedConfigParameter().isVisible());
		programmeCancelConditionsWidgets.setEnabled(enabled && adminConfigParameter.getProgrammeCancelConditionsAcceptedConfigParameter().isVisible());
		hotelConditionsWidgets.setEnabled(enabled && adminConfigParameter.getHotelConditionsAcceptedConfigParameter().isVisible());
		hotelCancelConditionsWidgets.setEnabled(enabled && adminConfigParameter.getHotelCancelConditionsAcceptedConfigParameter().isVisible());
	}


	public void addModifyListener(ModifyListener modifyListener) {
		promotionWidgets.addModifyListener(modifyListener);
		programmeConditionsWidgets.addModifyListener(modifyListener);
		programmeCancelConditionsWidgets.addModifyListener(modifyListener);
		hotelConditionsWidgets.addModifyListener(modifyListener);
		hotelCancelConditionsWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						promotionWidgets.syncWidgetsToEntity();
						programmeConditionsWidgets.syncWidgetsToEntity();
						programmeCancelConditionsWidgets.syncWidgetsToEntity();
						hotelConditionsWidgets.syncWidgetsToEntity();
						hotelCancelConditionsWidgets.syncWidgetsToEntity();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (configParameter != null) {
			promotionWidgets.syncEntityToWidgets();
			programmeConditionsWidgets.syncEntityToWidgets();
			programmeCancelConditionsWidgets.syncEntityToWidgets();
			hotelConditionsWidgets.syncEntityToWidgets();
			hotelCancelConditionsWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(PersonConfigParameter configParameter) {
		this.configParameter = configParameter;

		promotionWidgets.setFieldConfigParameter(configParameter.getPromotionConfigParameter());
		programmeConditionsWidgets.setFieldConfigParameter(configParameter.getProgrammeConditionsAcceptedConfigParameter());
		programmeCancelConditionsWidgets.setFieldConfigParameter(configParameter.getProgrammeCancelConditionsAcceptedConfigParameter());
		hotelConditionsWidgets.setFieldConfigParameter(configParameter.getHotelConditionsAcceptedConfigParameter());
		hotelCancelConditionsWidgets.setFieldConfigParameter(configParameter.getHotelCancelConditionsAcceptedConfigParameter());
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
