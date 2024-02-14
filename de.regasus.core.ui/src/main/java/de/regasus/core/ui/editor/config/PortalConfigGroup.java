package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.PortalConfigParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.portal.PortalI18N;

public class PortalConfigGroup extends Group {

	// the entity
	private PortalConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private PortalConfigParameter adminConfigParameter;

	// Widgets
	private BooleanConfigWidgets portalVisibleWidgets;
	private BooleanConfigWidgets createWidgets;
	private BooleanConfigWidgets specialConditionsWidgets;
	private BooleanConfigWidgets scriptComponentWidgets;
	private BooleanConfigWidgets offeringFilterWidgets;
	private BooleanConfigWidgets bookingRulesWidgets;


	public PortalConfigGroup(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		setLayout( new GridLayout(BooleanConfigWidgets.NUM_COLS, false) );
		setText( PortalI18N.Portal.getString() );

		portalVisibleWidgets =     new BooleanConfigWidgets(this, PortalI18N.Portal.getString());
		createWidgets =            new BooleanConfigWidgets(this, CoreI18N.Config_CreatePortal);
		specialConditionsWidgets = new BooleanConfigWidgets(this, CoreI18N.Config_SpecialConditions);
		scriptComponentWidgets =   new BooleanConfigWidgets(this, CoreI18N.Config_ScriptComponent);
		offeringFilterWidgets =    new BooleanConfigWidgets(this, CoreI18N.Config_OfferingFilter);
		bookingRulesWidgets =      new BooleanConfigWidgets(this, CoreI18N.Config_BookingRules);

		portalVisibleWidgets.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateEnabledStatus();
			}
		});
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(PortalConfigParameter adminConfigParameter) {
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

		// determine enable status for widgets
		boolean enabled = getEnabled();
		boolean portalVisibleEnabled = enabled && adminConfigParameter.getVisible();

		boolean portalVisible = portalVisibleWidgets.getValue();
		boolean createEnabled =            portalVisible && adminConfigParameter.getCreate();
		boolean specialConditionsEnabled = portalVisible && adminConfigParameter.getSpecialConditions();
		boolean scriptComponentEnabled =   portalVisible && adminConfigParameter.getScriptComponent();
		boolean offeringFilterEnabled =    portalVisible && adminConfigParameter.getOfferingFilter();
		boolean bookingRulesEnabled =      portalVisible && adminConfigParameter.getBookingRules();

		// enable/disable widgets
		portalVisibleWidgets.setEnabled(portalVisibleEnabled);
		createWidgets.setEnabled(createEnabled);
		specialConditionsWidgets.setEnabled(specialConditionsEnabled);
		scriptComponentWidgets.setEnabled(scriptComponentEnabled);
		offeringFilterWidgets.setEnabled(offeringFilterEnabled);
		bookingRulesWidgets.setEnabled(bookingRulesEnabled);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		portalVisibleWidgets    .addModifyListener(modifyListener);
		createWidgets           .addModifyListener(modifyListener);
		specialConditionsWidgets.addModifyListener(modifyListener);
		scriptComponentWidgets  .addModifyListener(modifyListener);
		offeringFilterWidgets   .addModifyListener(modifyListener);
		bookingRulesWidgets     .addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						portalVisibleWidgets    .setValue( configParameter.getVisible() );
						createWidgets           .setValue( configParameter.getCreate() );
						specialConditionsWidgets.setValue( configParameter.getSpecialConditions() );
						scriptComponentWidgets  .setValue( configParameter.getScriptComponent() );
						offeringFilterWidgets   .setValue( configParameter.getOfferingFilter() );
						bookingRulesWidgets     .setValue( configParameter.getBookingRules() );

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
			configParameter.setVisible(           portalVisibleWidgets.getValue() );
			configParameter.setCreate(            createWidgets.getValue() );
			configParameter.setSpecialConditions( specialConditionsWidgets.getValue() );
			configParameter.setScriptComponent(   scriptComponentWidgets.getValue() );
			configParameter.setOfferingFilter(    offeringFilterWidgets.getValue() );
			configParameter.setBookingRules(      bookingRulesWidgets.getValue() );
		}
	}


	public void setConfigParameter(PortalConfigParameter configParameter) {
		this.configParameter = configParameter;

		// syncWidgetsToEntity() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
