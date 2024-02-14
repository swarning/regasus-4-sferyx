package de.regasus.person;

import com.lambdalogic.messeinfo.config.parameterset.AddressLabelConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.salutation.AddressLabelGenerator;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.model.Activator;


/**
 * This class provides an AddressLabelGenerator with an actual AddressLabelConfigParameterSet.
 * It should be used instead of AddressLabelGenerator.getInstance() directly.
 */
public class AddressLabelGeneratorModel implements CacheModelListener<Long> {

	private static AddressLabelGeneratorModel singleton = null;
	
	private AddressLabelGenerator addressLabelGenerator;
	private ConfigParameterSetModel configParameterSetModel;
	
	
	public static AddressLabelGeneratorModel getInstance() {
		if (singleton == null) {
			singleton = new AddressLabelGeneratorModel();
		}
		return singleton;
	}

	
	private AddressLabelGeneratorModel() {
		addressLabelGenerator = AddressLabelGenerator.getInstance();
		
		configParameterSetModel = ConfigParameterSetModel.getInstance();
		initConfigParameterSet();
		configParameterSetModel.addListener(this);
	}


	private void initConfigParameterSet() {
		try {
			AddressLabelConfigParameterSet addressLabel = null;
			
			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
			if (configParameterSet != null) {
				addressLabel = configParameterSet.getAddressLabel();
			}
			
			addressLabelGenerator.setAddressLabelConfigParameterSet(addressLabel);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	
	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		initConfigParameterSet();		
	}

	
	public AddressLabelGenerator getAddressLabelGenerator() {
		return addressLabelGenerator;
	}
	
}
