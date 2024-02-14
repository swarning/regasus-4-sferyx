package de.regasus.core.model;

import com.lambdalogic.util.model.Model;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;

public abstract class MIModel<ModelType> extends Model<ModelType>
implements ModelListener {

	protected ServerModel serverModel;
	
	protected MIModel() {
		super();
		serverModel = ServerModel.getInstance();
		serverModel.addListener(this);
	}
	
	
	@Override
	public void dataChange(ModelEvent event) {
		try {
			if (event.getSource() == serverModel) {
				ServerModelEvent serverModelEvent = (ServerModelEvent) event;
				
				if (!serverModel.isShutdown() &&
					(
    					serverModelEvent.getType() == ServerModelEventType.REFRESH ||
    					serverModelEvent.getType() == ServerModelEventType.LOGIN ||
    					serverModelEvent.getType() == ServerModelEventType.LOGOUT
					)
				) {
					refresh();
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	

	@Override
	public ModelType getModelData() throws Exception {
		ModelType modelData = null;
		
		if (serverModel.isLoggedIn()) {
			modelData = super.getModelData();
		}
		
		return modelData;
	}

}
