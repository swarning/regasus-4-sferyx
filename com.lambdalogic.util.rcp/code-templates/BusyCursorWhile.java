import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.error.ErrorHandler;


public class BusyCursorWhile {


	public BusyCursorWhile() {
		
		// prepare long running operations
		// ...
	
		final boolean[] success = {false};
		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				public void run() {
					// do the long running operations
					// ...
					
					success[0] = true;
				}
			});
		}
		catch (Throwable t) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, null);
		}
	
		if (success[0]) {
			// do the things after success
			// ...
		}
		
	}
	
}
