package de.regasus.participant.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.I18N;
import de.regasus.common.File;
import de.regasus.event.EventFileModel;

public class NotificationTemplatesPage extends WizardPage {

	public static final String NAME = "NotificationTemplatesPage";

	private List<? extends IParticipant> participantList;

	private NotificationTemplatesGroup notificationTemplatesGroup;

	public ArrayList<TemplateChangeListener> templateChangeListenerList = new ArrayList<TemplateChangeListener>();


	protected NotificationTemplatesPage(List<? extends IParticipant> participantList) {
		super(NAME);
		this.participantList = participantList;

		setTitle(I18N.PrintNotifications);
	}


	@Override
	public void createControl(Composite parent) {
		try {
			notificationTemplatesGroup = new NotificationTemplatesGroup(parent, SWT.NONE);

			Long eventPK = participantList.get(0).getEventId();
			List<File> noteTemplateFiles = EventFileModel.getInstance().getNoteTemplateFiles(eventPK);
			notificationTemplatesGroup.setNotificationTemplateList(noteTemplateFiles);

			setControl(notificationTemplatesGroup);

			notificationTemplatesGroup.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					System.out.println("notificationTemplatesGroup.widgetSelected()");
					setPageComplete(isPageComplete());
				}
			});
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public boolean isPageComplete() {
		// at least one template must be selected
		boolean isPageComplete = getCheckedTemplatePKs().size() > 0;

		if (isPageComplete){
			Collection<String> extensions = notificationTemplatesGroup.getSelectedTemplateExtensions();

			for (TemplateChangeListener templateChangeListener : templateChangeListenerList) {
				templateChangeListener.templateChanged(extensions);
			}
		}

		return isPageComplete;
	}


	public List<Long> getCheckedTemplatePKs() {
		return notificationTemplatesGroup.getCheckedTemplatePKs();
	}


	public void addTemplateChangeListener(TemplateChangeListener templateChangeListener) {
		templateChangeListenerList.add(templateChangeListener);
	}

}
