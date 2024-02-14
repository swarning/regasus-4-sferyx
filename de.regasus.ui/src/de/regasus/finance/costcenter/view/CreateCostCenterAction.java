package de.regasus.finance.costcenter.view;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.costcenter.editor.CostCenterEditor;
import de.regasus.finance.costcenter.editor.CostCenterEditorInput;
import de.regasus.ui.Activator;

public class CreateCostCenterAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.CreateCostCenterAction"; 

	private final IWorkbenchWindow window;
	
	
	public CreateCostCenterAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(de.regasus.finance.FinanceI18N.CostCenter_Action_Create_Text);
		setToolTipText(de.regasus.finance.FinanceI18N.CostCenter_Action_Create_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}

	
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		CostCenterEditorInput editorInput = new CostCenterEditorInput();
		try {
			page.openEditor(editorInput, CostCenterEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
