package de.regasus.core.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

/**
 * Base class for Actions that should be disabled if the user is logged out.
 * Sub classes should either overwrite run() or runWithBusyCursor(), where the latter shows the busy 
 * cursor.
 */
public abstract class AbstractAction extends Action implements ActionFactory.IWorkbenchAction {

	// Models
	private ServerModel serverModel;

	private ModelListener serverModelListener;
	
	
	public AbstractAction() {
		this(true);
	}
	
	
	public AbstractAction(boolean disableWhenLoggedOut) {
		super();

		// enable and disable the Action whether the user logs in or out
		if (disableWhenLoggedOut) {
			// get instance of ServerModel
    		serverModel = ServerModel.getInstance();
    		
    		// observer ServerModel
    		serverModelListener = new ModelListener() {
    			@Override
    			public void dataChange(ModelEvent event) {
    				boolean enabled = serverModel.isLoggedIn();
    				setEnabled(enabled);
    			}
    		};
    		serverModel.addListener(serverModelListener);
    		
    		// initialize enable status
    		serverModelListener.dataChange(null);
		}
	}
	

	@Override
	public void dispose() {
		// stop observing ServerModel
		if (serverModel != null && serverModelListener != null) {
			serverModel.removeListener(serverModelListener);
		}
	}


	/**
	 * Sub classes should overwrite this method alternatively to run(), to show the busy cursor 
	 * while running their action code.
	 */
	public void runWithBusyCursor() {
	}
	
	
	/* Default implementation, that calls runWithBusyCursor() while showing the busy cursor.
	 * Sub classes should either overwrite run() or runWithBusyCursor().
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				public void run() {
					try {
						runWithBusyCursor();
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
				}
			});
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

}
