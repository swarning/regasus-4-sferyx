package de.regasus.core.model;

import java.util.Collections;
import java.util.List;



public abstract class MIListModel<ModelType> extends MIModel<List<ModelType>> {

	protected MIListModel() {
		super();
	}
	
	
	@Override
	public List<ModelType> getModelData() throws Exception {
		List<ModelType> modelData = super.getModelData();
		
		if (modelData == null) {
			modelData = Collections.emptyList();
		}
		
		return modelData;
	}

}
