package de.regasus.email.template.search.view;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailI18N;
import de.regasus.email.IImageKeys;
import de.regasus.email.template.editor.EmailTemplateEditor;
import de.regasus.email.template.editor.EmailTemplateEditorInput;
import de.regasus.ui.Activator;

public class EditEmailTemplateAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener {
	public static final String ID = "de.regasus.email.action.EditEmailTemplateAction"; 

	private final IWorkbenchWindow window;

	private List<EmailTemplate> selectedEtsd;

	
	public EditEmailTemplateAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(EmailI18N.EditEmailTemplate_Text);
		setToolTipText(EmailI18N.EditEmailTemplate_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.EMAIL_EDIT
		));
		
		window.getSelectionService().addSelectionListener(EmailTemplateSearchView.ID, this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(EmailTemplateSearchView.ID, this);
	}
	
	
	public void run() {
		EmailTemplate etsd = selectedEtsd.get(0);

		IWorkbenchPage page = window.getActivePage();
		EmailTemplateEditorInput editorInput = new EmailTemplateEditorInput(etsd.getID(), etsd.getEventPK());
		
		
		try {
			page.openEditor(editorInput, EmailTemplateEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}	
	}

	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		selectedEtsd = SelectionHelper.toList(selection, EmailTemplate.class);
		setEnabled(selectedEtsd.size() == 1);
	}

}
