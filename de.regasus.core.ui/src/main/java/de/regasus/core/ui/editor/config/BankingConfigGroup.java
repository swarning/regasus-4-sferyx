package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.PersonConfigParameter;
import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;


public class BankingConfigGroup extends Group {

	// the entity
	private PersonConfigParameter configParameter;

	// the entity
	private PersonConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets creditCardWidgets;
	private FieldConfigWidgets bankWidgets;


	public BankingConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(ContactLabel.Banking.getString());

		bankWidgets = new FieldConfigWidgets(this, ContactLabel.bankAccount.getString());
		creditCardWidgets = new FieldConfigWidgets(this, AbstractPerson.CREDIT_CARD.getString());
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

		bankWidgets.setEnabled(			enabled && adminConfigParameter.getBankConfigParameter().isVisible());
		creditCardWidgets.setEnabled(	enabled && adminConfigParameter.getCreditCardConfigParameter().isVisible());
	}


	public void addModifiyListener(ModifyListener modifyListener) {
		bankWidgets.addModifyListener(modifyListener);
		creditCardWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {

				@Override
				public void run() {
					try {
						bankWidgets.syncWidgetsToEntity();
						creditCardWidgets.syncWidgetsToEntity();
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
			bankWidgets.syncEntityToWidgets();
			creditCardWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(PersonConfigParameter personConfigParameter) {
		this.configParameter = personConfigParameter;

		bankWidgets.setFieldConfigParameter(personConfigParameter.getBankConfigParameter());
		creditCardWidgets.setFieldConfigParameter(personConfigParameter.getCreditCardConfigParameter());
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
