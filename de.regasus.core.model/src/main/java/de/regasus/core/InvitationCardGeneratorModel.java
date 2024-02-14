package de.regasus.core;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.common.salutation.InvitationCardGenerator;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;

public class InvitationCardGeneratorModel implements CacheModelListener<Long> {

	private static InvitationCardGeneratorModel instance;

	private InvitationCardGenerator invitationCardGenerator;

	private ConfigParameterSetModel configParameterSetModel;



	public static InvitationCardGeneratorModel getInstance() {
		if (instance == null) {
			instance = new InvitationCardGeneratorModel();
		}
		return instance;
	}


	private InvitationCardGeneratorModel() {
		super();
		configParameterSetModel = ConfigParameterSetModel.getInstance();
	}


	public InvitationCardGenerator getInvitationCardGenerator() throws Exception {
		if (invitationCardGenerator == null) {

			configParameterSetModel.addListener(this);

			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
			invitationCardGenerator = InvitationCardGenerator.getInstance();
			invitationCardGenerator.setInvitationCardConfigParameterSet(configParameterSet.getInvitationCard());
		}
		return invitationCardGenerator;
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
		if (invitationCardGenerator != null) {
			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
			if (configParameterSet != null) {
				invitationCardGenerator.setInvitationCardConfigParameterSet(configParameterSet.getInvitationCard());
			}
		}
	}

}
