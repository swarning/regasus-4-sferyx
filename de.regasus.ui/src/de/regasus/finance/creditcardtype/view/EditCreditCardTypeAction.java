package de.regasus.finance.creditcardtype.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;
import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.creditcardtype.editor.CreditCardTypeEditor;
import de.regasus.finance.creditcardtype.editor.CreditCardTypeEditorInput;
import de.regasus.ui.Activator;

public class EditCreditCardTypeAction extends AbstractAction implements ISelectionListener {
	public static final String ID = "com.lambdalogic.mi.invoice.ui.EditCreditCardTypeAction"; 

	private final IWorkbenchWindow window;

	private Long creditCardTypeCode = null;

	
	public EditCreditCardTypeAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		setId(ID);
		setText(FinanceI18N.CreditCardType_Action_Edit_Text);
		setToolTipText(FinanceI18N.CreditCardType_Action_Edit_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID, 
			IImageKeys.EDIT
		));
		
		window.getSelectionService().addSelectionListener(this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	public void run() {
		if (creditCardTypeCode != null) {
			IWorkbenchPage page = window.getActivePage();
			CreditCardTypeEditorInput editorInput = new CreditCardTypeEditorInput(creditCardTypeCode);
			try {
				page.openEditor(editorInput, CreditCardTypeEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}

	
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		creditCardTypeCode = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object selectedObject = selection.getFirstElement();
				if (selectedObject instanceof CreditCardTypeVO) {
					creditCardTypeCode = ((CreditCardTypeVO) selectedObject).getPrimaryKey();
				}
			}
		}
		setEnabled(creditCardTypeCode != null);
	}

}
