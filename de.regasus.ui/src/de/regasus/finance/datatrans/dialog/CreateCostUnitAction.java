package de.regasus.finance.datatrans.dialog;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.finance.costunit.editor.CostUnitEditor;
import de.regasus.finance.costunit.editor.CostUnitEditorInput;
import de.regasus.ui.Activator;

public class CreateCostUnitAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.CreateCostUnitAction"; 

	private final IWorkbenchWindow window;
	
	
	public CreateCostUnitAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(de.regasus.finance.FinanceI18N.CostUnit_Action_Create_Text);
		setToolTipText(de.regasus.finance.FinanceI18N.CostUnit_Action_Create_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}

	
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		CostUnitEditorInput editorInput = new CostUnitEditorInput();
		try {
			page.openEditor(editorInput, CostUnitEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
}
