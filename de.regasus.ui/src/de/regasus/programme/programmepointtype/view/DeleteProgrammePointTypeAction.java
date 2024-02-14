package de.regasus.programme.programmepointtype.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.programme.ProgrammePointTypeModel;
import de.regasus.programme.programmepointtype.editor.ProgrammePointTypeEditor;

public class DeleteProgrammePointTypeAction extends AbstractAction implements ISelectionListener {

	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.programmePointType.DeleteProgrammePointTypeAction"; 

	private final IWorkbenchWindow window;
	private List<ProgrammePointTypeVO> selectedProgrammePointTypeVOs = new ArrayList<ProgrammePointTypeVO>();
	
	
	
	public DeleteProgrammePointTypeAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);

		this.window = window;
		setId(ID);
		setText(I18N.DeleteProgrammePointTypeAction_Text);
		setToolTipText(I18N.DeleteProgrammePointTypeAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.DELETE
		));
	
		window.getSelectionService().addSelectionListener(this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	public void run() {
		if (! selectedProgrammePointTypeVOs.isEmpty()) {
			// open confirmation dialog before deletion
			boolean deleteOK = false;
			if (selectedProgrammePointTypeVOs.size() == 1) {
				// create message text
				String language = Locale.getDefault().getLanguage();
				String title = I18N.DeleteProgrammePointTypeConfirmation_Title;
				String message = I18N.DeleteProgrammePointTypeConfirmation_Message;
				
				// insert name of the ProgrammePointType into message text
				ProgrammePointTypeVO programmePointTypeVO = selectedProgrammePointTypeVOs.get(0);
				String name = programmePointTypeVO.getName().getString(language);
				message = message.replaceFirst("<name>", name); 
				
				// open dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				// create message text
				String title = I18N.DeleteProgrammePointTypeListConfirmation_Title;
				String message = I18N.DeleteProgrammePointTypeListConfirmation_Message;
				
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
						final List<Long> programmePointTypePKs = 
							(List<Long>) ProgrammePointTypeVO.getPKs(selectedProgrammePointTypeVOs);
							
						try {
							List<ProgrammePointTypeVO> copies = new ArrayList<ProgrammePointTypeVO>(selectedProgrammePointTypeVOs);
							for (ProgrammePointTypeVO programmePointTypeVO : copies) {
								// delete ProgrammePointType
								ProgrammePointTypeModel.getInstance().delete(programmePointTypeVO);
							}
						}
						catch (Throwable t) {
							String msg = I18N.DeleteProgrammePointTypeErrorMessage;
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
							// cancel if an error occurs while deletion to avoid closing editors
							return;
						}
						
						// search for editors and close them
						ProgrammePointTypeEditor.closeEditors(programmePointTypePKs);
					}
					
				});
			}
		}			
	}

	
	@SuppressWarnings("rawtypes")
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedProgrammePointTypeVOs.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object selectedElement = it.next();
				if (selectedElement instanceof ProgrammePointTypeVO) {
					ProgrammePointTypeVO programmePointTypeVO = (ProgrammePointTypeVO) selectedElement;
					selectedProgrammePointTypeVOs.add(programmePointTypeVO);
				}
				else {
					selectedProgrammePointTypeVOs.clear();
					break;
				}
			}
		}
		setEnabled(!selectedProgrammePointTypeVOs.isEmpty());
	}

}
