package de.regasus.event.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;

import de.regasus.common.composite.SmtpSettingsGroup;

public class EventSmtpComposite extends Composite {

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	// This group used to be in the masterdata.ui plugin, I moved it to the event.ui plugin
	// in order to remove the dependency from email.ui to masterdata.ui, which lead to cycles
	// when i needed the dependency from formedit.admin to email.ui.

	private SmtpSettingsGroup smtpSettingsGroup;

	// *
	// * Widgets
	// **************************************************************************


	public EventSmtpComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new FillLayout(SWT.HORIZONTAL));

		smtpSettingsGroup = new SmtpSettingsGroup(this, SWT.NONE);

		smtpSettingsGroup.addModifyListener(modifySupport);
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



	public void setEventVO(EventVO eventVO) {
		SmtpSettingsVO smtpSettingsVO = eventVO.getSmtpSettingsVO();
		smtpSettingsGroup.setSmtpSettingsVO(smtpSettingsVO);
	}

	public void syncEntityToWidgets() {
		smtpSettingsGroup.syncEntityToWidgets();
	}


	@Override
	public boolean setFocus() {
		return smtpSettingsGroup.setFocus();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
