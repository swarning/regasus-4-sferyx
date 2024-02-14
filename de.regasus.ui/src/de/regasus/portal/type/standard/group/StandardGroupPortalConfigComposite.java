package de.regasus.portal.type.standard.group;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.portal.editor.PortalConfigComposite;
import de.regasus.ui.Activator;

public class StandardGroupPortalConfigComposite extends Composite implements PortalConfigComposite {

	// the entity
	private Portal portal;
	private StandardGroupPortalConfig portalConfig;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private StartPageSettingsGroup startPageSettingsGroup;
	private RegistrationSettingsGroup registrationSettingsGroup;
	private ParticipantTypeSettingsGroup participantTypeSettingsGroup;
	private ProgrammeBookingSettingsGroup programmeBookingSettingsGroup;
	private OtherSettingsGroup otherSettingsGroup;

	// *
	// * Widgets
	// **************************************************************************

	public StandardGroupPortalConfigComposite(Composite parent) {
		super(parent, SWT.NONE);
	}


	@Override
	public void setPortal(Portal portal) throws Exception {
		this.portal = portal;

		portalConfig = (StandardGroupPortalConfig) portal.getPortalConfig();
	}


	@Override
	public void createWidgets() throws Exception {
		/* layout with 2 columns
		 */
		setLayout( new GridLayout(2, true) );

		GridDataFactory groupGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.FILL)
			.grab(true,  false);


		// start page settings
		startPageSettingsGroup = new StartPageSettingsGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(startPageSettingsGroup);
		startPageSettingsGroup.addModifyListener(modifySupport);

		// registration settings
		registrationSettingsGroup = new RegistrationSettingsGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(registrationSettingsGroup);
		registrationSettingsGroup.addModifyListener(modifySupport);

		// participant type settings
		participantTypeSettingsGroup = new ParticipantTypeSettingsGroup(this, SWT.NONE, portal);
		groupGridDataFactory.applyTo(participantTypeSettingsGroup);
		participantTypeSettingsGroup.addModifyListener(modifySupport);

		// programme booking settings
		programmeBookingSettingsGroup = new ProgrammeBookingSettingsGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(programmeBookingSettingsGroup);
		programmeBookingSettingsGroup.addModifyListener(modifySupport);

		// other settings
		otherSettingsGroup = new OtherSettingsGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(otherSettingsGroup);
		otherSettingsGroup.addModifyListener(modifySupport);
	}


	@Override
	public void syncWidgetsToEntity() {
		if (portalConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						startPageSettingsGroup.setEntity(portalConfig);
						registrationSettingsGroup.setEntity(portalConfig);
						participantTypeSettingsGroup.setEntity(portalConfig);
						programmeBookingSettingsGroup.setEntity(portalConfig);
						otherSettingsGroup.setEntity(portalConfig);
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
			startPageSettingsGroup.syncEntityToWidgets();
			registrationSettingsGroup.syncEntityToWidgets();
			participantTypeSettingsGroup.syncEntityToWidgets();
			programmeBookingSettingsGroup.syncEntityToWidgets();
			otherSettingsGroup.syncEntityToWidgets();
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
