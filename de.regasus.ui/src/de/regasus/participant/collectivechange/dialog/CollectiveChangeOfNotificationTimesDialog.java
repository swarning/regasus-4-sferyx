package de.regasus.participant.collectivechange.dialog;

import java.util.Date;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class CollectiveChangeOfNotificationTimesDialog extends TitleAreaDialog  {

	private int participantCount;

	private Date programmeNoteTime;
	private boolean isChangeProgrammeNoteTime = false;

	private Date hotelNoteTime;
	private boolean isChangeHotelNoteTime = false;



	private DateTimeComposite programmeNoteDateTimeComposite;
	private Button changeProgrammeNoteTimeButton;

	private DateTimeComposite hotelNoteDateTimeComposite;
	private Button changeHotelNoteTimeButton;



	public CollectiveChangeOfNotificationTimesDialog(Shell parentShell, int participantCount) {
		super(parentShell);

		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.participantCount = participantCount;
	}


	@Override
    public void create() {
		super.create();

		// set title and message after the dialog has been opened
		String title = I18N.CollectiveChangeNotificationTimesDialog_Title;
		title = title.replace("<count>", String.valueOf(participantCount));

		setTitle(title);
		setMessage(I18N.CollectiveChangeNotificationTimesDialog_Message);
    }


	 /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

    	updateStates();
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
    		composite.setLayout(new GridLayout(3, false));


    		// ============ Program ===============
    		Label label = new Label(composite, SWT.NONE);
    		label.setText( Participant.PROGRAMME_NOTE_TIME.getAbbreviation() );
    		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

    		changeProgrammeNoteTimeButton = new Button(composite, SWT.CHECK);
    		changeProgrammeNoteTimeButton.setText(UtilI18N.Change);
    		changeProgrammeNoteTimeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    		changeProgrammeNoteTimeButton.addSelectionListener(new SelectionAdapter() {
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				updateStates();
    			}
    		});

    		programmeNoteDateTimeComposite = new DateTimeComposite(composite, SWT.BORDER);
    		programmeNoteDateTimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    		programmeNoteDateTimeComposite.addModifyListener(new ModifyListener() {
    			@Override
				public void modifyText(ModifyEvent e) {
    				updateStates();
    				performCollectiveChange();
    			}
    		});


    		// ============ Hotel ===============
    		label = new Label(composite, SWT.NONE);
    		label.setText( Participant.HOTEL_NOTE_TIME.getAbbreviation() );
    		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

    		changeHotelNoteTimeButton = new Button(composite, SWT.CHECK);
    		changeHotelNoteTimeButton.setText(UtilI18N.Change);
    		changeHotelNoteTimeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    		changeHotelNoteTimeButton.addSelectionListener(new SelectionAdapter() {
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				updateStates();
    			}
    		});

    		hotelNoteDateTimeComposite = new DateTimeComposite(composite, SWT.BORDER);
    		hotelNoteDateTimeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    		hotelNoteDateTimeComposite.addModifyListener(new ModifyListener() {
    			@Override
				public void modifyText(ModifyEvent e) {
    				updateStates();
    				performCollectiveChange();
    			}
    		});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

    	return area;
    }


    private void updateStates() {
		isChangeProgrammeNoteTime = changeProgrammeNoteTimeButton.getSelection();
		programmeNoteDateTimeComposite.setEnabled(isChangeProgrammeNoteTime);

		isChangeHotelNoteTime = changeHotelNoteTimeButton.getSelection();
		hotelNoteDateTimeComposite.setEnabled(isChangeHotelNoteTime);

		boolean enabled =  isChangeHotelNoteTime || isChangeProgrammeNoteTime;

    	getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}


    public void performCollectiveChange() {
    	programmeNoteTime = programmeNoteDateTimeComposite.getDate();
		hotelNoteTime = hotelNoteDateTimeComposite.getDate();
	}


    public Date getProgrammeNoteTime() {
    	return programmeNoteTime;
    }


    public Date getHotelNoteTime() {
    	return hotelNoteTime;
    }


    public boolean isChangeHotelNoteTime() {
    	return isChangeHotelNoteTime;
    }


    public boolean isChangeProgrammeNoteTime() {
    	return isChangeProgrammeNoteTime;
    }

}
