package de.regasus.finance.customeraccount.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.invoice.data.CustomerAccountVO;
import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.customeraccount.editor.CustomerAccountEditor;
import de.regasus.finance.customeraccount.editor.CustomerAccountEditorInput;
import de.regasus.ui.Activator;

public class EditCustomerAccountAction extends AbstractAction implements ISelectionListener {
	public static final String ID = "com.lambdalogic.mi.invoice.ui.EditCustomerAccountAction"; 

	private final IWorkbenchWindow window;

	private String customerAccountNo = null;

	
	public EditCustomerAccountAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		setId(ID);
		setText(FinanceI18N.CustomerAccount_Action_Edit_Text);
		setToolTipText(FinanceI18N.CustomerAccount_Action_Edit_ToolTip);
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
		if (customerAccountNo != null) {
			IWorkbenchPage page = window.getActivePage();
			CustomerAccountEditorInput editorInput = new CustomerAccountEditorInput(customerAccountNo);
			try {
				page.openEditor(editorInput, CustomerAccountEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}

	
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		customerAccountNo = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object selectedObject = selection.getFirstElement();
				if (selectedObject instanceof CustomerAccountVO) {
					customerAccountNo = ((CustomerAccountVO) selectedObject).getPrimaryKey();
				}
			}
		}
		setEnabled(customerAccountNo != null);
	}

}
