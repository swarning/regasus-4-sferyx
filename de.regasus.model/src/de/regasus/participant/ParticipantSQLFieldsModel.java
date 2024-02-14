package de.regasus.participant;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.CountryModel;
import de.regasus.core.CreditCardTypeModel;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MICacheModel;
import de.regasus.email.EmailTemplateModel;
import de.regasus.event.EventModel;
import de.regasus.finance.InvoiceNoRangeModel;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.programme.ProgrammeOfferingModel;
import de.regasus.programme.ProgrammePointModel;

/**
 * Model that manages {@link ClientParticipantSearch}.
 * Client can get instances of {@link ClientParticipantSearch} for certain Events.
 * To get an Event-independent instance, use the dummy Long {@link ParticipantSQLFieldsModel#NO_EVENT_KEY}.
 */
public class ParticipantSQLFieldsModel
extends MICacheModel<Long, ClientParticipantSearch> {

	private static ParticipantSQLFieldsModel singleton = null;

	public static final Long NO_EVENT_KEY = 0L;

	// Models with global data (independent from Event)
	private LanguageModel languageModel;
	private CountryModel countryModel;
	private CreditCardTypeModel creditCardTypeModel;
	private ParticipantStateModel participantStateModel;
	private HotelModel hotelModel;
	/* It is not necessary to observe ProfileCustomFieldGroupModel, because if the Group of a ProfileCustomField
	 * changes, ProfileCustomFieldModel will fire a CacheModelEvent, too.
	 */
	private ProfileCustomFieldModel profileCustomFieldModel;
	private ProfileCustomFieldGroupModel profileCustomFieldGroupModel;
	private ConfigParameterSetModel configParameterSetModel;

	// Models with data that belongs to a single Event
	private ParticipantTypeModel participantTypeModel;
	private InvoiceNoRangeModel invoiceNoRangeModel;
	private ProgrammePointModel programmePointModel;
	private ProgrammeOfferingModel programmeOfferingModel;
	private EmailTemplateModel emailTemplateModel;
	private HotelContingentModel hotelContingentModel;
	private EventModel eventModel;
	/* It is not necessary to observe ParticipantCustomFieldGroupModel, because if the Group of a ParticipantCustomField
	 * changes, ParticipantCustomFieldModel will fire a CacheModelEvent, too.
	 */
	private ParticipantCustomFieldModel participantCustomFieldModel;
	private ParticipantCustomFieldGroupModel participantCustomFieldGroupModel;


	private ParticipantSQLFieldsModel() {
	}


	public static ParticipantSQLFieldsModel getInstance() {
		if (singleton == null) {
			singleton = new ParticipantSQLFieldsModel();
			singleton.init();
		}
		return singleton;
	}


	private void init() {
		// init Models with global data (independent from Event)

		languageModel = LanguageModel.getInstance();
		languageModel.addListener(cacheModelListener);

		countryModel = CountryModel.getInstance();
		countryModel.addListener(cacheModelListener);

		creditCardTypeModel = CreditCardTypeModel.getInstance();
		creditCardTypeModel.addListener(cacheModelListener);

		participantStateModel = ParticipantStateModel.getInstance();
		participantStateModel.addListener(cacheModelListener);

		hotelModel = HotelModel.getInstance();
		hotelModel.addListener(cacheModelListener);

		profileCustomFieldModel = ProfileCustomFieldModel.getInstance();
		profileCustomFieldModel.addListener(cacheModelListener);

		profileCustomFieldGroupModel = ProfileCustomFieldGroupModel.getInstance();
		profileCustomFieldGroupModel.addListener(cacheModelListener);

		configParameterSetModel = ConfigParameterSetModel.getInstance();
		// observing the ConfigParameterSetModel is not necessary


		// init Models with data that belongs to a single Event

		participantTypeModel = ParticipantTypeModel.getInstance();
		participantTypeModel.addListener(cacheModelListener);

		invoiceNoRangeModel = InvoiceNoRangeModel.getInstance();
		invoiceNoRangeModel.addListener(cacheModelListener);

		programmePointModel = ProgrammePointModel.getInstance();
		programmePointModel.addListener(cacheModelListener);

		programmeOfferingModel = ProgrammeOfferingModel.getInstance();
		programmeOfferingModel.addListener(cacheModelListener);

		emailTemplateModel = EmailTemplateModel.getInstance();
		emailTemplateModel.addListener(cacheModelListener);

		hotelContingentModel = HotelContingentModel.getInstance();
		hotelContingentModel.addListener(cacheModelListener);

		eventModel = EventModel.getInstance();
		eventModel.addListener(cacheModelListener);

		participantCustomFieldModel = ParticipantCustomFieldModel.getInstance();
		participantCustomFieldModel.addListener(cacheModelListener);

		participantCustomFieldGroupModel = ParticipantCustomFieldGroupModel.getInstance();
		participantCustomFieldGroupModel.addListener(cacheModelListener);
	}


	private CacheModelListener cacheModelListener = new CacheModelListener() {
		@Override
		public void dataChange(CacheModelEvent event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				// Models with global data (independent from Event)
				if (event.getSource() == countryModel) {
					// init Country values in all ClientParticipantSearch
					Collection<ClientParticipantSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientParticipantSearch clientParticipantSearch : loadedData) {
						clientParticipantSearch.initCountryValues();
					}
				}
				else if (event.getSource() == languageModel) {
					// init Language values in all ClientParticipantSearch
					Collection<ClientParticipantSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientParticipantSearch clientParticipantSearch : loadedData) {
						clientParticipantSearch.initLanguageValues();
					}
				}
				else if (event.getSource() == creditCardTypeModel) {
					// init Credit Card Type values in all ClientParticipantSearch
					Collection<ClientParticipantSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientParticipantSearch clientParticipantSearch : loadedData) {
						clientParticipantSearch.initCreditCardTypeValues();
					}
				}
				else if (event.getSource() == participantStateModel) {
					// init Participant State fields in all ClientParticipantSearch
					Collection<ClientParticipantSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientParticipantSearch clientParticipantSearch : loadedData) {
						clientParticipantSearch.initParticipantStateFields();
					}
				}
				else if (event.getSource() == hotelModel) {
					// init Hotel fields in all ClientParticipantSearch
					Collection<ClientParticipantSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientParticipantSearch clientParticipantSearch : loadedData) {
						clientParticipantSearch.initHotelFields();
					}
				}
				else if (  event.getSource() == profileCustomFieldModel
						|| event.getSource() == profileCustomFieldGroupModel
				) {
					// init Profile Custom Fields in all ClientParticipantSearch
					for (ClientParticipantSearch clientParticipantSearch : getLoadedAndCachedEntities()) {
						clientParticipantSearch.initProfileCustomFieldSQLFields();
					}
				}
				// Models with data that belongs to a single Event
				else if (event.getSource() == participantTypeModel) {
					/* Either the data Participant Type changed or the relation between a Participant Type and an Event.
					 * In both cases we cannot find out which Events are affected. So we have to init the Participant
					 * Type fields in all ClientParticipantSearch.
					 */
					Collection<ClientParticipantSearch> loadedData = getLoadedAndCachedEntities();
					for (ClientParticipantSearch clientParticipantSearch : loadedData) {
						clientParticipantSearch.initParticipantTypeFields();
					}
				}
				else if (event.getSource() == invoiceNoRangeModel) {
					// init Invoice fields in all affected ClientParticipantSearch (with the same eventPK as the Invoice No Range)
					for (Object key : event.getKeyList()) {
						InvoiceNoRangeCVO inrCVO = invoiceNoRangeModel.getInvoiceNoRangeCVO( (Long) key);
						Long eventPK = inrCVO.getEventPK();
						ClientParticipantSearch clientParticipantSearch = getEntityIfAvailable(eventPK);
						if (clientParticipantSearch != null) {
							clientParticipantSearch.initInvoiceFields();
						}
					}
				}
				else if (event.getSource() == programmePointModel) {
					// init Programme fields in all affected ClientParticipantSearch (with the same eventPK as the Programme Point)
					for (Object key : event.getKeyList()) {
						ProgrammePointCVO ppCVO = programmePointModel.getProgrammePointCVO( (Long) key);
						Long eventPK = ppCVO.getEventPK();
						ClientParticipantSearch clientParticipantSearch = getEntityIfAvailable(eventPK);
						if (clientParticipantSearch != null) {
							clientParticipantSearch.initProgrammeFields();
						}
					}
				}
				else if (event.getSource() == programmeOfferingModel) {
					// init Programme fields in all affected ClientParticipantSearch (with the same eventPK as the Programme Offering)
					for (Object key : event.getKeyList()) {
						ProgrammeOfferingCVO poCVO = programmeOfferingModel.getProgrammeOfferingCVO( (Long) key);
						Long ppPK = poCVO.getVO().getProgrammePointPK();
						ProgrammePointCVO ppCVO = programmePointModel.getProgrammePointCVO(ppPK);
						Long eventPK = ppCVO.getEventPK();
						ClientParticipantSearch clientParticipantSearch = getEntityIfAvailable(eventPK);
						if (clientParticipantSearch != null) {
							clientParticipantSearch.initProgrammeFields();
						}
					}
				}
				else if (event.getSource() == hotelContingentModel) {
					// init Hotel fields in all affected ClientParticipantSearch (with the same eventPK as the Hotel Contingent)
					for (Object key : event.getKeyList()) {
						HotelContingentCVO ppCVO = hotelContingentModel.getHotelContingentCVO( (Long) key);
						Long eventPK = ppCVO.getEventPK();
						ClientParticipantSearch clientParticipantSearch = getEntityIfAvailable(eventPK);
						if (clientParticipantSearch != null) {
							clientParticipantSearch.initHotelFields();
						}
					}
				}
				else if (event.getSource() == emailTemplateModel) {
					// init Email fields in all affected ClientParticipantSearch (with the same eventPK as the Email Template)
					for (Object key : event.getKeyList()) {
						EmailTemplate emailTemplate = emailTemplateModel.getEmailTemplate( (Long) key);
						Long eventPK = emailTemplate.getEventPK();
						ClientParticipantSearch clientParticipantSearch = getEntityIfAvailable(eventPK);
						if (clientParticipantSearch != null) {
							clientParticipantSearch.initEmailFields();
						}
					}
				}
				else if (event.getSource() == eventModel) {
					// init Event in all affected ClientParticipantSearch (with the same eventPK as the Event)
					List<?> eventPKs = event.getKeyList();
					for (ClientParticipantSearch clientParticipantSearch : getLoadedAndCachedEntities()) {
						if (eventPKs.contains(clientParticipantSearch.getEventPK())) {
							clientParticipantSearch.initEvent();
						}
					}
				}
				else if (event.getSource() == participantCustomFieldGroupModel) {
					// init CustomFieldSQL fields in all affected ClientParticipantSearch (with the same eventPK as the Participant Custom Field Group)

					// determine Event PKs of affected ParticipantCustomFieldGroup
					Set<Long> eventPKs = new HashSet<>();
					for (Object key : event.getKeyList()) {
						ParticipantCustomFieldGroup customFieldGroup = participantCustomFieldGroupModel.getParticipantCustomFieldGroup( (Long) key);
						eventPKs.add( customFieldGroup.getEventPK() );
					}

					// refresh CustomField of affected Events
					for (Long eventPK : eventPKs) {
						ClientParticipantSearch clientParticipantSearch = getEntityIfAvailable(eventPK);
						if (clientParticipantSearch != null) {
							clientParticipantSearch.initCustomFieldSQLFields();
						}
					}
				}
				else if (event.getSource() == participantCustomFieldModel) {
					// init CustomFieldSQL fields in all affected ClientParticipantSearch (with the same eventPK as the Participant Custom Field)

					// determine Event PKs of affected ParticipantCustomField
					Set<Long> eventPKs = new HashSet<>();
					for (Object key : event.getKeyList()) {
						ParticipantCustomField customField = participantCustomFieldModel.getParticipantCustomField( (Long) key);
						eventPKs.add( customField.getEventPK() );
					}

					// refresh CustomField of affected Events
					for (Long eventPK : eventPKs) {
						ClientParticipantSearch clientParticipantSearch = getEntityIfAvailable(eventPK);
						if (clientParticipantSearch != null) {
							clientParticipantSearch.initCustomFieldSQLFields();
						}
					}
				}

				fireDataChange();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(ClientParticipantSearch entity) {
		Long key = entity.getEventPK();
		if (key == null) {
			key = NO_EVENT_KEY;
		}
		return key;
	}


	@Override
	protected ClientParticipantSearch getEntityFromServer(Long eventPK) throws Exception {
		ClientParticipantSearch participantSearch = null;

		if (eventPK == NO_EVENT_KEY || eventPK == null) {
			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
			participantSearch = new ClientParticipantSearch(null /*eventPK*/, configParameterSet);
		}
		else {
			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);
			participantSearch = new ClientParticipantSearch(eventPK, configParameterSet);

			/* Register also as FK-Listener because we want to know if the list of Participant Types for this event
			 * changes. But first we de-register in case that we registered already earlier.
			 */
			participantTypeModel.removeForeignKeyListener(cacheModelListener, eventPK);
			participantTypeModel.addForeignKeyListener(cacheModelListener, eventPK);
		}

		/* We don't register as FK-Listener at invoiceNoRangeModel, because
		 * the FK of an INR never changes.
		 */

		return participantSearch;
	}


	public ClientParticipantSearch getParticipantSearch(Long eventPK) throws Exception {
		ClientParticipantSearch participantSearch = null;
		if (eventPK != null) {
			participantSearch = getEntity(eventPK);
		}
		return participantSearch;
	}

}
