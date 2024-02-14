package de.regasus.finance.currency.view;

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

import com.lambdalogic.messeinfo.invoice.data.CurrencyVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.CurrencyModel;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.currency.editor.CurrencyEditor;
import de.regasus.ui.Activator;

public class DeleteCurrencyAction extends AbstractAction implements ISelectionListener {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.DeleteCurrencyAction"; 

	private final IWorkbenchWindow window;
	private List<CurrencyVO> selectedCurrencyVOs = new ArrayList<CurrencyVO>();
	
	
	
	public DeleteCurrencyAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		setId(ID);
		setText(FinanceI18N.Currency_Action_Delete_Text);
		setToolTipText(FinanceI18N.Currency_Action_Delete_ToolTip);
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
		if (!selectedCurrencyVOs.isEmpty()) {
			// open confirmation dialog before deletion
			boolean deleteOK = false;
			if (selectedCurrencyVOs.size() == 1) {
				// create message text
				final String title = UtilI18N.Question;
				String message = FinanceI18N.Currency_Action_Delete_ConfirmationMessage;

				// insert name into message text
				final CurrencyVO currencyVO = selectedCurrencyVOs.get(0);
				String name = currencyVO.getID();
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
				String message = FinanceI18N.Currency_Action_DeleteList_ConfirmationMessage;
				
				// open dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			
			// If the user answered 'Yes' in the dialog
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					public void run() {
						/* Iterate over copy, because selectedCurrencys will be
						 * indirectly updated while deleteting the entities via the model.
						 * After deleting there're no entities selected.
						 */
						for (CurrencyVO currencyVO : new ArrayList<CurrencyVO>(selectedCurrencyVOs)) {
							try {
								CurrencyModel.getInstance().delete(currencyVO);

								// close editor
								CurrencyEditor.closeEditor(currencyVO.getPrimaryKey());
							}
							catch (Throwable t) {
								String message = FinanceI18N.Currency_Action_Delete_ErrorMessage;
								message = message.replaceFirst("<name>", currencyVO.getID()); 
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
		selectedCurrencyVOs.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object selectedElement = (Object) it.next();
				if (selectedElement instanceof CurrencyVO) {
					CurrencyVO currencyVO = (CurrencyVO) selectedElement;
					selectedCurrencyVOs.add(currencyVO);
				}
				else {
					selectedCurrencyVOs.clear();
					break;
				}
			}
		}
		setEnabled(!selectedCurrencyVOs.isEmpty());
	}

}
