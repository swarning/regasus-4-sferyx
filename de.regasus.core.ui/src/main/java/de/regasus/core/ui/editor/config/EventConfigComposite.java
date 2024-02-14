package de.regasus.core.ui.editor.config;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.ConfigParameter;
import com.lambdalogic.messeinfo.config.parameter.EventConfigParameter;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class EventConfigComposite extends Composite {

	// the entity
	private ConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private ConfigParameter adminConfigParameter;

	private ConfigScope scope;

	// Widgets
	private FieldConfigWidgets eventWidgets;

	private EventGeneralConfigGroup eventGeneralConfigGroup;
	private ProgrammeConfigGroup programmeConfigGroup;
	private HotelConfigGroup hotelConfigGroup;
	private EventInvoiceConfigGroup invoiceConfigGroup;
	private CertificateConfigGroup certificateConfigGroup;
	private FormEditorConfigGroup formEditorConfigGroup;
	private PortalConfigGroup portalConfigGroup;
	private LocationConfigGroup locationConfigGroup;
	private OnsiteWorkflowConfigGroup onsiteWorkflowConfigGroup;
	private PushServiceSettingsConfigGroup pushServiceSettingsConfigGroup;
	private TerminalConfigGroup terminalConfigGroup;


	private static final int NUM_COLUMNS = 3;


	private final GridDataFactory configGroupGridDataFactory = GridDataFactory
		.swtDefaults()
		.align(SWT.FILL,  SWT.CENTER);


	public EventConfigComposite(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		this.scope = scope;

		setLayout(new GridLayout(NUM_COLUMNS, false));


		Composite topArea = buildTopArea(this);
		GridDataFactory
			.swtDefaults()
			.align(SWT.LEFT, SWT.FILL)
			.span(NUM_COLUMNS, 1)
			.applyTo(topArea);


		GridDataFactory columnGridDataFactory = GridDataFactory.fillDefaults();

		Composite column1Composite = buildColumn1(this);
		columnGridDataFactory.applyTo(column1Composite);

		Composite column2Composite = buildColumn2(this);
		columnGridDataFactory.applyTo(column2Composite);

		Composite column3Composite = buildColumn3(this);
		columnGridDataFactory.applyTo(column3Composite);
	}


	private Composite buildTopArea(Composite parent) {
		Composite topAreaComposite = new Composite(parent, SWT.NONE);
		topAreaComposite.setLayout(new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false));

		eventWidgets = new FieldConfigWidgets(topAreaComposite, ParticipantLabel.Event.getString());
		eventWidgets.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateEnabledStatus();
			}
		});

		return topAreaComposite;
	}


	private Composite buildColumn1(Composite parent) {
		Composite columnComposite = new Composite(parent, SWT.NONE);
		columnComposite.setLayout(new GridLayout());

		eventGeneralConfigGroup = new EventGeneralConfigGroup(columnComposite, SWT.NONE, scope);
		configGroupGridDataFactory.applyTo(eventGeneralConfigGroup);


		programmeConfigGroup = new ProgrammeConfigGroup(columnComposite, SWT.NONE, scope);
		configGroupGridDataFactory.applyTo(programmeConfigGroup);


		hotelConfigGroup = new HotelConfigGroup(columnComposite, SWT.NONE, scope);
		configGroupGridDataFactory.applyTo(hotelConfigGroup);

		return columnComposite;
	}


	private Composite buildColumn2(Composite parent) {
		Composite columnComposite = new Composite(parent, SWT.NONE);
		columnComposite.setLayout(new GridLayout());

		invoiceConfigGroup = new EventInvoiceConfigGroup(columnComposite, SWT.NONE, scope);
		configGroupGridDataFactory.applyTo(invoiceConfigGroup);

		certificateConfigGroup = new CertificateConfigGroup(columnComposite, SWT.NONE, scope);
		configGroupGridDataFactory.applyTo(certificateConfigGroup);

		return columnComposite;
	}


	private Composite buildColumn3(Composite parent) {
		Composite columnComposite = new Composite(parent, SWT.NONE);
		columnComposite.setLayout(new GridLayout());

		formEditorConfigGroup = new FormEditorConfigGroup(columnComposite, SWT.NONE);
		configGroupGridDataFactory.applyTo(formEditorConfigGroup);

		portalConfigGroup = new PortalConfigGroup(columnComposite, SWT.NONE, scope);
		portalConfigGroup.setText(CoreI18N.Config_EventPortal);
		configGroupGridDataFactory.applyTo(portalConfigGroup);

		locationConfigGroup = new LocationConfigGroup(columnComposite, SWT.NONE, scope);
		configGroupGridDataFactory.applyTo(locationConfigGroup);

		onsiteWorkflowConfigGroup = new OnsiteWorkflowConfigGroup(columnComposite, SWT.NONE, scope);
		configGroupGridDataFactory.applyTo(onsiteWorkflowConfigGroup);

		pushServiceSettingsConfigGroup = new PushServiceSettingsConfigGroup(columnComposite, SWT.NONE, scope);
		configGroupGridDataFactory.applyTo(pushServiceSettingsConfigGroup);

   		terminalConfigGroup = new TerminalConfigGroup(columnComposite, SWT.NONE, scope);
   		configGroupGridDataFactory.applyTo(terminalConfigGroup);

   		return columnComposite;
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(ConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		eventGeneralConfigGroup.setAdminConfigParameter( adminConfigParameter.getEventConfigParameter() );
		programmeConfigGroup.setAdminConfigParameter(adminConfigParameter);
		hotelConfigGroup.setAdminConfigParameter(adminConfigParameter);
		invoiceConfigGroup.setAdminConfigParameter( adminConfigParameter.getEventConfigParameter() );
		certificateConfigGroup.setAdminConfigParameter( adminConfigParameter.getEventConfigParameter().getCertificateConfigParameter() );
		formEditorConfigGroup.setAdminConfigParameter( adminConfigParameter.getEventConfigParameter() );
		portalConfigGroup.setAdminConfigParameter( adminConfigParameter.getEventConfigParameter().getPortalConfigParameter() );
		locationConfigGroup.setAdminConfigParameter(adminConfigParameter);
		onsiteWorkflowConfigGroup.setAdminConfigParameter( adminConfigParameter.getEventConfigParameter() );
		pushServiceSettingsConfigGroup.setAdminConfigParameter( adminConfigParameter.getEventConfigParameter() );
		if (terminalConfigGroup != null) {
			terminalConfigGroup.setAdminConfigParameter( adminConfigParameter.getEventConfigParameter().getTerminalConfigParameter() );
		}

		updateEnabledStatus();
	}


	protected void updateEnabledStatus() {
		boolean enabled = getEnabled();

		eventWidgets.setEnabled(enabled && adminConfigParameter.getEventConfigParameter().isVisible());

		// visibility of all other widgets depends further on the current value of eventWidgets ...
		enabled = enabled && eventWidgets.getVisible();

		// and on the setting of the current field in globalAdminConfig
		eventGeneralConfigGroup.setEnabled(enabled);
		programmeConfigGroup.setEnabled(enabled);
		hotelConfigGroup.setEnabled(enabled);
		invoiceConfigGroup.setEnabled(enabled);
		certificateConfigGroup.setEnabled(enabled);
		formEditorConfigGroup.setEnabled(enabled);
		portalConfigGroup.setEnabled(enabled);
		locationConfigGroup.setEnabled(enabled);
		onsiteWorkflowConfigGroup.setEnabled(enabled);
		pushServiceSettingsConfigGroup.setEnabled(enabled);
		if (terminalConfigGroup != null) {
			terminalConfigGroup.setEnabled(enabled);
		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		eventWidgets.addModifyListener(modifyListener);
		eventGeneralConfigGroup.addModifyListener(modifyListener);
		programmeConfigGroup.addModifyListener(modifyListener);
		hotelConfigGroup.addModifyListener(modifyListener);
		invoiceConfigGroup.addModifyListener(modifyListener);
		certificateConfigGroup.addModifyListener(modifyListener);
		formEditorConfigGroup.addModifyListener(modifyListener);
		portalConfigGroup.addModifyListener(modifyListener);
		locationConfigGroup.addModifyListener(modifyListener);
		onsiteWorkflowConfigGroup.addModifyListener(modifyListener);
		pushServiceSettingsConfigGroup.addModifyListener(modifyListener);
		if (terminalConfigGroup != null) {
			terminalConfigGroup.addModifyListener(modifyListener);
		}
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						eventWidgets.syncWidgetsToEntity();
						eventGeneralConfigGroup.syncWidgetsToEntity();
						programmeConfigGroup.syncWidgetsToEntity();
						hotelConfigGroup.syncWidgetsToEntity();
						invoiceConfigGroup.syncWidgetsToEntity();
						certificateConfigGroup.syncWidgetsToEntity();
						formEditorConfigGroup.syncWidgetsToEntity();
						portalConfigGroup.syncWidgetsToEntity();
						locationConfigGroup.syncWidgetsToEntity();
						onsiteWorkflowConfigGroup.syncWidgetsToEntity();
						pushServiceSettingsConfigGroup.syncWidgetsToEntity();
						if (terminalConfigGroup != null) {
							terminalConfigGroup.syncWidgetsToEntity();
						}

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
			eventWidgets.syncEntityToWidgets();
			eventGeneralConfigGroup.syncEntityToWidgets();
			programmeConfigGroup.syncEntityToWidgets();
			hotelConfigGroup.syncEntityToWidgets();
			invoiceConfigGroup.syncEntityToWidgets();
			certificateConfigGroup.syncEntityToWidgets();
			formEditorConfigGroup.syncEntityToWidgets();
			portalConfigGroup.syncEntityToWidgets();
			locationConfigGroup.syncEntityToWidgets();
			onsiteWorkflowConfigGroup.syncEntityToWidgets();
			pushServiceSettingsConfigGroup.syncEntityToWidgets();
			if (terminalConfigGroup != null) {
				terminalConfigGroup.syncEntityToWidgets();
			}
		}
	}


	public void setConfigParameter(ConfigParameter configParameter) {
		this.configParameter = Objects.requireNonNull(configParameter);

		EventConfigParameter eventConfigParameter = configParameter.getEventConfigParameter();

		// set entity to other composites
		eventWidgets.setFieldConfigParameter(eventConfigParameter);
		eventGeneralConfigGroup.setConfigParameter(eventConfigParameter);
		programmeConfigGroup.setConfigParameter(configParameter);
		hotelConfigGroup.setConfigParameter(configParameter);
		invoiceConfigGroup.setConfigParameter(eventConfigParameter);
		certificateConfigGroup.setConfigParameter( eventConfigParameter.getCertificateConfigParameter() );
		formEditorConfigGroup.setConfigParameter(eventConfigParameter);
		portalConfigGroup.setConfigParameter( eventConfigParameter.getPortalConfigParameter() );
		locationConfigGroup.setConfigParameter(configParameter);
		onsiteWorkflowConfigGroup.setConfigParameter(eventConfigParameter);
		pushServiceSettingsConfigGroup.setConfigParameter(eventConfigParameter);
		if (terminalConfigGroup != null) {
			terminalConfigGroup.setConfigParameter( eventConfigParameter.getTerminalConfigParameter() );
		}

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
