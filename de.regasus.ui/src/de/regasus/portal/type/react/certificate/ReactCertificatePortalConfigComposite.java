package de.regasus.portal.type.react.certificate;

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

public class ReactCertificatePortalConfigComposite extends Composite implements PortalConfigComposite {
	
	// the entity
	private ReactCertificatePortalConfig portalConfig;
	
	protected ModifySupport modifySupport = new ModifySupport(this);
	
	// **************************************************************************
	// * Widgets
	// *
	
	private AuthenticationGroup authenticationGroup;
	private OtherSettingsGroup otherSettingsGroup;
	

	ReactCertificatePortalConfigComposite(Composite parent) {
		super(parent, SWT.NONE);
	}

	
	@Override
	public void setPortal(Portal portal) throws Exception {
		portalConfig = (ReactCertificatePortalConfig) portal.getPortalConfig();
	}

	
	@Override
	public void createWidgets() throws Exception {
		/* layout with 2 columns
		 */
		setLayout( new GridLayout(2, true) );
		
		GridDataFactory groupGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.FILL)
			.grab(true,  false);
		
		// authentication settings
		authenticationGroup = new AuthenticationGroup(this, SWT.NONE);
		groupGridDataFactory.applyTo(authenticationGroup);
		authenticationGroup.addModifyListener(modifySupport);
		
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
						authenticationGroup.setEntity(portalConfig);
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
			authenticationGroup.syncEntityToWidgets();
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
