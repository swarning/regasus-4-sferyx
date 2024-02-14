package de.regasus.profile.editor.overview;

import java.util.List;
import java.util.Locale;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.ProfileConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileCorrespondence;
import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.common.FileSummary;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.profile.ProfileFileModel;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

public class ProfileSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long profileID;

	private ProfileModel profileModel;
	private ProfileFileModel profileFileModel;

	private ProfileConfigParameterSet profileConfigParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public ProfileSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long profileID,
		ProfileConfigParameterSet profileConfigParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.profileID = profileID;
		this.profileConfigParameterSet = profileConfigParameterSet;

		addDisposeListener(this);

		profileModel = ProfileModel.getInstance();
		profileModel.addListener(this, profileID);

		profileFileModel = ProfileFileModel.getInstance();
		profileFileModel.addForeignKeyListener(this, profileID);

		refreshSection();
	}


	@Override
	protected String getTitle() {
		return ProfileLabel.Profile.getString();
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;


    		// get data
			Profile profile = profileModel.getProfile(profileID);

    		// profile status
			String language = Locale.getDefault().getLanguage();
			addIfNotEmpty(Profile.PROFILE_STATUS.getString(), profile.getProfileStatus().getString(language));


			// correspondence
    		/* It's not necessary to observe CorrespondenceModel, because correspondence is always saved
    		 * with Participant together.
    		 */
    		if (profileConfigParameterSet == null || profileConfigParameterSet.getCorrespondence().isVisible()) {
    			int correspondenceCount = 0;
    			List<ProfileCorrespondence> correspondenceList = profile.getCorrespondenceList();
    			if (correspondenceList != null) {
    				correspondenceCount = correspondenceList.size();
    			}
    			addIfNotEmpty(ContactLabel.Correspondence.getString(), correspondenceCount);
    		}

    		// documents
    		if (profileConfigParameterSet == null || profileConfigParameterSet.getDocument().isVisible()) {
    			int documentCount = 0;
    			List<FileSummary> profileDocuments = profileFileModel.getProfileDocumentsByProfileId(profileID);
    			if (profileDocuments != null) {
    				documentCount = profileDocuments.size();
    			}
    			addIfNotEmpty(ContactLabel.Files.getString(), documentCount);
    		}
		}
		finally {
			ignoreCacheModelEvents = false;
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if ( ! ignoreCacheModelEvents) {
				refreshSection();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		if (profileModel != null && profileID != null) {
			try {
				profileModel.removeListener(this, profileID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		if (profileFileModel != null && profileID != null) {
			try {
				profileFileModel.removeForeignKeyListener(this, profileID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}
