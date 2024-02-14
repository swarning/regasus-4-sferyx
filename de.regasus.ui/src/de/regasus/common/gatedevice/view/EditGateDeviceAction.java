/**
 * EditGateDeviceAction.java
 * created on 25.09.2013 14:00:09
 */
package de.regasus.common.gatedevice.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.data.GateDeviceVO;

import de.regasus.I18N;
import de.regasus.common.gatedevice.editor.GateDeviceEditor;
import de.regasus.common.gatedevice.editor.GateDeviceEditorInput;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;

public class EditGateDeviceAction 
extends AbstractAction
implements ISelectionListener {
	
	public static final String ID = "de.regasus.event.gatedevice.EditGateDeviceAction";
	
	private final IWorkbenchWindow window;
	
	private Long gateDevicePK;
	
	
	public EditGateDeviceAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		setId(ID);
		setText(I18N.EditGateDeviceAction_Text);
		setToolTipText(I18N.EditGateDeviceAction_ToolTip);
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.EDIT
		));
		
		window.getSelectionService().addSelectionListener(this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	@Override
	public void run() {
		if (gateDevicePK != null) {
			IWorkbenchPage page = window.getActivePage();
			GateDeviceEditorInput editorInput = new GateDeviceEditorInput(gateDevicePK);
			try {
				page.openEditor(editorInput, GateDeviceEditor.ID);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		super.run();
	}
	

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		gateDevicePK = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object selectedElement = selection.getFirstElement();
				if (selectedElement instanceof GateDeviceVO) {
					GateDeviceVO gateDeviceVO = (GateDeviceVO) selectedElement;
					gateDevicePK = gateDeviceVO.getID();
				}
			}
		}
		setEnabled(gateDevicePK != null);
	}

}
