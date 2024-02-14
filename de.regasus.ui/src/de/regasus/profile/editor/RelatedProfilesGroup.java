package de.regasus.profile.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileRelation;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileRelationModel;
import de.regasus.profile.ProfileRelationTypeModel;
import de.regasus.ui.Activator;

public class RelatedProfilesGroup
extends Group
implements CacheModelListener<Long>, DisposeListener  {

	// The Entity
	private Profile profile;

	// support for ModifyListeners
	private ModifySupport modifySupport;
	private ModifySupport layoutModifyListenerConnector;

	// Widgets
	private List<RelatedProfileRow> relatedProfileRows = new ArrayList<>();

	// Models
	private ProfileModel profileModel = ProfileModel.getInstance();
	private ProfileRelationModel profileRelationModel = ProfileRelationModel.getInstance();
	private ProfileRelationTypeModel profileRelationTypeModel = ProfileRelationTypeModel.getInstance();

	private boolean ignoreProfileRelationModel = false;

	// Work Data

	private List<ProfileRelation> profileRelations;

	private Profile secondPerson;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public RelatedProfilesGroup(Composite parent, int style) {
		super(parent, style);

		addDisposeListener(this);

		modifySupport = new ModifySupport(this);
		layoutModifyListenerConnector = new ModifySupport(this);

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);

		profileModel.addListener(this);
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (profile != null) {
			profileModel.removeListener(this);

			if (profile.getID() != null) {
				profileRelationModel.removeForeignKeyListener(this, profile.getID());
			}
		}
	}

	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

	// **************************************************************************
	// * Modifying Layout
	// *

	public void addLayoutModifyListener(ModifyListener modifyListener) {
		layoutModifyListenerConnector.addListener(modifyListener);
	}


	public void removeLayoutModifyListener(ModifyListener modifyListener) {
		layoutModifyListenerConnector.removeListener(modifyListener);
	}

	// *
	// * Modifying Layout
	// **************************************************************************


	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.widget;
			if (button.getSelection()) {
				secondPerson = (Profile) button.getData();
			}
			else {
				secondPerson = null;
			}

			modifySupport.fire();
			updateRows();
		}
	};


	private void syncWidgetsToEntity() {
		if (profile != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {

				@Override
				public void run() {
					try {
						Long mainProfileID = profile.getPK();
						if (mainProfileID != null) {
							// Make sure there are as much rows as related profiles
							int relationsCount = profileRelations.size();
							while (relatedProfileRows.size() < relationsCount) {
								RelatedProfileRow relatedProfileRow = new RelatedProfileRow(RelatedProfilesGroup.this);
								relatedProfileRow.addSelectionListener(selectionListener);
								relatedProfileRow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
								relatedProfileRows.add(relatedProfileRow);
							}
							while (relatedProfileRows.size() > relationsCount) {
								RelatedProfileRow row = relatedProfileRows.remove(relatedProfileRows.size() - 1);
								row.dispose();
							}


							// set profile data to rows
							for (int i = 0; i < profileRelations.size(); i++) {
								ProfileRelation profileRelation = profileRelations.get(i);

								Long otherProfileID = profileRelation.getOtherProfileID(profile.getID());
								Profile otherProfile = profileModel.getProfile(otherProfileID);
								String otherProfileRole= profileRelationTypeModel.getRole(otherProfileID, profileRelation);

								relatedProfileRows.get(i).setProfileAndRole(otherProfile, otherProfileRole);
							}

							updateRows();

							layoutModifyListenerConnector.fire();
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}

			});
		}
	}


	public void syncEntityToWidgets() {
		if (profile != null ) {
			if (secondPerson != null) {
				profile.setSecondPersonID( secondPerson.getID() );
			}
			else {
				profile.setSecondPersonID(null);
			}
		}
	}


	public void setProfile(Profile aProfile) {
		// Check if person changed and remove/add listeners
		Long oldProfileID = null;
		if (this.profile != null) {
			oldProfileID = this.profile.getID();
		}

		Long newProfileID = null;
		if (aProfile != null) {
			newProfileID = aProfile.getID();
		}

		if (oldProfileID != null && !oldProfileID.equals(newProfileID)) {
			profileRelationModel.removeForeignKeyListener(this, oldProfileID);
		}


		this.profile = aProfile;
		this.secondPerson = null;

		if (aProfile.getSecondPersonID() != null) {
			try {
				secondPerson = profileModel.getProfile(aProfile.getSecondPersonID());
			}
			catch(Exception e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		try {
			profileRelations = profileRelationModel.getProfileRelationsByProfile(newProfileID);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		syncWidgetsToEntity();


		if (newProfileID != null) {
			profileRelationModel.addForeignKeyListener(this, newProfileID);
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	private void updateRows() {
		for (RelatedProfileRow secondPersonRow : relatedProfileRows) {
			secondPersonRow.updateForSecondPerson(secondPerson);
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (event.getSource() == profileModel) {
			// sync widgets to entity if at least one of the related profiles has changed
			for (Long key : event.getKeyList()) {
				if (!key.equals(profile.getID())) {
    				for (ProfileRelation profileRelation : profileRelations) {
    					if (profileRelation.containsAsRole(key)) {
    						syncWidgetsToEntity();
    						return;
    					}
    				}
				}
			}
		}
		else if (event.getSource() == profileRelationModel && ! ignoreProfileRelationModel) {
			ignoreProfileRelationModel = true;
			try {
				// The event from profile relations concerns this profile, must set anew to make changes visible

				// reload all ProfileRelations
				profileRelations = profileRelationModel.getProfileRelationsByProfile(profile.getID());

				syncWidgetsToEntity();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
			finally {
				ignoreProfileRelationModel = false;
			}
		}
	}


	public boolean isEmpty() {
		return relatedProfileRows.isEmpty();
	}

}
