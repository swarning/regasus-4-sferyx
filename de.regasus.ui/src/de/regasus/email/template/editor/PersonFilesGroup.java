package de.regasus.email.template.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class PersonFilesGroup extends Group {

	// Widgets
	private Text personFiles;

	// Other Attributes
	private EmailTemplate emailTemplate;


	/**
	 * The listeners that are notified when the user makes changes in this group.
	 */
	private ModifySupport modifySupport = new ModifySupport(this);



	public PersonFilesGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		setLayout(new GridLayout(2, false));
		setText( EmailLabel.PersonFiles.getString() );
		setToolTipText( EmailLabel.PersonFiles_Description.getString() );
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// File Names
		{
			Label label = new Label(this, SWT.WRAP | SWT.LEFT);
			label.setText( EmailLabel.PersonFileExpression.getString() );
			label.setToolTipText( EmailLabel.PersonFileExpression_Description.getString() );
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

			personFiles = new Text(this, SWT.BORDER);
			personFiles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			personFiles.addModifyListener(modifySupport);
		}
	}


	/**
	 * Stores the widgets' contents to the entity.
	 */
	public void syncEntityToWidgets(EmailTemplate emailTemplate) {
		// Badges
		emailTemplate.setPersonFileExpression( personFiles.getText() );
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
						personFiles.setText( avoidNull(emailTemplate.getPersonFileExpression()) );
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
