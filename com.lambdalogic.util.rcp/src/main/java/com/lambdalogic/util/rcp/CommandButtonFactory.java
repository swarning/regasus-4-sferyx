package com.lambdalogic.util.rcp;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.lambdalogic.util.rcp.error.ErrorHandler;


public class CommandButtonFactory {

	public static Button createButton(final Composite parent, final int style, final String commandID) {
		final Button button = new Button(parent, style);
		
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		
		final Command command = commandService.getCommand(commandID);
		
		// init text
		try {
			button.setText(command.getName());
		}
		catch (NotDefinedException e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, CommandButtonFactory.class.getName(), e);
		}
		
		// init toolTip/description
		try {
			button.setToolTipText(command.getDescription());
		}
		catch (NotDefinedException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
		
		// init enable state
		button.setEnabled(command.isEnabled());
		
		
		final ICommandListener commandListener = new CommandButtonListener(button);
		command.addCommandListener(commandListener);

		/**
		 * If you don't to this, any part de/activation may leat to an SWTException: Widget is disposed,
		 * because the button might be disposed, but still be listening to command events.
		 */
		button.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				command.removeCommandListener(commandListener);
			}
		});
		
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
					handlerService.executeCommand(commandID, null);
				}
				catch (Exception e) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

		return button;
	}
	
}
