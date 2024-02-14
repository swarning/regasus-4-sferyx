package de.regasus.portal.type.react.certificate;

import org.eclipse.swt.widgets.Composite;

import de.regasus.portal.Portal;
import de.regasus.portal.portal.editor.PortalConfigComposite;
import de.regasus.portal.portal.editor.PortalConfigCompositeFactory;

public class ReactCertificatePortalConfigCompositeFactory implements PortalConfigCompositeFactory {

	@Override
	public PortalConfigComposite create(Composite parent, Portal portal) throws Exception {
		ReactCertificatePortalConfigComposite composite = new ReactCertificatePortalConfigComposite(parent);
		composite.setPortal(portal);
		composite.createWidgets();
		composite.syncWidgetsToEntity();
		return composite;
	}

}
