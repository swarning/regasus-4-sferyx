/**
 * ProfileConfigComposite.java
 * created on 31.05.2013 10:18:54
 */
package de.regasus.core.ui.editor.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.parameter.ProfileConfigParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class ProfileConfigComposite extends Composite {

	// the entity
	private ProfileConfigParameter profileConfigParameter;

	// filter
	private ProfileConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets profileWidgets;
	private FieldConfigWidgets profileRelationWidgets;
	private FieldConfigWidgets profileRoleWidgets;

	private UserConfigGroup userConfigGroup;
	private PersonConfigGroup personConfigGroup;
	private ApprovalConfigGroup approvalConfigGroup;
	private CommunicationConfigGroup communicationConfigGroup;
	private AddressConfigGroup addressConfigGroup;
	private BankingConfigGroup bankingConfigGroup;
	private ProfileCustomFieldsConfigGroup customFieldsConfigGroup;
	private ProfileConfigGroup profileConfigGroup;


	public ProfileConfigComposite(Composite parent, int style) {
		super(parent, style);

		final int numColumns = 3;
		setLayout(new GridLayout(numColumns, false));

		{ // Top area for profileWidgets
    		Composite composite = new Composite(this, SWT.NONE);
			composite.setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
			composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, numColumns, 1));

    		profileWidgets = new FieldConfigWidgets(composite, CoreI18N.Config_Profile);
    		profileWidgets.addModifyListener(new ModifyListener() {
    			@Override
    			public void modifyText(ModifyEvent e) {
    				updateEnabledStatus();
    			}
    		});


    		profileRelationWidgets = new FieldConfigWidgets(composite, CoreI18N.Config_ProfileRelations);

    		profileRoleWidgets = new FieldConfigWidgets(composite, CoreI18N.Config_ProfileRoles);
		}


		// 1 Composite for each of the 3 columns

		{ // Column 1
			Composite composite = new Composite(this, SWT.NONE);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));


			userConfigGroup = new UserConfigGroup(composite, style);
			userConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

			personConfigGroup = new PersonConfigGroup(composite, style);
			personConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

			approvalConfigGroup = new ApprovalConfigGroup(composite, style);
			approvalConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

			communicationConfigGroup = new CommunicationConfigGroup(composite,	style);
			communicationConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		}

		{ // Column 2
			Composite composite = new Composite(this, SWT.NONE);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

			addressConfigGroup = new AddressConfigGroup(composite, style);
			addressConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

			bankingConfigGroup = new BankingConfigGroup(composite, style);
			bankingConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

			customFieldsConfigGroup = new ProfileCustomFieldsConfigGroup(composite, style);
			customFieldsConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		}

		{ // Column 3
			Composite composite = new Composite(this, SWT.NONE);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

			profileConfigGroup = new ProfileConfigGroup(composite, style);
			profileConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		}
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(ProfileConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		userConfigGroup.setAdminConfigParameter(adminConfigParameter);
		personConfigGroup.setAdminConfigParameter(adminConfigParameter);
		approvalConfigGroup.setAdminConfigParameter(adminConfigParameter);
		communicationConfigGroup.setAdminConfigParameter( adminConfigParameter.getCommunicationConfigParameter() );
		addressConfigGroup.setAdminConfigParameter( adminConfigParameter.getAddressConfigParameter() );
		bankingConfigGroup.setAdminConfigParameter(adminConfigParameter);
		customFieldsConfigGroup.setAdminConfigParameter(adminConfigParameter);
		profileConfigGroup.setAdminConfigParameter(adminConfigParameter);

		updateEnabledStatus();
	}


	protected void updateEnabledStatus() {
		boolean enabled = getEnabled();

		profileWidgets.setEnabled(enabled && adminConfigParameter.isVisible());

		// visibility of all other widgets depends further on the current value of profileWidgets ...
		enabled = enabled && profileWidgets.getVisible();

		// and on the setting of the current field in globalAdminConfig
		profileRelationWidgets.setEnabled(enabled && adminConfigParameter.getProfileRelationConfigParameter().isVisible());
		profileRoleWidgets.setEnabled(enabled && adminConfigParameter.getProfileRoleConfigParameter().isVisible());

		userConfigGroup.setEnabled(enabled);
		personConfigGroup.setEnabled(enabled);
		approvalConfigGroup.setEnabled(enabled);
		communicationConfigGroup.setEnabled(enabled);
		addressConfigGroup.setEnabled(enabled);
		bankingConfigGroup.setEnabled(enabled);
		customFieldsConfigGroup.setEnabled(enabled);
		profileConfigGroup.setEnabled(enabled);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		profileWidgets.addModifyListener(modifyListener);
		profileRelationWidgets.addModifyListener(modifyListener);
		profileRoleWidgets.addModifyListener(modifyListener);

		userConfigGroup.addModifyListener(modifyListener);
		personConfigGroup.addModifyListener(modifyListener);
		approvalConfigGroup.addModifyListener(modifyListener);
		communicationConfigGroup.addModifyListener(modifyListener);
		addressConfigGroup.addModifyListener(modifyListener);
		bankingConfigGroup.addModifiyListener(modifyListener);
		customFieldsConfigGroup.addModifyListener(modifyListener);
		profileConfigGroup.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (profileConfigParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						profileWidgets.syncWidgetsToEntity();
						profileRelationWidgets.syncWidgetsToEntity();
						profileRoleWidgets.syncWidgetsToEntity();

						userConfigGroup.syncWidgetsToEntity();
						personConfigGroup.syncWidgetsToEntity();
						approvalConfigGroup.syncWidgetsToEntity();
						communicationConfigGroup.syncWidgetsToEntity();
						addressConfigGroup.syncWidgetsToEntity();
						bankingConfigGroup.syncWidgetsToEntity();
						customFieldsConfigGroup.syncWidgetsToEntity();
						profileConfigGroup.syncWidgetsToEntity();

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
		if (profileConfigParameter != null) {
			profileWidgets.syncEntityToWidgets();
			profileRelationWidgets.syncEntityToWidgets();
			profileRoleWidgets.syncEntityToWidgets();

			userConfigGroup.syncEntityToWidgets();
			personConfigGroup.syncEntityToWidgets();
			approvalConfigGroup.syncEntityToWidgets();
			communicationConfigGroup.syncEntityToWidgets();
			addressConfigGroup.syncEntityToWidgets();
			bankingConfigGroup.syncEntityToWidgets();
			customFieldsConfigGroup.syncEntityToWidgets();
			profileConfigGroup.syncEntityToWidgets();
		}
	}


	public void setProfileConfigParameter(ProfileConfigParameter profileConfigParameter) {
		this.profileConfigParameter = profileConfigParameter;

		// set entity to other composites
		profileWidgets.setFieldConfigParameter(profileConfigParameter);
		profileRelationWidgets.setFieldConfigParameter(profileConfigParameter.getProfileRelationConfigParameter());
		profileRoleWidgets.setFieldConfigParameter(profileConfigParameter.getProfileRoleConfigParameter());

		userConfigGroup.setConfigParameter(profileConfigParameter);
		personConfigGroup.setConfigParameter(profileConfigParameter);
		approvalConfigGroup.setConfigParameter(profileConfigParameter);
		communicationConfigGroup.setConfigParameter(profileConfigParameter.getCommunicationConfigParameter());
		addressConfigGroup.setConfigParameter(profileConfigParameter.getAddressConfigParameter());
		bankingConfigGroup.setConfigParameter(profileConfigParameter);
		customFieldsConfigGroup.setConfigParameter(profileConfigParameter);
		profileConfigGroup.setProfileConfigParameter(profileConfigParameter);

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
