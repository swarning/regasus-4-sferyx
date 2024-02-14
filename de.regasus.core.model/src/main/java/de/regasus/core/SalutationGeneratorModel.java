package de.regasus.core;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.common.salutation.SalutationGenerator;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;

public class SalutationGeneratorModel implements CacheModelListener<Long> {

	private static SalutationGeneratorModel instance;

	private SalutationGenerator salutationGenerator;

	private ConfigParameterSetModel configParameterSetModel;



	public static SalutationGeneratorModel getInstance() {
		if (instance == null) {
			instance = new SalutationGeneratorModel();
		}
		return instance;
	}


	private SalutationGeneratorModel() {
		super();
		configParameterSetModel = ConfigParameterSetModel.getInstance();
	}


	public SalutationGenerator getSalutationGenerator() throws Exception {
		if (salutationGenerator == null) {

			configParameterSetModel.addListener(this);

			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
			salutationGenerator = new SalutationGenerator( configParameterSet.getSalutation() );
		}
		return salutationGenerator;
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			refresh();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void refresh() throws Exception {
		if (salutationGenerator != null) {
			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
			if (configParameterSet != null) {
				salutationGenerator.setSalutationConfigParameterSet(configParameterSet.getSalutation());
			}
		}
	}

}
