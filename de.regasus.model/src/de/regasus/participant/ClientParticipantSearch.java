package de.regasus.participant;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.util.exception.ErrorMessageException;

import de.regasus.email.EmailTemplateSearchValuesProvider;
import de.regasus.event.EventModel;
import de.regasus.finance.InvoiceNoRangeSearchValuesProvider;
import de.regasus.hotel.HotelContingentSearchValuesProvider;
import de.regasus.hotel.HotelSearchValuesProvider;
import de.regasus.person.ClientPersonSearch;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.programme.ProgrammeOfferingSearchValuesProvider;
import de.regasus.programme.ProgrammePointSearchValuesProvider;
import de.regasus.programme.ProgrammePointWithWorkGroupsSearchValuesProvider;

public class ClientParticipantSearch extends ParticipantSearch {

	private static final long serialVersionUID = 1L;

	protected ParticipantStateSearchValuesProvider participantStateSearchValuesProvider;
	protected ParticipantTypeSearchValuesProvider participantTypeSearchValuesProvider;
	protected EmailTemplateSearchValuesProvider emailTemplateSearchValuesProvider;
	protected ProgrammePointSearchValuesProvider programmePointSearchValuesProvider;
	protected ProgrammePointSearchValuesProvider programmePointWithWaitListSearchValuesProvider;
	protected ProgrammePointSearchValuesProvider programmePointWithEmptyValueSearchValuesProvider;
	protected ProgrammePointWithWorkGroupsSearchValuesProvider programmePointWithWorkGroupsSearchValuesProvider;
	protected ProgrammeOfferingSearchValuesProvider programmeOfferingSearchValuesProvider;
	protected ProgrammeOfferingSearchValuesProvider programmeOfferingWithWaitListSearchValuesProvider;
	protected InvoiceNoRangeSearchValuesProvider invoiceNoRangeSearchValuesProvider;
	protected HotelSearchValuesProvider hotelSearchValuesProvider;
	protected HotelContingentSearchValuesProvider hotelContingentSearchValuesProvider;

	static {
		ClientPersonSearch.initStaticFields();
	}


