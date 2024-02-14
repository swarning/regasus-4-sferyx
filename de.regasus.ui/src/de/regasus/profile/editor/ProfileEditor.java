package de.regasus.profile.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.messeinfo.config.parameterset.AddressConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ProfileConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.exception.ProfileDuplicateException;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.messeinfo.profile.ProfileStatus;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.I18N;
import de.regasus.common.composite.AddressGroupsComposite;
import de.regasus.common.composite.ApprovalGroup;
import de.regasus.common.composite.BankGroup;
import de.regasus.common.composite.CommunicationGroup;
import de.regasus.common.composite.CreditCardGroup;
import de.regasus.common.composite.PersonGroup;
import de.regasus.common.composite.UserCredentialsGroup;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.PropertyModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.participant.editor.ISaveListener;
import de.regasus.person.PersonLinkModel;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.profile.ProfileFileModel;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileProvider;
import de.regasus.profile.ProfileRelationModel;
import de.regasus.profile.dialog.ProfileDuplicateDialog;
import de.regasus.profile.editor.document.ProfileFileComposite;
import de.regasus.ui.Activator;

/**
 * Editor for {@link Profile}.
 *
 * After opening, the ProfileEditor should display the tab that was displayed in the last active editor.
 * If, for example, you switch to the address tab in the ProfileEditor, this should be displayed as the first tab
 * the next time you open the editor. The implementation uses the Enum tab, in which an enum value is defined for
 * each tab. The variable initialTab saves the last tab displayed. In createPartControl it is ensured that the tab
 * specified in initialTab is displayed at the end. In addition, the TabFolder receives a SelectionListener that
 * updates the initialTab variable every time you switch to another tab.
 */
