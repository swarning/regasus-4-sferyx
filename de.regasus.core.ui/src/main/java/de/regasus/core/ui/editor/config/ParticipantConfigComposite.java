package de.regasus.core.ui.editor.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.parameter.ParticipantConfigParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class ParticipantConfigComposite extends Composite {

	// the entity
	private ParticipantConfigParameter participantConfigParameter;

	// corresponding admin Config that controls which settings are enabled
	private ParticipantConfigParameter adminConfigParameter;

	// Widgets
	private UserConfigGroup userConfigGroup;
	private PersonConfigGroup personConfigGroup;
	private ApprovalConfigGroup approvalConfigGroup;
	private CommunicationConfigGroup communicationConfigGroup;
	private AddressConfigGroup addressConfigGroup;
	private BankingConfigGroup bankingConfigGroup;
	private ParticipantCustomFieldsConfigGroup customFieldsConfigGroup;
	private ParticipantConfigGroup participantDataConfigGroup;
	private MembershipConfigGroup membershipConfigGroup;
	private PreferredPaymentTypeConfigGroup preferredPaymentTypeConfigGroup;


	public ParticipantConfigComposite(Composite parent, int style) {
		super(parent, style);

		final int numColumns = 3;
		setLayout(new GridLayout(numColumns, false));

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

    		communicationConfigGroup = new CommunicationConfigGroup(composite, style);
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

			customFieldsConfigGroup = new ParticipantCustomFieldsConfigGroup(composite, style);
			customFieldsConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		}

		{ // Column 3
    		Composite composite = new Composite(this, SWT.NONE);
    		composite.setLayout(new GridLayout());
    		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    		participantDataConfigGroup = new ParticipantConfigGroup(composite, style);
    		participantDataConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

    		membershipConfigGroup = new MembershipConfigGroup(composite, style);
    		membershipConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

    		preferredPaymentTypeConfigGroup = new PreferredPaymentTypeConfigGroup(composite, style);
    		preferredPaymentTypeConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		}
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(ParticipantConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		userConfigGroup.setAdminConfigParameter(adminConfigParameter);
		personConfigGroup.setAdminConfigParameter(adminConfigParameter);
		approvalConfigGroup.setAdminConfigParameter(adminConfigParameter);
		communicationConfigGroup.setAdminConfigParameter( adminConfigParameter.getCommunicationConfigParameter() );
		addressConfigGroup.setAdminConfigParameter( adminConfigParameter.getAddressConfigParameter() );
		bankingConfigGroup.setAdminConfigParameter(adminConfigParameter);
		customFieldsConfigGroup.setAdminConfigParameter(adminConfigParameter);
		participantDataConfigGroup.setAdminConfigParameter(adminConfigParameter);
		membershipConfigGroup.setAdminConfigParameter( adminConfigParameter.getMembershipConfigParameter() );
		preferredPaymentTypeConfigGroup.setAdminConfigParameter( adminConfigParameter.getPreferredPaymentTypeConfigParameter());

		updateEnabledStatus();
	}


	protected void updateEnabledStatus() {
		// visibility of all other widgets depends further on the current value of eventWidgets ...
		boolean enabled = getEnabled();

		// and on the setting of the current field in globalAdminConfig
		userConfigGroup.setEnabled(enabled);
		personConfigGroup.setEnabled(enabled);
		approvalConfigGroup.setEnabled(enabled);
		communicationConfigGroup.setEnabled(enabled);
		addressConfigGroup.setEnabled(enabled);
		bankingConfigGroup.setEnabled(enabled);
		customFieldsConfigGroup.setEnabled(enabled);
		participantDataConfigGroup.setEnabled(enabled);
		membershipConfigGroup.setEnabled(enabled);
		preferredPaymentTypeConfigGroup.setEnabled(enabled);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		userConfigGroup.addModifyListener(modifyListener);
		personConfigGroup.addModifyListener(modifyListener);
		approvalConfigGroup.addModifyListener(modifyListener);
		communicationConfigGroup.addModifyListener(modifyListener);
		addressConfigGroup.addModifyListener(modifyListener);
		bankingConfigGroup.addModifiyListener(modifyListener);
		customFieldsConfigGroup.addModifyListener(modifyListener);
		participantDataConfigGroup.addModifyListener(modifyListener);
		membershipConfigGroup.addModifyListener(modifyListener);
		preferredPaymentTypeConfigGroup.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (participantConfigParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						userConfigGroup.syncWidgetsToEntity();
						personConfigGroup.syncWidgetsToEntity();
						approvalConfigGroup.syncWidgetsToEntity();
						communicationConfigGroup.syncWidgetsToEntity();
						addressConfigGroup.syncWidgetsToEntity();
						bankingConfigGroup.syncWidgetsToEntity();
						customFieldsConfigGroup.syncWidgetsToEntity();
						participantDataConfigGroup.syncWidgetsToEntity();
						membershipConfigGroup.syncWidgetsToEntity();
						preferredPaymentTypeConfigGroup.syncWidgetsToEntity();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (participantConfigParameter != null) {
			userConfigGroup.syncEntityToWidgets();
			personConfigGroup.syncEntityToWidgets();
			approvalConfigGroup.syncEntityToWidgets();
			communicationConfigGroup.syncEntityToWidgets();
			addressConfigGroup.syncEntityToWidgets();
			bankingConfigGroup.syncEntityToWidgets();
			customFieldsConfigGroup.syncEntityToWidgets();
			participantDataConfigGroup.syncEntityToWidgets();
			membershipConfigGroup.syncEntityToWidgets();
			preferredPaymentTypeConfigGroup.syncEntityToWidgets();
		}
	}


	public void setParticipantConfigParameter(ParticipantConfigParameter participantConfigParameter) {
		this.participantConfigParameter = participantConfigParameter;

		// set entity to other composites
		userConfigGroup.setConfigParameter(participantConfigParameter);
		personConfigGroup.setConfigParameter(participantConfigParameter);
		approvalConfigGroup.setConfigParameter(participantConfigParameter);
		communicationConfigGroup.setConfigParameter( participantConfigParameter.getCommunicationConfigParameter() );
		addressConfigGroup.setConfigParameter( participantConfigParameter.getAddressConfigParameter() );
		bankingConfigGroup.setConfigParameter(participantConfigParameter);
		customFieldsConfigGroup.setParticipantConfigParameter(participantConfigParameter);
		participantDataConfigGroup.setParticipantConfigParameter(participantConfigParameter);
		membershipConfigGroup.setConfigParameter( participantConfigParameter.getMembershipConfigParameter() );
		preferredPaymentTypeConfigGroup.setConfigParameter( participantConfigParameter.getPreferredPaymentTypeConfigParameter() );

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
