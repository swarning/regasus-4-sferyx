/**
 * DeleteGateDeviceAction.java
 * created on 25.09.2013 14:11:52
 */
package de.regasus.common.gatedevice.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.data.GateDeviceVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.common.GateDeviceModel;
import de.regasus.common.gatedevice.editor.GateDeviceEditor;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;

public class DeleteGateDeviceAction 
extends AbstractAction 
implements ISelectionListener {

	public static final String ID = "de.regasus.event.gatedevice.DeleteGateDeviceAction";
	
	private final IWorkbenchWindow window;
	private List<GateDeviceVO> selectedGateDeviceVOs = new ArrayList<GateDeviceVO>();;
	
	
	public DeleteGateDeviceAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);

		this.window = window;
		
		setId(ID);
		setText(I18N.DeleteGateDeviceAction_Text);
		setToolTipText(I18N.DeleteGateDeviceAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.DELETE
		));
	
		window.getSelectionService().addSelectionListener(this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	@Override
	public void run() {
		if (!selectedGateDeviceVOs.isEmpty()) {
			// open confirmation dialog before deletion
			boolean deleteOK = false;
			if (selectedGateDeviceVOs.size() == 1) {
				// create message text
				String title = I18N.DeleteGateDeviceConfirmation_Title;
				String message = I18N.DeleteGateDeviceConfirmation_Message;
				
				// insert name of the GateDevice into message text
				GateDeviceVO gateDeviceVO = selectedGateDeviceVOs.get(0);
				String name = gateDeviceVO.getName();
				message = message.replaceFirst("<name>", name); 
				
				// open dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				// create message text
				String title = I18N.DeleteGateDeviceListConfirmation_Title;
				String message = I18N.DeleteGateDeviceListConfirmation_Message;
				
				// open dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					public void run() {
						/* Get the PKs now, because selected... will indirectly updated 
						 * while deleting the entities via the model. After deleting
						 * there're no entities selected.
						 */
						final List<Long> gateDevicePKs = 
							(List<Long>) GateDeviceVO.getPKs(selectedGateDeviceVOs);
							
						try {
							List<GateDeviceVO> copies = new ArrayList<GateDeviceVO>(selectedGateDeviceVOs);
							for (GateDeviceVO gateDeviceVO : copies) {
								// delete ProgrammePointType
								GateDeviceModel.getInstance().delete(gateDeviceVO);
							}
						}
						catch (Throwable t) {
							String msg = I18N.DeleteGateDeviceErrorMessage;
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
							// cancel if an error occurs while deletion to avoid closing editors
							return;
						}
						
						// search for editors and close them
						GateDeviceEditor.closeEditors(gateDevicePKs);
					}
					
				});
			}
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedGateDeviceVOs.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object selectedElement = it.next();
				if (selectedElement instanceof GateDeviceVO) {
					GateDeviceVO gateDeviceVO = (GateDeviceVO) selectedElement;
					selectedGateDeviceVOs.add(gateDeviceVO);
				}
				else {
					selectedGateDeviceVOs.clear();
					break;
				}
			}
		}
		setEnabled(!selectedGateDeviceVOs.isEmpty());
	}

}
