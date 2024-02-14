package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.MembershipConfigParameter;
import com.lambdalogic.messeinfo.contact.data.Membership;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;


public class MembershipConfigGroup extends Group {

	// the entity
	private MembershipConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private MembershipConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets membershipWidgets;

	private FieldConfigWidgets statusWidgets;
	private FieldConfigWidgets typeWidgets;
	private FieldConfigWidgets beginWidgets;
	private FieldConfigWidgets endWidgets;


	public MembershipConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false));
		setText( Participant.MEMBERSHIP.getString() );


		membershipWidgets = new FieldConfigWidgets(this, Participant.MEMBERSHIP.getString());
		membershipWidgets.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateEnabledStatus();
			}
		});


		statusWidgets = new FieldConfigWidgets(this, Membership.STATUS.getLabel());
		typeWidgets = new FieldConfigWidgets(this, Membership.TYPE.getLabel());
		beginWidgets = new FieldConfigWidgets(this, Membership.BEGIN.getLabel());
		endWidgets = new FieldConfigWidgets(this, Membership.END.getLabel());
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(MembershipConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		updateEnabledStatus();
	}


	private void updateEnabledStatus() {
		/* visibility of membershipWidgets depends on
		 * - enable-state of the Group
		 * - the setting of corresponding Admin-Config
		 */

		/* Use getEnabled() instead of isEnabled(), because isEnabled() returns only true if the
		 * Control and all its parent controls are enabled, whereas the result of getEnabled()
		 * relates only to the Control itself.
		 * For some reason, isEnbaled() returns false.
		 */
		boolean enabled = getEnabled();

		membershipWidgets.setEnabled(enabled && adminConfigParameter.isVisible());

		// visibility of all other widgets depends further on the current value of membershipWidgets ...
		// and on the setting of the current field in globalAdminConfig
		enabled = enabled && membershipWidgets.getVisible();

		statusWidgets.setEnabled(	enabled && adminConfigParameter.getStatusConfigParameter().isVisible());
		typeWidgets.setEnabled(		enabled && adminConfigParameter.getTypeConfigParameter().isVisible());
		beginWidgets.setEnabled(	enabled && adminConfigParameter.getBeginConfigParameter().isVisible());
		endWidgets.setEnabled(		enabled && adminConfigParameter.getEndConfigParameter().isVisible());
	}


	public void addModifyListener(ModifyListener modifyListener) {
		membershipWidgets.addModifyListener(modifyListener);

		statusWidgets.addModifyListener(modifyListener);
		typeWidgets.addModifyListener(modifyListener);
		beginWidgets.addModifyListener(modifyListener);
		endWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						membershipWidgets.syncWidgetsToEntity();

						statusWidgets.syncWidgetsToEntity();
						typeWidgets.syncWidgetsToEntity();
						beginWidgets.syncWidgetsToEntity();
						endWidgets.syncWidgetsToEntity();

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
			membershipWidgets.syncEntityToWidgets();

			statusWidgets.syncEntityToWidgets();
			typeWidgets.syncEntityToWidgets();
			beginWidgets.syncEntityToWidgets();
			endWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(MembershipConfigParameter configParameter) {
		this.configParameter = configParameter;

		// set entity to other composites
		membershipWidgets.setFieldConfigParameter(configParameter);

		statusWidgets.setFieldConfigParameter(configParameter.getStatusConfigParameter());
		typeWidgets.setFieldConfigParameter(configParameter.getTypeConfigParameter());
		beginWidgets.setFieldConfigParameter(configParameter.getBeginConfigParameter());
		endWidgets.setFieldConfigParameter(configParameter.getEndConfigParameter());

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
