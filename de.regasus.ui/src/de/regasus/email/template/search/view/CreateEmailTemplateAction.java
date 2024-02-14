package de.regasus.email.template.search.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailI18N;
import de.regasus.email.IImageKeys;
import de.regasus.email.template.editor.EmailTemplateEditor;
import de.regasus.email.template.editor.EmailTemplateEditorInput;
import de.regasus.ui.Activator;

/**
 * An action that opens an editor for a new EmailTemplate object. The (optional) EventPK for which the EmailTemplate is to be created 
 * must have been set before execution of the action.
 * 
 * @author manfred
 * 
 */
public class CreateEmailTemplateAction extends Action implements ActionFactory.IWorkbenchAction, ModelListener {

	public static final String ID = "de.regasus.email.action.CreateEmailTemplateAction"; 

	// Models
	private ServerModel serverModel = ServerModel.getInstance();
		
	// *************************************************************************
	// * Attribute
	// *

	private final IWorkbenchWindow window;

	private EmailTemplateSearchView emailTemplateSearchView;

	// *************************************************************************
	// * Constructor
	// *

	public CreateEmailTemplateAction(IWorkbenchWindow window, EmailTemplateSearchView emailTemplateSearchView) {
		super();
		this.window = window;
		this.emailTemplateSearchView = emailTemplateSearchView;
		setId(ID);
		setText(EmailI18N.CreateEmailTemplate_Text);
		setToolTipText(EmailI18N.CreateEmailTemplate_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.EMAIL_ADD));
		serverModel.addListener(this);
		this.dataChange(null);
	}
	
	
	
	// *************************************************************************
	// * Methods
	// *

	/**
	 * Creates an EditorInput for opening an Editor for a new EmailTemplate object, with a null id (to tell it is a new
	 * one), but possibly with an eventPK for which the EmailTemplate is to be created.
	 * 
	 * @author manfred
	 * 
	 */
	public void run() {
		Long eventPKLong = emailTemplateSearchView.getEventPK();
		
		EmailTemplateEditorInput editorInput = new EmailTemplateEditorInput(null, eventPKLong);
		IWorkbenchPage page = window.getActivePage();
		try {
			page.openEditor(editorInput, EmailTemplateEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	
	public void dispose() {
		serverModel.removeListener(this);
	}

	
	public void dataChange(ModelEvent event) {
		boolean enable = serverModel.isLoggedIn();
		setEnabled(enable);
	}

}
