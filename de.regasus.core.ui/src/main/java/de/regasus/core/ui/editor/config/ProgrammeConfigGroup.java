package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.ConfigParameter;
import com.lambdalogic.messeinfo.config.parameter.ProgrammeConfigParameter;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class ProgrammeConfigGroup extends Group {

	// the entity
	private ConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private ConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets globalProgrammeWidgets;
	private FieldConfigWidgets programmeWidgets;
	private FieldConfigWidgets additionalPriceWidgets;
	private FieldConfigWidgets waitListWidgets;


	private boolean globalScope;


	public ProgrammeConfigGroup(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(ParticipantLabel.Programme.getString());

		globalScope = (scope == ConfigScope.GLOBAL_ADMIN || scope == ConfigScope.GLOBAL_CUSTOMER);

		if (globalScope) {
			globalProgrammeWidgets = new FieldConfigWidgets(
				this,
				CoreI18N.Config_GlobalProgrammeMasterData,
				CoreI18N.Config_GlobalProgrammeMasterData_toolTip
			);

			SWTHelper.horizontalLine(this);
		}

		programmeWidgets = new FieldConfigWidgets(
			this,
			CoreI18N.Config_Programme,
			CoreI18N.Config_Programme_toolTip
		);
		programmeWidgets.addModifyListener(new ModifyListener() {
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


		waitListWidgets = new FieldConfigWidgets(
			this,
			CoreI18N.Config_WaitList,
			CoreI18N.Config_WaitList_toolTip
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
		boolean globalProgrammeVisible = adminConfigParameter.getProgrammeConfigParameter().isVisible();

		ProgrammeConfigParameter programmeConfigParameter = adminConfigParameter.getEventConfigParameter().getProgrammeConfigParameter();
		boolean eventProgrammeVisible = programmeConfigParameter.isVisible();
		boolean additionalPriceVisible = programmeConfigParameter.getAdditionalPriceConfigParameter().isVisible();
		boolean waitListVisible = programmeConfigParameter.getWaitListConfigParameter().isVisible();

		if (globalScope) {
			globalProgrammeWidgets.setEnabled(enabled && globalProgrammeVisible);
		}

		programmeWidgets.setEnabled(enabled && eventProgrammeVisible);

		// visibility of additionalPriceWidgets and waitListWidgets depends further on the current value of programmeWidgets
		additionalPriceWidgets.setEnabled(enabled && programmeWidgets.getVisible() && additionalPriceVisible);
		waitListWidgets.setEnabled(enabled && programmeWidgets.getVisible() && waitListVisible);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		if (globalScope) {
			globalProgrammeWidgets.addModifyListener(modifyListener);
		}
		programmeWidgets.addModifyListener(modifyListener);
		additionalPriceWidgets.addModifyListener(modifyListener);
		waitListWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (globalScope) {
							globalProgrammeWidgets.syncWidgetsToEntity();
						}
						programmeWidgets.syncWidgetsToEntity();
						additionalPriceWidgets.syncWidgetsToEntity();
						waitListWidgets.syncWidgetsToEntity();

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
				globalProgrammeWidgets.syncEntityToWidgets();
			}
			programmeWidgets.syncEntityToWidgets();
			additionalPriceWidgets.syncEntityToWidgets();
			waitListWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(ConfigParameter configParameter) {
		this.configParameter = configParameter;

		// set entity to other composites
		if (globalScope) {
			globalProgrammeWidgets.setFieldConfigParameter(configParameter.getProgrammeConfigParameter());
		}

		ProgrammeConfigParameter programmeConfigParameter = configParameter.getEventConfigParameter().getProgrammeConfigParameter();
		programmeWidgets.setFieldConfigParameter(programmeConfigParameter);
		additionalPriceWidgets.setFieldConfigParameter(programmeConfigParameter.getAdditionalPriceConfigParameter());
		waitListWidgets.setFieldConfigParameter(programmeConfigParameter.getWaitListConfigParameter());

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
