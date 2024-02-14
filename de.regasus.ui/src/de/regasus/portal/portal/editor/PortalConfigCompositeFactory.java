package de.regasus.portal.portal.editor;

import org.eclipse.swt.widgets.Composite;

import de.regasus.portal.Portal;
import de.regasus.portal.PortalType;

/**
 * This interface has to be implemented by the classes that are defined in {@link PortalType#getConfigCompositeFactoryClassName()}.
 * Implementing classes are instantiated dynamically
 */
public interface PortalConfigCompositeFactory {

	PortalConfigComposite create(Composite parent, Portal portal) throws Exception;

}
