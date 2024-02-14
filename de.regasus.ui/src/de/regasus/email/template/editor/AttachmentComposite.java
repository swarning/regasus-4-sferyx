package de.regasus.email.template.editor;

import java.io.File;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.email.EmailAttachmentContainerHelper;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.ImagesAndAttachmentsFileContainer;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.core.ConfigParameterSetModel;


public class AttachmentComposite extends Composite {

	private ModifySupport modifySupport = new ModifySupport(this);

	private EmailTemplateEditor emailTemplateEditor;
	private Long eventId;

	// the entity
	private EmailTemplate emailTemplate;

	private AttachmentsGroup attachmentsGroup;
	private InvoicesGroup invoicesGroup;
	private NotificationTemplatesGroup notificationTemplatesGroup;
	private PersonFilesGroup personFilesGroup;
	private BadgesGroup badgesGroup;
	private CertificatesGroup certificatesGroup;


	// tmp dir for the duration of this Composite for image files
	private File tmpDir = null;


	public AttachmentComposite(
		Composite parent,
		int style,
		EmailTemplateEditor emailTemplateEditor
	)
	throws Exception {
		super(parent, style);

		// delete tmp dir when this Composite is disposed
		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (tmpDir != null) {
					FileHelper.deleteRecursively(tmpDir);
				}
			}
		});


		this.emailTemplateEditor = emailTemplateEditor;
		this.eventId = emailTemplateEditor.getEmailTemplate().getEventPK();

		createWidgets();
	}


	private void createWidgets() throws Exception {
		final int NUM_COLS = eventId != null ? 2 : 1;
		setLayout(new GridLayout(NUM_COLS, true));


		// The group for adding and removing file attachments
		attachmentsGroup = new AttachmentsGroup(this, SWT.NONE, emailTemplateEditor);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(attachmentsGroup);
		attachmentsGroup.addModifyListener(modifySupport);

		if (eventId != null) {
			notificationTemplatesGroup = new NotificationTemplatesGroup(this, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(notificationTemplatesGroup);
			notificationTemplatesGroup.addModifyListener(modifySupport);
		}

		personFilesGroup = new PersonFilesGroup(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(personFilesGroup);
		personFilesGroup.addModifyListener(modifySupport);

		if (eventId != null) {
			ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventId);
			if (   configParameterSet.getEvent().getCertificate().isVisible()
				&& configParameterSet.getEvent().getCertificate().getEmail().isVisible()
			) {
    			certificatesGroup = new CertificatesGroup(this, SWT.NONE);
    			GridDataFactory.fillDefaults().grab(true, false).applyTo(certificatesGroup);
    			certificatesGroup.addModifyListener(modifySupport);
			}

    		invoicesGroup = new InvoicesGroup(this, SWT.NONE, eventId);
    		GridDataFactory.fillDefaults().grab(true, false).applyTo(invoicesGroup);
    		invoicesGroup.addModifyListener(modifySupport);

    		badgesGroup = new BadgesGroup(this, SWT.NONE);
    		GridDataFactory.fillDefaults().grab(true, false).applyTo(badgesGroup);
    		badgesGroup.addModifyListener(modifySupport);
		}
	}


	public List<File> setEmailTemplate(EmailTemplate emailTemplate) throws Exception {
		this.emailTemplate = emailTemplate;

		if (notificationTemplatesGroup != null) {
			notificationTemplatesGroup.setEmailTemplate(emailTemplate);
		}

		personFilesGroup.setEmailTemplate(emailTemplate);

		if (certificatesGroup != null) {
			certificatesGroup.setEmailTemplate(emailTemplate);
		}

		if (invoicesGroup != null) {
			invoicesGroup.setEmailTemplate(emailTemplate);
		}

		if (badgesGroup != null) {
			badgesGroup.setEmailTemplate(emailTemplate);
		}


		// Extract files for attachmentGroup AND htmlEditorComposite and from unzipping byte array for
		// attachments
		byte[] bytes = emailTemplate.getAttachments();


		// delete previous tmp dir
		if (tmpDir != null) {
			FileHelper.deleteRecursively(tmpDir);
		}

		// create new tmp dir
		tmpDir = FileHelper.createTempDirectory("regasus", "");


		ImagesAndAttachmentsFileContainer unzipped =
			EmailAttachmentContainerHelper.buildImagesAndAttachmentsFileContainer(tmpDir, bytes);

		attachmentsGroup.setFileList(unzipped.attachments);

		List<File> imageFileList = unzipped.images;
		return imageFileList;
	}


	public void syncEntityToWidgets(List<File> imageFileList) throws Exception {
		// Collect Files from attachmentGroup AND htmlEditorComposite and store them in zipped byte array for attachments.
		List<File> attachments = attachmentsGroup.getFileList();
		byte[] bytes = EmailAttachmentContainerHelper.zip(imageFileList, attachments);
		emailTemplate.setAttachments(bytes);

		if (notificationTemplatesGroup != null) {
			notificationTemplatesGroup.syncEntityToWidgets(emailTemplate);
		}

		personFilesGroup.syncEntityToWidgets(emailTemplate);

		if (certificatesGroup != null) {
			certificatesGroup.syncEntityToWidgets(emailTemplate);
		}

		if (invoicesGroup != null) {
			invoicesGroup.syncEntityToWidgets(emailTemplate);
		}

		if (badgesGroup != null) {
			badgesGroup.syncEntityToWidgets(emailTemplate);
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
