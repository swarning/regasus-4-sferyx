package de.regasus.portal.type.standard.cvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.portal.Portal;
import de.regasus.portal.portal.editor.PortalConfigComposite;

public class StandardCventPortalConfigComposite extends Composite implements PortalConfigComposite {

	// the entity
	private Portal portal;
	private StandardCventPortalConfig portalConfig;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *


	// *
	// * Widgets
	// **************************************************************************

	public StandardCventPortalConfigComposite(Composite parent) {
		super(parent, SWT.NONE);
	}


	@Override
	public void setPortal(Portal portal) throws Exception {
		this.portal = portal;

		portalConfig = (StandardCventPortalConfig) portal.getPortalConfig();
	}


	@Override
	public void createWidgets() throws Exception {
	}


	@Override
	public void syncWidgetsToEntity() {
	}


	@Override
	public void syncEntityToWidgets() {
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
