package de.regasus.email.dispatch.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.common.composite.SmtpSettingsGroup;
import de.regasus.email.EmailI18N;

/**
 * A wizard page to modify for a particular dispatch order the SMTP settings that 
 * are predefined in the event or in the server properties.
 */
public class SmtpSettingsPage extends WizardPage {

	private static final String NAME = "SmtpSettingsPage";

	// *************************************************************************
	// * Widgets
	// *

	private SmtpSettingsGroup group;
	
	// *************************************************************************
	// * Other Attributes
	// *
	
	private SmtpSettingsVO settings;


	// *************************************************************************
	// * Constructor
	// *

	protected SmtpSettingsPage(SmtpSettingsVO settings, EventVO eventVO) {
		super(NAME);
		this.settings = settings;
		
		if (eventVO == null) {
			setTitle(EmailLabel.SmtpSettings.getString());
			setDescription(EmailI18N.SmtpSettingsPage_NoEvent_Description);
		}
		else {
			String mnemonic = eventVO.getMnemonic();
			setTitle(NLS.bind(EmailLabel.SmtpSettingsFor.getString(), mnemonic));
			setDescription(NLS.bind(EmailI18N.SmtpSettingsPage_Event_Description, mnemonic));
		}
	}
	
	public void createControl(Composite parent) {
		group = new SmtpSettingsGroup(parent, SWT.NONE);
		group.setSmtpSettingsVO(settings);
		setControl(group);
		
		group.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				updateState();				
			}
		});
		updateState();
	}

	
	public void syncEntityToWidgets() {
		group.syncEntityToWidgets();
	}


	// *************************************************************************
	// * Event handling
	// *

	private void updateState() {
		group.syncEntityToWidgets();
		
		if (settings == null || settings.getHost().length() == 0) {
			setErrorMessage(EmailI18N.Error_NoSmtpHost);
			setPageComplete(false);
		}
		else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}

}
