package de.regasus.portal.type.react.hotelspeaker;

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
import de.regasus.portal.type.react.hotelspeaker.ReactHotelSpeakerPortalConfig;
import de.regasus.ui.Activator;

public class ReactHotelSpeakerPortalConfigComposite extends Composite implements PortalConfigComposite {

	// the entity
	private Portal portal;
	private ReactHotelSpeakerPortalConfig portalConfig;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private GeneralSettingsGroup generalSettingsGroup;
	private RegistrationSettingsGroup registrationSettingsGroup;
	private OtherSettingsGroup otherSettingsGroup;

	// *
	// * Widgets
	// **************************************************************************

	public ReactHotelSpeakerPortalConfigComposite(Composite parent) {
		super(parent, SWT.NONE);
	}


	@Override
	public void setPortal(Portal portal) throws Exception {
		this.portal = portal;

		portalConfig = (ReactHotelSpeakerPortalConfig) portal.getPortalConfig();
	}


	@Override
	public void createWidgets() throws Exception {
		/* layout with 2 columns
		 */
		setLayout( new GridLayout(2, true) );

		GridDataFactory groupGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.FILL)
			.grab(true,  false);


		// general settings
		generalSettingsGroup = new GeneralSettingsGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(generalSettingsGroup);
		generalSettingsGroup.addModifyListener(modifySupport);

		// registration settings
		registrationSettingsGroup = new RegistrationSettingsGroup(this, SWT.NONE, portal);
		groupGridDataFactory.applyTo(registrationSettingsGroup);
		registrationSettingsGroup.addModifyListener(modifySupport);

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
						generalSettingsGroup.setEntity(portalConfig);
						registrationSettingsGroup.setEntity(portalConfig);
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
			generalSettingsGroup.syncEntityToWidgets();
			registrationSettingsGroup.syncEntityToWidgets();
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
