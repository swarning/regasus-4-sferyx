package de.regasus.participant.collectivechange.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.participant.Participant;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.state.combo.ParticipantStateCombo;
import de.regasus.ui.Activator;

public class CollectiveChangeParticipantStateDialog extends TitleAreaDialog  {

	/**
	 * Number of selected participants.
	 */
	private int participantCount;

	/**
	 * Widget for ParticipantState to set.
	 */
	private ParticipantStateCombo participantStateCombo;

	/**
	 * Selected ParticipantState.
	 */
	private Long participantStatePK;


	public CollectiveChangeParticipantStateDialog(Shell parentShell, int participantCount) {
		super(parentShell);

		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.participantCount = participantCount;
	}


	@Override
    public void create() {
		super.create();

		// set title and message after the dialog has been opened
		String title = I18N.CollectiveChangeParticipantStateDialog_Title;
		title = title.replace("<count>", String.valueOf(participantCount));

		setTitle(title);
		setMessage(I18N.CollectiveChangeParticipantStateDialog_Message);
    }


	 /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

    	resetButtonState();
    }


    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
    	Composite area = (Composite) super.createDialogArea(parent);

        try {
        	area.setLayout(new GridLayout());

	        Composite composite = new Composite(area, SWT.NONE);
	        composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			composite.setLayout(new GridLayout(2, false));

			Label label = new Label(composite, SWT.NONE);
			label.setText( Participant.PARTICIPANT_STATE.getString() );
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

			participantStateCombo = new ParticipantStateCombo(composite, SWT.READ_ONLY);
			participantStateCombo.setWithEmptyElement(false);
			participantStateCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			participantStateCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					participantStatePK = participantStateCombo.getEntity().getID();
					resetButtonState();
				}
			});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

    	return area;
    }


    private void resetButtonState() {
    	boolean pageComplete = participantStateCombo.getEntity() != null;
    	getButton(IDialogConstants.OK_ID).setEnabled(pageComplete);
    }


    public Long getParticipantStatePK() {
    	return participantStatePK;
    }

}
