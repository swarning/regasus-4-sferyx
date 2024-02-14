package de.regasus.programme.programmepointtype.view;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.programme.programmepointtype.editor.ProgrammePointTypeEditor;
import de.regasus.programme.programmepointtype.editor.ProgrammePointTypeEditorInput;

public class CreateProgrammePointTypeAction extends AbstractAction {

	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.programmePointType.CreateProgrammePointTypeAction"; 

	private final IWorkbenchWindow window;
	

	
	public CreateProgrammePointTypeAction(IWorkbenchWindow window) {
		super();
		
		this.window = window;
		setId(ID);
		setText(I18N.CreateProgrammePointTypeAction_Text);
		setToolTipText(I18N.CreateProgrammePointTypeAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.CREATE
		));
	}
	

	/* Don't runWithBusyCursor(), because.
	 * a) Opening an Editor doesn't last so long.
	 * b) runWithBusyCursor() is not done in the Display-Thread.
	 */
	@Override
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		ProgrammePointTypeEditorInput editorInput = new ProgrammePointTypeEditorInput();
		try {
			page.openEditor(editorInput, ProgrammePointTypeEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
