package de.regasus.person;

import static de.regasus.LookupService.getContactMgr;

import java.util.List;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MIListModel;
import de.regasus.model.Activator;
import de.regasus.participant.ParticipantModel;
import de.regasus.profile.ProfileModel;

/**
 * A model that keeps the list of known functions (of Profiles and Participants, so they can be shown for selection.
 * <p>
 * Events from the ProfileModel and ParticipantModel are evaluated to find out whether there might be new (or less) 
 * function after the changes of a Profile/Participant.
 */
public class FunctionListModel extends MIListModel<String> implements CacheModelListener<Long> {
	private static FunctionListModel singleton = null;

	// other Models
	private ParticipantModel participantModel;
	private ProfileModel profileModel;

	
	private FunctionListModel() {
		super();
		participantModel = ParticipantModel.getInstance();
		profileModel = ProfileModel.getInstance();
	}


	public static FunctionListModel getInstance() {
		if (singleton == null) {
			singleton = new FunctionListModel();
			singleton.init();
		}
		return singleton;
	}


	protected void init() {
		participantModel.addListener(this);
		profileModel.addListener(this);
	}


	@Override
	protected List<String> getModelDataFromServer() throws Exception {
		return getContactMgr().getFunctions();
	}


	/**
	 * Evaluate whether the function list needs to be refreshed because of a deletion or update or creation of a participant.
	 */
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}
		
		try {
			// If the data is loaded, update it 
			if (isLoaded()) {
				if (event.getSource() == participantModel) {
					if (event.getOperation() == CacheModelOperation.CREATE ||
						event.getOperation() == CacheModelOperation.UPDATE
					) {
						List<Long> keyList = event.getKeyList();
						List<Participant> participantList = participantModel.getParticipants(keyList);
						for (Participant participant : participantList) {
							List<String> modelData2 = getModelData();
							
							String function = participant.getFunction();
							if (!modelData2.contains(function)) {
								modelData2.add(function);
							}
							
							String address1Function = participant.getAddress1().getFunction();
							if (!modelData2.contains(address1Function)) {
								modelData2.add(address1Function);
							}
							
							String address2Function = participant.getAddress2().getFunction();
							if (!modelData2.contains(address2Function)) {
								modelData2.add(address2Function);
							}
							
							String address3Function = participant.getAddress3().getFunction();
							if (!modelData2.contains(address3Function)) {
								modelData2.add(address3Function);
							}
							
							String address4Function = participant.getAddress4().getFunction();
							if (!modelData2.contains(address4Function)) {
								modelData2.add(address4Function);
							}
						}
						
						fireDataChange();
					}
				}
				else if (event.getSource() == profileModel) {
					if (event.getOperation() == CacheModelOperation.CREATE ||
						event.getOperation() == CacheModelOperation.UPDATE
					) {
						List<Long> keyList = event.getKeyList();
						List<Profile> profileList = profileModel.getProfiles(keyList);
						for (Profile profile : profileList) {
							List<String> modelData2 = getModelData();

							String function = profile.getFunction();
							if (!modelData2.contains(function)) {
								modelData2.add(function);
							}
							
							String address1Function = profile.getAddress1().getFunction();
							if (!modelData2.contains(address1Function)) {
								modelData2.add(address1Function);
							}
							
							String address2Function = profile.getAddress2().getFunction();
							if (!modelData2.contains(address2Function)) {
								modelData2.add(address2Function);
							}
							
							String address3Function = profile.getAddress3().getFunction();
							if (!modelData2.contains(address3Function)) {
								modelData2.add(address3Function);
							}
							
							String address4Function = profile.getAddress4().getFunction();
							if (!modelData2.contains(address4Function)) {
								modelData2.add(address4Function);
							}
						}
						
						fireDataChange();
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		} 
	}
	
}
