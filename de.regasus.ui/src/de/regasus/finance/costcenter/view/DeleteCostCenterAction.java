package de.regasus.finance.costcenter.view;

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

import com.lambdalogic.messeinfo.invoice.data.CostCenterVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.CostCenter1Model;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.costcenter.editor.CostCenterEditor;
import de.regasus.ui.Activator;

public class DeleteCostCenterAction extends AbstractAction implements ISelectionListener {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.DeleteCostCenterAction"; 

	private final IWorkbenchWindow window;
	private List<CostCenterVO> selectedCostCenterVOs = new ArrayList<CostCenterVO>();
	
	
	
	public DeleteCostCenterAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		setId(ID);
		setText(FinanceI18N.CostCenter_Action_Delete_Text);
		setToolTipText(FinanceI18N.CostCenter_Action_Delete_ToolTip);
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
		if (!selectedCostCenterVOs.isEmpty()) {
			// open confirmation dialog before deletion
			boolean deleteOK = false;
			if (selectedCostCenterVOs.size() == 1) {
				final String title = UtilI18N.Question;
				String message = FinanceI18N.CostCenter_Action_Delete_ConfirmationMessage;

				// insert name into message text
				final CostCenterVO costCenter = selectedCostCenterVOs.get(0);
				String name = costCenter.getName();
				if (name == null) {
					name = "";
				}
				message = message.replaceFirst("<name>", name); 

				// open dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				// create message text
				final String title = UtilI18N.Question;
				String message = FinanceI18N.CostCenter_Action_DeleteList_ConfirmationMessage;

				// open dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			
			// If the user answered 'Yes' in the dialog
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					public void run() {
						/* Iterate over copy, because selectedCostCenterVOs will be
						 * indirectly updated while deleteting the entities via the model.
						 * After deleting there're no entities selected.
						 */
						for(CostCenterVO ccVO : new ArrayList<CostCenterVO>(selectedCostCenterVOs)) {
							try {
								CostCenter1Model.getInstance().delete(ccVO);

								// close editor
								CostCenterEditor.closeEditor(ccVO.getNo());
							}
							catch (Throwable t) {
								String message = FinanceI18N.CostCenter_Action_Delete_ErrorMessage;
								final String name = ccVO.getName();
								message = message.replaceFirst("<name>", name); 
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, message);
								return;
							}
							
						}
					}
					
				});
			}
		}			
	}

	
	@SuppressWarnings("rawtypes")
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedCostCenterVOs.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object selectedElement = (Object) it.next();
				if (selectedElement instanceof CostCenterVO) {
					CostCenterVO costCenter = (CostCenterVO) selectedElement;
					selectedCostCenterVOs.add(costCenter);
				}
				else {
					selectedCostCenterVOs.clear();
					break;
				}
			}
		}
		setEnabled(!selectedCostCenterVOs.isEmpty());
	}

}
