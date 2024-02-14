package de.regasus.participant.view;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.messeinfo.profile.PersonLinkData;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.sql.ProfileSearch;
import com.lambdalogic.messeinfo.report.WhereClauseReportParameter;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.core.ui.search.SearchInterceptor;
import de.regasus.core.ui.view.AbstractView;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.event.combo.EventCombo;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantProvider;
import de.regasus.participant.command.EditParticipantCommandHandler;
import de.regasus.participant.search.ParticipantSearchComposite;
import de.regasus.participant.view.pref.ParticipantToProfileViewPreference;
import de.regasus.person.PersonLinkModel;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileProvider;
import de.regasus.profile.ProfileSearchModel;
import de.regasus.profile.command.EditProfileCommandHandler;
import de.regasus.profile.search.ProfileSearchTable;
import de.regasus.ui.Activator;


/**
 * View to create Profiles based on Participants including duplicate check.
 *
 * See:
 * https://lambdalogic.atlassian.net/browse/MIRCP-2656
 * "Duplicate check when creating Profiles from Participants"
 */
public class ParticipantToProfileView extends AbstractView implements EventIdProvider {
	public static final String ID = "ParticipantToProfileView";

	private ParticipantToProfileViewPreference preference;

	// Models
	private ParticipantModel participantModel = ParticipantModel.getInstance();
	private ProfileModel profileModel = ProfileModel.getInstance();
	private ProfileSearchModel profileSearchModel = ProfileSearchModel.getDetachedInstance();
	private PersonLinkModel personLinkModel = PersonLinkModel.getInstance();


	// Widgets
	private EventCombo eventCombo;

	private ParticipantSearchComposite participantSearchComposite;

	private Button checkLastNameButton;
	private Button checkFirstNameButton;
	private Button checkEmailButton;
	private Button checkCityButton;


	/**
	 * Button to create a Profile based on Participant data.
	 */
	private Button createButton;

	/**
	 * Button to link a Participant with a Profile.
	 */
	private Button linkButton;

	/**
	 * Table that shows matching Profiles to the selected Participant
	 */
	private ProfileSearchTable profileSearchTable;


	/**
	 * Current List of Profiles.
	 */
	private List<Profile> profileList = new ArrayList<>();

	private ArrayList<ParticipantSearchData> selectedParticipants = new ArrayList<>();
	private ArrayList<Profile> selectedProfiles = new ArrayList<>();


	/**
	 * The last value of visible as get from the ConfigParameterSet in isVisible().
	 * Has to be stored because the result of isVisible() should not change in the case that the
	 * getConfigParameterSet() returns null.
	 */
	private boolean visible = false;


	public ParticipantToProfileView() {
		preference = ParticipantToProfileViewPreference.getInstance();
	}


	private CacheModelListener<Long> profileModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			// remove Participants from ParticipantSearchComposite who are deleted or linked with a Profile
			try {
				List<Long> removeParticipantPKs = new ArrayList<>();

				if (event.getOperation() == CacheModelOperation.CREATE) {
					List<Profile> profileList = profileModel.getProfiles( event.getKeyList() );
					for (Profile profile : profileList) {
						Long personLink = profile.getPersonLink();
						if (personLink != null) {
							PersonLinkData personLinkData = personLinkModel.getPersonLinkData( personLink );
							if (personLinkData.getProfileID() != null) {
								removeParticipantPKs.addAll(personLinkData.getParticipantIDs());
							}
						}
					}
				}

				if ( ! removeParticipantPKs.isEmpty()) {
					participantSearchComposite.remove(removeParticipantPKs);
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	};


	private CacheModelListener<Long> participantModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			// remove Participants from ParticipantSearchComposite who are deleted or linked with a Profile
			try {
				List<Long> removeParticipantPKs = new ArrayList<>();

				if (event.getOperation() == CacheModelOperation.DELETE) {
					removeParticipantPKs.addAll(  event.getKeyList() );
				}
				else if (event.getOperation() == CacheModelOperation.REFRESH
					  || event.getOperation() == CacheModelOperation.UPDATE
				) {
					List<Participant> participantList = participantModel.getParticipants( event.getKeyList() );
					for (Participant participant : participantList) {
						Long personLink = participant.getPersonLink();
						if (personLink != null) {
							PersonLinkData personLinkData = personLinkModel.getPersonLinkData( personLink );
							if (personLinkData.getProfileID() != null) {
								removeParticipantPKs.add(participant.getID());
							}
						}
					}
				}

				if ( ! removeParticipantPKs.isEmpty()) {
					participantSearchComposite.remove(removeParticipantPKs);
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	};


	private ISelectionChangedListener participantSelectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			// determine selected Participants
			List<ParticipantSearchData> psdList = SelectionHelper.getSelection(
				participantSearchComposite.getTableViewer(),
				ParticipantSearchData.class
			);

			// copy PKs to selectedParticipantPKs
			selectedParticipants.clear();
			if (psdList != null) {
				selectedParticipants.addAll(psdList);
			}


			// sync Profile table
			syncProfileTable();
		}
	};


