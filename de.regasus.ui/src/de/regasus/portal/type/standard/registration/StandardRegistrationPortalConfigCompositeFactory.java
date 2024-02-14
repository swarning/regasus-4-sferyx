package de.regasus.portal.type.standard.registration;

import org.eclipse.swt.widgets.Composite;

import de.regasus.portal.Portal;
import de.regasus.portal.portal.editor.PortalConfigComposite;
import de.regasus.portal.portal.editor.PortalConfigCompositeFactory;


public class StandardRegistrationPortalConfigCompositeFactory implements PortalConfigCompositeFactory {

	@Override
	public PortalConfigComposite create(Composite parent, Portal portal) throws Exception {
		StandardRegistrationPortalConfigComposite composite = new StandardRegistrationPortalConfigComposite(parent);
		composite.setPortal(portal);
		composite.createWidgets();
		composite.syncWidgetsToEntity();
		return composite;
	}

}
