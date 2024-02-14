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
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class CollectiveChangeRegistrationDateDialog extends TitleAreaDialog  {

	private int participantCount;

	private Button deleteButton;
	private DateComposite dateComposite;

	private Date registerDate = null;


	public CollectiveChangeRegistrationDateDialog(Shell parentShell, int participantCount) {
		super(parentShell);

		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.participantCount = participantCount;
	}


	@Override
    public void create() {
		super.create();

		// set title and message after the dialog has been opened
		String title = I18N.CollectiveChangeRegistrationDateDialog_Title;
		title = title.replace("<count>", String.valueOf(participantCount));

		setTitle(title);
		setMessage(I18N.CollectiveChangeRegistrationDateDialog_Message);
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
    		composite.setLayout(new GridLayout(3, false));


    		Label label = new Label(composite, SWT.NONE);
    		label.setText( Participant.REGISTER_DATE.getString() );
    		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

    		dateComposite = new DateComposite(composite, SWT.BORDER);
    		dateComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
    		WidgetSizer.setWidth(dateComposite);
    		dateComposite.addModifyListener(new ModifyListener() {
    			@Override
				public void modifyText(ModifyEvent e) {
    				resetButtonState();
    				updateValues();
    			}
    		});

    		deleteButton = new Button(composite, SWT.CHECK);
    		deleteButton.setText(UtilI18N.Clear);
    		deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

    		deleteButton.addSelectionListener(new SelectionAdapter() {
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				dateComposite.setEnabled(! deleteButton.getSelection());
    				resetButtonState();
    				updateValues();
    			}
    		});
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

    	return area;
    }


    private void updateValues() {
		if (!deleteButton.getSelection()) {
			registerDate = dateComposite.getDate();
		}
	}


    private void resetButtonState() {
    	boolean pageComplete = dateComposite.getLocalDate() != null || deleteButton.getSelection();
    	getButton(IDialogConstants.OK_ID).setEnabled(pageComplete);
    }


    public Date getRegistartionDate() {
    	return registerDate;
    }

}
