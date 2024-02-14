package de.regasus.event.editor;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;

import de.regasus.I18N;
import de.regasus.common.File;
import de.regasus.event.EventFileModel;

public class EventBadgeTemplateGroup extends EventTemplateGroup {

	public EventBadgeTemplateGroup(Composite parent) {
		super(parent, I18N.BadgeTemplates);
	}


	@Override
	protected Collection<File> readTemplates(Long eventId) throws Exception {
		return EventFileModel.getInstance().getBadgeTemplateFiles(eventId);
	}


	@Override
	protected File uploadTemplate(Long eventId, byte[] content, String language, String filePath) throws Exception {
		File file = EventFileModel.getInstance().uploadBadgeTemplate(eventId, content, language, filePath);
		return file;
	}


	@Override
	protected void deleteTemplate(File template) throws Exception {
		EventFileModel.getInstance().deleteBadgeTemplate(template);
	}


	@Override
	protected String getUploadDialogTitle() {
		return I18N.BadgeTemplateUploadDialogTitle;
	}

}
