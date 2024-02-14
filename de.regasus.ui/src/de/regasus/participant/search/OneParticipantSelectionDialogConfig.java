package de.regasus.participant.search;

import java.util.Collection;

import com.lambdalogic.util.rcp.ISelectionDialogConfig;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;

public class OneParticipantSelectionDialogConfig implements ISelectionDialogConfig {

	public static final OneParticipantSelectionDialogConfig INSTANCE = new OneParticipantSelectionDialogConfig();


	private OneParticipantSelectionDialogConfig() {
	}


	@Override
	public SelectionMode getSelectionMode() {
		return SelectionMode.SINGLE_SELECTION;
	}


	@Override
	public boolean canFinish(Collection<?> selectedItems) {
		return selectedItems != null && selectedItems.size() == 1;
	}


	@Override
	public String getMessage() {
		return I18N.ParticipantSelectionDialog_Description_One;
	}

}
