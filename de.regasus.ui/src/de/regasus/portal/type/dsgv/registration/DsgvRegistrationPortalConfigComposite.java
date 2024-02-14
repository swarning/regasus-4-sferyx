package de.regasus.portal.type.dsgv.registration;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.portal.editor.PortalConfigComposite;
import de.regasus.ui.Activator;


public class DsgvRegistrationPortalConfigComposite extends Composite implements PortalConfigComposite {

	// the entity
	private Portal portal;
	private DsgvRegistrationPortalConfig portalConfig;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private VisibilitySettingsGroup visibilitySettingsGroup;
	private RegistrationSettingsGroup registrationSettingsGroup;
	private ParticipantTypeSettingsGroup participantTypeSettingsGroup;
	private ProgrammeBookingSettingsGroup programmeBookingSettingsGroup;
	private CompanionSettingsGroup companionSettingsGroup;
	private ExternalSystemsSettingsGroup externalSystemsSettingsGroup;

	// *
	// * Widgets
	// **************************************************************************

	public DsgvRegistrationPortalConfigComposite(Composite parent) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	public void setPortal(Portal portal) throws Exception {
		this.portal = portal;

		portalConfig = (DsgvRegistrationPortalConfig) portal.getPortalConfig();
	}


	@Override
	public void createWidgets() throws Exception {
		/* layout with 2 columns
		 */
		final int NUM_COLS = 2;
		setLayout( new GridLayout(NUM_COLS, true) );

		GridDataFactory groupGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.FILL)
			.grab(true,  false);


		// visibility settings
		visibilitySettingsGroup = new VisibilitySettingsGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(visibilitySettingsGroup);
		visibilitySettingsGroup.addModifyListener(modifySupport);

		// registration settings
		registrationSettingsGroup = new RegistrationSettingsGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(registrationSettingsGroup);
		registrationSettingsGroup.addModifyListener(modifySupport);

		// participant type settings
		participantTypeSettingsGroup = new ParticipantTypeSettingsGroup(this, SWT.NONE, portal.getId());
		groupGridDataFactory.applyTo(participantTypeSettingsGroup);
		participantTypeSettingsGroup.addModifyListener(modifySupport);

		// programme booking settings
		programmeBookingSettingsGroup = new ProgrammeBookingSettingsGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(programmeBookingSettingsGroup);
		programmeBookingSettingsGroup.addModifyListener(modifySupport);

		// companion settings
		companionSettingsGroup = new CompanionSettingsGroup(this, SWT.NONE, portal);
		groupGridDataFactory.applyTo(companionSettingsGroup);
		companionSettingsGroup.addModifyListener(modifySupport);


		// file explanation
		{
			Group group = new Group(this, SWT.NONE);
			groupGridDataFactory.applyTo(group);
			group.setText(UtilI18N.Files);
			group.setLayout( new FillLayout() );

			Label fileExplanationLabel = new Label(group, SWT.WRAP);
			fileExplanationLabel.setText(DsgvRegistrationPortalI18N.FileExplanation);
		}

		// other settings
		externalSystemsSettingsGroup = new ExternalSystemsSettingsGroup(this, SWT.NONE);
		groupGridDataFactory.copy().span(NUM_COLS, 1).applyTo(externalSystemsSettingsGroup);
		externalSystemsSettingsGroup.addModifyListener(modifySupport);
	}


	@Override
	public void syncWidgetsToEntity() {
		if (portalConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						visibilitySettingsGroup.setEntity(portalConfig);
						registrationSettingsGroup.setEntity(portalConfig);
						participantTypeSettingsGroup.setEntity(portalConfig);
						programmeBookingSettingsGroup.setEntity(portalConfig);
						companionSettingsGroup.setEntity(portalConfig);
						externalSystemsSettingsGroup.setEntity(portalConfig);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (portalConfig != null) {
			visibilitySettingsGroup.syncEntityToWidgets();
			registrationSettingsGroup.syncEntityToWidgets();
			participantTypeSettingsGroup.syncEntityToWidgets();
			programmeBookingSettingsGroup.syncEntityToWidgets();
			companionSettingsGroup.syncEntityToWidgets();
			externalSystemsSettingsGroup.syncEntityToWidgets();
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
