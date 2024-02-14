package de.regasus.profile.relation.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileRelation;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.view.AbstractView;
import de.regasus.participant.editor.ISaveListener;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileProvider;
import de.regasus.profile.ProfileRelationModel;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.profile.editor.ProfileEditorInput;
import de.regasus.ui.Activator;

public class ProfileRelationView
extends AbstractView
implements IPartListener2, CacheModelListener<Long>, ISaveListener, ProfileProvider {
    /* ISaveListener
     * The ProfileRelationView has to be an ISaveListener of the ProfileEditor, to sync to a ProfileEditor
     * when it's saved. Listening to CREATE events of the ProfileModel doesn't work, because the order
     * in which listeners are informed is not deterministic. If the ProfileRelationView is informed before
     * the ProfileEditor, the ProfileRelationView cannot sync to the ProfileEditor, because the latter
     * doesn't know the Profile's ID yet.
     */

	public static final String ID = "ProfileRelationView";

	private Label profileNameLabel;
	private TableViewer tableViewer;
	private ProfileRelationTable profileRelationTable;

	private Long profileID;
	private Set<Long> connectedProfileIDs;

	private ProfileModel profileModel;
	private ProfileRelationModel profileRelationModel;

	// Actions
	private CreateProfileRelationAction createProfileRelationAction;


	/**
	 * The last value of visible as get from the ConfigParameterSet in isVisible().
	 * Has to be stored because the result of isVisible() should not change in the case that the
	 * getConfigParameterSet() returns null.
	 */
	private boolean visible = false;


	/* (non-Javadoc)
	 * @see de.regasus.core.ui.view.AbstractView#isVisible()
	 */
	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible =	getConfigParameterSet().getProfile().isVisible() &&
						getConfigParameterSet().getProfile().getProfileRelation().isVisible();
		}
		return visible;
	}


	public ProfileRelationView() {
		profileModel = ProfileModel.getInstance();
		profileRelationModel = ProfileRelationModel.getInstance();

		ProfileEditor.addSaveListener(this);

		connectedProfileIDs = new HashSet<>();
	}


	@Override
	public void createWidgets(Composite parent) {
		if (isVisible()) {
			Composite container = new Composite(parent, SWT.NONE);
			GridLayout viewLayout = new GridLayout(2, false);
			container.setLayout(viewLayout);

			// row 1, col 1
			CLabel relationLabel = new CLabel(container, SWT.NONE);
			Image profileImage = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PROFILE).createImage();
			relationLabel.setImage(profileImage);
			relationLabel.setText(I18N.ProfileRelationView_RelationsOfProfile);


			// create widgets

			// row 1, col 2
			profileNameLabel = new Label(container, SWT.NONE);
			profileNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// row 2, col 1 - 2
			Composite relationTableComposite = new Composite(container, SWT.BORDER);
			relationTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			TableColumnLayout layout = new TableColumnLayout();
			relationTableComposite.setLayout(layout);


			// create table

			Table table = new Table(relationTableComposite, SWT.MULTI | SWT.FULL_SELECTION);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			TableColumn role1Column = new TableColumn(table, SWT.NONE);
			layout.setColumnData(role1Column, new ColumnWeightData(140));
			role1Column.setText(I18N.ProfileRelationView_Column_ProfileRelationTypeDesc);

			TableColumn profile2NameColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(profile2NameColumn, new ColumnWeightData(140));
			profile2NameColumn.setText(I18N.ProfileRelationView_Column_OtherProfile);

			profileRelationTable = new ProfileRelationTable(table);

			tableViewer = profileRelationTable.getViewer();


			getSite().setSelectionProvider(tableViewer);

			// create Actions and add them to different menus
			initializeActions();
			setContributionItemsVisible(true);

			hookContextMenu();
			hookDoubleClickAction();

			syncToCurrentProfileProvider();
			getSite().getPage().addPartListener(this);

		}
		else {
			Label label = new Label(parent, SWT.NONE);
			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
		}
	}


	private void initializeActions() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();

		if (createProfileRelationAction == null) {
			createProfileRelationAction = new CreateProfileRelationAction(window);

			contributeToActionBars();
		}
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ProfileRelationView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
		tableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void hookDoubleClickAction() {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (profileID != null) {
    				IWorkbenchWindow window = getSite().getWorkbenchWindow();
    				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    				ProfileRelation profileRelation = (ProfileRelation) selection.getFirstElement();

    				Long connectedProfileID = null;
    				if (profileID.equals(profileRelation.getProfile1ID())) {
    					connectedProfileID = profileRelation.getProfile2ID();
    				}
    				else {
    					connectedProfileID = profileRelation.getProfile1ID();
    				}

    				ProfileEditorInput editorInput = new ProfileEditorInput(connectedProfileID);
    				try {
    					window.getActivePage().openEditor(editorInput, ProfileEditor.ID);
    				}
    				catch (PartInitException e) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    				}
				}
			}
		});
	}


	private void fillContextMenu(IMenuManager manager) {
		manager.add(createProfileRelationAction);

		// update the menu, necessary when it changes after after its first initialization
		manager.update(true);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}


	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}


	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(createProfileRelationAction);

		// update the menu, necessary when it changes after after its first initialization
		manager.update(true);
	}


	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(createProfileRelationAction);

		// update the tool bar, necessary when it changes after its first initialization
		manager.update(true);
	}


	@Override
	public void setFocus() {
		try {
			if (profileNameLabel != null && !profileNameLabel.isDisposed() && profileNameLabel.isEnabled()) {
				profileNameLabel.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


    // *************************************************************************
    // * IPartListener2
    // *

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		syncToCurrentProfileProvider();
	}


	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	// *
	// * IPartListener2
    // *************************************************************************

	private void syncToCurrentProfileProvider() {
		try {
			IWorkbenchPart activePart = getSite().getPage().getActivePart();

			if (activePart != null) {
				if (activePart instanceof ProfileProvider) {
					ProfileProvider profileProvider = (ProfileProvider) activePart;
					setProfileID(profileProvider.getProfilePK());
				}
				else if (profileID != null && tableViewer.getInput() == null) {
					/*
					 * That is the case, if the ConfigEditor is activated and the profileRelationView is set to invisible
					 * then visible again. So the profileID is not null, but the activePart is not instance of ProfileProvider,
					 * without this else if block the tableViewer is leer because the input is not set.
					 */
					refreshProfile();
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == profileRelationModel) {
				refreshProfileRelations();
			}
			else if (event.getSource() == profileModel) {
				// DELETE is irrelevant because the ProfileProvider changes anyway.

				if (event.getOperation() == CacheModelOperation.UPDATE
					|| event.getOperation() == CacheModelOperation.REFRESH
				) {
					refreshProfile();
				}
				else if (event.getOperation() == CacheModelOperation.CREATE) {
					/* CREATE is relevant. Although newly created Profiles have no ProfileRelation,
					 * they may be added later, therefore the view has to be set to the current Profile.
					 * Created Profiles are generally saved in a ProfileProvider (ProfileEditor),
					 * so we initiate a sync to the current ProfileProvider. If the Profile has been
					 * created otherwise, this is unnecessary but not fatal, because the synchronisation
					 * is only done if the provided Profile has changed.
					 */
					syncToCurrentProfileProvider();
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void setProfileID(Long profileID) throws Exception {
		// determine the ID of the current (old) profile
		Long oldProfileID = this.profileID;

		// go on if the profile changed
		if (!EqualsHelper.isEqual(oldProfileID, profileID)) {
			this.profileID = profileID;

			// remove as listeners for old profile
			if (oldProfileID != null) {
    			profileModel.removeListener(this, oldProfileID);
    			profileRelationModel.removeForeignKeyListener(this, oldProfileID);
			}

			// add as listeners for new profile
			if (profileID != null) {
    			profileModel.addListener(this, profileID);
    			profileRelationModel.addForeignKeyListener(this, profileID);
			}

			refreshProfile();
		}
	}


	private void refreshProfile() throws Exception {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					// load Profile
					Profile profile = null;
					if (profileID != null) {
						profile = profileModel.getProfile(profileID);
					}

					// set profile name
            		String name = "";
        			if (profile != null) {
        				name = profile.getName();
        			}
            		profileNameLabel.setText(name);

            		// set profileID
            		profileRelationTable.setProfileID(profileID);
            		createProfileRelationAction.setProfile(profile);

            		refreshProfileRelations();
				}
    			catch (Exception e) {
    				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    			}
    		}
    	});
	}


	private void refreshProfileRelations() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					// remove as listener for old connected profiles
					for (Long profileID : connectedProfileIDs) {
						profileModel.removeListener(ProfileRelationView.this, profileID);
					}
					connectedProfileIDs.clear();


					if (profileID != null) {
						// get ProfileRelations from model
						List<ProfileRelation> profileRelationList = profileRelationModel.getProfileRelationsByProfile(profileID);

						if (profileRelationList == null) {
							profileRelationList = Collections.emptyList();
						}
						else {
							// determine connected profileIDs
							for (ProfileRelation profileRelation : profileRelationList) {
								Long connectedProfileID = profileRelation.getOtherProfileID(profileID);
								connectedProfileIDs.add(connectedProfileID);
							}

							// listen for changes on connected Profiles
							for (Long connectedProfileID : connectedProfileIDs) {
								profileModel.addListener(ProfileRelationView.this, connectedProfileID);
							}

							/* Load connected Profiles, to load all of them at once, because
							 * otherwise they will be loaded one after another by the
							 * ProfileRelationTable.
							 */
							profileModel.getProfiles(new ArrayList<>(connectedProfileIDs));
						}

						tableViewer.setInput(profileRelationList);
						tableViewer.refresh();
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	@Override
	public void dispose() {
		removeListener();

		super.dispose();
	}


	@Override
	protected void removeListener() {
		try {
			try {
				getSite().getPage().removePartListener(this);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}


			try {
				ProfileEditor.removeSaveListener(this);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}


			if (profileID != null) {
				// remove as ForeignKeyListener from ProfileModel
				try {
					profileModel.removeListener(this, profileID);
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}

				// remove as listener for old connected profiles from ProfileModel
				if (connectedProfileIDs != null) {
					for (Long profileID : connectedProfileIDs) {
						try {
							profileModel.removeListener(this, profileID);
						}
						catch (Exception e) {
							com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
						}
					}
				}

				// remove as foreign key listener from ProfileRelationModel
				try {
					profileRelationModel.removeForeignKeyListener(this, profileID);
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	@Override
	public Long getProfilePK() {
		return profileID;
	}


	@Override
	public void saved(Object source, boolean create) {
		syncToCurrentProfileProvider();
	}

}
