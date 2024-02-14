package de.regasus.core.model;

import com.lambdalogic.util.model.ModelEvent;

public class ServerModelEvent extends ModelEvent {

	private ServerModelEventType type;
	
	public ServerModelEvent(Object source, ServerModelEventType type) {
		super(source);
		this.type = type;
	}

	
	public ServerModelEventType getType() {
		return type;
	}
	
}
