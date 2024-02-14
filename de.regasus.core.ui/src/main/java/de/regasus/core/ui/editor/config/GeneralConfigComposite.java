package de.regasus.core.ui.editor.config;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.ConfigConfigParameter;
import com.lambdalogic.messeinfo.config.parameter.ConfigParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class GeneralConfigComposite extends Composite {

	// the entities
	private ConfigConfigParameter configConfigParameter;

	// corresponding admin Config that controls which settings are enabled
	private ConfigParameter adminConfigParameter;

	private ConfigScope scope;

	// Widgets
	private ConfigConfigGroup configConfigGroup;
	private SalutationConfigGroup salutationConfigGroup;
	private InvitationCardConfigGroup invitationCardConfigGroup;
	private AddressLabelConfigGroup addressLabelConfigGroup;
	private PortalConfigGroup portalConfigGroup;


	private static final int NUM_COLUMNS = 2;


	private final GridDataFactory configGroupGridDataFactory = GridDataFactory
		.swtDefaults()
		.align(SWT.FILL,  SWT.CENTER);


	public GeneralConfigComposite(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		this.scope = scope;

		setLayout(new GridLayout(NUM_COLUMNS, false));


		GridDataFactory columnGridDataFactory = GridDataFactory.fillDefaults();

		Composite column1Composite = buildColumn1(this);
		columnGridDataFactory.applyTo(column1Composite);

		Composite column2Composite = buildColumn2(this);
		columnGridDataFactory.applyTo(column2Composite);
	}


	private Composite buildColumn1(Composite parent) {
		Composite columnComposite = new Composite(parent, SWT.NONE);
		columnComposite.setLayout(new GridLayout());

		if (scope == ConfigScope.GLOBAL_ADMIN) {
    		configConfigGroup = new ConfigConfigGroup(columnComposite, SWT.NONE);
    		configGroupGridDataFactory.applyTo(configConfigGroup);
		}

		salutationConfigGroup = new SalutationConfigGroup(columnComposite, SWT.NONE);
		configGroupGridDataFactory.applyTo(salutationConfigGroup);

		invitationCardConfigGroup = new InvitationCardConfigGroup(columnComposite, SWT.NONE);
		configGroupGridDataFactory.applyTo(invitationCardConfigGroup);

		addressLabelConfigGroup = new AddressLabelConfigGroup(columnComposite, SWT.NONE);
		configGroupGridDataFactory.applyTo(addressLabelConfigGroup);

		return columnComposite;
	}


	private Composite buildColumn2(Composite parent) {
		Composite columnComposite = new Composite(parent, SWT.NONE);
		columnComposite.setLayout(new GridLayout());

		portalConfigGroup = new PortalConfigGroup(columnComposite, SWT.NONE, scope);
		portalConfigGroup.setText(CoreI18N.Config_GlobalPortal);
		// make more width, because the group text is very long
		configGroupGridDataFactory.copy().hint(300, SWT.DEFAULT).applyTo(portalConfigGroup);

		return columnComposite;
	}


	public void addModifyListener(ModifyListener modifyListener) {
		if (configConfigGroup != null) {
			configConfigGroup.addModifyListener(modifyListener);
		}
		salutationConfigGroup.addModifyListener(modifyListener);
		invitationCardConfigGroup.addModifyListener(modifyListener);
		addressLabelConfigGroup.addModifyListener(modifyListener);
		portalConfigGroup.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configConfigParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {

					try {
						if (configConfigGroup != null) {
							configConfigGroup.syncWidgetsToEntity();
						}

						salutationConfigGroup.syncWidgetsToEntity();
						invitationCardConfigGroup.syncWidgetsToEntity();
						addressLabelConfigGroup.syncWidgetsToEntity();
						portalConfigGroup.syncWidgetsToEntity();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}

				}
			});
		}

	}


	public void syncEntityToWidgets() {
		if (configConfigGroup != null && configConfigParameter != null) {
			configConfigGroup.syncEntityToWidgets();
		}

		salutationConfigGroup.syncEntityToWidgets();
		invitationCardConfigGroup.syncEntityToWidgets();
		addressLabelConfigGroup.syncEntityToWidgets();
		portalConfigGroup.syncEntityToWidgets();
	}


	public void setConfigParameter(ConfigParameter configParameter) {
		Objects.requireNonNull(configParameter);

		configConfigParameter = configParameter.getConfigConfigParameter();

		// set entity to other composites
		if (configConfigGroup != null) {
			configConfigGroup.setConfigConfigParameter(configConfigParameter);
		}
		salutationConfigGroup.setSalutationConfigParameter( configParameter.getSalutationConfigParameter() );
		invitationCardConfigGroup.setInvitationCardConfigParameter( configParameter.getInvitationCardConfigParameter() );
		addressLabelConfigGroup.setAddressLabelConfigParameter( configParameter.getAddressLabelConfigParameter() );
		portalConfigGroup.setConfigParameter( configParameter.getPortalConfigParameter() );

		// syncEntityToWidgets() is called from outside
	}


	public void setAdminConfigParameter(ConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		portalConfigGroup.setAdminConfigParameter( adminConfigParameter.getPortalConfigParameter() );

		updateEnabledStatus();
	}


	protected void updateEnabledStatus() {
		boolean enabled = getEnabled();

		portalConfigGroup.setEnabled( enabled && adminConfigParameter.getPortalConfigParameter().getVisible() );
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
