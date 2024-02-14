package de.regasus.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.profile.Profile;

import de.regasus.profile.ProfileModel;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.profile.editor.ProfileEditorInput;

public class ProfileSelectionHelper {

	public static List<Long> getProfileIDs(ISelection selection) {
		List<Long> profileIDs = null;

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;

			profileIDs = new ArrayList<>();

			Iterator<?> iterator = sselection.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();

				if (o instanceof Profile) {
					Profile profile = (Profile) o;
					profileIDs.add(profile.getID());
				}
			}
		}

		return profileIDs;
	}


	public static List<Profile> getProfiles(ISelection selection) throws Exception {
		ArrayList<Profile> profileList = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;

			profileList = new ArrayList<>();

			Iterator<?> iterator = sselection.iterator();

			while (iterator.hasNext()) {
				Object o = iterator.next();

				if (o instanceof Profile) {
					profileList.add((Profile) o);
				}
				else {
					throw new RuntimeException("Not a Profile: " + o.getClass().getName());
				}
			}
		}
		return profileList;
	}


	/**
	 * Helper-Method for Profile-CommandHandlers to determine the selected Profiles either
	 * from a IWorkbenchPart (e.g. ProfileEditor) or the current selection
	 * (e.g. in the ProfileSearchView).
	 *
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static List<Profile> getProfiles(ExecutionEvent event) throws Exception {
		// Determine the Participants
		List<Profile> profileList = null;

		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart != null && activePart instanceof ProfileEditor) {
			// The active part is a ParticipantProvider: Get the Participant of the ParticipantProvider (e.g. ParticipantEditor).
			ProfileEditor profileEditor = (ProfileEditor) activePart;

			// save the ProfileEditor
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveEditor(profileEditor, true);

			final Profile profile = profileEditor.getProfile();
			profileList = Collections.singletonList(profile);
		}
		else {
			// The active part is no ProfileEditor: Get the selected Profile(s).
			final ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection != null) {
				profileList = getProfiles(selection);
			}
		}

		return profileList;
	}


	/**
	 * Helper-Method for Profile-CommandHandlers to determine the selected Profile either
	 * from a IWorkbenchPart (e.g. ProfileEditor) or the current selection
	 * (e.g. in the ProfileSearchView).
	 * If the current selections contains more than one profile, a RuntimeException is thrown.
	 *
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static Profile getProfile(ExecutionEvent event) throws Exception {
		Profile profile = null;

		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
//		if (activePart != null && activePart instanceof ProfileEditor) {
//			// The active part is a ParticipantProvider: Get the Participant of the ParticipantProvider (e.g. ParticipantEditor).
//			ProfileEditor profileEditor = (ProfileEditor) activePart;
//
//			// save the ProfileEditor
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveEditor(profileEditor, true);
//
//			profile = profileEditor.getProfile();
//		}
//		else

		Long profilePK = null;
		if (activePart != null && activePart instanceof ProfileProvider) {
			// The active part is a ProfileProvider: Get the Profile of the ProfileProvider (e.g. ProfileEditor, ProfileRelationView).
			ProfileProvider profileProvider = (ProfileProvider) activePart;
			profilePK = profileProvider.getProfilePK();
		}
		else {
			// The active part is no ProfileProvider: Get the selected Profile(s).
			final ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection != null) {
				List<Profile> profileList = getProfiles(selection);
				if (profileList != null && !profileList.isEmpty()) {
					if (profileList.size() == 1) {
						profilePK = profileList.get(0).getID();
					}
					else {
						throw new RuntimeException(
							"There are " + profileList.size() + " profiles selected.\n" +
							"This command doesn't allow selections with more than one profile."
						);
					}
				}
			}
		}

		if (profilePK != null) {
			// save the ProfileEditor
			ProfileEditorInput editorInput = new ProfileEditorInput(profilePK);
			boolean safe = ProfileEditor.saveEditor(editorInput);
			if (safe) {
				profile = ProfileModel.getInstance().getProfile(profilePK);
			}
		}
		return profile;
	}

}
