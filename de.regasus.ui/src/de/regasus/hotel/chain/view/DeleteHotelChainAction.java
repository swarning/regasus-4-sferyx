package de.regasus.hotel.chain.view;

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

import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.hotel.HotelChain;
import de.regasus.hotel.HotelChainModel;
import de.regasus.hotel.chain.editor.HotelChainEditor;

public class DeleteHotelChainAction extends AbstractAction implements ISelectionListener {
	
	public static final String ID = "com.lambdalogic.mi.masterdata.ui.hotel.DeleteHotelChainAction"; 

	private final IWorkbenchWindow window;
	private List<HotelChain> selectedHotelChains = new ArrayList<HotelChain>();
	
	
	
	public DeleteHotelChainAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);

		this.window = window;
		setId(ID);
		setText(UtilI18N.Delete);
		setToolTipText(UtilI18N.Delete);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			IImageKeys.DELETE
		));
	
		window.getSelectionService().addSelectionListener(this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	public void run() {
		if (! selectedHotelChains.isEmpty()) {
			// open confirmation dialog before deletion
			boolean deleteOK = false;
			if (selectedHotelChains.size() == 1) {
				// create message text
				String title = I18N.DeleteHotelChainConfirmation_Title;
				String message = I18N.DeleteHotelChainConfirmation_Message;
				
				// insert name of the ProgrammePointType into message text
				HotelChain hotelChain = selectedHotelChains.get(0);
				String name = hotelChain.getName();
				message = message.replaceFirst("<name>", name); 
				
				// open dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				// create message text
				String title = I18N.DeleteHotelChainListConfirmation_Title;
				String message = I18N.DeleteHotelChainListConfirmation_Message;
				
				// open dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			
			// If the user answered 'Yes' in the dialog...
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					public void run() {
						/* Get the PKs now, because selected... will indirectly updated 
						 * while deleting the entities via the model. After deleting
						 * there're no entities selected.
						 */
						final List<Long> hotelChainPKs = (List<Long>) HotelChain.getPrimaryKeyList(selectedHotelChains);
							
						try {
							List<HotelChain> copies = new ArrayList<HotelChain>(selectedHotelChains);
							for (HotelChain hotelChain : copies) {
								// delete HotelChain
								HotelChainModel.getInstance().delete(hotelChain);
							}
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
							// cancel if an error occurs while deletion to avoid closing editors
							return;
						}
						
						// search for editors and close them
						HotelChainEditor.closeEditors(hotelChainPKs);
					}
					
				});
			}
		}			
	}

	
	@SuppressWarnings("rawtypes")
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedHotelChains.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object selectedElement = it.next();
				if (selectedElement instanceof HotelChain) {
					HotelChain hotelChain = (HotelChain) selectedElement;
					selectedHotelChains.add(hotelChain);
				}
				else {
					selectedHotelChains.clear();
					break;
				}
			}
		}
		setEnabled(!selectedHotelChains.isEmpty());
	}
	
}
