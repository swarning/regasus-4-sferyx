package de.regasus.email.template.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

/**
 * A composite used in the {@link EmailTemplateEditor} to control what dynamic attachments are to be added.
 */
public class CertificatesGroup extends Group {

	// Widgets
	private Button withCertificatesButton;

	// Other Attributes
	private EmailTemplate emailTemplate;


	/**
	 * The listeners that are notified when the user makes changes in this group.
	 */
	private ModifySupport modifySupport = new ModifySupport(this);



	public CertificatesGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		setLayout(new GridLayout(2, false));
		setText(EmailLabel.Certificates.getString());
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		{
			withCertificatesButton = new Button(this, SWT.CHECK);
			withCertificatesButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

			withCertificatesButton.addSelectionListener(modifySupport);

			Label withBadgesLabel = new Label(this, SWT.WRAP | SWT.LEFT);
			withBadgesLabel.setText(EmailLabel.WithCertificates.getString());
			withBadgesLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		}
	}


	/**
	 * Stores the widgets' contents to the entity.
	 */
	public void syncEntityToWidgets(EmailTemplate emailTemplate) {
		emailTemplate.setWithCertificates(withCertificatesButton.getSelection());
	}


	public void setEmailTemplate(EmailTemplate emailTemplate) {
		this.emailTemplate = emailTemplate;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (emailTemplate != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						withCertificatesButton.setSelection(emailTemplate.isWithCertificates());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
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
