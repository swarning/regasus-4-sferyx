package de.regasus.email.template.search.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailI18N;
import de.regasus.email.EmailTemplateModel;
import de.regasus.email.IImageKeys;
import de.regasus.email.template.dialog.CopyEmailTemplateOtherEventWizard;
import de.regasus.ui.Activator;

/**
 * This action creates a duplicate of the selected {@link EmailTemplate}s.
 * 
 * @author manfred
 *
 */
public class DuplicateEmailTemplateAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener {
	public static final String ID = "de.regasus.email.action.DuplicateEmailTemplateAction"; 

	private final IWorkbenchWindow window;

	private List<EmailTemplate> selectedEtsd = new ArrayList<EmailTemplate>();

	
	public DuplicateEmailTemplateAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(EmailI18N.DuplicateEmailTemplate_Text);
		setToolTipText(EmailI18N.DuplicateEmailTemplate_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.EMAIL_DUPLICATE
		));
		
		window.getSelectionService().addSelectionListener(EmailTemplateSearchView.ID, this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	public void run() {
		try {
			List<Long> emailTemplateIDs = new ArrayList<Long>();
			for (EmailTemplate etsd : selectedEtsd) {
				emailTemplateIDs.add(etsd.getID());
			}
			
			EmailTemplateModel emailTemplateModel = EmailTemplateModel.getInstance();
			List<EmailTemplate> emailTemplateList = emailTemplateModel.getEmailTemplates(emailTemplateIDs);

			
			CopyEmailTemplateOtherEventWizard wizard = new CopyEmailTemplateOtherEventWizard(emailTemplateList);
			WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
			dialog.create();
			dialog.getShell().setSize(700, 600);
			dialog.open();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
			
	}

	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof EmailTemplateSearchView) {
			selectedEtsd = SelectionHelper.toList(selection, EmailTemplate.class);
			setEnabled(!selectedEtsd.isEmpty());
		}
		else {
			setEnabled(false);
		}
	}
}
