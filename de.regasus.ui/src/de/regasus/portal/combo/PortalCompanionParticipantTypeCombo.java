package de.regasus.portal.combo;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.regasus.event.ParticipantType;
import de.regasus.portal.Portal;


public class PortalCompanionParticipantTypeCombo extends PortalParticipantTypeCombo {

	public PortalCompanionParticipantTypeCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected Collection<ParticipantType> getModelData() throws Exception {
		List<ParticipantType> modelData = null;
		if (portalId != null) {
			Portal portal = portalModel.getPortal(portalId);
			List<Long> participantTypeIds = portal.getCompanionParticipantTypeIds();
			modelData = participantTypeModel.getParticipantTypes(participantTypeIds);
		}
		return modelData;
	}

}
