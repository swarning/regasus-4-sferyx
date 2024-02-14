package de.regasus.portal.type.openid.connect;

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

public class OpenIDConnectPortalConfigComposite extends Composite implements PortalConfigComposite {

	// the entity
	private OpenIDConnectPortalConfig portalConfig;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private AuthenticationSettingsGroup authenticationSettingsGroup;
	private ExternalSystemsSettingsGroup externalSystemsSettingsGroup;

	// *
	// * Widgets
	// **************************************************************************

	public OpenIDConnectPortalConfigComposite(Composite parent) {
		super(parent, SWT.NONE);
	}


	@Override
	public void setPortal(Portal portal) throws Exception {
		portalConfig = (OpenIDConnectPortalConfig) portal.getPortalConfig();
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

		// authentication settings
		authenticationSettingsGroup = new AuthenticationSettingsGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(authenticationSettingsGroup);
		authenticationSettingsGroup.addModifyListener(modifySupport);

		// external systems settings
		externalSystemsSettingsGroup = new ExternalSystemsSettingsGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(externalSystemsSettingsGroup);
		externalSystemsSettingsGroup.addModifyListener(modifySupport);
	}


	@Override
	public void syncWidgetsToEntity() {
		if (portalConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						authenticationSettingsGroup.setEntity(portalConfig);
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
			authenticationSettingsGroup.syncEntityToWidgets();
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
