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
import de.regasus.participant.type.combo.ParticipantTypeCombo;
import de.regasus.ui.Activator;

public class CollectiveChangeParticipantTypeDialog extends TitleAreaDialog  {

	/**
	 * Number of selected participants.
	 */
	private int participantCount;

	/**
	 * Widget for ParticipantType to set.
	 */
	private ParticipantTypeCombo participantTypeCombo;

	/**
	 * Selected ParticipantType.
	 */
    private Long participantTypePK;

    /**
     * Event Long of all selected Participants.
     */
    private Long eventID;


	public CollectiveChangeParticipantTypeDialog(Shell parentShell, Long eventID, int participantCount) {
		super(parentShell);

		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.eventID = eventID;
		this.participantCount = participantCount;
	}


	@Override
    public void create() {
		super.create();

		// set title and message after the dialog has been opened
		String title = I18N.CollectiveChangeParticipantTypeDialog_Title;
		title = title.replace("<count>", String.valueOf(participantCount));

		setTitle(title);
		setMessage(I18N.CollectiveChangeParticipantTypeDialog_Message);
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
			label.setText( Participant.PARTICIPANT_TYPE.getString() );
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

			participantTypeCombo = new ParticipantTypeCombo(composite, SWT.READ_ONLY);
			participantTypeCombo.setWithEmptyElement(false);
			participantTypeCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true));
			participantTypeCombo.setEventID(eventID);
			participantTypeCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					participantTypePK = participantTypeCombo.getEntity().getId();
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
    	boolean pageComplete = participantTypeCombo.getEntity() != null;
    	getButton(IDialogConstants.OK_ID).setEnabled(pageComplete);
    }


    public Long getParticipantTypePK() {
    	return participantTypePK;
    }

}