public class ProfileEditor extends AbstractEditor<ProfileEditorInput>
	implements CacheModelListener<Long>, IRefreshableEditorPart, ProfileProvider {

	public static final String ID = "ProfileEditor";

	protected static List<ISaveListener> saveListenerList;

	/**
	 * The last displayed Tab.
	 */
	private static int lastSelectedTabIndex = 1;

	// the entity
	private Profile profile;

	private boolean copy = false;

	// models
	private ProfileModel profileModel;
	private PersonLinkModel personLinkModel;
	private ProfileRelationModel profileRelationModel;
	private ProfileFileModel profileFileModel;
	private ConfigParameterSetModel configParameterSetModel;

	// ConfigParameterSet
	private ConfigParameterSet configParameterSet;
	private ProfileConfigParameterSet profileConfigParameterSet;

	// **************************************************************************
	// * Widgets
	// *
	private ProfileOverviewComposite profileOverviewComposite;

	private PersonGroup personGroup;

	private ApprovalGroup approvalGroup;

	private UserCredentialsGroup userCredentialsGroup;

	private ProfileGroup profileGroup;

	private ProfileRoleGroup profileRoleGroup;

	private CommunicationGroup communicationGroup;

	private MultiLineText note;

	private AddressGroupsComposite addressGroupsComposite;

	private CreditCardGroup creditCardGroup;

	private BankGroup bankGroup;

	private List<ProfileCustomFieldComposite> profileCustomFieldComposites = new ArrayList<>();

	private ProfileFileComposite fileComposite;

	private RelatedProfilesGroup relatedProfilesGroup;

	private ProfileCorrespondenceManagementComposite correspondenceManagementComposite;

	private TabFolder tabFolder;

	// TabItems
	private TabItem overviewTabItem;
	private LazyScrolledTabItem personTabItem;
	private LazyScrolledTabItem addressTabItem;
	private LazyScrolledTabItem bankingTabItem;

	private TabItem correspondenceTabItem;
	private LazyScrolledTabItem fileTabItem;

	/*
	 * Since there are a few tabs allow to display on ProfileCustomFieldEditor, but there are many groups more than
	 * those. Some groups will be display under Person Tab of the Profile. So, this is initialized for the tab to be
	 * shown on ProfileCustomFieldEditor only.
	 */
	private Set<ProfileCustomFieldGroupLocation> customFieldTabLocations = new HashSet<>();
	{
		customFieldTabLocations.add(ProfileCustomFieldGroupLocation.TAB_1);
		customFieldTabLocations.add(ProfileCustomFieldGroupLocation.TAB_2);
		customFieldTabLocations.add(ProfileCustomFieldGroupLocation.TAB_3);
	}


	// *
	// * Widgets
	// **************************************************************************

	@Override
	public void init() throws Exception {
		// handle EditorInput
		/*
		 * ist hier etwas speziell, da ProfileEditorInput auch ein initialisiertes Profile enthalten kann
		 */
		Long profileID = editorInput.getKey();
		this.copy = editorInput.isCopy();

		// get models
		profileModel = ProfileModel.getInstance();
		personLinkModel = PersonLinkModel.getInstance();
		profileRelationModel = ProfileRelationModel.getInstance();
		profileFileModel = ProfileFileModel.getInstance();
		configParameterSetModel = ConfigParameterSetModel.getInstance();

		if (profileID != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			profile = profileModel.getExtendedProfile(profileID);

			// register at model
			profileModel.addListener(this);
		}
		else if (copy && editorInput.getCopyProfileID() != null) {
			// get entity
			profile = profileModel.getExtendedProfile(editorInput.getCopyProfileID());

			Profile newProfile = ProfileModel.getInitialProfile();
			newProfile.copyFrom(profile);

			newProfile.setID(null);
			newProfile.setNewTime(null);
			newProfile.setNewUser(null);
			newProfile.setEditTime(null);
			newProfile.setEditUser(null);
			newProfile.setPersonLink(null);
			newProfile.setSyncId(null);
			newProfile.setWebId(null);
			newProfile.setParticipantLinks(null);

			// set default value
			newProfile.setProfileStatus(ProfileStatus.ACTIVE);

			profile = newProfile;
		}
		else {
			// create empty entity
			profile = ProfileModel.getInitialProfile();

			// set default value
			profile.setProfileStatus(ProfileStatus.ACTIVE);
		}

		// init ConfigurationParameterSet
		configParameterSet = configParameterSetModel.getConfigParameterSet();
		profileConfigParameterSet = configParameterSet.getProfile();
	}


	@Override
	public void dispose() {
		if (profileModel != null) {
			try {
				profileModel.removeListener(this);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	private void createTabsOfPersistedProfiles() throws Exception {
		createOverviewTab(tabFolder);
	}


	protected void setEntity(Profile profile) {
		if (!isNew()) {
			// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			profile = profile.clone();
		}
		this.profile = profile;

		personGroup.setPerson(profile);

		approvalGroup.setPerson(profile);

		if (relatedProfilesGroup != null) {
			relatedProfilesGroup.setProfile(profile);
		}

		if (communicationGroup != null) {
			communicationGroup.setCommunication(profile.getCommunication());
		}

		if (userCredentialsGroup != null) {
			userCredentialsGroup.setPerson(profile);
		}

		if (profileGroup != null) {
			profileGroup.setProfile(profile);
		}

		if (profileRoleGroup != null) {
			profileRoleGroup.setProfile(profile);
		}

		if (addressGroupsComposite != null) {
			addressGroupsComposite.setAbstractPerson(profile);
		}

		if (bankGroup != null) {
			bankGroup.setBank(profile.getBank());
		}

		if (creditCardGroup != null) {
			creditCardGroup.setCreditCard(profile.getCreditCard());
		}

		for (ProfileCustomFieldComposite profileCustomFieldComposite : profileCustomFieldComposites) {
			profileCustomFieldComposite.setProfile(profile);
		}

		if (correspondenceManagementComposite != null) {
			correspondenceManagementComposite.setProfile(profile);
		}

		if (fileComposite != null) {
			fileComposite.setProfile(profile);
		}

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return ProfileLabel.Profile.getString();
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			tabFolder = new TabFolder(parent, SWT.NONE);

			createPersonTab(tabFolder);
			createAddressesTab(tabFolder);
			createBankingTab(tabFolder);

			if (profileConfigParameterSet.getCustomField().isVisible()) {
				List<ProfileCustomFieldGroup> groups = ProfileCustomFieldGroupModel.getInstance().getAllProfileCustomFieldGroups();
				Map<ProfileCustomFieldGroupLocation, List<ProfileCustomFieldGroup>> locationToGroupsMap = createLocationToGroupsMap(groups);
				List<ProfileCustomFieldComposite> customFieldTabs = createProfileCustomFieldTabs(tabFolder, locationToGroupsMap);
				this.profileCustomFieldComposites.addAll(customFieldTabs);
			}

			if (!isNew()) {
				createTabsOfPersistedProfiles();
			}

			createCorrespondenceTab(tabFolder);
			createDocumentTab(tabFolder);

			// set data before last selected tab is restored
			// profile data is needed in createDocumentTab()
			setEntity(profile);
			if (copy) {
				setDirty(true);
			}

			// Restore last selected tab
			if (!isNew()) {
				tabFolder.setSelection(lastSelectedTabIndex);
			}
			tabFolder.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int selectionIndex = tabFolder.getSelectionIndex();
					if (selectionIndex != -1) {
						lastSelectedTabIndex = selectionIndex;
					}
				}
			});

			// after sync add this as ModifyListener to all widgets and groups
			personGroup.addModifyListener(this);
			approvalGroup.addModifyListener(this);
			if (relatedProfilesGroup != null) {
				relatedProfilesGroup.addModifyListener(this);
			}

			if (communicationGroup != null) {
				communicationGroup.addModifyListener(this);
			}

			if (userCredentialsGroup != null) {
				userCredentialsGroup.addModifyListener(this);
			}

			if (profileGroup != null) {
				profileGroup.addModifyListener(this);
			}

			if (profileRoleGroup != null) {
				profileRoleGroup.addModifyListener(this);
			}

			if (addressGroupsComposite != null) {
				addressGroupsComposite.addModifyListener(this);
			}

			if (creditCardGroup != null) {
				creditCardGroup.addModifyListener(this);
			}

			if (bankGroup != null) {
				bankGroup.addModifyListener(this);
			}

			if (note != null) {
				note.addModifyListener(this);
			}

			for (ProfileCustomFieldComposite profileCustomFieldComposite : profileCustomFieldComposites) {
				profileCustomFieldComposite.addModifyListener(this);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void createOverviewTab(TabFolder tabFolder) {
		overviewTabItem = new TabItem(tabFolder, SWT.NONE, 0);
		overviewTabItem.setText(UtilI18N.Overview);

		profileOverviewComposite = new ProfileOverviewComposite(
			tabFolder,
			profile.getID(),
			configParameterSet
		);
		overviewTabItem.setControl(profileOverviewComposite);
	}


	private void createPersonTab(TabFolder tabFolder) throws Exception {
		personTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		personTabItem.setText(ContactLabel.person.getString());
		Composite personComposite = personTabItem.getContentComposite();
		personComposite.setLayout(new GridLayout(2, true));

		Composite leftComposite = new Composite(personComposite, SWT.NONE);
		leftComposite.setLayout(new GridLayout());
		leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite rightComposite = new Composite(personComposite, SWT.NONE);
		rightComposite.setLayout(new GridLayout());
		rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// PersonGroup
		personGroup = new PersonGroup(leftComposite, SWT.NONE, profileConfigParameterSet);
		personGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// ApprovalGroup
		approvalGroup = new ApprovalGroup(leftComposite, SWT.NONE, profileConfigParameterSet);
		approvalGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Related Profiles and Second Person Group
		if (profileConfigParameterSet.getSecondPerson().isVisible()) {
			relatedProfilesGroup = new RelatedProfilesGroup(leftComposite, SWT.NONE);
			relatedProfilesGroup.setText(ProfileLabel.RelatedProfilesAndSecondPerson.getString());
			final GridData relatedProfilesGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
			relatedProfilesGroup.setLayoutData(relatedProfilesGroupLayoutData);
			relatedProfilesGroup.addLayoutModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					updateVisibilityOfRelatedProfilesGroup(relatedProfilesGroupLayoutData);
				}
			});

			updateVisibilityOfRelatedProfilesGroup(relatedProfilesGroupLayoutData);
		}

		// UserCredentialsGroup
		if (profileConfigParameterSet.getUserName().isVisible() ||
			profileConfigParameterSet.getPassword().isVisible()
		) {
			userCredentialsGroup = new UserCredentialsGroup(rightComposite, SWT.NONE, configParameterSet.getProfile());
			userCredentialsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}

		// ProfileGroup
		if ( profileConfigParameterSet.getProfileState().isVisible() ) {
			profileGroup = new ProfileGroup(rightComposite, SWT.NONE, configParameterSet);
			profileGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}

		// ProfileRoleGroup
		if (profileConfigParameterSet.getProfileRole().isVisible()) {
			profileRoleGroup = new ProfileRoleGroup(rightComposite, SWT.NONE);
			profileRoleGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}

		// CommunicationGroup
		if (profileConfigParameterSet.getCommunication().isVisible()) {
			communicationGroup = new CommunicationGroup(
				rightComposite,
				SWT.NONE,
				profileConfigParameterSet.getCommunication()
			);
			communicationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}


		// Custom Fields
		List<ProfileCustomFieldGroup> groups = ProfileCustomFieldGroupModel.getInstance().getAllProfileCustomFieldGroups();
		Set<ProfileCustomFieldGroup> leftSideGroups = new TreeSet<>( ProfileCustomFieldGroup_Location_Position_Comparator.getInstance() );
		Set<ProfileCustomFieldGroup> rightSideGroups = new TreeSet<>( ProfileCustomFieldGroup_Location_Position_Comparator.getInstance() );
		for (ProfileCustomFieldGroup group : groups) {
			switch (group.getLocation()) {
				case PSNLT:
					leftSideGroups.add(group);
					break;
				case PSNRT:
					rightSideGroups.add(group);
					break;
				default:
					break;
			}
		}

		Map<ProfileCustomFieldGroup, List<ProfileCustomField>> leftSideGroupToFieldsMap = keepOnlyGroupWithCustomFields(leftSideGroups);
		if (!leftSideGroupToFieldsMap.isEmpty()) {
			ProfileCustomFieldComposite customFieldWidget = createProfileCustomFieldWidgets(
				leftComposite,
				this,
				leftSideGroupToFieldsMap
			);

			// make LazyComposite visible
			customFieldWidget.setVisible(true);

			this.profileCustomFieldComposites.add(customFieldWidget);
		}

		Map<ProfileCustomFieldGroup, List<ProfileCustomField>> rightSideGroupToFieldsMap = keepOnlyGroupWithCustomFields(rightSideGroups);
		if (!rightSideGroupToFieldsMap.isEmpty()) {
			ProfileCustomFieldComposite customFieldWidget = createProfileCustomFieldWidgets(
				rightComposite,
				this,
				rightSideGroupToFieldsMap
			);

			// make LazyComposite visible
			customFieldWidget.setVisible(true);

			this.profileCustomFieldComposites.add(customFieldWidget);
		}


		// note
		if (profileConfigParameterSet.getNote().isVisible()) {
			Composite noteComposite = new Composite(personComposite, SWT.NONE);
			GridData gd_noteComposite = new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1);
			noteComposite.setLayoutData(gd_noteComposite);
			GridLayout noteCompositeGridLayout = new GridLayout();
			noteCompositeGridLayout.numColumns = 2;
			noteComposite.setLayout(noteCompositeGridLayout);
			Label noteLabel = new Label(noteComposite, SWT.NONE);
			noteLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			noteLabel.setText( Profile.NOTE.getLabel() );
			noteLabel.setToolTipText( Profile.NOTE.getDescription() );
			note = new MultiLineText(noteComposite, SWT.BORDER, true);
			GridData gd_note = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd_note.verticalSpan = 2;
			note.setLayoutData(gd_note);
			note.setTextLimit( Profile.NOTE.getMaxLength() );
		}

		personTabItem.refreshScrollbars();
	}


	private void createAddressesTab(TabFolder tabFolder) throws Exception {
		addressTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		addressTabItem.setText(ContactLabel.addresses.getString());
		AddressConfigParameterSet addressConfigParameterSet = profileConfigParameterSet.getAddress();
		addressGroupsComposite = new AddressGroupsComposite(addressTabItem.getContentComposite(), SWT.NONE, addressConfigParameterSet);
		addressGroupsComposite.setHomeCountryPK( PropertyModel.getInstance().getDefaultCountry() );
		personGroup.addModifyListener(addressGroupsComposite);

		if (relatedProfilesGroup != null) {
    		relatedProfilesGroup.addModifyListener(new ModifyListener() {
    			@Override
    			public void modifyText(ModifyEvent e) {
    				relatedProfilesGroup.syncEntityToWidgets();
    				personGroup.refreshDefaultSalutation();
    				if (addressGroupsComposite != null) {
    					addressGroupsComposite.refreshDefaultAddressLabel();
    				}
    			}
    		});
    	}

		addressTabItem.refreshScrollbars();
	}


	private void createBankingTab(TabFolder tabFolder) throws Exception {
		if (profileConfigParameterSet.getBank().isVisible() ||
			profileConfigParameterSet.getCreditCard().isVisible()
		) {
			bankingTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			bankingTabItem.setText(ContactLabel.Banking.getString());
			Composite bankingComposite = bankingTabItem.getContentComposite();
			bankingComposite.setLayout(new GridLayout(2, true));

			// CreditCardGroup
			if (profileConfigParameterSet.getCreditCard().isVisible()) {
				creditCardGroup = new CreditCardGroup(bankingComposite, SWT.NONE);
				creditCardGroup.setText( Profile.CREDIT_CARD.getString() );
				GridData gd_creditCardGroup = new GridData(SWT.FILL, SWT.CENTER, true, false);
				creditCardGroup.setLayoutData(gd_creditCardGroup);
			}

			// BankGroup
			if (profileConfigParameterSet.getBank().isVisible()) {
				bankGroup = new BankGroup(bankingComposite, SWT.NONE);
				bankGroup.setText(ContactLabel.bankAccount.getString());
				GridData gd_bankGroup = new GridData(SWT.FILL, SWT.TOP, true, false);
				bankGroup.setLayoutData(gd_bankGroup);
			}

			bankingTabItem.refreshScrollbars();
		}
	}


	/**
	 * Creates new profile custom field tabs if there are both groups and custom fields. An empty list can be returned
	 * in case there is no such group or all groups are empty (no such custom fields).
	 *
	 * @param tabFolder
	 *            the target tab folder that new tabs will be inside
	 * @param locationToGroupsMap
	 *            a map represent the one-to-many relationship between tab and groups
	 * @return a new profile custom field tab or empty list
	 * @throws Exception
	 */
	private List<ProfileCustomFieldComposite> createProfileCustomFieldTabs(
		TabFolder tabFolder,
		Map<ProfileCustomFieldGroupLocation, List<ProfileCustomFieldGroup>> locationToGroupsMap
	)
	throws Exception {
		List<ProfileCustomFieldComposite> customFieldCompositeList = new ArrayList<>();

		boolean showCustomFields = profileConfigParameterSet.getCustomField().isVisible();
		if (showCustomFields) {
    		for (Entry<ProfileCustomFieldGroupLocation, List<ProfileCustomFieldGroup>> mapEntry : locationToGroupsMap.entrySet()) {
    			ProfileCustomFieldGroupLocation location = mapEntry.getKey();
    			List<ProfileCustomFieldGroup> customFieldGroups = mapEntry.getValue();

    			// determine if this location is one of the 3 custom field Tabs
    			boolean isCustomFieldTabLocation = customFieldTabLocations.contains(location);
    			if (isCustomFieldTabLocation) {
        			// determine if there is any custom field to show at this location
        			Map<ProfileCustomFieldGroup, List<ProfileCustomField>> groupToCustomFieldsMap = keepOnlyGroupWithCustomFields(customFieldGroups);
        			boolean showCustomFieldsHere = !groupToCustomFieldsMap.isEmpty();

        			// if there is any custom field to show at this location
        			if (showCustomFieldsHere) {
            			LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
            			tabItem.setText( getLocationName(location) );

            			ProfileCustomFieldComposite customFieldComposite = createProfileCustomFieldWidgets(
            				tabItem.getContentComposite(),	// parent
            				this,							// modifyListener
            				groupToCustomFieldsMap
            			);

            			customFieldCompositeList.add(customFieldComposite);

            			tabItem.refreshScrollbars();
        			}
    			}
    		}
		}

		return Collections.unmodifiableList(customFieldCompositeList);
	}


	/**
	 * Creates a new composite of profile custom field corresponds to the relationship of group and fields parameter
	 *
	 * @param parent
	 *            the composite where this new composite will be inside
	 * @param modifyListener
	 * @param mapGroupAndFields
	 *            the relationship between group and fields
	 * @return a new composite of profile custom field after register the modify listener
	 * @throws Exception
	 */
	private ProfileCustomFieldComposite createProfileCustomFieldWidgets(
		Composite parent,
		ModifyListener modifyListener,
		Map<ProfileCustomFieldGroup, List<ProfileCustomField>> mapGroupAndFields
	)
	throws Exception {
		ProfileCustomFieldComposite customFieldComposite = new ProfileCustomFieldComposite(
			parent,
			SWT.NONE,
			mapGroupAndFields
		);

		customFieldComposite.addModifyListener(modifyListener);
		return customFieldComposite;
	}


	/**
	 * Creates a new map between custom field group and its fields. In case all groups are empty or no such group, the
	 * empty map will be return.
	 *
	 * @param groups
	 *            a list of group
	 * @return a new map between custom field group and its fields or empty map
	 * @throws Exception
	 */
	private Map<ProfileCustomFieldGroup, List<ProfileCustomField>> keepOnlyGroupWithCustomFields(
		Collection<ProfileCustomFieldGroup> groups
	)
	throws Exception {
		Map<ProfileCustomFieldGroup, List<ProfileCustomField>> groupToCustomFieldsMap = ProfileCustomFieldModel.getInstance()
			.getProfileCustomFieldsByGroupMap();
		Map<ProfileCustomFieldGroup, List<ProfileCustomField>> groupToNonEmptyCustomFieldsMap = new LinkedHashMap<>();
		for (ProfileCustomFieldGroup group : groups) {
			List<ProfileCustomField> allFieldsOfCurrentGroup = groupToCustomFieldsMap.get(group);
			if (!allFieldsOfCurrentGroup.isEmpty()) {
				groupToNonEmptyCustomFieldsMap.put(group, allFieldsOfCurrentGroup);
			}
		}
		return groupToNonEmptyCustomFieldsMap;
	}


	/**
	 * Determine the text representation of a {@link ProfileCustomFieldGroupLocation}.
	 *
	 * @return
	 * @throws Exception
	 */
	private String getLocationName(ProfileCustomFieldGroupLocation location) throws Exception {
		String textOfLocation = PropertyModel.getInstance().getPropertyValue( location.getKey() );
		return textOfLocation;
	}


	/**
	 * Groups all profile custom field groups under the location they belong to
	 *
	 * @param groups
	 *            all groups without categorize the location where they belong
	 * @return a new map represent one-to-many relationship between location and groups
	 * @throws Exception
	 */
	private Map<ProfileCustomFieldGroupLocation, List<ProfileCustomFieldGroup>> createLocationToGroupsMap(
		List<ProfileCustomFieldGroup> groups
	)
	throws Exception {
		Map<ProfileCustomFieldGroupLocation, List<ProfileCustomFieldGroup>> mapTabAndGroups = new EnumMap<>(
			ProfileCustomFieldGroupLocation.class
		);

		Collections.sort(groups, ProfileCustomFieldGroup_Location_Position_Comparator.getInstance());
		for (ProfileCustomFieldGroup group : groups) {
			ProfileCustomFieldGroupLocation currentTab = group.getLocation();
			List<ProfileCustomFieldGroup> groupUnderCurrentTab = mapTabAndGroups.get(currentTab);
			if (groupUnderCurrentTab == null) {
				groupUnderCurrentTab = new ArrayList<>();
				mapTabAndGroups.put(currentTab, groupUnderCurrentTab);
			}
			groupUnderCurrentTab.add(group);
		}
		return mapTabAndGroups;
	}


	private void createCorrespondenceTab(TabFolder tabFolder) throws Exception {
		if (correspondenceTabItem == null && profileConfigParameterSet.getCorrespondence().isVisible()) {
			correspondenceTabItem = new TabItem(tabFolder, SWT.NONE);
			correspondenceTabItem.setText(ContactLabel.Correspondence.getString());

			correspondenceManagementComposite = new ProfileCorrespondenceManagementComposite(tabFolder, SWT.NONE);
			correspondenceTabItem.setControl(correspondenceManagementComposite);

			correspondenceManagementComposite.addModifyListener(this);
		}
	}


	private void createDocumentTab(TabFolder tabFolder) throws Exception {
		if (fileTabItem == null && profileConfigParameterSet.getDocument().isVisible()) {
			fileTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			fileTabItem.setText(ContactLabel.Files.getString());

			fileComposite = new ProfileFileComposite(tabFolder, SWT.NONE);
			fileTabItem.setControl(fileComposite);

			fileTabItem.refreshScrollbars();
		}
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();

		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/*
				 * Save new entity. On success we get the updated entity, else an Exception will be thrown.
				 */

				/*
				 * create profile with duplicate-check we need to use array here to assign variable in busyCursoWhile...
				 */
				final ProfileDuplicateException[] duplicateExceptions = new ProfileDuplicateException[1];
				final Profile[] profiles = new Profile[1];
				BusyCursorHelper.busyCursorWhile(new Runnable() {
					@Override
					public void run() {
						try {
							profiles[0] = profileModel.create(profile, false);
						}
						catch (ProfileDuplicateException e) {
							duplicateExceptions[0] = e;
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}
					}
				});

				/*
				 * If a DuplicateException occurred, handle it (show the ProfileDuplicateDialog).
				 */
				if (duplicateExceptions[0] != null) {
					handleDuplicateException(duplicateExceptions[0]);
				}
				else if (profiles[0] != null) {
					profile = profiles[0];
				}

				if (profile.getID() != null) {
					// observe the ProfileModel
					profileModel.addListener(this);

					// Set the PK of the new entity to the EditorInput
					editorInput.setKey(profile.getID());

					/*
					 * Create the tabs which are hidden for new profiles. This must be called before setEntity(profile).
					 * Otherwise the new tabs have no profile.
					 */
					createTabsOfPersistedProfiles();

					// set new entity
					setEntity(profile);
				}
			}
			else {
				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				profileModel.update(profile);

				// setEntity will be called indirectly in dataChange()
			}

			// inform ISaveListeners
			fireSaved(create);

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			String msg = null;
			if (create) {
				msg = I18N.CreateProfileErrorMessage;
			}
			else {
				msg = I18N.UpdateProfileErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	private void handleDuplicateException(ProfileDuplicateException duplicateException) {
		Collection<Profile> duplicates = duplicateException.getDuplicates();

		ProfileDuplicateDialog dialog = new ProfileDuplicateDialog(
			getSite().getShell(),
			I18N.DuplicateDialog_Title,
			profile,
			duplicates
		);
		dialog.open();

		profile = dialog.getProfile();
	}


	private void syncWidgetsToEntity() {
		if (profile != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						if (note != null) {
							note.setText(StringHelper.avoidNull(profile.getNote()));
						}

						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());

						// signal that editor has no unsaved data anymore
						setDirty(false);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void syncEntityToWidgets() {
		if (profile != null) {
			personGroup.syncEntityToWidgets();
			approvalGroup.syncEntityToWidgets();

			if (relatedProfilesGroup != null) {
				relatedProfilesGroup.syncEntityToWidgets();
			}

			if (communicationGroup != null) {
				communicationGroup.syncEntityToWidgets();
			}

			if (userCredentialsGroup != null) {
				userCredentialsGroup.syncEntityToWidgets();
			}

			if (profileGroup != null) {
				profileGroup.syncEntityToWidgets();
			}

			if (profileRoleGroup != null) {
				profileRoleGroup.syncEntityToWidgets();
			}

			if (note != null) {
				profile.setNote(note.getText());
			}

			if (addressGroupsComposite != null) {
				addressGroupsComposite.syncEntityToWidgets();
			}

			if (creditCardGroup != null) {
				creditCardGroup.syncEntityToWidgets();
			}

			if (bankGroup != null) {
				bankGroup.syncEntityToWidgets();
			}

			for (ProfileCustomFieldComposite profileCustomFieldComposite : profileCustomFieldComposites) {
				profileCustomFieldComposite.syncEntityToWidgets();
			}

			if (correspondenceManagementComposite != null) {
				correspondenceManagementComposite.syncEntityToWidgets();
			}
		}
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			ContactLabel.VigenereCode.getString(),
			ContactLabel.Vigenere2Code.getString(),
			Profile.PERSON_LINK.getString(),
			Profile.SYNC_ID.getString(),
			Profile.WEB_ID.getString(),
			Profile.PORTAL.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(profile.getID()),
			profile.getVigenereCode(),
			profile.getVigenere2Code(),
			StringHelper.avoidNull(profile.getPersonLink()),
			StringHelper.avoidNull(profile.getSyncId()),
			StringHelper.avoidNull(profile.getWebId()),
			StringHelper.avoidNull(profile.getPortalPK()),
			formatHelper.formatDateTime(profile.getNewTime()),
			profile.getNewDisplayUserStr(),
			formatHelper.formatDateTime(profile.getEditTime()),
			profile.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ProfileLabel.Profile.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent event) {
		try {
			// The entity of this editor was changed somehow.
			if (event.getSource() == profileModel) {
				dataChangeProfileModel(event);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void dataChangeProfileModel(CacheModelEvent<Long> event) throws Exception {
		for (Long profileID : event.getKeyList()) {
			// First consider those events of THIS profile
			if (profileID.equals(profile.getID())) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (profile != null) {
					profile = profileModel.getExtendedProfile(profile.getID());
					if (profile != null) {
						setEntity(profile);
					}
					else if (ServerModel.getInstance().isLoggedIn()) {
						closeBecauseDeletion();
					}
				}
			}
			// Then consider the event that the changed profile is the second person of this one
			else if (profileID.equals(profile.getSecondPersonID())) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					profile.setSecondPersonID(null);
				}
				else {
					// getExtendedProfile to refresh change of second person
					profile = profileModel.getExtendedProfile(profile.getID());
				}

				if (relatedProfilesGroup != null) {
					relatedProfilesGroup.setProfile(profile);
				}

				personGroup.setPerson(profile);
				personGroup.refreshDefaultSalutation();

				approvalGroup.setPerson(profile);

				if (addressGroupsComposite != null) {
					addressGroupsComposite.setAbstractPerson(profile);
					addressGroupsComposite.refreshDefaultAddressLabel();
				}
			}
		}
	}


	/**
	 * An editor refresh which reads the profile data anew from the server
	 */
	@Override
	public void refresh() throws Exception {
		if (profile != null) {
			// refresh the old personLink
			Long oldPersonLink = profile.getPersonLink();
			if (oldPersonLink != null) {
				personLinkModel.refresh(oldPersonLink);
			}

			if (profile.getID() != null) {
				// refresh the profile, what will lead to a new instance of profile
				profileModel.refresh(profile.getID());
			}

			// refresh the new personLink
			Long newPersonLink = profile.getPersonLink();
			if (newPersonLink != null && !newPersonLink.equals(oldPersonLink)) {
				personLinkModel.refresh(newPersonLink);
			}

			// refresh detail entities (correspondence, documents)
			if (profile.getID() != null) {
				Long profileID = profile.getID();

				// refresh profileRelations
				profileRelationModel.refreshForeignKey(profileID);

				// refresh profileDocuments
				profileFileModel.refreshForeignKey(profileID);
			}

			/*
			 * Reload data if the editor is still dirty. The models only fire events if the data really changed
			 * (isSameVersion()). So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				profile = profileModel.getExtendedProfile(profile.getID());
				if (profile != null) {
					setEntity(profile);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return profile.getID() == null;
	}


	public Profile getProfile() {
		return profile;
	}


	/**
	 * Correct the input of some fields automatically.
	 */
	public void autoCorrection() {
		personGroup.autoCorrection();

		if (addressGroupsComposite != null) {
			addressGroupsComposite.autoCorrection();
		}

		if (creditCardGroup != null) {
			creditCardGroup.autoCorrection();
		}

		if (bankGroup != null) {
			bankGroup.autoCorrection();
		}
	}


	@Override
	protected String getName() {
		String name = null;
		if (profile != null) {
			name = profile.getName();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.ProfileEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.ProfileEditor_DefaultToolTip;
	}


	@Override
	public Long getProfilePK() {
		Long profileID = null;
		if (profile != null) {
			profileID = profile.getID();
		}
		return profileID;
	}


	// **************************************************************************
	// * Save Listener
	// *

	public static void addSaveListener(ISaveListener saveListener) {
		if (saveListenerList == null) {
			saveListenerList = new ArrayList<>();
		}

		saveListenerList.add(saveListener);
	}


	public static void removeSaveListener(ISaveListener saveListener) {
		if (saveListenerList != null) {
			saveListenerList.remove(saveListener);
		}
	}


	private void fireSaved(boolean create) {
		if (saveListenerList != null) {
			for (ISaveListener saveListener : saveListenerList) {
				saveListener.saved(this, create);
			}
		}
	}


	public void updateVisibilityOfRelatedProfilesGroup(final GridData relatedProfilesGroupLayoutData) {
		if (relatedProfilesGroup != null) {
			relatedProfilesGroupLayoutData.exclude = relatedProfilesGroup.isEmpty();
			relatedProfilesGroup.setVisible(!relatedProfilesGroup.isEmpty());
			relatedProfilesGroup.getParent().getParent().layout(true, true);
		}
	}

	// *
	// * Save Listener
	// **************************************************************************


	public static boolean saveEditor(Long profileID) {
		ProfileEditorInput editorInput = new ProfileEditorInput(profileID);
		return AbstractEditor.saveEditor(editorInput);
	}


	public static boolean saveEditor(Collection<Long> profileIDs) {
		if (profileIDs != null) {
			for (Long profilePK : profileIDs) {
				if ( !saveEditor(profilePK) ) {
					return false;
				}
			}
		}
		return true;
	}

}
