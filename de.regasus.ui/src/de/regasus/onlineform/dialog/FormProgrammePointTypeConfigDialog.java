package de.regasus.onlineform.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.FormProgrammePointTypeConfigVO;

import de.regasus.onlineform.OnlineFormI18N;

/**
 * The dialog shows the editable table of FormProgrammePointTypeConfigVOs (with checkboxes) together with buttons to
 * rearrange their order. Upon OK pressed the posision is taken to the VOs
 * 
 * @author manfred
 */
public class FormProgrammePointTypeConfigDialog extends TitleAreaDialog {

	private List<FormProgrammePointTypeConfigVO> configVOs;

	private FormProgrammePointTypeConfigComposite formProgrammePointTypeConfigComposite;

	private boolean useProgrammePointTypeNamesAsHeaders;
	

	public FormProgrammePointTypeConfigDialog(Shell parentShell, List<FormProgrammePointTypeConfigVO> configVOs, boolean useProgrammePointTypeNamesAsHeaders) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.useProgrammePointTypeNamesAsHeaders = useProgrammePointTypeNamesAsHeaders;
		this.configVOs = configVOs;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(ParticipantLabel.ProgrammePointTypes.getString());
		setMessage(OnlineFormI18N.ConfigureOrderAndBookingRequirementsForProgrammePointsPerTypes);

		Composite area = (Composite) super.createDialogArea(parent);

		formProgrammePointTypeConfigComposite = new FormProgrammePointTypeConfigComposite(area, SWT.NONE);
		formProgrammePointTypeConfigComposite.setConfigVOs(configVOs);
		formProgrammePointTypeConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		formProgrammePointTypeConfigComposite.setUseProgrammePointTypeNamesAsHeaders(useProgrammePointTypeNamesAsHeaders);
		
		return area;
	}


	/**
	 * The Window Icon and Title are made identical to the icon and the tooltip of the button that opens this dialog.
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setSize(900, 400);
		shell.setText(ParticipantLabel.ProgrammePointTypes.getString());
	}


	@Override
	protected void okPressed() {
		for (int i = 0; i < configVOs.size(); i++) {
			configVOs.get(i).setPosition(i);
			useProgrammePointTypeNamesAsHeaders = formProgrammePointTypeConfigComposite.isUseProgrammePointTypeNamesAsHeaders();
		}
		super.okPressed();
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	public boolean isUseProgrammePointTypeNamesAsHeaders() {
		return useProgrammePointTypeNamesAsHeaders;
	}

}
