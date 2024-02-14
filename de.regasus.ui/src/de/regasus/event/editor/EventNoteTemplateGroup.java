package de.regasus.event.editor;

import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.exception.WarnMessageException;
import com.lambdalogic.messeinfo.kernel.KernelMessages;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.common.File;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventFileModel;
import de.regasus.ui.Activator;

public class EventNoteTemplateGroup extends EventTemplateGroup {

	public EventNoteTemplateGroup(Composite parent) {
		super(parent, I18N.NoteTemplates);
	}


	@Override
	protected Collection<File> readTemplates(Long eventId) throws Exception {
		return EventFileModel.getInstance().getNoteTemplateFiles(eventId);
	}


	@Override
	protected File uploadTemplate(Long eventId, byte[] content, String language, String filePath) throws Exception {
		File file = EventFileModel.getInstance().uploadNoteTemplate(eventId, content, language, filePath);
		return file;
	}


	/* Overridden because special error handling is necessary.
	 */
	@Override
	protected void deleteTemplate(File file) throws Exception {
		try {
			EventFileModel.getInstance().deleteNoteTemplate(file, false);
		}
		catch (WarnMessageException e) {
			if (e.getErrorCode().equals(KernelMessages.NoteTemplateNotDeleted_ReferencingEmailTemplate_Message.name())) {
				String msg = e.getMessage();
				msg += "\n\n" + I18N.DeleteNoteTemplateInSpiteOfReferencingEmailTemplates;
				boolean answer = MessageDialog.openQuestion(getShell(), UtilI18N.Question, msg);
				if (answer) {
					try {
						EventFileModel.getInstance().deleteNoteTemplate(file, true);
					}
					catch (Exception e1) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
					}
				}
			}
			else {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	protected String getUploadDialogTitle() {
		return I18N.NoteTemplateUploadDialogTitle;
	}

}
