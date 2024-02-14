package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigLabel;
import com.lambdalogic.messeinfo.config.parameter.ConfigConfigParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class ConfigConfigGroup extends Group {

	// the entity
	private ConfigConfigParameter configConfigParameter;


	// Widgets
	private BooleanConfigWidgets allowCustomerConfigWidgets;


	public ConfigConfigGroup(
		Composite parent,
		int style
	) {
		super(parent, style);

		final GridLayout gridLayout = new GridLayout(BooleanConfigWidgets.NUM_COLS, false);
		setLayout(gridLayout);
		setText(CoreI18N.Config_CustomerConfiguration);


		allowCustomerConfigWidgets = new BooleanConfigWidgets(
			this,
			ConfigLabel.AllowCustomerConfig_Label.getString(),
			ConfigLabel.AllowCustomerConfig_Description.getString()
		);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		allowCustomerConfigWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configConfigParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						allowCustomerConfigWidgets.setValue(configConfigParameter.getAllowCustomerConfig());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (configConfigParameter != null) {
			configConfigParameter.setAllowCustomerConfig(allowCustomerConfigWidgets.getValue());
		}
	}


	public void setConfigConfigParameter(ConfigConfigParameter configConfigParameter) {
		this.configConfigParameter = configConfigParameter;

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
