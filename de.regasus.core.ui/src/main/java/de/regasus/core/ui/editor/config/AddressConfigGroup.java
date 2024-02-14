package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.AddressConfigParameter;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;


public class AddressConfigGroup extends Group {

	// the entity
	private AddressConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private AddressConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets address1Widgets;
	private FieldConfigWidgets address2Widgets;
	private FieldConfigWidgets address3Widgets;
	private FieldConfigWidgets address4Widgets;
	private FieldConfigWidgets addressRoleWidgets;
	private FieldConfigWidgets organisationWidgets;
	private FieldConfigWidgets departmentWidgets;
	private FieldConfigWidgets addresseeWidgets;
	private FieldConfigWidgets functionWidgets;
	private FieldConfigWidgets stateWidgets;


	public AddressConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false));
		setText(ContactLabel.Address.getString());

		address1Widgets = new FieldConfigWidgets(this, ContactLabel.Address.getString() + " 1");
		address2Widgets = new FieldConfigWidgets(this, ContactLabel.Address.getString() + " 2");
		address3Widgets = new FieldConfigWidgets(this, ContactLabel.Address.getString() + " 3");
		address4Widgets = new FieldConfigWidgets(this, ContactLabel.Address.getString() + " 4");
		addressRoleWidgets = new FieldConfigWidgets(this, ContactLabel.AddressRole.getString());
		organisationWidgets = new FieldConfigWidgets(this, Address.ORGANISATION.getString());
		departmentWidgets = new FieldConfigWidgets(this, Address.DEPARTMENT.getString());
		addresseeWidgets = new FieldConfigWidgets(this, Address.ADDRESSEE.getString());
		functionWidgets = new FieldConfigWidgets(this, Address.FUNCTION.getString());
		stateWidgets = new FieldConfigWidgets(this, Address.STATE.getString());
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(AddressConfigParameter adminConfigParameter) {
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

		address1Widgets.setEnabled(		enabled && adminConfigParameter.getAddress1ConfigParameter().isVisible());
		address2Widgets.setEnabled(		enabled && adminConfigParameter.getAddress2ConfigParameter().isVisible());
		address3Widgets.setEnabled(		enabled && adminConfigParameter.getAddress3ConfigParameter().isVisible());
		address4Widgets.setEnabled(		enabled && adminConfigParameter.getAddress4ConfigParameter().isVisible());
		addressRoleWidgets.setEnabled(	enabled && adminConfigParameter.getAddressRoleConfigParameter().isVisible());
		organisationWidgets.setEnabled(	enabled && adminConfigParameter.getOrganisationConfigParameter().isVisible());
		departmentWidgets.setEnabled(	enabled && adminConfigParameter.getDepartmentConfigParameter().isVisible());
		addresseeWidgets.setEnabled(	enabled && adminConfigParameter.getAddresseeConfigParameter().isVisible());
		functionWidgets.setEnabled(		enabled && adminConfigParameter.getFunctionConfigParameter().isVisible());
		stateWidgets.setEnabled(		enabled && adminConfigParameter.getStateConfigParameter().isVisible());
	}


	public void addModifyListener(ModifyListener modifyListener) {
		address1Widgets.addModifyListener(modifyListener);
		address2Widgets.addModifyListener(modifyListener);
		address3Widgets.addModifyListener(modifyListener);
		address4Widgets.addModifyListener(modifyListener);
		addressRoleWidgets.addModifyListener(modifyListener);
		organisationWidgets.addModifyListener(modifyListener);
		departmentWidgets.addModifyListener(modifyListener);
		addresseeWidgets.addModifyListener(modifyListener);
		functionWidgets.addModifyListener(modifyListener);
		stateWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						address1Widgets.syncWidgetsToEntity();
						address2Widgets.syncWidgetsToEntity();
						address3Widgets.syncWidgetsToEntity();
						address4Widgets.syncWidgetsToEntity();
						addressRoleWidgets.syncWidgetsToEntity();
						organisationWidgets.syncWidgetsToEntity();
						departmentWidgets.syncWidgetsToEntity();
						addresseeWidgets.syncWidgetsToEntity();
						functionWidgets.syncWidgetsToEntity();
						stateWidgets.syncWidgetsToEntity();
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
			address1Widgets.syncEntityToWidgets();
			address2Widgets.syncEntityToWidgets();
			address3Widgets.syncEntityToWidgets();
			address4Widgets.syncEntityToWidgets();
			addressRoleWidgets.syncEntityToWidgets();
			organisationWidgets.syncEntityToWidgets();
			departmentWidgets.syncEntityToWidgets();
			addresseeWidgets.syncEntityToWidgets();
			functionWidgets.syncEntityToWidgets();
			stateWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(AddressConfigParameter configParameter) {
		this.configParameter = configParameter;

		// set entity to other composites
		address1Widgets.setFieldConfigParameter(configParameter.getAddress1ConfigParameter());
		address2Widgets.setFieldConfigParameter(configParameter.getAddress2ConfigParameter());
		address3Widgets.setFieldConfigParameter(configParameter.getAddress3ConfigParameter());
		address4Widgets.setFieldConfigParameter(configParameter.getAddress4ConfigParameter());
		addressRoleWidgets.setFieldConfigParameter(configParameter.getAddressRoleConfigParameter());
		organisationWidgets.setFieldConfigParameter(configParameter.getOrganisationConfigParameter());
		departmentWidgets.setFieldConfigParameter(configParameter.getDepartmentConfigParameter());
		addresseeWidgets.setFieldConfigParameter(configParameter.getAddresseeConfigParameter());
		functionWidgets.setFieldConfigParameter(configParameter.getFunctionConfigParameter());
		stateWidgets.setFieldConfigParameter(configParameter.getStateConfigParameter());

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
