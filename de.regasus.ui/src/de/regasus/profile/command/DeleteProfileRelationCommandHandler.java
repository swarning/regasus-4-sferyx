package de.regasus.profile.command;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileRelation;
import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.ProfileRelationModel;
import de.regasus.profile.ProfileRelationTypeModel;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.profile.editor.ProfileEditorInput;
import de.regasus.profile.relation.ProfileRelationSelectionHelper;
import de.regasus.ui.Activator;


public class DeleteProfileRelationCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			final List<ProfileRelation> profileRelationList = ProfileRelationSelectionHelper.getProfileRelations(event);

			if (profileRelationList != null && !profileRelationList.isEmpty()) {

				// Save all ProfileEditors of involved Profiles, because they will be refreshed.
				for (ProfileRelation profileRelation : profileRelationList) {
					ProfileEditorInput editorInput = new ProfileEditorInput(profileRelation.getProfile1ID());
					if ( ! ProfileEditor.saveEditor(editorInput)) {
						return null;
					}

					editorInput = new ProfileEditorInput(profileRelation.getProfile2ID());
					if ( ! ProfileEditor.saveEditor(editorInput)) {
						return null;
					}
				}


				String language = Locale.getDefault().getLanguage();
				String title = null;
				String message = null;
				if (profileRelationList.size() == 1) {
					title = I18N.DeleteOneProfileRelationConfirmation_Title;
					message = I18N.DeleteOneProfileRelationConfirmation_Message;

					ProfileRelationTypeModel profileRelationTypeModel = ProfileRelationTypeModel.getInstance();
					ProfileModel profileModel = ProfileModel.getInstance();

					ProfileRelation profileRelation = profileRelationList.get(0);

					Long profileRelationTypeID = profileRelation.getProfileRelationTypeID();
					ProfileRelationType profileRelationType = profileRelationTypeModel.getProfileRelationType(profileRelationTypeID);

					Long profile1ID = profileRelation.getProfile1ID();
					Profile profile1 = profileModel.getProfile(profile1ID);

					Long profile2ID = profileRelation.getProfile2ID();
					Profile profile2 = profileModel.getProfile(profile2ID);

					StringBuilder name = new StringBuilder(500);
					name.append("\"");
					name.append(profile1.getName());
					name.append(" ");
					name.append(profileRelationType.getDescription12(language));
					name.append(" ");
					name.append(profile2.getName());
					name.append("\"");

					if (profileRelationType.isDirected()) {
						name.append(" / \"");
						name.append(profile2.getName());
						name.append(" ");
						name.append("profileRelationType.getDescription21(language)");
						name.append(" ");
						name.append(profile1.getName());
						name.append("\"");
					}

					message = message.replaceFirst("<name>", name.toString());
				}
				else {
					title = I18N.DeleteManyProfileRelationConfirmation_Title;
					message = I18N.DeleteManyProfileRelationConfirmation_Message;

					String count = String.valueOf(profileRelationList.size());
					message = message.replaceFirst("<count>", count);
				}

				final boolean deleteOK = MessageDialog.openQuestion(HandlerUtil.getActiveShell(event), title, message);

				if (deleteOK) {
					BusyCursorHelper.busyCursorWhile(new Runnable() {

						@Override
						public void run() {
							try {
								ProfileRelationModel.getInstance().delete(profileRelationList);
							}
							catch (Throwable t) {
								String msg = I18N.DeleteProfileRelationErrorMessage;
								RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
							}
						}
					});
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return null;
	}

}
