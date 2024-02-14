package de.regasus.finance;

import com.lambdalogic.messeinfo.invoice.sql.InvoiceSearch;
import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.event.EventModel;
import de.regasus.event.EventSearchValuesProvider;
import de.regasus.participant.ParticipantStateSearchValuesProvider;
import de.regasus.participant.ParticipantTypeSearchValuesProvider;

public class ClientInvoiceSearch extends InvoiceSearch {

	private static final long serialVersionUID = 1L;

	protected ParticipantStateSearchValuesProvider participantStateSearchValuesProvider;
	protected ParticipantTypeSearchValuesProvider participantTypeSearchValuesProvider;
	protected InvoiceNoRangeSearchValuesProvider invoiceNoRangeSearchValuesProvider;


	static {
		EventSearchValuesProvider eventSearchValuesProvider = new EventSearchValuesProvider();
        EVENT.setSearchValuesProvider(eventSearchValuesProvider);
	}


	public ClientInvoiceSearch(Long eventPK)
    throws Exception {
        super(eventPK);

        participantStateSearchValuesProvider = new ParticipantStateSearchValuesProvider();
        PARTICIPANT_STATE.setSearchValuesProvider(participantStateSearchValuesProvider);

        participantTypeSearchValuesProvider = new ParticipantTypeSearchValuesProvider(eventPK);
        PARTICIPANT_TYPE.setSearchValuesProvider(participantTypeSearchValuesProvider);

    	invoiceNoRangeSearchValuesProvider = new InvoiceNoRangeSearchValuesProvider(
    		false,	// withYesKey
    		eventPK
    	);
        INVOICE_NO_RANGE.setSearchValuesProvider(invoiceNoRangeSearchValuesProvider);
    }


	@Override
	public EventVO getEventVO() throws Exception {
        if (eventPK != null) {
        	return EventModel.getInstance().getEventVO(eventPK);
        }
        return null;
	}

}
