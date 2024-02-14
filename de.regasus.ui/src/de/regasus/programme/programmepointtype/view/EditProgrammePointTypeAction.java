package de.regasus.programme.programmepointtype.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.I18N;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.programme.programmepointtype.editor.ProgrammePointTypeEditor;
import de.regasus.programme.programmepointtype.editor.ProgrammePointTypeEditorInput;


public class EditProgrammePointTypeAction extends AbstractAction implements ISelectionListener {
	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.programmePointType.EditProgrammePointTypeAction"; 

	private final IWorkbenchWindow window;

	private Long programmePointTypePK = null;

	
	public EditProgrammePointTypeAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		setId(ID);
		setText(I18N.EditProgrammePointTypeAction_Text);
		setToolTipText(I18N.EditProgrammePointTypeAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.EDIT
		));
		
		window.getSelectionService().addSelectionListener(this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	@Override
	public void run() {
		if (programmePointTypePK != null) {
			IWorkbenchPage page = window.getActivePage();
			ProgrammePointTypeEditorInput editorInput = new ProgrammePointTypeEditorInput(programmePointTypePK);
			try {
				page.openEditor(editorInput, ProgrammePointTypeEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}

	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		programmePointTypePK = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object selectedElement = selection.getFirstElement();
				if (selectedElement instanceof ProgrammePointTypeVO) {
					ProgrammePointTypeVO programmePointTypeVO = (ProgrammePointTypeVO) selectedElement;
					programmePointTypePK = programmePointTypeVO.getPK();
				}
			}
		}
		setEnabled(programmePointTypePK != null);
	}

}
