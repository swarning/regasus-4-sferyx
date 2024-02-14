package de.regasus.profile.relationtype.view;

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

import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.ProfileRelationTypeModel;
import de.regasus.profile.relationtype.editor.ProfileRelationTypeEditor;

public class DeleteProfileRelationTypeAction extends AbstractAction implements ISelectionListener {

	public static final String ID = "de.regasus.profile.action.DeleteProfileRelationTypeAction"; 
	
	private final IWorkbenchWindow window;
	private List<ProfileRelationType> selectedProfileRelationTypes = new ArrayList<ProfileRelationType>();
	
	
	public DeleteProfileRelationTypeAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the 
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);
		
		this.window = window;
		
		setId(ID);
		setText(I18N.DeleteProfileRelationTypeAction_Text);
		setToolTipText(I18N.DeleteProfileRelationTypeAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.DELETE
		));
	
		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	@Override
	public void run() {
		if (!selectedProfileRelationTypes.isEmpty()) {
			boolean deleteOK = false;
			if (selectedProfileRelationTypes.size() == 1) {
				final String language = Locale.getDefault().getLanguage();
				final String title = I18N.DeleteProfileRelationTypeConfirmation_Title;
				String message = I18N.DeleteProfileRelationTypeConfirmation_Message;
				
				final ProfileRelationType profileRelationType = selectedProfileRelationTypes.get(0);
				final String name = profileRelationType.getName(language);
				message = message.replaceFirst("<name>", name); 
				
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				final String title = I18N.DeleteProfileRelationTypeListConfirmation_Title;
				String message = I18N.DeleteProfileRelationTypeListConfirmation_Message;
				
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {
					@Override
					public void run() {
						final List<Long> profileRelationTypeIDs = 
							ProfileRelationType.getPrimaryKeyList(selectedProfileRelationTypes);
						
						try {
							List<ProfileRelationType> copies = new ArrayList<ProfileRelationType>(selectedProfileRelationTypes);
							for (ProfileRelationType profileRelationType : copies) {
								ProfileRelationTypeModel.getInstance().delete(profileRelationType);
							}
						}
						catch (Throwable t) {
							String msg = I18N.DeleteProfileRelationTypeErrorMessage;
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
							return;
						}
						
						ProfileRelationTypeEditor.closeEditors(profileRelationTypeIDs);
					}
				});
			}
		}
	}


	@Override
	@SuppressWarnings("rawtypes")
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedProfileRelationTypes.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object selectedObject = it.next();
				if (selectedObject instanceof ProfileRelationType) {
					ProfileRelationType profileRelationType = (ProfileRelationType) selectedObject;
					selectedProfileRelationTypes.add(profileRelationType);
				}
				else {
					selectedProfileRelationTypes.clear();
					break;
				}
			}
		}
		setEnabled(!selectedProfileRelationTypes.isEmpty());
	}

}
