package de.regasus.participant.search;

import java.util.Locale;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.event.EventModel;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.participant.ParticipantTypeModel;

enum ParticipantSearchTableColumns {NUMBER, FIRST_NAME, LAST_NAME, CITY, PARTICIPANT_TYPE, PARTICIPANT_STATE, ORGANISATION, CUSTOMER_NO, EVENT}

public class ParticipantSearchTable extends SimpleTable<ParticipantSearchData, ParticipantSearchTableColumns> {

	private String language = Locale.getDefault().getLanguage();
	private EventModel eventModel;


	public ParticipantSearchTable(Table table) {
		super(
			table,
			ParticipantSearchTableColumns.class,
			true,	// sortable
			false	// editable
		);
		eventModel = EventModel.getInstance();
	}


	@Override
	public String getColumnText(ParticipantSearchData participantSearchData, ParticipantSearchTableColumns column) {
		String label = null;

		switch (column) {
			case NUMBER:
				if (participantSearchData.getNumber() != null) {
					label = participantSearchData.getNumber().toString();
				}
				break;
			case FIRST_NAME:
				label = participantSearchData.getFirstName();
				break;
			case LAST_NAME:
				label = participantSearchData.getLastName();
				break;
			case CITY:
				label = participantSearchData.getCity();
				break;
			case PARTICIPANT_TYPE:
				LanguageString participantTypeName = null;
				try {
					Long ptPK = participantSearchData.getParticipantTypePK();
					ParticipantType participantType = ParticipantTypeModel.getInstance().getParticipantType(ptPK);
					participantTypeName = participantType.getName();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}

				if (participantTypeName != null) {
					label = participantTypeName.getString(language);
				}
				else {
					label = "";
				}

				break;
			case PARTICIPANT_STATE:
				Long statePK = participantSearchData.getStatePK();
				try {
					ParticipantState state = ParticipantStateModel.getInstance().getParticipantState(statePK);
					label = state.getString(language);
				}
				catch (Exception e) {
					// No error dialog because there will be as many error dialogs as table rows.
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
				break;
			case ORGANISATION:
				label = participantSearchData.getOrganisation();
				break;
			case CUSTOMER_NO:
				label = participantSearchData.getCustomerNo();
				break;
			case EVENT:
				try {
					EventVO eventVO = eventModel.getEventVO(participantSearchData.getEventId());
					label = eventVO.getMnemonic();
				}
				catch (Exception e) {
					System.err.println(e);
				}
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected Comparable<?> getColumnComparableValue(ParticipantSearchData participantSearchData, ParticipantSearchTableColumns column) {
		/* Return values that a not of type String, e.g. Date Integer.
		 * Values of type String can be ordered by super.getColumnComparableValue(),
		 * because their visible value returned by getColumnText() is equal to their sort value.
		 */
		switch (column) {
    		case NUMBER:
    			return participantSearchData.getNumber();
    		default:
    			return super.getColumnComparableValue(participantSearchData, column);
		}
	}


	@Override
	protected ParticipantSearchTableColumns getDefaultSortColumn() {
		return ParticipantSearchTableColumns.NUMBER;
	}

}
