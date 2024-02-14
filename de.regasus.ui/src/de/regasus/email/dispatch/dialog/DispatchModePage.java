package de.regasus.email.dispatch.dialog;

import java.util.Date;
import java.util.Objects;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;

import de.regasus.email.EmailI18N;
import de.regasus.email.dispatch.DispatchMode;
import de.regasus.email.dispatch.pref.EmailDispatchPreference;

/**
 * A wizard page to determine whether the sending of an email should take place
 * <ul>
 * <li>from the client's VM immediately</li>
 * <li>from the server's VM immediately</li>
 * <li>from the server's VM, but later at a point in time that is to be specified, too.</li>
 * </ul>
 */
public class DispatchModePage extends WizardPage implements SelectionListener {

	private static final String NAME = "DispatchModePage";

	// *************************************************************************
	// * Widgets
	// *

	private Button produceClientDispatchClientButton;

	private Button produceServerDispatchServerButton;

	private Button produceServerDispatchServerScheduledButton;

	private DateTimeComposite scheduledDateTimeComposite;

	// *************************************************************************
	// * Other Attributes
	// *

	/**
	 * An enum that tells the selected dispatch variant
	 */
	private DispatchMode dispatchMode;

	/**
	 * The date of now, so that we control that the user doesn't select a date in the past.
	 */
	private Date now;

	/**
	 * The date at which the sending of the email shall take place.
	 */
	private Date scheduledDate;

	private SmtpSettingsVO smtpSettings;


	// *************************************************************************
	// * Constructor
	// *

	protected DispatchModePage(SmtpSettingsVO smtpSettings) {
		super(NAME);
		setTitle(EmailLabel.DispatchSettings.getString());

		this.smtpSettings = Objects.requireNonNull(smtpSettings);
	}


	@Override
	public void createControl(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(EmailLabel.DispatchSettings.getString());
		group.setLayout(new GridLayout(3, false));


		// 1st Button
		produceClientDispatchClientButton = new Button(group, SWT.RADIO);
		produceClientDispatchClientButton.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));
		produceClientDispatchClientButton.addSelectionListener(this);

		Label produceClientDispatchLabel = new Label(group, SWT.WRAP);
		produceClientDispatchLabel.setText(EmailI18N.EmailSettings_ProduceAndSendOnClient);
		produceClientDispatchLabel.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, true, false, 2, 1));


		// 2nd Button
		produceServerDispatchServerButton = new Button(group, SWT.RADIO);
		produceServerDispatchServerButton.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));
		produceServerDispatchServerButton.addSelectionListener(this);

		// Give information on chunking
		int chunkSize = smtpSettings.getChunkSize();
		String message = EmailI18N.EmailSettings_ProduceAndSendOnServer.replace("<chunksize>", String.valueOf(chunkSize));

		Label produceServerDispatchServerLabel = new Label(group, SWT.WRAP);
		produceServerDispatchServerLabel.setText(message);
		produceServerDispatchServerLabel.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, true, false, 2, 1));


		// 3rd Button
		produceServerDispatchServerScheduledButton = new Button(group, SWT.RADIO);
		produceServerDispatchServerScheduledButton.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));
		produceServerDispatchServerScheduledButton.addSelectionListener(this);

		Label produceServerDispatchServerLabelScheduled = new Label(group, SWT.WRAP);
		produceServerDispatchServerLabelScheduled.setText(EmailI18N.EmailSettings_ProduceAndSendOnServerScheduled);
		produceServerDispatchServerLabelScheduled.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, true, false, 2, 1));

		// Entry for Date
		new Label(group, SWT.NONE); // Dummy
		Label scheduledDateLabel = new Label(group, SWT.NONE);
		scheduledDateLabel.setText(EmailI18N.EmailSettings_ScheduledDate);
		scheduledDateTimeComposite = new DateTimeComposite(group, SWT.NONE);

		// Determine the option to be preselected from the preferenecs (holding the previously used option)
		dispatchMode = EmailDispatchPreference.getInstance().getDispatchMode();

		produceClientDispatchClientButton.setSelection(dispatchMode == DispatchMode.IMMEDIATE_CLIENT);
		produceServerDispatchServerButton.setSelection(dispatchMode == DispatchMode.IMMEDIATE_SERVER);
		produceServerDispatchServerScheduledButton.setSelection(dispatchMode == DispatchMode.SCHEDULED_SERVER);
		scheduledDateTimeComposite.setEnabled(dispatchMode == DispatchMode.SCHEDULED_SERVER);
		now = new Date();

		scheduledDate = new Date(now.getTime() + 5 * 60 * 1000); // in 5 minutes
		scheduledDateTimeComposite.setDate(scheduledDate);
		scheduledDateTimeComposite.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateState();
			}
		});

		setControl(group);
	}

	// *************************************************************************
	// * Getters
	// *

	public DispatchMode getDispatchMode() {
		return dispatchMode;
	}


	public Date getScheduledDate() {
		return scheduledDate;
	}


	// *************************************************************************
	// * Event handling
	// *

	/**
	 * Not called.
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/**
	 * Gets called when one of the buttons is clicked.
	 */
	@Override
	public void widgetSelected(SelectionEvent event) {
		if (!ModifySupport.isDeselectedRadioButton(event)) {
			updateState();
		}
	}


	/**
	 * Cares for the relationship between the buttons and the entry field for the date.
	 * The date is only enabled when the "scheduled" button is selected; and if that is
	 * the case <i>and</i> the entered date is before {@link #now}, there is an error
	 * shown at the top of the wizard pages.
	 */
	private void updateState() {
		if (produceClientDispatchClientButton.getSelection()) {
			dispatchMode = DispatchMode.IMMEDIATE_CLIENT;
		}
		else if (produceServerDispatchServerButton.getSelection()) {
			dispatchMode = DispatchMode.IMMEDIATE_SERVER;
		}
		else if (produceServerDispatchServerScheduledButton.getSelection()) {
			dispatchMode = DispatchMode.SCHEDULED_SERVER;
		}
		scheduledDateTimeComposite.setEnabled(dispatchMode == DispatchMode.SCHEDULED_SERVER);

		scheduledDate = scheduledDateTimeComposite.getDate();
		if (scheduledDate.before(now) && dispatchMode == DispatchMode.SCHEDULED_SERVER) {
			setErrorMessage(EmailI18N.Error_ScheduledDispatchInPast);
			setPageComplete(false);
		}
		else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}

}
