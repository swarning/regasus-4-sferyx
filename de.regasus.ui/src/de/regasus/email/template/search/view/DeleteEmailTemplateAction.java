package de.regasus.email.template.search.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailMessage;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.email.EmailI18N;
import de.regasus.email.EmailTemplateModel;
import de.regasus.email.IImageKeys;
import de.regasus.ui.Activator;


/**
 * An action that is enabled when one or more entities are selected; upon execution the user is asked for
 * confirmation, after which the entities' ids are given to the model for deletion.
 *
 * @author manfred
 *
 */
public class DeleteEmailTemplateAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener{

	public static final String ID = "de.regasus.email.action.DeleteEmailTemplateAction";

	private final IWorkbenchWindow window;
	private List<EmailTemplate> selectedEtsd = new ArrayList<>();



	public DeleteEmailTemplateAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(EmailI18N.DeleteEmailTemplate_Text);
		setToolTipText(EmailI18N.DeleteEmailTemplate_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.EMAIL_DELETE
		));

		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	/**
	 * Deletes the selected EmailTemplateSearchData (one or more).
	 */
	@Override
	public void run() {
		String message = null;
		if (selectedEtsd.size() == 1) {
			message = NLS.bind(UtilI18N.ReallyDeleteOne, EmailLabel.EmailTemplate.getString(), selectedEtsd.get(0).getName());
		}
		else {
			message = NLS.bind(UtilI18N.ReallyDeleteMultiple, EmailLabel.EmailTemplates.getString());
		}


		boolean answer = MessageDialog.openQuestion(window.getShell(), UtilI18N.Question, message);
		if (answer) {
			List<Long> emailTemplateIDs = new ArrayList<>(selectedEtsd.size());
			for (EmailTemplate emailTemplateSearchData : selectedEtsd) {
				emailTemplateIDs.add(emailTemplateSearchData.getID());
			}

			try {
				EmailTemplateModel.getInstance().delete(emailTemplateIDs, false);
			}
			catch (ErrorMessageException e) {
				if (e.getErrorCode().equals(EmailMessage.CouldNotDeleteEmailTemplates_ExistingEmailDispatchOrders.name())) {
					message = EmailI18N.CouldNotDeleteEmailTemplates_ExistingEmailDispatchOrders_DeleteAnywayQuestion;
					answer = MessageDialog.openQuestion(window.getShell(), UtilI18N.Question, message);
					if (answer) {
						try {
							EmailTemplateModel.getInstance().delete(emailTemplateIDs, true);
						}
						catch (Exception e1) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
						}
					}
				}
				else {
					RegasusErrorHandler.handleUserError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
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
