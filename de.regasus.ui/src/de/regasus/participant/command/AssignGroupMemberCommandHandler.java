package de.regasus.participant.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.search.SearchInterceptor;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.search.OneOrManyParticipantSelectionDialogConfig;
import de.regasus.participant.search.ParticipantSelectionDialog;
import de.regasus.ui.Activator;

public class AssignGroupMemberCommandHandler extends AbstractHandler {

	private static final SearchInterceptor searchInterceptor = new SearchInterceptor() {
		@Override
		public void changeSearchParameter(List<SQLParameter> sqlParameters) {
			try {
				// SQLParameter to exclude group managers
				sqlParameters.add(
					ParticipantSearch.IS_GROUP_MANAGER.getSQLParameter(
						Boolean.FALSE,
						SQLOperator.EQUAL
					)
				);

				// SQLParameter to exclude companions
				sqlParameters.add(
					ParticipantSearch.IS_COMPANION.getSQLParameter(
						Boolean.FALSE,
						SQLOperator.EQUAL
					)
				);
			}
			catch (InvalidValuesException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the GroupManager
			IParticipant groupManager = ParticipantSelectionHelper.getParticipant(event);

			if (groupManager != null && groupManager.isGroupManager()) {
				// try to save the editor of the main participant
				boolean editorSaveCkeckOK = ParticipantEditor.saveEditor(groupManager.getPK());
				if (!editorSaveCkeckOK) {
					return null;
				}

				Shell shell = HandlerUtil.getActiveShellChecked(event);
				Long eventPK = groupManager.getEventId();

				ParticipantSelectionDialog participantSelectionDialog = new ParticipantSelectionDialog(
					shell,
					OneOrManyParticipantSelectionDialogConfig.INSTANCE,
					eventPK
				);
				participantSelectionDialog.setTitle(I18N.AssignGroupMemberWizard_Title);
				participantSelectionDialog.setMessage(I18N.AssignGroupMemberWizard_Message);
				participantSelectionDialog.setInitialSQLParameters( getSqlParameter() );
				participantSelectionDialog.setSearchInterceptor(searchInterceptor);

				participantSelectionDialog.open();
				if (!participantSelectionDialog.isCancelled()) {
					// save editors of selected Participants
					List<Long> selectedParticipantPKs = participantSelectionDialog.getSelectedPKs();
					editorSaveCkeckOK = ParticipantEditor.saveEditor(selectedParticipantPKs);

					// set add selected Participants to GroupManager
					List<? extends IParticipant> selectedParticipants = participantSelectionDialog.getSelectedParticipants();
					ParticipantModel.getInstance().setGroupManager(selectedParticipants, groupManager.getPK());
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}




	private ArrayList<SQLParameter> getSqlParameter() {
		ArrayList<SQLParameter> sqlParameterList = new ArrayList<SQLParameter>();
		try {
			// SQLParameter to show inactive last name
			SQLParameter lastNameSqlParameter = ParticipantSearch.LAST_NAME.getSQLParameter(
				null,
				SQLOperator.FUZZY_IGNORE_CASE
			);
			lastNameSqlParameter.setActive(false);
			sqlParameterList.add(lastNameSqlParameter);

			// SQLParameter to show inactive participant number
			SQLParameter numberSqlParameter = ParticipantSearch.NO.getSQLParameter(
				null,
				SQLOperator.EQUAL
			);
			numberSqlParameter.setActive(false);
			sqlParameterList.add(numberSqlParameter);
		}
		catch (InvalidValuesException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return sqlParameterList;
	}

}
