package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.ConfigParameter;
import com.lambdalogic.messeinfo.config.parameter.HotelConfigParameter;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class HotelConfigGroup extends Group {

	// the entity
	private ConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private ConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets globalHotelWidgets;
	private FieldConfigWidgets hotelWidgets;
	private FieldConfigWidgets additionalPriceWidgets;
	private FieldConfigWidgets bookSizeWidgets;
	private FieldConfigWidgets publicSizeWidgets;
	private FieldConfigWidgets reminderWidgets;
	private FieldConfigWidgets costCoverageWidgets;


	private boolean globalScope;


	public HotelConfigGroup(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(HotelLabel.Hotel.getString());

		globalScope = (scope == ConfigScope.GLOBAL_ADMIN || scope == ConfigScope.GLOBAL_CUSTOMER);


		// add widgets

		if (globalScope) {
			globalHotelWidgets = new FieldConfigWidgets(
				this,
				CoreI18N.Config_GlobalHotelMasterData,
				CoreI18N.Config_GlobalHotelMasterData_toolTip
			);

			SWTHelper.horizontalLine(this);
		}

		hotelWidgets = new FieldConfigWidgets(
			this,
			CoreI18N.Config_Hotels,
			CoreI18N.Config_Hotels_toolTip
		);

		// observe hotelwidgets to enable/disable other widgets that depend on its settings
		hotelWidgets.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateEnabledStatus();
			}
		});


		additionalPriceWidgets = new FieldConfigWidgets(
			this,
			CoreI18N.Config_AdditionalPrice,
			CoreI18N.Config_AdditionalPrice_toolTip
		);


		bookSizeWidgets = new FieldConfigWidgets(
			this,
			CoreI18N.Config_BookSize,
			CoreI18N.Config_BookSize_toolTip
		);


		publicSizeWidgets = new FieldConfigWidgets(
			this,
			CoreI18N.Config_PublicSize,
			CoreI18N.Config_PublicSize_toolTip
		);


		reminderWidgets = new FieldConfigWidgets(
			this,
			CoreI18N.Config_Reminder,
			CoreI18N.Config_Reminder_toolTip
		);


		costCoverageWidgets = new FieldConfigWidgets(
			this,
			CoreI18N.Config_CostCoverage,
			CoreI18N.Config_CostCoverage_toolTip
		);
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(ConfigParameter adminConfigParameter) {
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
		boolean globalHotelVisible = adminConfigParameter.getHotelConfigParameter().isVisible();

		HotelConfigParameter hotelConfigParameter = adminConfigParameter.getEventConfigParameter().getHotelConfigParameter();
		boolean eventHotelVisible = hotelConfigParameter.isVisible();
		boolean additionalPriceVisible = hotelConfigParameter.getAdditionalPriceConfigParameter().isVisible();
		boolean bookSizeVisible = hotelConfigParameter.getBookSizeConfigParameter().isVisible();
		boolean publicSizeVisible = hotelConfigParameter.getPublicSizeConfigParameter().isVisible();
		boolean reminderVisible = hotelConfigParameter.getReminderConfigParameter().isVisible();
		boolean costCoverageVisible = hotelConfigParameter.getCostCoverageConfigParameter().isVisible();


		if (globalScope) {
			globalHotelWidgets.setEnabled(enabled && globalHotelVisible);
		}

		hotelWidgets.setEnabled(enabled && eventHotelVisible);

		// visibility of the other widgets depends further on the current value of hotelWidgets ...
		additionalPriceWidgets.setEnabled(enabled && hotelWidgets.getVisible() && additionalPriceVisible);
		bookSizeWidgets.setEnabled(enabled && hotelWidgets.getVisible() && bookSizeVisible);
		publicSizeWidgets.setEnabled(enabled && hotelWidgets.getVisible() && publicSizeVisible);
		reminderWidgets.setEnabled(enabled && hotelWidgets.getVisible() && reminderVisible);
		costCoverageWidgets.setEnabled(enabled && hotelWidgets.getVisible() && costCoverageVisible);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		if (globalScope) {
			globalHotelWidgets.addModifyListener(modifyListener);
		}
		hotelWidgets.addModifyListener(modifyListener);
		additionalPriceWidgets.addModifyListener(modifyListener);
		bookSizeWidgets.addModifyListener(modifyListener);
		publicSizeWidgets.addModifyListener(modifyListener);
		reminderWidgets.addModifyListener(modifyListener);
		costCoverageWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (globalScope) {
							globalHotelWidgets.syncWidgetsToEntity();
						}
						hotelWidgets.syncWidgetsToEntity();
						additionalPriceWidgets.syncWidgetsToEntity();
						bookSizeWidgets.syncWidgetsToEntity();
						publicSizeWidgets.syncWidgetsToEntity();
						reminderWidgets.syncWidgetsToEntity();
						costCoverageWidgets.syncWidgetsToEntity();

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
			if (globalScope) {
				globalHotelWidgets.syncEntityToWidgets();
			}
			hotelWidgets.syncEntityToWidgets();
			additionalPriceWidgets.syncEntityToWidgets();
			bookSizeWidgets.syncEntityToWidgets();
			publicSizeWidgets.syncEntityToWidgets();
			reminderWidgets.syncEntityToWidgets();
			costCoverageWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(ConfigParameter configParameter) {
		this.configParameter = configParameter;

		// set entity to other composites
		if (globalScope) {
			globalHotelWidgets.setFieldConfigParameter( configParameter.getHotelConfigParameter() );
		}


		HotelConfigParameter hotelConfigParameter = configParameter.getEventConfigParameter().getHotelConfigParameter();

		hotelWidgets.setFieldConfigParameter(hotelConfigParameter);
		additionalPriceWidgets.setFieldConfigParameter( hotelConfigParameter.getAdditionalPriceConfigParameter() );
		bookSizeWidgets.setFieldConfigParameter( hotelConfigParameter.getBookSizeConfigParameter() );
		publicSizeWidgets.setFieldConfigParameter( hotelConfigParameter.getPublicSizeConfigParameter() );
		reminderWidgets.setFieldConfigParameter( hotelConfigParameter.getReminderConfigParameter() );
		costCoverageWidgets.setFieldConfigParameter( hotelConfigParameter.getCostCoverageConfigParameter() );

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
