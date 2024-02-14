package de.regasus.impex.eivfobi.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.dialog.EventWizardPage;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;
import de.regasus.participant.dialog.ParticipantSelectionWizardPage;


public class EIVFoBiCreateWizard extends Wizard {

	private Long eventPK;
	private Long programmePointPK;


	private EventWizardPage eventPage;
	private EIVFoBiProgrammePointWizardPage programmePointPage;
	private ParticipantSelectionWizardPage participantSelectionWizardPage ;


	private List<ParticipantSearchData> selectedParticipants;


	@Override
	public void addPages() {
		setWindowTitle(ImpexI18N.EIVFoBiCreateWizard_Title);

		eventPage = new EventWizardPage();
		eventPage.setTitle( ParticipantLabel.Event.getString() );
		eventPage.setDescription(ImpexI18N.EIVFoBiCreateWizard_eventPageDecription);
		eventPage.setInitiallySelectedEventPK(eventPK);


		eventPage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setEventPK( eventPage.getEventId() );
			}
		});
		addPage(eventPage);


		programmePointPage = new EIVFoBiProgrammePointWizardPage(eventPK);
		addPage(programmePointPage);


		participantSelectionWizardPage = new ParticipantSelectionWizardPage(SelectionMode.MULTI_SELECTION, eventPK);
		participantSelectionWizardPage.setInitialSQLParameters( getSqlParameter() );
		addPage(participantSelectionWizardPage);
	}


	private List<SQLParameter> getSqlParameter() {
		List<SQLParameter> sqlParameterList = new ArrayList<>();
		try {
			// SQLParameter to search for cme number
			SQLParameter cmeSqlParameter = ParticipantSearch.CME_NO.getSQLParameter(null, SQLOperator.NOT_EQUAL);
			cmeSqlParameter.setActive(true);
			sqlParameterList.add(cmeSqlParameter);
		}
		catch (InvalidValuesException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return sqlParameterList;
	}


	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		return 	currentPage == participantSelectionWizardPage && participantSelectionWizardPage.isPageComplete();
	}


	@Override
	public boolean performFinish() {
		selectedParticipants = participantSelectionWizardPage.getSelectedParticipants();
		programmePointPK = programmePointPage.getProgrammePointVO().getPK();

		return true;
	}


	public Long getEventPK() {
		return eventPK;
	}


	public void setEventPK(Long eventPK) {
		this.eventPK = eventPK;
		if (programmePointPage != null) {
			programmePointPage.setEventPK(eventPK);
		}
		if (participantSelectionWizardPage != null) {
			participantSelectionWizardPage.setEventPK(eventPK);
		}
	}


	public List<ParticipantSearchData> getSelectedParticipants() {
		return selectedParticipants;
	}


	public Long getProgrammePointPK() {
		return programmePointPK;
	}

}
