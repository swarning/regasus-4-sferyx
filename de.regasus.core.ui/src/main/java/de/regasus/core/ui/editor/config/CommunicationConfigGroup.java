package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.CommunicationConfigParameter;
import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.Communication;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;


public class CommunicationConfigGroup extends Group {

	// the entity
	private CommunicationConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private CommunicationConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets communicationWidgets;

	private FieldConfigWidgets phone1Widgets;
	private FieldConfigWidgets mobile1Widgets;
	private FieldConfigWidgets fax1Widgets;
	private FieldConfigWidgets email1Widgets;

	private FieldConfigWidgets phone2Widgets;
	private FieldConfigWidgets mobile2Widgets;
	private FieldConfigWidgets fax2Widgets;
	private FieldConfigWidgets email2Widgets;

	private FieldConfigWidgets phone3Widgets;
	private FieldConfigWidgets mobile3Widgets;
	private FieldConfigWidgets fax3Widgets;
	private FieldConfigWidgets email3Widgets;

	private FieldConfigWidgets wwwWidgets;



	public CommunicationConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false));
		setText( AbstractPerson.COMMUNICATION.getString() );


		communicationWidgets = new FieldConfigWidgets(this, AbstractPerson.COMMUNICATION.getString());
		communicationWidgets.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateEnabledStatus();
			}
		});


		phone1Widgets = new FieldConfigWidgets(this, Communication.PHONE1.getString());
		mobile1Widgets = new FieldConfigWidgets(this, Communication.MOBILE1.getString());
		fax1Widgets = new FieldConfigWidgets(this, Communication.FAX1.getString());
		email1Widgets = new FieldConfigWidgets(this, Communication.EMAIL1.getString());

		SWTHelper.verticalSpace(this);
		phone2Widgets = new FieldConfigWidgets(this, Communication.PHONE2.getString());
		mobile2Widgets = new FieldConfigWidgets(this, Communication.MOBILE2.getString());
		fax2Widgets = new FieldConfigWidgets(this, Communication.FAX2.getString());
		email2Widgets = new FieldConfigWidgets(this, Communication.EMAIL2.getString());

		SWTHelper.verticalSpace(this);
		phone3Widgets = new FieldConfigWidgets(this, Communication.PHONE3.getString());
		mobile3Widgets = new FieldConfigWidgets(this, Communication.MOBILE3.getString());
		fax3Widgets = new FieldConfigWidgets(this, Communication.FAX3.getString());
		email3Widgets = new FieldConfigWidgets(this, Communication.EMAIL3.getString());

		SWTHelper.verticalSpace(this);
		wwwWidgets = new FieldConfigWidgets(this, Communication.WWW.getString());
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(CommunicationConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		updateEnabledStatus();
	}


	private void updateEnabledStatus() {
		/* visibility of communicationWidgets depends on
		 * - enable-state of the Group
		 * - the setting of corresponding Admin-Config
		 */

		/* Use getEnabled() instead of isEnabled(), because isEnabled() returns only true if the
		 * Control and all its parent controls are enabled, whereas the result of getEnabled()
		 * relates only to the Control itself.
		 * For some reason, isEnbaled() returns false.
		 */
		boolean enabled = getEnabled();

		communicationWidgets.setEnabled(enabled && adminConfigParameter.isVisible());

		// visibility of all other widgets depends further on the current value of communicationWidgets ...
		// and on the setting of the current field in globalAdminConfig
		enabled = enabled && communicationWidgets.getVisible();

		phone1Widgets.setEnabled(	enabled && adminConfigParameter.getPhone1ConfigParameter().isVisible());
		mobile1Widgets.setEnabled(	enabled && adminConfigParameter.getMobile1ConfigParameter().isVisible());
		fax1Widgets.setEnabled(		enabled && adminConfigParameter.getFax1ConfigParameter().isVisible());
		email1Widgets.setEnabled(	enabled && adminConfigParameter.getEmail1ConfigParameter().isVisible());

		phone2Widgets.setEnabled(	enabled && adminConfigParameter.getPhone2ConfigParameter().isVisible());
		mobile2Widgets.setEnabled(	enabled && adminConfigParameter.getMobile2ConfigParameter().isVisible());
		fax2Widgets.setEnabled(		enabled && adminConfigParameter.getFax2ConfigParameter().isVisible());
		email2Widgets.setEnabled(	enabled && adminConfigParameter.getEmail2ConfigParameter().isVisible());

		phone3Widgets.setEnabled(	enabled && adminConfigParameter.getPhone3ConfigParameter().isVisible());
		mobile3Widgets.setEnabled(	enabled && adminConfigParameter.getMobile3ConfigParameter().isVisible());
		fax3Widgets.setEnabled(		enabled && adminConfigParameter.getFax3ConfigParameter ().isVisible());
		email3Widgets.setEnabled(	enabled && adminConfigParameter.getEmail3ConfigParameter().isVisible());

		wwwWidgets.setEnabled(		enabled && adminConfigParameter.getWwwConfigParameter().isVisible());
	}


	public void addModifyListener(ModifyListener modifyListener) {
		communicationWidgets.addModifyListener(modifyListener);

		phone1Widgets.addModifyListener(modifyListener);
		mobile1Widgets.addModifyListener(modifyListener);
		fax1Widgets.addModifyListener(modifyListener);
		email1Widgets.addModifyListener(modifyListener);

		phone2Widgets.addModifyListener(modifyListener);
		mobile2Widgets.addModifyListener(modifyListener);
		fax2Widgets.addModifyListener(modifyListener);
		email2Widgets.addModifyListener(modifyListener);

		phone3Widgets.addModifyListener(modifyListener);
		mobile3Widgets.addModifyListener(modifyListener);
		fax3Widgets.addModifyListener(modifyListener);
		email3Widgets.addModifyListener(modifyListener);

		wwwWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						communicationWidgets.syncWidgetsToEntity();

						phone1Widgets.syncWidgetsToEntity();
						mobile1Widgets.syncWidgetsToEntity();
						fax1Widgets.syncWidgetsToEntity();
						email1Widgets.syncWidgetsToEntity();

						phone2Widgets.syncWidgetsToEntity();
						mobile2Widgets.syncWidgetsToEntity();
						fax2Widgets.syncWidgetsToEntity();
						email2Widgets.syncWidgetsToEntity();

						phone3Widgets.syncWidgetsToEntity();
						mobile3Widgets.syncWidgetsToEntity();
						fax3Widgets.syncWidgetsToEntity();
						email3Widgets.syncWidgetsToEntity();

						wwwWidgets.syncWidgetsToEntity();

						updateEnabledStatus();
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
			communicationWidgets.syncEntityToWidgets();

			phone1Widgets.syncEntityToWidgets();
			mobile1Widgets.syncEntityToWidgets();
			fax1Widgets.syncEntityToWidgets();
			email1Widgets.syncEntityToWidgets();

			phone2Widgets.syncEntityToWidgets();
			mobile2Widgets.syncEntityToWidgets();
			fax2Widgets.syncEntityToWidgets();
			email2Widgets.syncEntityToWidgets();

			phone3Widgets.syncEntityToWidgets();
			mobile3Widgets.syncEntityToWidgets();
			fax3Widgets.syncEntityToWidgets();
			email3Widgets.syncEntityToWidgets();

			wwwWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(CommunicationConfigParameter configParameter) {
		this.configParameter = configParameter;

		// set entity to other composites
		communicationWidgets.setFieldConfigParameter(configParameter);

		phone1Widgets.setFieldConfigParameter(configParameter.getPhone1ConfigParameter());
		mobile1Widgets.setFieldConfigParameter(configParameter.getMobile1ConfigParameter());
		fax1Widgets.setFieldConfigParameter(configParameter.getFax1ConfigParameter());
		email1Widgets.setFieldConfigParameter(configParameter.getEmail1ConfigParameter());

		phone2Widgets.setFieldConfigParameter(configParameter.getPhone2ConfigParameter());
		mobile2Widgets.setFieldConfigParameter(configParameter.getMobile2ConfigParameter());
		fax2Widgets.setFieldConfigParameter(configParameter.getFax2ConfigParameter());
		email2Widgets.setFieldConfigParameter(configParameter.getEmail2ConfigParameter());

		phone3Widgets.setFieldConfigParameter(configParameter.getPhone3ConfigParameter());
		mobile3Widgets.setFieldConfigParameter(configParameter.getMobile3ConfigParameter());
		fax3Widgets.setFieldConfigParameter(configParameter.getFax3ConfigParameter());
		email3Widgets.setFieldConfigParameter(configParameter.getEmail3ConfigParameter());

		wwwWidgets.setFieldConfigParameter(configParameter.getWwwConfigParameter());

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
