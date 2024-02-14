package de.regasus.portal.type.react.registration;

import org.eclipse.swt.widgets.Composite;

import de.regasus.portal.Portal;
import de.regasus.portal.portal.editor.PortalConfigComposite;
import de.regasus.portal.portal.editor.PortalConfigCompositeFactory;


public class ReactRegistrationPortalConfigCompositeFactory implements PortalConfigCompositeFactory {

	@Override
	public PortalConfigComposite create(Composite parent, Portal portal) throws Exception {
		ReactRegistrationPortalConfigComposite composite = new ReactRegistrationPortalConfigComposite(parent);
		composite.setPortal(portal);
		composite.createWidgets();
		composite.syncWidgetsToEntity();
		return composite;
	}

}
