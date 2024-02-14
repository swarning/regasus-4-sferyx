package de.regasus.finance.creditcardtype.view;

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

import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.CreditCardTypeModel;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.creditcardtype.editor.CreditCardTypeEditor;
import de.regasus.ui.Activator;

public class DeleteCreditCardTypeAction extends AbstractAction implements ISelectionListener {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.DeleteCreditCardTypeAction"; 

	private final IWorkbenchWindow window;
	private List<CreditCardTypeVO> selectedCreditCardTypeVOs = new ArrayList<CreditCardTypeVO>();
	
	
	
	public DeleteCreditCardTypeAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		setId(ID);
		setText(FinanceI18N.CreditCardType_Action_Delete_Text);
		setToolTipText(FinanceI18N.CreditCardType_Action_Delete_ToolTip);
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
		if (!selectedCreditCardTypeVOs.isEmpty()) {
			// Auftragsbestätigung
			boolean deleteOK = false;
			if (selectedCreditCardTypeVOs.size() == 1) {
				final String title = UtilI18N.Question;
				String message = FinanceI18N.CreditCardType_Action_Delete_ConfirmationMessage;
				// Im Abfragetext den Namen des zu löschenden Landes einfügen
				final CreditCardTypeVO countryVO = selectedCreditCardTypeVOs.get(0);
				String name = countryVO.getName();
				if (name == null) name = "";
				message = message.replaceFirst("<name>", name); 
				// Dialog öffnen
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				final String title = UtilI18N.Question;
				String message = FinanceI18N.CreditCardType_Action_DeleteList_ConfirmationMessage;
				// Open the Dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			
			// If the user answered 'Yes' in the dialog
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					public void run() {
						/* Iterate over copy, because selectedCreditCardTypeVOs will be
						 * indirectly update while deleteting the entities via the model.
						 * After deleting there're no entities selected.
						 */
						for(CreditCardTypeVO cctVO : new ArrayList<CreditCardTypeVO>(selectedCreditCardTypeVOs)) {
							try {
								CreditCardTypeModel.getInstance().delete(cctVO);
								// Nach Editoren suchen und schließen
								CreditCardTypeEditor.closeEditor(cctVO.getPK());
							}
							catch (Throwable t) {
								String message = FinanceI18N.CreditCardType_Action_Delete_ErrorMessage;
								final String name = cctVO.getName();
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
		selectedCreditCardTypeVOs.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object selectedElement = (Object) it.next();
				if (selectedElement instanceof CreditCardTypeVO) {
					CreditCardTypeVO creditCardTypeVO = (CreditCardTypeVO) selectedElement;
					selectedCreditCardTypeVOs.add(creditCardTypeVO);
				}
				else {
					selectedCreditCardTypeVOs.clear();
					break;
				}
			}
		}
		setEnabled(!selectedCreditCardTypeVOs.isEmpty());
	}

}