	public ClientParticipantSearch(Long eventPK, ConfigParameterSet configParameterSet) throws Exception {
		super(eventPK, configParameterSet);

		participantStateSearchValuesProvider = new ParticipantStateSearchValuesProvider();
		PARTICIPANT_STATE.setSearchValuesProvider(participantStateSearchValuesProvider);

		participantTypeSearchValuesProvider = new ParticipantTypeSearchValuesProvider(eventPK);
		PARTICIPANT_TYPE.setSearchValuesProvider(participantTypeSearchValuesProvider);
		COMPANIONS.setSearchValuesProvider(participantTypeSearchValuesProvider);

		emailTemplateSearchValuesProvider = new EmailTemplateSearchValuesProvider(eventPK);
		HAS_RECEIVED_EMAIL_TEMPLATE.setSearchValuesProvider(emailTemplateSearchValuesProvider);
		HAS_SCHEDULED_OR_RECEIVED_EMAIL_TEMPLATE.setSearchValuesProvider(emailTemplateSearchValuesProvider);
		HAS_SCHEDULED_EMAIL_TEMPLATE.setSearchValuesProvider(emailTemplateSearchValuesProvider);

		if (eventPK != null) {
			if (   HAS_PROGRAMME_POINT != null
				|| HAS_CANCELLED_PROGRAMME_POINT_WITH_FEE != null
				|| HAS_CANCELLED_PROGRAMME_POINT_WITHOUT_FEE != null
			) {
				programmePointSearchValuesProvider = new ProgrammePointSearchValuesProvider(
					eventPK,
					false, // withEmptyValue
					false // only WaitList
				);

				if (HAS_PROGRAMME_POINT != null) {
					HAS_PROGRAMME_POINT.setSearchValuesProvider(programmePointSearchValuesProvider);
				}

				if (HAS_CANCELLED_PROGRAMME_POINT_WITH_FEE != null) {
					HAS_CANCELLED_PROGRAMME_POINT_WITH_FEE.setSearchValuesProvider(programmePointSearchValuesProvider);
				}

				if (HAS_CANCELLED_PROGRAMME_POINT_WITHOUT_FEE != null) {
					HAS_CANCELLED_PROGRAMME_POINT_WITHOUT_FEE.setSearchValuesProvider(programmePointSearchValuesProvider);
				}
			}

			if (HAS_PROGRAMME_POINT_ON_WAIT_LIST != null) {
				programmePointWithWaitListSearchValuesProvider = new ProgrammePointSearchValuesProvider(
					eventPK,
					false, // withEmptyValue
					true // only WaitList
				);
				HAS_PROGRAMME_POINT_ON_WAIT_LIST.setSearchValuesProvider(programmePointWithWaitListSearchValuesProvider);
			}

			if (HAS_LEAD != null) {
				programmePointWithEmptyValueSearchValuesProvider = new ProgrammePointSearchValuesProvider(
					eventPK,
					true, // withEmptyValue
					false // only WaitList
				);
				HAS_LEAD.setSearchValuesProvider(programmePointWithEmptyValueSearchValuesProvider);
			}

			if (HAS_OPEN_WORK_GROUP != null) {
				programmePointWithWorkGroupsSearchValuesProvider =
					new ProgrammePointWithWorkGroupsSearchValuesProvider(eventPK);
				HAS_OPEN_WORK_GROUP.setSearchValuesProvider(programmePointWithWorkGroupsSearchValuesProvider);
			}

			if (   HAS_PROGRAMME_OFFERING != null
				|| HAS_CANCELLED_PROGRAMME_OFFERING_WITH_FEE != null
				|| HAS_CANCELLED_PROGRAMME_OFFERING_WITHOUT_FEE != null
			) {
				programmeOfferingSearchValuesProvider = new ProgrammeOfferingSearchValuesProvider(
					eventPK,
					false // onlyWaitList
				);

				if (HAS_PROGRAMME_OFFERING != null) {
					HAS_PROGRAMME_OFFERING.setSearchValuesProvider(programmeOfferingSearchValuesProvider);
				}

				if (HAS_CANCELLED_PROGRAMME_OFFERING_WITH_FEE != null) {
					HAS_CANCELLED_PROGRAMME_OFFERING_WITH_FEE.setSearchValuesProvider(programmeOfferingSearchValuesProvider);
				}

				if (HAS_CANCELLED_PROGRAMME_OFFERING_WITHOUT_FEE != null) {
					HAS_CANCELLED_PROGRAMME_OFFERING_WITHOUT_FEE.setSearchValuesProvider(programmeOfferingSearchValuesProvider);
				}
			}

			if (HAS_PROGRAMME_OFFERING_ON_WAIT_LIST != null) {
				programmeOfferingWithWaitListSearchValuesProvider = new ProgrammeOfferingSearchValuesProvider(
					eventPK,
					true // onlyWaitList
				);
				HAS_PROGRAMME_OFFERING_ON_WAIT_LIST.setSearchValuesProvider(programmeOfferingWithWaitListSearchValuesProvider);
			}

			if (HAS_HOTEL_BOOKING_FOR_HOTEL != null) {
				hotelSearchValuesProvider = new HotelSearchValuesProvider(eventPK);

				HAS_HOTEL_BOOKING_FOR_HOTEL.setSearchValuesProvider(hotelSearchValuesProvider);
				HAS_APPLIED_HOTEL_COST_COVERAGE_FOR_HOTEL.setSearchValuesProvider(hotelSearchValuesProvider);
				HAS_AVAILABLE_HOTEL_COST_COVERAGE_FOR_HOTEL.setSearchValuesProvider(hotelSearchValuesProvider);
			}

			if (HAS_HOTEL_BOOKING_FOR_HOTEL_CONTINGENT != null) {
				hotelContingentSearchValuesProvider = new HotelContingentSearchValuesProvider(eventPK);

				HAS_HOTEL_BOOKING_FOR_HOTEL_CONTINGENT.setSearchValuesProvider(hotelContingentSearchValuesProvider);
				HAS_APPLIED_HOTEL_COST_COVERAGE_FOR_HOTEL_CONTINGENT.setSearchValuesProvider(hotelContingentSearchValuesProvider);
				HAS_AVAILABLE_HOTEL_COST_COVERAGE_FOR_HOTEL_CONTINGENT.setSearchValuesProvider(hotelContingentSearchValuesProvider);
			}
		}



		if (HAS_INVOICE != null ||
			HAS_CLOSED_INVOICE != null ||
			HAS_UNCLOSED_INVOICE != null ||
			HAS_UNDERPAYED_INVOICE != null ||
			HAS_OVERPAYED_INVOICE != null ||
			HAS_UNBALANCED_INVOICE != null ||
			HAS_CLOSED_UNPRINTED_INVOICE != null ||
			HAS_REMINDERS != null
		) {
			invoiceNoRangeSearchValuesProvider = new InvoiceNoRangeSearchValuesProvider(
				true, // withYesKey
				eventPK
			);

			if (HAS_INVOICE != null) {
				HAS_INVOICE.setSearchValuesProvider(invoiceNoRangeSearchValuesProvider);
			}
			if (HAS_CLOSED_INVOICE != null) {
				HAS_CLOSED_INVOICE.setSearchValuesProvider(invoiceNoRangeSearchValuesProvider);
			}
			if (HAS_UNCLOSED_INVOICE != null) {
				HAS_UNCLOSED_INVOICE.setSearchValuesProvider(invoiceNoRangeSearchValuesProvider);
			}
			if (HAS_UNDERPAYED_INVOICE != null) {
				HAS_UNDERPAYED_INVOICE.setSearchValuesProvider(invoiceNoRangeSearchValuesProvider);
			}
			if (HAS_OVERPAYED_INVOICE != null) {
				HAS_OVERPAYED_INVOICE.setSearchValuesProvider(invoiceNoRangeSearchValuesProvider);
			}
			if (HAS_UNBALANCED_INVOICE != null) {
				HAS_UNBALANCED_INVOICE.setSearchValuesProvider(invoiceNoRangeSearchValuesProvider);
			}
			if (HAS_CLOSED_UNPRINTED_INVOICE != null) {
				HAS_CLOSED_UNPRINTED_INVOICE.setSearchValuesProvider(invoiceNoRangeSearchValuesProvider);
			}
			if (HAS_REMINDERS != null) {
				HAS_REMINDERS.setSearchValuesProvider(invoiceNoRangeSearchValuesProvider);
			}
		}
	}