	private SelectionListener checkButtonSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// sync Profile table
			syncProfileTable();
		}
	};


	private ISelectionChangedListener profileSelectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			try {
				List<Profile> profileList = SelectionHelper.getSelection(
					profileSearchTable.getViewer(),
					Profile.class
				);

				selectedProfiles.clear();
				if (profileList != null) {
					selectedProfiles.addAll(profileList);
				}

				updateButtonState();
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	};


	private IPartListener2 partListener = new IPartListener2() {
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			syncPart();
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}
	};


	@Override
	public void createWidgets(Composite parent) throws Exception {
		if (isVisible()) {

			// define the View's Layout
			parent.setLayout(new FillLayout());

			SashForm mainSash = new SashForm(parent, SWT.VERTICAL);


			// *********************************************************************************************************
			// * Participant Search
			// *

			Group participantSearchGroup = new Group(mainSash, SWT.NONE);
			participantSearchGroup.setLayout(new GridLayout(2, false));
			participantSearchGroup.setText(I18N.ParticipantSearch);

			// Event selection Combo
			Label eventLabel = new Label(participantSearchGroup, SWT.NONE);
			eventLabel.setText( Participant.EVENT.getString() );

			eventCombo = new EventCombo(participantSearchGroup, SWT.READ_ONLY);
			eventCombo.setKeepEntityInList(false);
			eventCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			eventCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					Long eventPK = eventCombo.getEventPK();
					participantSearchComposite.setEventPK(eventPK);
				}
			});


			// ParticipantSearchComposite
			participantSearchComposite = new ParticipantSearchComposite(
				participantSearchGroup,
				SelectionMode.MULTI_SELECTION,
				SWT.NONE,
				true,	// useDetachedSearchModelInstance
				null	// eventPK
			);
			participantSearchComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			// make the Table the SelectionProvider
			getSite().setSelectionProvider(participantSearchComposite.getTableViewer());
			// make that Ctl+C copies table contents to clipboard
			participantSearchComposite.registerCopyAction(getViewSite().getActionBars());

			// limit Participants to those that are are not linked with a Profile
			participantSearchComposite.setSearchInterceptor(new SearchInterceptor() {
				@Override
				public void changeSearchParameter(List<SQLParameter> sqlParameters) {
					try {
						sqlParameters.add(
							ParticipantSearch.HAS_PROFILE.getSQLParameter(Boolean.FALSE, SQLOperator.EQUAL)
						);
					}
					catch (InvalidValuesException e) {
						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					}
				}
			});

			participantSearchComposite.getTableViewer().addPostSelectionChangedListener(participantSelectionListener);

			// *
			// * Participant Search
			// *********************************************************************************************************

			Composite lowerSashComposite = new Composite(mainSash, SWT.NONE);
			lowerSashComposite.setLayout(new GridLayout(2, false));

			// **************************************************************************
			// * Action Buttons
			// *

			// Buttons to create and link
			Composite buttonComposite = new Composite(lowerSashComposite, SWT.NONE);
			buttonComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			buttonComposite.setLayout(new FillLayout());


			createButton = new Button(buttonComposite, SWT.PUSH);
			createButton.setText(I18N.ParticipantToProfileView_CreateProfileFromParticipant);
			createButton.setToolTipText(I18N.ParticipantToProfileView_CreateProfileFromParticipant_tooltip);
			createButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createProfiles();
				}
			});


			linkButton = new Button(buttonComposite, SWT.PUSH);
			linkButton.setText(I18N.ParticipantToProfileView_LinkParticipantWithProfile);
			linkButton.setToolTipText(I18N.ParticipantToProfileView_LinkParticipantWithProfile_tooltip);
			linkButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					linkParticipantWithProfile();
				}
			});

			updateButtonState();

			// *
			// * Action Buttons
			// **************************************************************************

			Group dupSearchGroup = new Group(lowerSashComposite, SWT.NONE);
			dupSearchGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			dupSearchGroup.setLayout(new GridLayout(2, true));
			dupSearchGroup.setText(I18N.ParticipantToProfileView_DuplicateSearch);

			// **************************************************************************
			// * Duplicate Search Buttons
			// *

			Composite dupButtonComposite = new Composite(dupSearchGroup, SWT.NONE);
			dupButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			dupButtonComposite.setLayout(new RowLayout());

			checkLastNameButton = new Button(dupButtonComposite, SWT.CHECK);
			checkLastNameButton.setText(I18N.ParticipantToProfileView_CheckLastName);
			checkLastNameButton.addSelectionListener(checkButtonSelectionListener);

			checkFirstNameButton = new Button(dupButtonComposite, SWT.CHECK);
			checkFirstNameButton.setText(I18N.ParticipantToProfileView_CheckFirstName);
			checkFirstNameButton.addSelectionListener(checkButtonSelectionListener);

			checkEmailButton = new Button(dupButtonComposite, SWT.CHECK);
			checkEmailButton.setText(I18N.ParticipantToProfileView_CheckEmail);
			checkEmailButton.addSelectionListener(checkButtonSelectionListener);

			checkCityButton = new Button(dupButtonComposite, SWT.CHECK);
			checkCityButton.setText(I18N.ParticipantToProfileView_CheckCity);
			checkCityButton.addSelectionListener(checkButtonSelectionListener);

			// *
			// * Duplicate Search Buttons
			// **************************************************************************

			// **************************************************************************
			// * Profile Table
			// *

			Label profilesLabel = new Label(dupSearchGroup, SWT.NONE);
			profilesLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			profilesLabel.setText(I18N.ParticipantToProfileView_MatchingProfiles);

			Composite profileTableGroup = new Composite(dupSearchGroup, SWT.BORDER);
			profileTableGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			TableColumnLayout layout = new TableColumnLayout();
			profileTableGroup.setLayout(layout);

			Table table = new Table(profileTableGroup, SelectionMode.SINGLE_SELECTION.getSwtStyle());
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			TableColumn firstNameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(firstNameTableColumn, new ColumnWeightData(140));
			firstNameTableColumn.setText(Person.FIRST_NAME.getString());

			TableColumn lastNameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(lastNameTableColumn, new ColumnWeightData(140));
			lastNameTableColumn.setText(Person.LAST_NAME.getString());

			TableColumn organisationTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(organisationTableColumn, new ColumnWeightData(100));
			organisationTableColumn.setText( Address.ORGANISATION.getString() );

			TableColumn cityTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(cityTableColumn, new ColumnWeightData(100));
			cityTableColumn.setText( Address.CITY.getString() );

			profileSearchTable = new ProfileSearchTable(table);

			profileSearchTable.getViewer().addPostSelectionChangedListener(profileSelectionListener);

			// *
			// * Profile Table
			// **************************************************************************


			// other
			setContributionItemsVisible(true);

			initializeContextMenu();
			initializeDoubleClickAction();

			getSite().getPage().addPartListener(partListener);

			initFromPreferences();

			// observer ServerModel to init from preferences on login and save t preferences on logout
			ServerModel.getInstance().addListener(serverModelListener);

			profileModel.addListener(profileModelListener);
			participantModel.addListener(participantModelListener);
		}
		else {
			Label label = new Label(parent, SWT.NONE);
			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
		}
	}


	@Override
	public void dispose() {
		try {
			getSite().getPage().removePartListener(partListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			ServerModel.getInstance().removeListener(serverModelListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}


	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * This View is only visible if Events and Profiles are visible.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible =
				   getConfigParameterSet().getEvent().isVisible()
				&& getConfigParameterSet().getProfile().isVisible();
		}
		return visible;
	}


	public Long getSelectedEventPK() {
		Long eventPK = null;

		if (eventCombo != null) {
			eventPK = eventCombo.getEventPK();
		}

		return eventPK;
	}


	@Override
	public Long getEventId() {
		Long eventPK = getSelectedEventPK();
		return eventPK;
	}


	private void initializeContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");

		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuMgr.add(new Separator("emailAdditions"));

		TableViewer tableViewer = participantSearchComposite.getTableViewer();
		Table table = tableViewer.getTable();
		Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void initializeDoubleClickAction() {
		// open Participant Editor on double click on Participant
		participantSearchComposite.getTableViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				List<ParticipantSearchData> selectedParticipants = SelectionHelper.getSelection(
					participantSearchComposite.getTableViewer(),
					ParticipantSearchData.class
				);

				for (ParticipantSearchData participantSearchData : selectedParticipants) {
					Long participantID = participantSearchData.getPK();
					IWorkbenchWindow window = getSite().getWorkbenchWindow();
					EditParticipantCommandHandler.openParticipantEditor(window.getActivePage(), participantID);
				}
			}
		});

		// open Profile Editor on double click on Profile
		profileSearchTable.getViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				List<Profile> selectedProfiles = SelectionHelper.getSelection(
					profileSearchTable.getViewer(),
					Profile.class
				);

				for (Profile profile : selectedProfiles) {
					Long profileID = profile.getPK();
					IWorkbenchWindow window = getSite().getWorkbenchWindow();
					EditProfileCommandHandler.openProfileEditor(window.getActivePage(), profileID);
				}
			}
		});
	}


	/* Set focus to searchComposite and therewith to the search button.
	 * Otherwise the focus would be on eventCombo which is not wanted, because the user could change its value by
	 * accident easily.
	 */
	@Override
	public void setFocus() {
		try {
			if (participantSearchComposite != null &&
				!participantSearchComposite.isDisposed() &&
				participantSearchComposite.isEnabled()
			) {
				participantSearchComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	public void doSearch() {
		participantSearchComposite.doSearch();

	}


	public void refreshSelection() {
		ISelection selection = participantSearchComposite.getTableViewer().getSelection();
		participantSearchComposite.getTableViewer().setSelection(selection);
	}


	private void updateButtonState() {
		createButton.setEnabled( selectedParticipants.size() > 0 );
		linkButton.setEnabled( selectedParticipants.size() == 1 && selectedProfiles.size() == 1 );
	}


	private void syncPart() {
		try {
			IWorkbenchPart activePart = getSite().getPage().getActivePart();

			if (activePart != null) {

				if (activePart instanceof ParticipantProvider) {
					// determine the new rootPK and set it
					ParticipantProvider participantProvider = (ParticipantProvider) activePart;

					IParticipant newParticipant = participantProvider.getIParticipant();
					Long participantPK = newParticipant.getPK();
					// select Participant
					participantSearchComposite.setSelection(participantPK);
				}
				else if (activePart instanceof ProfileProvider) {
					// determine the new rootPK and set it
					ProfileProvider profileProvider = (ProfileProvider) activePart;
					Long profilePK = profileProvider.getProfilePK();

					// check if Profile table contains Profile
					Profile selectedProfile = null;
					for (Profile profile : profileList) {
						if (profile.getID().equals(profilePK)) {
							selectedProfile = profile;
							break;
						}
					}

					// select Profile
					if (selectedProfile != null) {
						ISelection selection = new StructuredSelection(selectedProfile);
						profileSearchTable.getViewer().setSelection(selection, true /*reveal*/);
					}

				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void syncProfileTable() {
		try {
			// build SQLParameters
			List<SQLParameter> sqlParameterList = new ArrayList<>();
			if (selectedParticipants != null && selectedParticipants.size() == 1) {
				ParticipantSearchData participantSearchData = selectedParticipants.get(0);

				// search Profiles that match with selected Participant

				if (checkLastNameButton.getSelection()) {
					String lastName = participantSearchData.getLastName();
					sqlParameterList.add(
						ProfileSearch.LAST_NAME.getSQLParameter(lastName, SQLOperator.FUZZY_LOWER_ASCII)
					);
				}

				if (checkFirstNameButton.getSelection()) {
					String firstName = participantSearchData.getFirstName();
					if (firstName != null && firstName.length() > 0) {
						sqlParameterList.add(
							ProfileSearch.FIRST_NAME.getSQLParameter(firstName, SQLOperator.FUZZY_LOWER_ASCII)
						);
					}
				}

				if (checkEmailButton.getSelection()) {
					String email = participantSearchData.getEmail();
					if (email != null && email.length() > 0) {
						sqlParameterList.add(
							ProfileSearch.EMAIL1.getSQLParameter(email, SQLOperator.FUZZY_IGNORE_CASE)
						);
					}
				}

				if (checkCityButton.getSelection()) {
					String city = participantSearchData.getCity();
					if (city != null && city.length() > 0) {
						sqlParameterList.add(
							ProfileSearch.CITY.getSQLParameter(city, SQLOperator.FUZZY_IGNORE_CASE)
						);
					}
				}
			}


			profileList.clear();
			if (!sqlParameterList.isEmpty()) {
				profileSearchModel.setSqlParameterList(sqlParameterList);
				profileList.addAll( profileSearchModel.getModelData() );
			}

			profileSearchTable.getViewer().setInput(profileList);

			updateButtonState();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Create Profiles for the selected Participants if no matching Profile exists according to the settings in the
	 * duplicate search area.
	 */
	private void createProfiles() {
		// show confirmation dialog
		if ( ! MessageDialog.openConfirm(
			getSite().getShell(),
			I18N.ParticipantToProfileView_CreateProfileFromParticipant,
			I18N.ParticipantToProfileView_CreateProfileFromParticipant_ConfirmationMsg
		)) {
			return;
		}


		// determine selected Participants
		final List<ParticipantSearchData> psdList =  new ArrayList<>(selectedParticipants);

		// values that have to be available in the Job (direct access not possible because of different thread)
		final boolean checkLastName = checkLastNameButton.getSelection();
		final boolean checkFirstName = checkFirstNameButton.getSelection();
		final boolean checkEmail = checkEmailButton.getSelection();
		final boolean checkCity = checkCityButton.getSelection();

		Job job = new Job(I18N.ParticipantToProfileView_CreateProfileFromParticipant) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// number of Participants for which a Profile has been created
				int processedCount = 0;

				try {
					monitor.beginTask(I18N.ParticipantToProfileView_CreateProfileFromParticipant, psdList.size());

					// List of SQLParameters
					List<SQLParameter> sqlParameterList = new ArrayList<>();
					List<Long> participantPKList = new ArrayList<>(1);

					// for each Participant check if Profiles exist that match with fields that are selected for duplicate search
					for (ParticipantSearchData participantSearchData : psdList) {
						sqlParameterList.clear();


						// add SQLParameter for each selected field

						if (checkLastName) {
							String lastName = participantSearchData.getLastName();
							sqlParameterList.add(
								ProfileSearch.LAST_NAME.getSQLParameter(lastName, SQLOperator.FUZZY_LOWER_ASCII)
							);
						}

						if (checkFirstName) {
							String firstName = participantSearchData.getFirstName();
							if (firstName != null && firstName.length() > 0) {
								sqlParameterList.add(
									ProfileSearch.FIRST_NAME.getSQLParameter(firstName, SQLOperator.FUZZY_LOWER_ASCII)
								);
							}
						}

						if (checkEmail) {
							String email = participantSearchData.getEmail();
							if (email != null && email.length() > 0) {
								sqlParameterList.add(
									ProfileSearch.EMAIL1.getSQLParameter(email, SQLOperator.FUZZY_IGNORE_CASE)
								);
							}
						}

						if (checkCity) {
							String city = participantSearchData.getCity();
							if (city != null && city.length() > 0) {
								sqlParameterList.add(
									ProfileSearch.CITY.getSQLParameter(city, SQLOperator.FUZZY_IGNORE_CASE)
								);
							}
						}


						List<Profile> matchingProfiles = null;
						if (!sqlParameterList.isEmpty()) {
							profileSearchModel.setSqlParameterList(sqlParameterList);
							matchingProfiles = profileSearchModel.getModelData();
						}

						if (matchingProfiles == null || matchingProfiles.isEmpty()) {
							// create Profile based on current Participant data
							participantPKList.clear();
							participantPKList.add(participantSearchData.getPK());
							profileModel.createByParticipants(participantPKList);

							processedCount++;
						}


						monitor.worked(1);

						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}

					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
				finally {
					final int processedCountFinal = processedCount;
					final int unprocessedCountFinal = psdList.size() - processedCount;

					SWTHelper.asyncExecDisplayThread(
						new Runnable() {
							@Override
							public void run() {
								// show summary message

								String msg = null;
								if (processedCountFinal == 1 && unprocessedCountFinal == 0) {
									msg = I18N.ParticipantToProfileView_CreateProfileFromParticipant;
								}
								else {
									msg = I18N.ParticipantToProfileView_CreateProfileFromParticipantSummaryMsg;
									msg = msg.replaceFirst("<processedCount>", String.valueOf(processedCountFinal));
									msg = msg.replaceFirst("<unprocessedCount>", String.valueOf(unprocessedCountFinal));

									if (unprocessedCountFinal == 0) {
										msg = msg.split("\n")[0];
									}
								}

								MessageDialog.openInformation(
									getSite().getShell(),
									I18N.ParticipantToProfileView_CreateProfileFromParticipant,
									msg
								);
							}
						}
					);

				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}


	private void linkParticipantWithProfile() {
		if (selectedParticipants.size() == 1 && selectedProfiles.size() == 1) {
			try {
				// determine selected Participant and Profile
				ParticipantSearchData participant = selectedParticipants.get(0);
				Profile profile = selectedProfiles.get(0);

				// build confirmation message
				String message = I18N.ParticipantToProfileView_LinkParticipantWithProfile_ConfirmationMsg;
				message = message.replaceFirst("<participantName>", participant.getName());
				message = message.replaceFirst("<profileName>", profile.getName());

				// show confirmation dialog
				if ( ! MessageDialog.openConfirm(
					getSite().getShell(),
					I18N.ParticipantToProfileView_LinkParticipantWithProfile,
					message
				)) {
					return;
				}

				//                   profileID,       participantID,       forceNewLink
				personLinkModel.link(profile.getID(), participant.getPK(), false);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	// *****************************************************************************************************************
	// * Preferences
	// *

	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			ServerModelEvent serverModelEvent = (ServerModelEvent) event;
			if (serverModelEvent.getType() == ServerModelEventType.BEFORE_LOGOUT) {
				// save values to preferences before the logout will remove them
				savePreferences();
			}
			else if (serverModelEvent.getType() == ServerModelEventType.LOGIN) {
				SWTHelper.asyncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						initFromPreferences();
					}
				});
			}
		}
	};


	private void savePreferences() {
		preference.setEventId( eventCombo.getEventPK() );
		preference.setEventFilter( eventCombo.getFilter() );

		// save where clause
		List<SQLParameter> sqlParameterList = participantSearchComposite.getSQLParameterListForPreferences();
		if ( notEmpty(sqlParameterList) ) {
			XMLContainer xmlContainer = new XMLContainer("<searchViewMemento/>");

			WhereClauseReportParameter parameter = new WhereClauseReportParameter(xmlContainer);
			parameter.setSQLParameters(sqlParameterList);

			String xmlSource = xmlContainer.getRawSource();
			preference.setSearchFields(xmlSource);
		}

		preference.setColumnOrder( participantSearchComposite.getColumnOrder() );
		preference.setColumnWidths( participantSearchComposite.getColumnWidths() );
		preference.setResultCountCheckboxSelected( participantSearchComposite.isResultCountSelected() );
		preference.setResultCount( participantSearchComposite.getResultCount() );

		// check Buttons
		preference.setCheckLastName( checkLastNameButton.getSelection() );
		preference.setCheckFirstName( checkFirstNameButton.getSelection() );
		preference.setCheckEmail( checkEmailButton.getSelection() );
		preference.setCheckCity( checkCityButton.getSelection() );

		preference.save();
	}


	private void initFromPreferences() {
		try {
    		// eventPK
    		Long eventPK = preference.getEventId();
    		// try to get the event data to assure that the event does exist
    		EventVO eventVO = null;
    		try {
    			eventVO = EventModel.getInstance().getEventVO(eventPK);
    		}
    		catch (Exception e) {
    			// don't show error dialog, just log the error
    			System.err.println(e);
    		}

    		if (eventVO != null) {
    			eventCombo.setEventPK(eventPK);
    		}

    		// set eventPK in SearchComposite, even if it is null
    		participantSearchComposite.setEventPK(eventPK);


    		// eventFilter
    		eventCombo.setFilter( preference.getEventFilter() );


    		// search fields
    		String whereClauseXMLSource = preference.getSearchFields();
    		if ( isNotEmpty(whereClauseXMLSource) ) {
    			XMLContainer xmlContainer = new XMLContainer(whereClauseXMLSource);
    			WhereClauseReportParameter parameters = new WhereClauseReportParameter(xmlContainer);
    			List<WhereField> whereFields = parameters.getWhereFields();
    			participantSearchComposite.setWhereFields(whereFields);
    		}


    		// InvalidThreadAccess can happen, because this method runs
    		// in non-UI-Thread via BusyCursorHelper.busyCursorWhile(Runnable)
    		// after "System > Alles aktualisieren"
    		Runnable tableUpdater = new Runnable() {
    			@Override
    			public void run() {
    				int[] columnOrder = preference.getColumnOrder();
    				if (columnOrder != null) {
    					participantSearchComposite.setColumnOrder(columnOrder);
    				}

    				int[] columnWidths = preference.getColumnWidths();
    				if (columnWidths != null) {
    					participantSearchComposite.setColumnWidths(columnWidths);
    				}
    			}
    		};
    		SWTHelper.asyncExecDisplayThread(tableUpdater);

    		participantSearchComposite.setResultCountSelection( preference.isResultCountCheckboxSelected() );
    		participantSearchComposite.setResultCount( preference.getResultCount() );


			// check Buttons
			checkLastNameButton.setSelection( preference.isCheckLastName() );
			checkFirstNameButton.setSelection( preference.isCheckFirstName() );
			checkEmailButton.setSelection( preference.isCheckEmail() );
			checkCityButton.setSelection( preference.isCheckCity() );
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Preferences
	// *****************************************************************************************************************

}
