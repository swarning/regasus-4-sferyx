/**
 * Refactored out from the CommandButtonFactory.
 */
package com.lambdalogic.util.rcp;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.widgets.Button;

final class CommandButtonListener implements ICommandListener {

	/**
	 * The button whose state needs to change when command definitions change
	 */
	private final Button button;

	CommandButtonListener(Button button) {
		this.button = button;
	}


	public void commandChanged(CommandEvent commandEvent) {
		Command command = commandEvent.getCommand();
		
		if (commandEvent.isEnabledChanged()) {
			button.setEnabled(command.isEnabled());
		}
		
		if (commandEvent.isNameChanged()) {
			try {
				button.setText(command.getName());
			}
			catch (NotDefinedException e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
		
		if (commandEvent.isEnabledChanged()) {
			try {
				button.setToolTipText(command.getDescription());
			}
			catch (NotDefinedException e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}
}