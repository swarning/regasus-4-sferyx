package de.regasus.finance;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantTypeModel;

public class InvoiceSQLFieldsModel
extends MICacheModel<Long, ClientInvoiceSearch> {

	private static InvoiceSQLFieldsModel singleton = null;

	public static final Long NO_EVENT_KEY = 0L;

	private EventModel eventModel = EventModel.getInstance();
	private ParticipantTypeModel participantTypeModel = ParticipantTypeModel.getInstance();
	private InvoiceNoRangeModel invoiceNoRangeModel = InvoiceNoRangeModel.getInstance();

	// InvoiceSearch does not support Participant Custom Fields yet
//	private ParticipantCustomFieldModel customFieldModel = ParticipantCustomFieldModel.getInstance();


	private InvoiceSQLFieldsModel() {
	}


	public static InvoiceSQLFieldsModel getInstance() {
		if (singleton == null) {
			singleton = new InvoiceSQLFieldsModel();
			singleton.init();
		}
		return singleton;
	}


	private void init() {
		eventModel.addListener(eventModelListener);
		participantTypeModel.addListener(participantTypeModelListener);

		invoiceNoRangeModel.addListener(invoiceNoRangeModelListener);
		// InvoiceSearch does not support Participant Custom Fields yet
//		customFieldModel.addListener(customFieldModelListener);
	}


	private CacheModelListener eventModelListener = new CacheModelListener() {
		@Override
		public void dataChange(CacheModelEvent event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				// init Event in all affected ClientParticipantSearch (with the same eventPK as the Invoice No Range)
				List<?> eventPKs = event.getKeyList();
				for (ClientInvoiceSearch clientInvoiceSearch : getLoadedAndCachedEntities()) {
					if (eventPKs.contains( clientInvoiceSearch.getEventPK() )) {
						clientInvoiceSearch.initEvent();
					}
				}

				fireDataChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private CacheModelListener participantTypeModelListener = new CacheModelListener() {
		@Override
		public void dataChange(CacheModelEvent event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				/* Either the data Participant Type changed or the relation between a Participant Type and an Event.
				 * In both cases we cannot find out which Events are affected. So we have to init the Participant
				 * Type fields in all ClientParticipantSearch.
				 */
				Collection<ClientInvoiceSearch> loadedData = getLoadedAndCachedEntities();
				for (ClientInvoiceSearch clientInvoiceSearch : loadedData) {
					clientInvoiceSearch.initParticipantTypeFields();
				}

				fireDataChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private CacheModelListener invoiceNoRangeModelListener = new CacheModelListener() {
		@Override
		public void dataChange(CacheModelEvent event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				// init Invoice fields in all affected ClientInvoiceSearch (with the same eventPK as the Invoice No Range)
				for (Object key : event.getKeyList()) {
					InvoiceNoRangeCVO inrCVO = invoiceNoRangeModel.getInvoiceNoRangeCVO( (Long) key);
					Long eventPK = inrCVO.getEventPK();
					ClientInvoiceSearch clientInvoiceSearch = getEntityIfAvailable(eventPK);
					if (clientInvoiceSearch != null) {
						clientInvoiceSearch.initInvoiceFields();
					}
				}

				fireDataChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};

	// InvoiceSearch does not support Participant Custom Fields yet
//	private CacheModelListener customFieldModelListener = new CacheModelListener() {
//		@Override
//		public void dataChange(CacheModelEvent event) {
//			if (!serverModel.isLoggedIn()) {
//				return;
//			}
//
//			try {
//				// init Invoice fields in all affected ClientInvoiceSearch (with the same eventPK as the Invoice No Range)
//				for (Object key : event.getKeyList()) {
//					ParticipantCustomField customField = customFieldModel.getParticipantCustomField( (Long) key);
//					Long eventPK = customField.getEventPK();
//					ClientInvoiceSearch clientInvoiceSearch = getEntityIfAvailable(eventPK);
//					if (clientInvoiceSearch != null) {
//						clientInvoiceSearch.initCustomSQLFields();
//					}
//				}
//
//				fireDataChange();
//			}
//			catch (Exception e) {
//				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//			}
//		}
//	};


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(ClientInvoiceSearch entity) {
		Long key = entity.getEventPK();
		if (key == null) {
			key = NO_EVENT_KEY;
		}
		return key;
	}


	@Override
	protected ClientInvoiceSearch getEntityFromServer(Long eventPK) throws Exception {
		ClientInvoiceSearch invoiceSearch = null;
		try {
			if (eventPK == NO_EVENT_KEY || eventPK == null) {
				invoiceSearch = new ClientInvoiceSearch(null /*eventPK*/);
			}
			else {
				invoiceSearch = new ClientInvoiceSearch(eventPK);

				/* Register also as FK-Listener because we want to know if the list of Participant Types for this event
				 * changes. But first we de-register in case that we registered already earlier.
				 */
				participantTypeModel.removeForeignKeyListener(participantTypeModelListener, eventPK);
				participantTypeModel.addForeignKeyListener(participantTypeModelListener, eventPK);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return invoiceSearch;
	}


	public ClientInvoiceSearch getInvoiceSearch(Long eventPK) throws Exception {
		ClientInvoiceSearch invoiceSearch = null;
		if (eventPK != null) {
			invoiceSearch = getEntity(eventPK);
		}
		return invoiceSearch;
	}

}
