package de.regasus.profile.search;

import java.util.Collection;

import com.lambdalogic.util.rcp.ISelectionDialogConfig;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;

public class OneProfileSelectionDialogConfig implements ISelectionDialogConfig {

	public static final OneProfileSelectionDialogConfig INSTANCE = new OneProfileSelectionDialogConfig();


	private OneProfileSelectionDialogConfig() {
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
		return I18N.ProfileSelectionDialog_Description_One;
	}

}
