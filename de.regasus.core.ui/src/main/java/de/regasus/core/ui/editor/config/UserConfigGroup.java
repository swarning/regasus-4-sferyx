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

public class UserConfigGroup extends Group {

	// the entity
	private PersonConfigParameter configParameter;

	// filter
	private PersonConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets userNameWidgets;
	private FieldConfigWidgets passwordWidgets;


	/** Array with all widgets to make some methods shorter*/
	private FieldConfigWidgets[] fieldConfigWidgetsList;


	public UserConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(ContactLabel.UserCredentials.getString());

		// Widgets
		userNameWidgets = new FieldConfigWidgets(this, Person.USER_NAME.getString());
		passwordWidgets = new FieldConfigWidgets(this, Person.PASSWORD.getString());

		fieldConfigWidgetsList = new FieldConfigWidgets[] {
			userNameWidgets,
			passwordWidgets
		};
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

		userNameWidgets.setEnabled(			enabled && adminConfigParameter.getUserNameConfigParameter().isVisible());
		passwordWidgets.setEnabled(			enabled && adminConfigParameter.getPasswordConfigParameter().isVisible());
	}


	public void addModifyListener(ModifyListener modifyListener) {
		for (FieldConfigWidgets fieldConfigWidgets : fieldConfigWidgetsList) {
			fieldConfigWidgets.addModifyListener(modifyListener);
		}
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {

				@Override
				public void run() {
					try {
						for (FieldConfigWidgets fcw : fieldConfigWidgetsList) {
							fcw.syncWidgetsToEntity();
						}
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
			for (FieldConfigWidgets fieldConfigWidgets : fieldConfigWidgetsList) {
				fieldConfigWidgets.syncEntityToWidgets();
			}
		}
	}


	public void setConfigParameter(PersonConfigParameter configParameter) {
		this.configParameter = configParameter;

		userNameWidgets.setFieldConfigParameter(configParameter.getUserNameConfigParameter());
		passwordWidgets.setFieldConfigParameter(configParameter.getPasswordConfigParameter());
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
