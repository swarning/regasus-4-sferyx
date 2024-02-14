package com.lambdalogic.util.rcp;

import java.util.Collection;

/**
 * Define how many rows have to be selected in a component.
 *
 */
public interface ISelectionDialogConfig {

	/**
	 * Decide if the given selection satisfies the requirements.
	 * @param selectedItems
	 * @return
	 */
	boolean canFinish(Collection<?> selectedItems);

	/**
	 * The {@link SelectionMode} corresponding to the number of rows that have to be selected.
	 * @return
	 */
	SelectionMode getSelectionMode();

	/**
	 * Text that asks the user to select items according to the requirements.
	 * @return
	 */
	String getMessage();

}
