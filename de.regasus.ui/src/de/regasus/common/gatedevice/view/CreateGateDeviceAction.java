/**
 * CreateGateDeviceAction.java
 * created on 25.09.2013 11:19:32
 */
package de.regasus.common.gatedevice.view;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.common.gatedevice.editor.GateDeviceEditor;
import de.regasus.common.gatedevice.editor.GateDeviceEditorInput;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;

public class CreateGateDeviceAction extends AbstractAction {
	
	public static final String ID = "de.regasus.event.gatedevice.CreateGateDeviceAction";
	
	private final IWorkbenchWindow window;
	
	
	public CreateGateDeviceAction(IWorkbenchWindow window) {
		super();
		
		this.window = window;
		setId(ID);
		setText(I18N.CreateGateDeviceAction_Text);
		setToolTipText(I18N.CreateGateDeviceAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}
	
	
	/* Don't runWithBusyCursor(), because.
	 * a) Opening an Editor doesn't last so long.
	 * b) runWithBusyCursor() is not done in the Display-Thread.
	 */
	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		GateDeviceEditorInput editorInput = new GateDeviceEditorInput();
		try {
			page.openEditor(editorInput, GateDeviceEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
