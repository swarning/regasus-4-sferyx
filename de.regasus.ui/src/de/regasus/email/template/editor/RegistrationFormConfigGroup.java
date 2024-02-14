package de.regasus.email.template.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.email.EmailI18N;
import de.regasus.onlineform.combo.RegistrationFormConfigCombo;


public class RegistrationFormConfigGroup extends Group {

	// *************************************************************************
	// * Widgets
	// *

	private RegistrationFormConfigCombo registrationFormCombo;

	// *************************************************************************
	// * Other Attributes
	// *

	/**
	 * The listeners that are notified when the user makes changes in this group.
	 */
	private ModifySupport modifySupport = new ModifySupport(this);
	
	private EmailTemplate emailTemplate;

	// *************************************************************************
	// * Constructor
	// *

	public RegistrationFormConfigGroup(Composite parent, int style, Long eventPK) throws Exception {
		super(parent, style);

		setText(EmailI18N.UsageByOnlineForm);

		setLayout(new GridLayout(2, false));
		
		// Combo for registration form config (or none)

		Label webIdLabel = new Label(this, SWT.NONE);
		webIdLabel.setText(EmailI18N.WebId);
		webIdLabel.setToolTipText(EmailI18N.WebId_desc);
				
		registrationFormCombo = new RegistrationFormConfigCombo(this, SWT.NONE, eventPK);
		registrationFormCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		registrationFormCombo.addModifyListener(modifySupport);
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	// **************************************************************************
	// * Synching and Modifying
	// *

	/**
	 * Stores the widgets' contents to the entity.
	 * @param emailTemplate 
	 */
	public void syncEntityToWidgets(EmailTemplate emailTemplate) {
		emailTemplate.setRegistrationFormConfigPK(registrationFormCombo.getRegistrationFormConfigID());
	}


	/**
	 * Show the entity's properties to the widgets
	 */
	public void syncWidgetsToEntity() {
		registrationFormCombo.setRegistrationFormConfigID(emailTemplate.getRegistrationFormConfigPK());
	}


//	public void addRegistrationFormConfigModifyListener(ModifyListener modifyListener) {
//		registrationFormCombo.addModifyListener(modifyListener);
//	}
//
//
//	public void removeRegistrationFormConfigModifyListener(ModifyListener modifyListener) {
//		registrationFormCombo.removeModifyListener(modifyListener);
//	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Synching and Modifying
	// **************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *
	
	public void setEmailTemplate(EmailTemplate emailTemplate) {
		this.emailTemplate = emailTemplate;
		syncWidgetsToEntity();
	}

}
