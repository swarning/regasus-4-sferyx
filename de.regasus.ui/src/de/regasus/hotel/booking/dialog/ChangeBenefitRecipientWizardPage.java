package de.regasus.hotel.booking.dialog;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.ui.search.SearchInterceptor;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.dialog.ParticipantLabelProvider;
import de.regasus.participant.dialog.ParticipantLabelSorter;
import de.regasus.participant.search.ParticipantSearchComposite;


public class ChangeBenefitRecipientWizardPage extends WizardPage implements ISelectionChangedListener {

	public static final String NAME = "ChangeBenefitRecipientWizard";

	// **************************************************************************
	// * Attributes
	// *

	/**
	 * The search composite which is inside this page
	 */
	private ParticipantSearchComposite participantSearchComposite;

	private SearchInterceptor searchInterceptor;

	private java.util.List<Participant> participantList;

	private ArrayList<SQLParameter> sqlParameterList;

	/**
	 * If not null, this page is only complete if the number of selected participants
	 * is contained in that collection
	 */
	private Collection<Integer> allowedSelectionCount;

	private Participant participant;

	private Button addButton;

	private Button removeButton;

	private ListViewer recipientListViewer;

	private boolean changed;

	// **************************************************************************
	// * Constructor
	// *
	public ChangeBenefitRecipientWizardPage(
		Participant participant,
		ArrayList<SQLParameter> sqlParameterList,
		java.util.List<Participant> participantList
	) {
		super(NAME);

		this.participantList = participantList;
		this.participant = participant;
		this.sqlParameterList = sqlParameterList;

		setTitle(I18N.ChangeBenefitRecipient);
	}


	// **************************************************************************
	// * Methods
	// *


	/**
	 * Puts the search composite inside this page
	 */
	@Override
	public void createControl(Composite parent) {

		Composite innerComposite = new Composite(parent, SWT.NONE);
		innerComposite.setLayout(new GridLayout(3, false));

		// Row 1: Labels and the Add-Remove-Buttons
		new Label(innerComposite, SWT.NONE).setText(UtilI18N.Search);

		Composite addRemoveButtonsComposite = new Composite(innerComposite, SWT.NONE);
		addRemoveButtonsComposite.setLayout(new GridLayout(1, false));
		addRemoveButtonsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 2));

		addButton = new Button(addRemoveButtonsComposite, SWT.PUSH);
		addButton.setBounds(0, 0, 75, 25);
		addButton.setText(">>");
		addButton.setToolTipText(UtilI18N.Add + UtilI18N.Ellipsis);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				add();
			}
		});

		removeButton = new Button(addRemoveButtonsComposite, SWT.PUSH);
		removeButton.setBounds(0, 0, 75, 25);
		removeButton.setText("<<");
		removeButton.setToolTipText(UtilI18N.Remove);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				remove();
			}
		});

		new Label(innerComposite, SWT.NONE).setText(UtilI18N.Selection);



		// Row 2, Col 1: ParticipantSearch
		participantSearchComposite = new ParticipantSearchComposite(
			innerComposite,
			SelectionMode.MULTI_SELECTION,
			SWT.NONE,
			true, 						// useDetachedSearchModelInstance
			participant.getEventId()	// eventPK
		);
		participantSearchComposite.setSearchInterceptor(searchInterceptor);
		participantSearchComposite.setInitialSQLParameters(sqlParameterList);

		participantSearchComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		participantSearchComposite.getTableViewer().addPostSelectionChangedListener(this);
		participantSearchComposite.doSearch();

		// Row 2, Col 3: ParticipantSearch: Selected Participants
		List recipientList = new List(innerComposite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
		layoutData.widthHint = 200;
		recipientList.setLayoutData(layoutData);
		recipientListViewer = new ListViewer(recipientList);
		recipientListViewer.setContentProvider(ArrayContentProvider.getInstance());
		recipientListViewer.setLabelProvider(new ParticipantLabelProvider());
		recipientListViewer.setInput(this.participantList);
		recipientListViewer.addPostSelectionChangedListener(this);
		recipientListViewer.setSorter(new ParticipantLabelSorter());

		setPageComplete(false);
		setControl(innerComposite);
	}


	/**
	 * Remove all selected participants from the list on the right-hand side
	 */
	protected void remove() {
		java.util.List<Participant> selection = SelectionHelper.getSelection(recipientListViewer, Participant.class);
		if (! selection.isEmpty()) {
			participantList.removeAll(selection);
			changed = true;
		}
		updateButtonStates();

	}

	/**
	 * Enable buttons depending on whether there are selections on the right- and/or left-hand side
	 *
	 */
	private void updateButtonStates() {
		recipientListViewer.refresh();

		boolean participantInListSelected = ! (recipientListViewer.getSelection().isEmpty());
		removeButton.setEnabled(participantInListSelected);

		boolean participantInSearchSelected = ! participantSearchComposite.getTableViewer().getSelection().isEmpty();
		addButton.setEnabled(participantInSearchSelected);

		computePageComplete();
	}



	/**
	 * From the Participants in the table, take the IDs of those who
	 * are not yet in the list, convert them to "real" participants,
	 * and add them to the list.
	 */
	protected void add() {
		// Collect IDs from Participants which already are on the right hand side
		ArrayList<Long> alreadySelectedParticipantIDs = new ArrayList<>();
		for (Participant p : participantList) {
			alreadySelectedParticipantIDs.add(p.getID());
		}

		java.util.List<Long> participantIDsToAdd = new ArrayList<>();
		TableViewer tableViewer = participantSearchComposite.getTableViewer();
		java.util.List<ParticipantSearchData> psdList = SelectionHelper.getSelection(tableViewer, ParticipantSearchData.class);

		// Find out IDs of participants not yet in list, and thus are to be added
		for (ParticipantSearchData participantSearchData : psdList) {
			Long id = participantSearchData.getPK();
			if (! alreadySelectedParticipantIDs.contains(id)) {
				participantIDsToAdd.add(id);
			}
		}

		// Convert IDs to real participants and put them into the list
		try {
			ParticipantModel participantModel = ParticipantModel.getInstance();
			java.util.List<Participant> participants = participantModel.getParticipants(participantIDsToAdd);
			for (Participant participant : participants) {
				participantList.add(participant);
				changed = true;
			}
			tableViewer.setSelection(new StructuredSelection());

		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
		updateButtonStates();

	}


	public java.util.List<Participant> getSelectedParticipants() {
		return participantList;
	}


	public Collection<Integer> getAllowedSelectionCount() {
		return allowedSelectionCount;
	}


	public void setAllowedSelectionCount(Collection<Integer> allowedSelectionCount) {
		this.allowedSelectionCount = allowedSelectionCount;
	}


	/**
	 * Indirectly enable the "Finish" button when the count of participants in the list on the
	 * right-hand side is equal to one of the allowed counts
	 */
	private void computePageComplete() {
		if (allowedSelectionCount == null) {
			setPageComplete(!participantList.isEmpty() && changed);

		} else {
			int size = participantList.size();
			boolean correctSelectionCount = allowedSelectionCount.contains(size);
			setPageComplete(correctSelectionCount && changed);
		}
	}


	public void setSearchInterceptor(SearchInterceptor searchInterceptor) {
		this.searchInterceptor = searchInterceptor;
		if (participantSearchComposite != null) {
			participantSearchComposite.setSearchInterceptor(searchInterceptor);
		}
	}


	@Override
	public void selectionChanged(SelectionChangedEvent arg0) {
		updateButtonStates();
	}


	public boolean canFinish() {
		return isPageComplete() && changed;
	}

}
