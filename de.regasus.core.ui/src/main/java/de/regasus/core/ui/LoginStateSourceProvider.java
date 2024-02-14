package de.regasus.core.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;

public class LoginStateSourceProvider 
extends AbstractSourceProvider 
implements ISourceProvider, ModelListener {

    public final static String LOGIN_STATE = "de.regasus.core.ui.var.loginState";
    private final static String LOGGED_IN = "loggedIn";
    private final static String LOGGED_OUT = "loggedOut";
    Boolean loggedIn = null;
	private ServerModel serverModel;

    public LoginStateSourceProvider() {
    	serverModel = ServerModel.getInstance();
		serverModel.addListener(this);
		
	}
  
    public Map<String, String> getCurrentState() {
    	if (loggedIn == null) {
    		loggedIn = Boolean.valueOf(serverModel.isLoggedIn());
    	}
        Map<String, String> currentStateMap = new HashMap<String, String>(1);
        String currentState =  loggedIn?LOGGED_IN:LOGGED_OUT;
        currentStateMap.put(LOGIN_STATE, currentState);
        return currentStateMap;
    }
    
    public void setLoggedIn(boolean loggedIn) {
        if (this.loggedIn == loggedIn) {
            return; // no change
        }
        
        this.loggedIn = loggedIn; 
        String currentState =  loggedIn?LOGGED_IN:LOGGED_OUT;
        fireSourceChanged(ISources.WORKBENCH, LOGIN_STATE, currentState);
    }

    
	public void dispose() {
		serverModel.removeListener(this);
	}

	public String[] getProvidedSourceNames() {
		 return new String[] {LOGIN_STATE}; 
	}

	public void dataChange(ModelEvent event) {
		if (event.getSource() instanceof ServerModel) {
			final boolean currentLoginState = serverModel.isLoggedIn();
			SWTHelper.asyncExecDisplayThread(
			new Runnable() {
				public void run() {
					setLoggedIn(currentLoginState);
				}
			});
		}
		
	} 

}
