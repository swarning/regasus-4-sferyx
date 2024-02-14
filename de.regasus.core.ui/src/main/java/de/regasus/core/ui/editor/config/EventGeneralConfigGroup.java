package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.EventConfigParameter;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.CommonI18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class EventGeneralConfigGroup extends Group {

	// the entity
	private EventConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private EventConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets externalIdWidgets;
	private FieldConfigWidgets geoDataWidgets;
	private FieldConfigWidgets participantWebTokenClaimsWidgets;


	public EventGeneralConfigGroup(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		setText(CoreI18N.Config_General);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );


		externalIdWidgets = new FieldConfigWidgets(this, CommonI18N.ExternalID.getString());
		geoDataWidgets = new FieldConfigWidgets(this, CommonI18N.GeoData.getString());
		participantWebTokenClaimsWidgets = new FieldConfigWidgets(this, EventVO.PARTICIPANT_WEB_TOKEN_CLAIMS.getLabel());
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(EventConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		updateEnabledStatus();
	}


	private void updateEnabledStatus() {
		/* visibility of widgets depends on
		 * - enable-state of the Group
		 * - the setting of globalAdminConfig
		 */

		/* Use getEnabled() instead of isEnabled(), because isEnabled() returns only true if the
		 * Control and all its parent controls are enabled, whereas the result of getEnabled()
		 * relates only to the Control itself.
		 * For some reason, isEnbaled() returns false.
		 */
		boolean enabled = getEnabled();
		boolean externalIdVisible = adminConfigParameter.getExternalIdConfigParameter().isVisible();
		boolean geoDataVisible = adminConfigParameter.getGeoDataConfigParameter().isVisible();
		boolean participantWebTokenClaimsVisible = adminConfigParameter.getParticipantWebTokenClaimsConfigParameter().isVisible();

		// visibility of externalIdWidgets depends on the setting of globalAdminConfig
		externalIdWidgets.setEnabled(enabled && externalIdVisible);
		geoDataWidgets.setEnabled(enabled && geoDataVisible);
		participantWebTokenClaimsWidgets.setEnabled(enabled && participantWebTokenClaimsVisible);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		externalIdWidgets.addModifyListener(modifyListener);
		geoDataWidgets.addModifyListener(modifyListener);
		participantWebTokenClaimsWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						externalIdWidgets.syncWidgetsToEntity();
						geoDataWidgets.syncWidgetsToEntity();
						participantWebTokenClaimsWidgets.syncWidgetsToEntity();
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
			externalIdWidgets.syncEntityToWidgets();
			geoDataWidgets.syncEntityToWidgets();
			participantWebTokenClaimsWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(EventConfigParameter configParameter) {
		this.configParameter = configParameter;

		externalIdWidgets.setFieldConfigParameter( configParameter.getExternalIdConfigParameter() );
		geoDataWidgets.setFieldConfigParameter( configParameter.getGeoDataConfigParameter() );
		participantWebTokenClaimsWidgets.setFieldConfigParameter( configParameter.getParticipantWebTokenClaimsConfigParameter() );
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
