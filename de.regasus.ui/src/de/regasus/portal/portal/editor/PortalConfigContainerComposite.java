package de.regasus.portal.portal.editor;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalType;
import de.regasus.ui.Activator;

/**
 * {@link Composite} that is used as a container for classes that are defined in
 * {@link PortalType#getConfigCompositeFactoryClassName()}. The content of this {@link Composite} is set dynamically after
 * the {@link Portal} is set and the {@link PortalType} and therewith the {@link PortalConfigComposite} is known.
 */
public class PortalConfigContainerComposite extends Composite {

	// the entity
	private Portal portal;

	private PortalConfigCompositeFactory factory = null;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private PortalConfigComposite portalConfigComposite;

	// *
	// * Widgets
	// **************************************************************************

	public PortalConfigContainerComposite(Composite parent, int style) throws Exception {
		super(parent, style);
		setLayout( new FillLayout() );

		// Widgets cannot be created here, because at this time the PortalConfigComposite is not known yet.
	}


	public void setPortal(Portal portal) {
		this.portal = portal;
		if (portal != null) {
    		try {
        		createWidgets();
    		}
    		catch (Exception e) {
    			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    		}
		}
	}


	private void createWidgets() throws Exception {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (portalConfigComposite != null) {
						portalConfigComposite.removeModifyListener(modifySupport);
						portalConfigComposite.dispose();
						portalConfigComposite = null;
					}

					portalConfigComposite = getFactory().create(PortalConfigContainerComposite.this, portal);
					layout();

					// after sync add this as ModifyListener to all widgets and groups
					portalConfigComposite.addModifyListener(modifySupport);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	private PortalConfigCompositeFactory getFactory() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (factory == null) {
    		PortalType portalType = portal.getPortalType();
    		String className = portalType.getConfigCompositeFactoryClassName();
    		Class<PortalConfigCompositeFactory> factoryClass = (Class<PortalConfigCompositeFactory>) Class.forName(className);
    		factory = factoryClass.newInstance();
		}
    	return factory;
	}


	public void syncEntityToWidgets() throws Exception {
		if (portalConfigComposite != null) {
			portalConfigComposite.syncEntityToWidgets();
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}
