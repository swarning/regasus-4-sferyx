package de.regasus.participant.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.search.SearchInterceptor;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

public class AssignGroupManagerWizard extends Wizard {


	private ParticipantSelectionWizardPage participantSelectionWizardPage;

	private Long eventPK;
	private Collection<IParticipant> participantList;


	public AssignGroupManagerWizard(Collection<IParticipant> participantList) {
		this.participantList = participantList;

		if (participantList == null) {
			throw new IllegalArgumentException("Parameter participantList must not be null.");
		}
		if (participantList.isEmpty()) {
			throw new IllegalArgumentException("Parameter participantList must not be empty.");
		}

		// determine the event
		for (IParticipant participant : participantList) {
			if (eventPK == null) {
				eventPK = participant.getEventId();
			}
			else if (!eventPK.equals(participant.getEventId())) {
				throw new IllegalArgumentException("All participants must belong to the same event.");
			}
		}
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



	@Override
	public void addPages() {

		participantSelectionWizardPage = new ParticipantSelectionWizardPage(SelectionMode.SINGLE_SELECTION, eventPK);
		participantSelectionWizardPage.setInitialSQLParameters( getSqlParameter() );
		participantSelectionWizardPage.setSearchInterceptor(new SearchInterceptor() {
			@Override
			public void changeSearchParameter(List<SQLParameter> sqlParameters) {
				try {
					// SQLParameter to show only group managers
					sqlParameters.add(
						ParticipantSearch.IS_GROUP_MANAGER.getSQLParameter(
							Boolean.TRUE,
							SQLOperator.EQUAL
						)
					);
				}
				catch (InvalidValuesException e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});


		addPage(participantSelectionWizardPage);
		participantSelectionWizardPage.setTitle(getWindowTitle());
		participantSelectionWizardPage.setMessage(I18N.AssignGroupManagerWizard_Message);
	}


	@Override
	public boolean performFinish() {
		Long groupManagerPK = null;

		List<ParticipantSearchData> selectedParticipants = participantSelectionWizardPage.getSelectedParticipants();
		if (selectedParticipants.size() == 1) {
			groupManagerPK = selectedParticipants.get(0).getPK();
		}

		if (groupManagerPK != null) {
			try {
				ParticipantModel.getInstance().setGroupManager(participantList, groupManagerPK);
				return true;
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		return false;
	}


	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();

		if (currentPage == participantSelectionWizardPage) {
			return currentPage.isPageComplete();
		}
		else {
			return false;
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == participantSelectionWizardPage)
			return null;
		else {
			return super.getNextPage(page);
		}
	}

	@Override
	public String getWindowTitle() {
		return I18N.AssignGroupManagerWizard_Title;
	}


	public Point getPreferredSize() {
		return new Point(800, 700);
	}

}
