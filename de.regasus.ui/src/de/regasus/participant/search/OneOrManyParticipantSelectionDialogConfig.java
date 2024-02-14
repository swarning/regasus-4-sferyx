package de.regasus.participant.search;

import java.util.Collection;

import com.lambdalogic.util.rcp.ISelectionDialogConfig;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;

public class OneOrManyParticipantSelectionDialogConfig implements ISelectionDialogConfig {

	public static final OneOrManyParticipantSelectionDialogConfig INSTANCE = new OneOrManyParticipantSelectionDialogConfig();


	private OneOrManyParticipantSelectionDialogConfig() {
	}


	@Override
	public SelectionMode getSelectionMode() {
		return SelectionMode.MULTI_SELECTION;
	}


	@Override
	public boolean canFinish(Collection<?> selectedItems) {
		return selectedItems != null && selectedItems.size() >= 1;
	}


	@Override
	public String getMessage() {
		return I18N.ParticipantSelectionDialog_Description_OneOrMany;
	}

}
