package de.regasus.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.widget.SWTHelper;

public class UserSourceProvider
extends AbstractSourceProvider
implements ModelListener {

	public static final String SOURCE_NAME_USER = "user";
    
    
	public static final String[] SOURCE_NAMES = {
		SOURCE_NAME_USER
	};

	
    private ServerModel serverModel;
	
    
	public UserSourceProvider() {
		serverModel = ServerModel.getInstance();
		serverModel.addListener(this);
	}
	

	public void dispose() {
		serverModel.removeListener(this);
	}


	@SuppressWarnings("unchecked")
	public Map getCurrentState() {
        Map<String, String> currentStateMap = new HashMap<String, String>(1);
        
        String user = serverModel.getUser();
		currentStateMap.put(SOURCE_NAME_USER, user);

		return currentStateMap;
	}


	public String[] getProvidedSourceNames() {
		return SOURCE_NAMES; 
	}


	@Override
	public void dataChange(ModelEvent event) {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			public void run() {
				fireSourceChanged(ISources.WORKBENCH, getCurrentState());
			}
		});
	}
	
}
