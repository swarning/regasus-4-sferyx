package de.regasus.portal.type.dsgv.registration;

import org.eclipse.swt.widgets.Composite;

import de.regasus.portal.Portal;
import de.regasus.portal.portal.editor.PortalConfigComposite;
import de.regasus.portal.portal.editor.PortalConfigCompositeFactory;


public class DsgvRegistrationPortalConfigCompositeFactory implements PortalConfigCompositeFactory {

	@Override
	public PortalConfigComposite create(Composite parent, Portal portal) throws Exception {
		DsgvRegistrationPortalConfigComposite composite = new DsgvRegistrationPortalConfigComposite(parent);
		composite.setPortal(portal);
		composite.createWidgets();
		composite.syncWidgetsToEntity();
		return composite;
	}

}
