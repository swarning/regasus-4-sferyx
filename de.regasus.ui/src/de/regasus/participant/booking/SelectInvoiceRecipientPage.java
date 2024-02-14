package de.regasus.participant.booking;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.participant.search.ParticipantSearchComposite;

/**
 * A wizard page which shows three radio buttons to determine who should be the participant. If the third button is
 * selected, a participant search composite becomes visible, and you can only continue to the next page when there
 * is a participant actually selected.
 * <p>
 * See https://mi2.lambdalogic.de/jira/browse/MIRCP-104
 */
public class SelectInvoiceRecipientPage extends WizardPage {

	public static final String NAME = "SelectInvoiceRecipientPage";


	private Long eventPK;


	// widgets
	private Button eachParticipantThemSelfRadioButton;

	private Button groupManagerOrMainParticipantOrThemselfRadioButton;

	private Button otherPraticipantRadioButton;

	private TableViewer tableViewer;

	private ParticipantSearchComposite searchComposite;


	public SelectInvoiceRecipientPage(Long eventPK) {
		super(NAME);

		this.eventPK = eventPK;

		setTitle(I18N.SelectInvoiceRecipientPage_Title);
		setMessage(I18N.SelectInvoiceRecipientPage_Message);
	}


	/**
	 * Shows three radio buttons to determine who should be the participant
	 */
	@Override
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(new GridLayout(2, false));

		// 1. Radio Button: Group Manager or main Participant or themselves
		groupManagerOrMainParticipantOrThemselfRadioButton = new Button(controlComposite, SWT.RADIO);

		Label label1 = new Label(controlComposite, SWT.WRAP);
		label1.setText(I18N.CreateProgrammeBookings_GroupManagerOrMainParticipantOrThemselfLabel);
		label1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));


		// 2. Radio Button: each Participant themselves
		eachParticipantThemSelfRadioButton = new Button(controlComposite, SWT.RADIO);

		Label label2 = new Label(controlComposite, SWT.WRAP);
		label2.setText(I18N.CreateProgrammeBookings_EachParticipantThemSelfRadioButton);
		label2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));


		// 3. Radio Button: other Participant
		otherPraticipantRadioButton = new Button(controlComposite, SWT.RADIO);

		Label label3 = new Label(controlComposite, SWT.WRAP);
		label3.setText(I18N.CreateProgrammeBookings_OneInvoiceRecipientForAllSelectedParticipants);
		label3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));


		// select 1. Radio Button by default
		groupManagerOrMainParticipantOrThemselfRadioButton.setSelection(true);


		// handle selection of Radio Buttons
		SelectionListener selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!ModifySupport.isDeselectedRadioButton(event)) {
					updateInternalState();
				}
			}
		};

		/*
		 * Attention:
		 * Observe all radio buttons, not only the otherPraticipantRadioButton, because we cannot rely on that events
		 * for deselected radio buttons are actually fired!
		 */
		groupManagerOrMainParticipantOrThemselfRadioButton.addSelectionListener(selectionListener);
		eachParticipantThemSelfRadioButton.addSelectionListener(selectionListener);
		otherPraticipantRadioButton.addSelectionListener(selectionListener);


		// Participant Search
		searchComposite = new ParticipantSearchComposite(
			controlComposite,
			SelectionMode.SINGLE_SELECTION,
			SWT.NONE,
			true, // useDetachedSearchModelInstance
			eventPK
		);
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableViewer = searchComposite.getTableViewer();
		searchComposite.setVisible(false);


		tableViewer.getTable().addSelectionListener(selectionListener);

		setControl(controlComposite);
	}


	protected void updateInternalState() {
		searchComposite.setVisible(otherPraticipantRadioButton.getSelection());

		switch (getInvoiceRecipientSelectionPolicy()) {
			case EACH_PARTICIPANT_THEMSELF:
			case GROUPMANAGER_OR_MAIN_PARTICIPANT_OR_THEMSELF:
				setPageComplete(true);
				break;
			case OTHER_PRATICIPANT:
				setPageComplete(getParticipant() != null);
		}

	}


	public InvoiceRecipientSelectionPolicy getInvoiceRecipientSelectionPolicy() {
		if (otherPraticipantRadioButton != null && otherPraticipantRadioButton.getSelection()) {
			return InvoiceRecipientSelectionPolicy.OTHER_PRATICIPANT;
		}
		else if (eachParticipantThemSelfRadioButton != null && eachParticipantThemSelfRadioButton.getSelection()) {
			return InvoiceRecipientSelectionPolicy.EACH_PARTICIPANT_THEMSELF;
		}
		else {
			return InvoiceRecipientSelectionPolicy.GROUPMANAGER_OR_MAIN_PARTICIPANT_OR_THEMSELF;
		}
	}


	public ParticipantSearchData getParticipant() {
		if (tableViewer == null || tableViewer.getSelection().isEmpty()) {
			return null;
		}
		return SelectionHelper.getUniqueSelected(tableViewer.getSelection());
	}

}
