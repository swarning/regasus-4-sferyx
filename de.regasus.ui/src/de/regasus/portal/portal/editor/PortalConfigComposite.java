package de.regasus.portal.portal.editor;

import org.eclipse.swt.events.ModifyListener;

import de.regasus.portal.Portal;

/**
 * This interface has to be implemented by the classes that are built by {@link PortalConfigCompositeFactory}.
 */
public interface PortalConfigComposite {

	void setPortal(Portal portal) throws Exception;

	void createWidgets() throws Exception;

	void syncWidgetsToEntity();

	void syncEntityToWidgets() throws Exception;

	void addModifyListener(ModifyListener modifyListener);

	void removeModifyListener(ModifyListener modifyListener);

	void dispose();

}
