package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.PreferredPaymentTypeConfigParameter;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class PreferredPaymentTypeConfigGroup extends Group {
	
	// the entity
	private PreferredPaymentTypeConfigParameter configParameter;
	
	// corresponding admin Config that controls which settings are enabled
	private PreferredPaymentTypeConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets preferredPaymentTypeWidgets;
	private FieldConfigWidgets programmeBookingWidgets;
	private FieldConfigWidgets hotelBookingWidgets;
	
	

	public PreferredPaymentTypeConfigGroup(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false));
		setText(ParticipantLabel.PreferredPaymentType.getString());
		
		preferredPaymentTypeWidgets = new FieldConfigWidgets(this, ParticipantLabel.PreferredPaymentType.getString());
		preferredPaymentTypeWidgets.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateEnabledStatus();
			}
		});
		
		programmeBookingWidgets = new FieldConfigWidgets(this, ParticipantLabel.ProgrammeBookings.getString());
		hotelBookingWidgets = new FieldConfigWidgets(this, ParticipantLabel.HotelBookings.getString());
	}
	
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}
	
	
	public void setAdminConfigParameter(PreferredPaymentTypeConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;
		
		updateEnabledStatus();
	}
	
	
	private void updateEnabledStatus() {
		/* visibility of preferredPaymentTypeWidgets depends on
		 * - enable-state of the Group
		 * - the setting of corresponding Admin-Config
		 */

		/* Use getEnabled() instead of isEnabled(), because isEnabled() returns only true if the
		 * Control and all its parent controls are enabled, whereas the result of getEnabled()
		 * relates only to the Control itself.
		 * For some reason, isEnbaled() returns false.
		 */
		boolean enabled = getEnabled();
		
		preferredPaymentTypeWidgets.setEnabled(enabled && adminConfigParameter.isVisible());
		
		// visibility of all other widgets depends further on the current value of preferredPaymentTypeWidgets ...
		// and on the setting of the current field in globalAdminConfig
		enabled = enabled && preferredPaymentTypeWidgets.getVisible();
		
		programmeBookingWidgets.setEnabled( enabled && adminConfigParameter.getProgrammeBookingConfigParameter().isVisible());
		hotelBookingWidgets.setEnabled(		enabled && adminConfigParameter.getHotelBookingConfigParameter().isVisible());
	}
	
	
	public void addModifyListener(ModifyListener modifyListener) {
		preferredPaymentTypeWidgets.addModifyListener(modifyListener);
		
		programmeBookingWidgets.addModifyListener(modifyListener);
		hotelBookingWidgets.addModifyListener(modifyListener);
	}
	
	
	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						preferredPaymentTypeWidgets.syncWidgetsToEntity();

						programmeBookingWidgets.syncWidgetsToEntity();
						hotelBookingWidgets.syncWidgetsToEntity();

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
			preferredPaymentTypeWidgets.syncEntityToWidgets();

			programmeBookingWidgets.syncEntityToWidgets();
			hotelBookingWidgets.syncEntityToWidgets();
		}
	}
	
	
	public void setConfigParameter(PreferredPaymentTypeConfigParameter configParameter) {
		this.configParameter = configParameter;
		
		preferredPaymentTypeWidgets.setFieldConfigParameter(configParameter);
		
		programmeBookingWidgets.setFieldConfigParameter(configParameter.getProgrammeBookingConfigParameter());
		hotelBookingWidgets.setFieldConfigParameter(configParameter.getHotelBookingConfigParameter());
	}
	
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