	@Override
	protected void initEventPK(Long eventPK) throws Exception {
		super.initEventPK(eventPK);

		if (participantTypeSearchValuesProvider != null) {
			participantTypeSearchValuesProvider.setEventPK(eventPK);
		}

		if (emailTemplateSearchValuesProvider != null) {
			emailTemplateSearchValuesProvider.setEventPK(eventPK);
		}

		if (programmePointSearchValuesProvider != null) {
			programmePointSearchValuesProvider.setEventPK(eventPK);
		}

		if (programmePointWithWaitListSearchValuesProvider != null) {
			programmePointWithWaitListSearchValuesProvider.setEventPK(eventPK);
		}

		if (programmePointWithWorkGroupsSearchValuesProvider != null) {
			programmePointWithWorkGroupsSearchValuesProvider.setEventPK(eventPK);
		}

		if (programmeOfferingSearchValuesProvider != null) {
			programmeOfferingSearchValuesProvider.setEventPK(eventPK);
		}

		if (programmeOfferingWithWaitListSearchValuesProvider != null) {
			programmeOfferingWithWaitListSearchValuesProvider.setEventPK(eventPK);
		}

		if (invoiceNoRangeSearchValuesProvider != null) {
			invoiceNoRangeSearchValuesProvider.setEventPK(eventPK);
		}

		if (hotelSearchValuesProvider != null) {
			hotelSearchValuesProvider.setEventPK(eventPK);
		}

		if (hotelContingentSearchValuesProvider != null) {
			hotelContingentSearchValuesProvider.setEventPK(eventPK);
		}
	}


	@Override
	public EventVO getEventVO() throws Exception {
		if (eventVO == null && eventPK != null) {
			eventVO = EventModel.getInstance().getEventVO(eventPK);
		}

		return eventVO;
	}


	@Override
	protected List<ProgrammeOfferingCVO> getProgrammeOfferingCVOsByEventPK()
	throws Exception {
		return super.getProgrammeOfferingCVOsByEventPK();

		/*
		 * Unfortunately, there is not yet an efficient way to load all PPs and POs through a Model.
		 * Although all PPs of an Event can be loaded, not all POs of an Event can be loaded.
		 */

		// Long eventPkAsLong = DefaultPK.getLongValue(eventPK);
		//
		// ProgrammePointModel ppModel = ProgrammePointModel.getInstance();
		// List<ProgrammePointVO> programmePointVOs = ppModel.getProgrammePointVOsByEventPK(eventPkAsLong);
		//
		// ProgrammeOfferingModel programmeOfferingModel = ProgrammeOfferingModel.getInstance();
		// programmeOfferingModel.getProgrammeOfferingVOsByProgrammePointPK(programmePointPK)
		//
		// // get ProgrammeOfferingCVOs (including ProgrammePointVOs) of the Event
		// ProgrammeOfferingCVOSettings programmeOfferingCVOSettings = new ProgrammeOfferingCVOSettings();
		// programmeOfferingCVOSettings.withParticipantTypeName = true;
		// programmeOfferingCVOSettings.programmePointCVOSettings = new ProgrammePointCVOSettings();
		//
		// IProgrammeOfferingManager poMgr = getProgrammeOfferingMgr();
		// List<ProgrammeOfferingCVO> programmeOfferingCVOs = poMgr.getProgrammeOfferingCVOsByEventPK(
		// eventPK,
		// null, /*referenceDate*/
		// false, /*onlyValid*/
		// false, /*onlyUseInOnlineForm*/
		// programmeOfferingCVOSettings
		// );
		//
		// return programmeOfferingCVOs;
	}


	@Override
	protected List<ParticipantCustomField> getParticipantCustomFields(Long eventID)
	throws ErrorMessageException {
		try {
			ParticipantCustomFieldModel model = ParticipantCustomFieldModel.getInstance();
			List<ParticipantCustomField> customFields = model.getParticipantCustomFieldsByEventPK(eventID);

			// copy List, because it could be unmodifiable
			customFields = createArrayList(customFields);

			return customFields;
		}
		catch (Exception e) {
			throw new ErrorMessageException(e);
		}
	}


	@Override
	protected List<ProfileCustomField> getProfileCustomFields() throws Exception {
		ProfileCustomFieldModel model = ProfileCustomFieldModel.getInstance();
		Collection<ProfileCustomField> customFields = model.getAllProfileCustomFields();

		// copy List, because it could be unmodifiable
		customFields = createArrayList(customFields);

		return createArrayList(customFields);
	}

}
