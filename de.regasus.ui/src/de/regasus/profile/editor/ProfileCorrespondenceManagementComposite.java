package de.regasus.profile.editor;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.CorrespondenceType;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileCorrespondence;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.composite.AbstractCorrespondenceManagementComposite;
import de.regasus.common.composite.CorrespondenceComposite;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class ProfileCorrespondenceManagementComposite 
extends AbstractCorrespondenceManagementComposite<ProfileCorrespondence> {

	// the entity
	private Profile profile;
	
	
	/**
	 * Create the composite. It shows scroll bars when the space is not enough
	 * for all the profile correspondences.
	 * 
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ProfileCorrespondenceManagementComposite(final Composite tabFolder, int style) {
		super(tabFolder, style);
	}
	
	
	@Override
	public void refreshData() {
		syncWidgetsToEntity();
	}

	
	private void syncWidgetsToEntity() {
		if (profile != null && profile.getID() != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				public void run() {
					try {
						modifySupport.setEnabled(false);
						
						// get sub-entity list
						List<ProfileCorrespondence> correspondenceList = profile.getCorrespondenceList();
						
						// set number of necessary Composites
						compositeListSupport.setSize(correspondenceList.size());

						// set n sub-entities to n sub-Composites
						for (int i = 0; i < correspondenceList.size(); i++) {
							// set sub-entity to sub-Composite
							compositeListSupport.getComposite(i).setCorrespondence( correspondenceList.get(i) );
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}
	
	
	@Override
	public void syncEntityToWidgets() {
		if (isInitialized()) {
			profile.clearCorrespondenceList();

			try {
				for (CorrespondenceComposite<ProfileCorrespondence> corrComp : compositeListSupport.getCompositeList()) {
					ProfileCorrespondence correspondence = corrComp.getCorrespondence();
					profile.addCorrespondence( correspondence );
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	protected ProfileCorrespondence createEntity() throws Exception {
		ProfileCorrespondence correspondence = new ProfileCorrespondence();
		correspondence.setCorrespondenceTime(new Date());
		correspondence.setType(CorrespondenceType.Other);
		
		// add user name, because it is visible in the editor
		correspondence.setNewUser(ServerModel.getInstance().getModelData().getUser());
		
		return correspondence;
	}


	public Profile getProfile() {
		return profile;
	}


	public void setProfile(Profile profile) {
		this.profile = profile;
		if (isInitialized()) {
			syncWidgetsToEntity();
		}
	}

}
