package de.regasus.participant.editor.history;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.StringHelper.NEW_LINE;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.Bank;
import com.lambdalogic.messeinfo.contact.Communication;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.CreditCard;
import com.lambdalogic.messeinfo.contact.CreditCardAlias;
import com.lambdalogic.messeinfo.contact.CustomFieldFormatter;
import com.lambdalogic.messeinfo.contact.CustomFieldType;
import com.lambdalogic.messeinfo.contact.data.AddressVO;
import com.lambdalogic.messeinfo.contact.data.BankVO;
import com.lambdalogic.messeinfo.contact.data.CommunicationVO;
import com.lambdalogic.messeinfo.contact.data.CreditCardAliasVO;
import com.lambdalogic.messeinfo.contact.data.CreditCardVO;
import com.lambdalogic.messeinfo.contact.data.Membership;
import com.lambdalogic.messeinfo.contact.data.MembershipVO;
import com.lambdalogic.messeinfo.contact.data.PersonVO;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.kernel.AbstractEntity2EditTimeComparator;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO_EditTime_Comparator;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldValue;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.HotelCostCoverage;
import com.lambdalogic.messeinfo.participant.data.ParticipantVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.error.ErrorHandler;

import de.regasus.I18N;
import de.regasus.common.AddressRole;
import de.regasus.common.Country;
import de.regasus.common.Language;
import de.regasus.core.CountryModel;
import de.regasus.core.LanguageModel;
import de.regasus.event.EventModel;
import de.regasus.event.ParticipantType;
import de.regasus.history.FieldChangeGroup;
import de.regasus.history.IHistoryEvent;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.participant.ParticipantHistoryContainer;
import de.regasus.participant.ParticipantHistoryModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.participant.editor.ParticipantEditor;

/**
 * Analyses the relative changes between the current ParticipantVO and the newest of the ParticipantHistoryVOs,
 * as well as (within the list of ParticipantHistoryVO) those between each newer and its older version, and puts
 * all those relative changes each in one EditParticipantEvent, which may be shown presented in any sort of UI (currently
 * in HTML, see HistoryEventList2HmlConverter).
 * <p>
 * As so many helper classes, this one has just static methods.
 */
class ParticipantHistoryEventHelper {

	private static final long TIME_TOLERANCE = 3000;

	private static final String CUSTOM_FIELD_GROUP_NAME = ContactLabel.CustomFields.getString();


	/**
	 * Constructs the list of events that is shown on the history view of the {@link ParticipantEditor}, starting
	 * with a {@link CreateParticipantEvent}, followed by {@link EditParticipantEvent}s which are constructed from
	 * the deltas between the records in the history table, respectively from the delta between the current participant and the most
	 * recent record in the history table.
	 *
	 */
	public static List<IHistoryEvent> createParticipantHistoryEventList(Participant participant)
	throws Exception {
		List<IHistoryEvent> eventList = CollectionsHelper.createArrayList();

		// add CreateParticipantEvent
		CreateParticipantEvent createParticipantEvent = new CreateParticipantEvent(
			participant.getNewTime(),
			participant.getNewDisplayUserStr()
		);
		eventList.add(createParticipantEvent);

		// add EditParticipantEvents
		ParticipantHistoryModel phModel = ParticipantHistoryModel.getInstance();
		ParticipantHistoryContainer participantHistoryContainer = phModel.getParticipantHistory(participant.getID());
		List<ParticipantVO> participantHistoryVOs = participantHistoryContainer.getParticipantHistoryVOs();

		// sort chronologically
		Collections.sort(participantHistoryVOs, AbstractVO_EditTime_Comparator.getInstance());

		if (notEmpty(participantHistoryVOs)) {
			int count = participantHistoryVOs.size();

			// add the deltas between the records in the history table
			for (int i = 1; i < count; i++) {
				EditParticipantEvent editParticipantEvent = createEditParticipantEvent(
					participantHistoryVOs.get(i - 1),
					participantHistoryVOs.get(i)
				);

				// Avoid empty lines in GUI (MIRCP-2127)
				if (! editParticipantEvent.isEmpty()) {
					eventList.add(editParticipantEvent);
				}
			}

			// add the delta between the current participant and the most
			// recent record in the history table
			EditParticipantEvent editParticipantEvent = createEditParticipantEvent(
				participantHistoryVOs.get(count - 1),
				participant.getParticipantVO()
			);

			// Avoid empty lines in GUI (MIRCP-2127)
			if (! editParticipantEvent.isEmpty()) {
				eventList.add(editParticipantEvent);
			}
		}

		addCustomFieldValues(participant, participantHistoryContainer, eventList);

		return eventList;
	}


	/**
	 * Creates and fills an {@link EditParticipantEvent} with several {@link FieldChangeGroup}s from the differences in various aspects of two Participants.
	 */
	private static EditParticipantEvent createEditParticipantEvent(ParticipantVO oldVO, ParticipantVO newVO) {
		EditParticipantEvent editParticipantEvent = new EditParticipantEvent(
			newVO.getEditTime(),
			newVO.getEditDisplayUserStr()
		);

		editParticipantEvent.add( createPersonFieldChangeGroup(oldVO, newVO) );
		editParticipantEvent.add( createParticipantFieldChangeGroup(oldVO, newVO) );
		editParticipantEvent.add( createMembershipFieldChangeGroup(oldVO.getMembership(), newVO.getMembership()) );
		editParticipantEvent.add( createHotelCostCoverageFieldChangeGroup(oldVO.getHotelCostCoverage(), newVO.getHotelCostCoverage()) );
		editParticipantEvent.add( createCommunicationFieldChangeGroup(oldVO.getCommunicationVO(), newVO.getCommunicationVO()) );

		for (int idx=1; idx<= 4; idx++) {
			FieldChangeGroup fieldChangeGroup = createAddressFieldChangeGroup(
				idx,
				oldVO.getAddressVO(idx),
				oldVO.getMainAddressNumber(),
				oldVO.getInvoiceAddressNumber(),
				newVO.getAddressVO(idx),
				newVO.getMainAddressNumber(),
				newVO.getInvoiceAddressNumber()
			);
			editParticipantEvent.add(fieldChangeGroup);
		}
		editParticipantEvent.add(createCreditCardFieldChangeGroup(oldVO.getCreditCardVO(), newVO.getCreditCardVO()));
		editParticipantEvent.add(createCreditCardAliasFieldChangeGroup(oldVO.getCreditCardAliasVO(), newVO.getCreditCardAliasVO()));
		editParticipantEvent.add(createBankFieldChangeGroup(oldVO.getBankVO(), newVO.getBankVO()));
		editParticipantEvent.add(createCustomFieldFieldChangeGroup(oldVO, newVO));

		return editParticipantEvent;
	}


	private static FieldChangeGroup createPersonFieldChangeGroup(PersonVO oldVO, PersonVO newVO) {
		FieldChangeGroup fieldChangeGroup = new FieldChangeGroup(ContactLabel.person.getString());

		/** Abstract Person
		 */
		// no personLink, because it is a technical field that users cannot set
		// no syncId, because it is a technical field that users cannot set
		fieldChangeGroup.addIfNeeded(Participant.NOTE, oldVO.getNote(), newVO.getNote());
		fieldChangeGroup.addIfNeeded(UtilI18N.Language, getLanguageName(oldVO.getLanguageCode()), getLanguageName(newVO.getLanguageCode()));
		fieldChangeGroup.addIfNeeded(Participant.CUSTOMER_ACCOUNT_NUMBER, oldVO.getCustomerAccountNumber(), newVO.getCustomerAccountNumber());
		fieldChangeGroup.addIfNeeded(Participant.TAX_ID, oldVO.getTaxId(), newVO.getTaxId());

		fieldChangeGroup.addIfNeeded(Participant.WEB_ID, oldVO.getWebID(), newVO.getWebID());
		fieldChangeGroup.addIfNeeded(Participant.GENDER, oldVO.getGender(), newVO.getGender());
		fieldChangeGroup.addIfNeeded(Participant.DEGREE, oldVO.getDegree(), newVO.getDegree());
		fieldChangeGroup.addIfNeeded(Participant.NOBILITY, oldVO.getNobility(), newVO.getNobility());
		fieldChangeGroup.addIfNeeded(Participant.ADMIN_TITLE, oldVO.getAdminTitle(), newVO.getAdminTitle());
		fieldChangeGroup.addIfNeeded(Participant.FIRST_NAME, oldVO.getFirstName(), newVO.getFirstName());
		fieldChangeGroup.addIfNeeded(Participant.MIDDLE_NAME, oldVO.getMiddleName(), newVO.getMiddleName());
		fieldChangeGroup.addIfNeeded(Participant.NOBILITY_PREFIX, oldVO.getNobilityPrefix(), newVO.getNobilityPrefix());
		fieldChangeGroup.addIfNeeded(Participant.LAST_NAME, oldVO.getLastName(), newVO.getLastName());
		fieldChangeGroup.addIfNeeded(Participant.MANDATE, oldVO.getMandate(), newVO.getMandate());
		fieldChangeGroup.addIfNeeded(Participant.FUNCTION, oldVO.getFunction(), newVO.getFunction());
		fieldChangeGroup.addIfNeeded(Participant.SALUTATION, oldVO.getIndividualSalutation(), newVO.getIndividualSalutation());
		fieldChangeGroup.addIfNeeded(Participant.INVITATION_CARD, oldVO.getIndividualInvitationCard(), newVO.getIndividualInvitationCard());
		fieldChangeGroup.addIfNeeded(Participant.DATE_OF_BIRTH, TypeHelper.toI18NDate(oldVO.getDateOfBirth()), TypeHelper.toI18NDate(newVO.getDateOfBirth()));
		fieldChangeGroup.addIfNeeded(Participant.PLACE_OF_BIRTH, oldVO.getPlaceOfBirth(), newVO.getPlaceOfBirth());
		fieldChangeGroup.addIfNeeded(Participant.NATIONALITY, getCountryName(oldVO.getNationalityPK()), getCountryName(newVO.getNationalityPK()));
		fieldChangeGroup.addIfNeeded(Participant.CUSTOMER_NO, oldVO.getCustomerNo(), newVO.getCustomerNo());
		fieldChangeGroup.addIfNeeded(Participant.CME_NO, oldVO.getCmeNo(), newVO.getCmeNo());
		fieldChangeGroup.addIfNeeded(Participant.PASSPORT_ID, oldVO.getPassportID(), newVO.getPassportID());

		// approvals
		fieldChangeGroup.addIfNeeded(Participant.PRIVACY_ACCEPTED, oldVO.getPrivacyAccepted(), newVO.getPrivacyAccepted());
		fieldChangeGroup.addIfNeeded(Participant.PROMOTION, oldVO.getPromotion(), newVO.getPromotion());
		fieldChangeGroup.addIfNeeded(Participant.PROGRAMME_CONDITIONS_ACCEPTED, oldVO.getProgrammeConditionsAccepted(), newVO.getProgrammeConditionsAccepted());
		fieldChangeGroup.addIfNeeded(Participant.PROGRAMME_CANCEL_CONDITIONS_ACCEPTED, oldVO.getProgrammeCancelConditionsAccepted(), newVO.getProgrammeCancelConditionsAccepted());
		fieldChangeGroup.addIfNeeded(Participant.HOTEL_CONDITIONS_ACCEPTED, oldVO.getHotelConditionsAccepted(), newVO.getHotelConditionsAccepted());
		fieldChangeGroup.addIfNeeded(Participant.HOTEL_CANCEL_CONDITIONS_ACCEPTED, oldVO.getHotelCancelConditionsAccepted(), newVO.getHotelCancelConditionsAccepted());

		// following fields are not regarded, because they are internal:
		// facebookID, facebookToken, twitterID, twitterToken, linkedInID, linkedInToken

		return fieldChangeGroup;
	}


	private static FieldChangeGroup createParticipantFieldChangeGroup(ParticipantVO oldVO, ParticipantVO newVO) {
		FieldChangeGroup fieldChangeGroup = new FieldChangeGroup(ParticipantLabel.Participant.getString());

		// no number, because it cannot change
		fieldChangeGroup.addIfNeeded(Participant.PARTICIPANT_TYPE, getParticipantTypeName(oldVO.getParticipantTypePK()), getParticipantTypeName(newVO.getParticipantTypePK()));
		fieldChangeGroup.addIfNeeded(Participant.PROOF_PROVIDED, oldVO.isProofProvided(), newVO.isProofProvided());
		fieldChangeGroup.addIfNeeded(Participant.VIP, oldVO.isVIP(), newVO.isVIP());
		fieldChangeGroup.addIfNeeded(Participant.WWW_REGISTRATION, oldVO.isWwwRegistration(), newVO.isWwwRegistration());
		fieldChangeGroup.addIfNeeded(Participant.PARTICIPANT_STATE, getParticipantStateName(oldVO.getParticipantStatePK()), getParticipantStateName(newVO.getParticipantStatePK()));
		fieldChangeGroup.addIfNeeded(Participant.REGISTER_DATE, TypeHelper.toI18NDate(oldVO.getRegisterDate()), TypeHelper.toI18NDate(newVO.getRegisterDate()));
		fieldChangeGroup.addIfNeeded(Participant.PROGRAMME_NOTE_TIME, TypeHelper.toI18NDateMinute(oldVO.getProgrammeNoteTime()), TypeHelper.toI18NDateMinute(newVO.getProgrammeNoteTime()));
		fieldChangeGroup.addIfNeeded(Participant.HOTEL_NOTE_TIME, TypeHelper.toI18NDateMinute(oldVO.getHotelNoteTime()), TypeHelper.toI18NDateMinute(newVO.getHotelNoteTime()));
		fieldChangeGroup.addIfNeeded(Participant.CERTIFICATE_PRINT, TypeHelper.toI18NDateMinute(oldVO.getCertificatePrint()), TypeHelper.toI18NDateMinute(newVO.getCertificatePrint()));

		// group manager and companions
		fieldChangeGroup.addIfNeeded(ParticipantLabel.IsGroupManager, oldVO.isGroupManager(), newVO.isGroupManager());
		fieldChangeGroup.addIfNeeded(ParticipantLabel.IsGroupMember, getGroupMemberInfo(oldVO), getGroupMemberInfo(newVO));
		fieldChangeGroup.addIfNeeded(ParticipantLabel.IsCompanion, getCompanionOfInfo(oldVO), getCompanionOfInfo(newVO));
		fieldChangeGroup.addIfNeeded(ParticipantLabel.SecondPerson, getSecondPersonInfo(oldVO), getSecondPersonInfo(newVO));

		fieldChangeGroup.addIfNeeded(Participant.ANONYM, oldVO.isAnonym(), newVO.isAnonym());

		fieldChangeGroup.addIfNeeded(Participant.PREFERRED_PROGRAMME_PAYMENT_TYPE, oldVO.getPreferredProgrammePaymentType(), newVO.getPreferredProgrammePaymentType());
		fieldChangeGroup.addIfNeeded(Participant.PREFERRED_PROGRAMME_PAYMENT_TYPE, oldVO.getPreferredHotelPaymentType(), newVO.getPreferredHotelPaymentType());

		return fieldChangeGroup;
	}


	private static FieldChangeGroup createMembershipFieldChangeGroup(MembershipVO oldVO, MembershipVO newVO) {
		FieldChangeGroup fieldChangeGroup = new FieldChangeGroup(Participant.MEMBERSHIP.getString());

		fieldChangeGroup.addIfNeeded(Membership.STATUS.getLabel(), oldVO.getStatus(), newVO.getStatus());
		fieldChangeGroup.addIfNeeded(Membership.TYPE.getLabel(),   oldVO.getType(),   newVO.getType());
		fieldChangeGroup.addIfNeeded(Membership.BEGIN.getLabel(),  oldVO.getBegin(),  newVO.getBegin());
		fieldChangeGroup.addIfNeeded(Membership.END.getLabel(),    oldVO.getEnd(),    newVO.getEnd());

		return fieldChangeGroup;
	}


	private static FieldChangeGroup createHotelCostCoverageFieldChangeGroup(
		HotelCostCoverage oldCostCoverage,
		HotelCostCoverage newCostCoverage
	) {
		FieldChangeGroup fieldChangeGroup = new FieldChangeGroup( Participant.HOTEL_COST_COVERAGE.getString() );

		fieldChangeGroup.addIfNeeded(
			HotelCostCoverage.NUMBER_OF_NIGHTS.getLabel(),
			oldCostCoverage.getNumberOfNights(),
			newCostCoverage.getNumberOfNights()
		);

		fieldChangeGroup.addIfNeeded(
			HotelLabel.HotelOffering.getString(),
			getHotelOfferingInfo( oldCostCoverage.getOfferingId() ),
			getHotelOfferingInfo( newCostCoverage.getOfferingId() )
		);

		fieldChangeGroup.addIfNeeded(
			UtilI18N.Status,
			oldCostCoverage.getBookingId() == null ? I18N.HotelCostCoverage_StatusText_NotUsed : I18N.HotelCostCoverage_StatusText_Used,
			newCostCoverage.getBookingId() == null ? I18N.HotelCostCoverage_StatusText_NotUsed : I18N.HotelCostCoverage_StatusText_Used
		);

		return fieldChangeGroup;
	}


	private static FieldChangeGroup createCommunicationFieldChangeGroup(CommunicationVO oldVO, CommunicationVO newVO) {
		FieldChangeGroup fieldChangeGroup = new FieldChangeGroup(Participant.COMMUNICATION.getString());
		fieldChangeGroup.addIfNeeded(Communication.PHONE1, oldVO.getPhone1(), newVO.getPhone1());
		fieldChangeGroup.addIfNeeded(Communication.PHONE2, oldVO.getPhone2(), newVO.getPhone2());
		fieldChangeGroup.addIfNeeded(Communication.PHONE3, oldVO.getPhone3(), newVO.getPhone3());
		fieldChangeGroup.addIfNeeded(Communication.MOBILE1, oldVO.getMobile1(), newVO.getMobile1());
		fieldChangeGroup.addIfNeeded(Communication.MOBILE2, oldVO.getMobile2(), newVO.getMobile2());
		fieldChangeGroup.addIfNeeded(Communication.MOBILE3, oldVO.getMobile3(), newVO.getMobile3());
		fieldChangeGroup.addIfNeeded(Communication.FAX1, oldVO.getFax1(), newVO.getFax1());
		fieldChangeGroup.addIfNeeded(Communication.FAX2, oldVO.getFax2(), newVO.getFax2());
		fieldChangeGroup.addIfNeeded(Communication.FAX3, oldVO.getFax3(), newVO.getFax3());
		fieldChangeGroup.addIfNeeded(Communication.EMAIL1, oldVO.getEmail1(), newVO.getEmail1());
		fieldChangeGroup.addIfNeeded(Communication.EMAIL2, oldVO.getEmail2(), newVO.getEmail2());
		fieldChangeGroup.addIfNeeded(Communication.EMAIL3, oldVO.getEmail3(), newVO.getEmail3());
		fieldChangeGroup.addIfNeeded(Communication.WWW, oldVO.getWww(), newVO.getWww());

		return fieldChangeGroup;
	}


	private static FieldChangeGroup createAddressFieldChangeGroup(
		int addressNumber,
		AddressVO oldAddressVO,
		int oldMainAddressNumber,
		int oldInvoiceAddressNumber,
		AddressVO newAddressVO,
		int newMainAddressNumber,
		int newInvoiceAddressNumber
	) {
		FieldChangeGroup fieldChangeGroup = new FieldChangeGroup(ContactLabel.Address.getString() + " " + addressNumber);

		boolean isOldMainAddress = (addressNumber == oldMainAddressNumber);
		boolean isOldInvoiceAddress = (addressNumber == oldInvoiceAddressNumber);

		boolean isNewMainAddress = (addressNumber == newMainAddressNumber);
		boolean isNewInvoiceAddress = (addressNumber == newInvoiceAddressNumber);

		String oldRoles = AddressRole.getRolesText(isOldMainAddress, isOldInvoiceAddress);
		String newRoles = AddressRole.getRolesText(isNewMainAddress, isNewInvoiceAddress);

		fieldChangeGroup.addIfNeeded(ContactLabel.AddressRole, oldRoles, newRoles);
		fieldChangeGroup.addIfNeeded(Address.ADDRESS_TYPE, oldAddressVO.getAddressType().getString(), newAddressVO.getAddressType().getString());
		fieldChangeGroup.addIfNeeded(Address.ORGANISATION, oldAddressVO.getOrganisation(), newAddressVO.getOrganisation());
		fieldChangeGroup.addIfNeeded(Address.DEPARTMENT, oldAddressVO.getDepartment(), newAddressVO.getDepartment());
		fieldChangeGroup.addIfNeeded(Address.ADDRESSEE, oldAddressVO.getAddressee(), newAddressVO.getAddressee());
		fieldChangeGroup.addIfNeeded(Address.FUNCTION, oldAddressVO.getFunction(), newAddressVO.getFunction());
		fieldChangeGroup.addIfNeeded(Address.STREET, oldAddressVO.getStreet(), newAddressVO.getStreet());
		fieldChangeGroup.addIfNeeded(Address.COUNTRY, getCountryName(oldAddressVO.getCountryPK()), getCountryName(newAddressVO.getCountryPK()));
		fieldChangeGroup.addIfNeeded(Address.ZIP, oldAddressVO.getZip(), newAddressVO.getZip());
		fieldChangeGroup.addIfNeeded(Address.CITY, oldAddressVO.getCity(), newAddressVO.getCity());
		fieldChangeGroup.addIfNeeded(Address.STATE, oldAddressVO.getState(), newAddressVO.getState());
		fieldChangeGroup.addIfNeeded(Address.LABEL, oldAddressVO.getLabel(), newAddressVO.getLabel());

		return fieldChangeGroup;
	}


	private static FieldChangeGroup createBankFieldChangeGroup(BankVO oldVO, BankVO newVO) {

		FieldChangeGroup fieldChangeGroup = new FieldChangeGroup(ContactLabel.Banking.getString());
		fieldChangeGroup.addIfNeeded(Bank.BANK_OWNER, oldVO.getBankOwner(), newVO.getBankOwner());
		fieldChangeGroup.addIfNeeded(Bank.BANK_NAME, oldVO.getBankName(), newVO.getBankName());
		fieldChangeGroup.addIfNeeded(Bank.BANK_IDENTIFIER_CODE, oldVO.getBankIdentifierCode(), newVO.getBankIdentifierCode());
		fieldChangeGroup.addIfNeeded(Bank.BANK_ACCOUNT_NUMBER, oldVO.getBankAccountNumber(), newVO.getBankAccountNumber());
		fieldChangeGroup.addIfNeeded(Bank.IBAN, oldVO.getIban(), newVO.getIban());
		fieldChangeGroup.addIfNeeded(Bank.BIC, oldVO.getBic(), newVO.getBic());

		return fieldChangeGroup;
	}


	private static FieldChangeGroup createCreditCardFieldChangeGroup(CreditCardVO oldVO, CreditCardVO newVO) {

		FieldChangeGroup fieldChangeGroup = new FieldChangeGroup( Participant.CREDIT_CARD.getString() );
		fieldChangeGroup.addIfNeeded( CreditCard.CREDIT_CARD_TYPE.getLabel(), oldVO.getCreditCardTypeName(), newVO.getCreditCardTypeName());
		fieldChangeGroup.addIfNeeded( CreditCard.OWNER.getLabel(), oldVO.getOwner(), newVO.getOwner());

		/* Special handling for the Credit Card Number and Check Sum:
		 * Determine equality by comparing original values,
		 * but add the invisible ones!
		 */
		if (!EqualsHelper.isEqual(oldVO.getNumber(), newVO.getNumber())) {
			fieldChangeGroup.add(CreditCard.NUMBER.getLabel(), oldVO.getNoInvisible(), newVO.getNoInvisible());
		}
		if (!EqualsHelper.isEqual(oldVO.getCheckSum(), newVO.getCheckSum())) {
			fieldChangeGroup.add(CreditCard.CHECK_SUM.getLabel(), oldVO.getCheckSumInvisible(), newVO.getCheckSumInvisible());
		}


		fieldChangeGroup.addIfNeeded(CreditCard.EXPIRATION.getLabel(), oldVO.getExpirationAsString(), newVO.getExpirationAsString());

		return fieldChangeGroup;
	}

	private static FieldChangeGroup createCreditCardAliasFieldChangeGroup(CreditCardAliasVO oldVO, CreditCardAliasVO newVO) {
		FieldChangeGroup fieldChangeGroup = new FieldChangeGroup( Participant.CREDIT_CARD_ALIAS.getString() );
		fieldChangeGroup.addIfNeeded(CreditCardAlias.CREDIT_CARD_TYPE.getLabel(), oldVO.getCreditCardTypeName(), newVO.getCreditCardTypeName());
		fieldChangeGroup.addIfNeeded(CreditCardAlias.ALIAS.getLabel(), oldVO.getAlias(), newVO.getAlias());
		fieldChangeGroup.addIfNeeded(CreditCardAlias.EXPIRATION.getLabel(), oldVO.getExpirationAsString(), newVO.getExpirationAsString());
		fieldChangeGroup.addIfNeeded(CreditCardAlias.MASKED_NUMBER.getLabel(), oldVO.getMaskedNumber(), newVO.getMaskedNumber());
		return fieldChangeGroup;
	}

	private static FieldChangeGroup createCustomFieldFieldChangeGroup(ParticipantVO oldVO, ParticipantVO newVO) {

		FieldChangeGroup fieldChangeGroup = new FieldChangeGroup(CUSTOM_FIELD_GROUP_NAME);
		try {

			EventVO event = EventModel.getInstance().getEventVO(oldVO.getEventId());
			String[] oldFields = oldVO.getCustomFields();
			String[] newFields = newVO.getCustomFields();

			for (int i = 0; i < oldFields.length; i++) {
				if (! EqualsHelper.isEqual(oldFields[i], newFields[i])) {
					// Attention: The participants custom fields start with no 1!
					int customFieldNo = i + 1;
					String customFieldName = event.getCustomFieldName(customFieldNo);
//					if (customFieldName == null) {
//						// The custom field may not be used anymore
//						customFieldName = ParticipantLabel.CustomField + String.valueOf(customFieldNo);
//					}

					// if the custom field is not used (has no name) don't show it
					if (customFieldName != null) {
						fieldChangeGroup.add(customFieldName, oldFields[i], newFields[i]);
					}
				}
			}

		}
		catch (Exception e) {
			ErrorHandler.logError(e);
		}

		return fieldChangeGroup;
	}

	// *********************************************************************************************
	// * ParticipantCustomFieldValue
	// *

	/**
	 * Load ParticipantCustomFieldValues and its history and merge them into eventList.
	 * Changes in the history with the same time as an existing IHistoryEvent are added to this
	 * if it is a CreateParticipantEvent or an EditParticipantEvent.
	 *
	 * @param id
	 * @param eventList
	 * @throws Exception
	 */
	private static void addCustomFieldValues(
		Participant participant,
		ParticipantHistoryContainer participantHistoryContainer,
		List<IHistoryEvent> eventList
	)
	throws Exception {
		// get CustomFields of the Participant's Event
		Long eventPK = participant.getEventId();
		List<ParticipantCustomField> customFields =
			ParticipantCustomFieldModel.getInstance().getParticipantCustomFieldsByEventPK(eventPK);

		// customFields are already ordered by their position!


		// get current CustomFieldValues
		List<ParticipantCustomFieldValue> customFieldValues = participant.getCustomFieldValues();

		// get historical CustomFieldValues
		List<ParticipantCustomFieldValue> customFieldValueHistoryList = participantHistoryContainer.getCustomFieldValueHistoryList();



		// create Map from CustomField-ID to the List of all its CustomFieldValues (historical and current)
		// avoid entries with empty Lists
		Map<Long, List<ParticipantCustomFieldValue>> customFieldID2ValuesMap = MapHelper.createHashMap(customFields.size());

		// add historical values to map
		for (ParticipantCustomFieldValue customFieldValue : customFieldValueHistoryList) {
			Long customFieldPK = customFieldValue.getCustomFieldPK();
			List<ParticipantCustomFieldValue> list = customFieldID2ValuesMap.get(customFieldPK);
			if (list == null) {
				list = CollectionsHelper.createArrayList();
				customFieldID2ValuesMap.put(customFieldPK, list);
			}
			list.add(customFieldValue);
		}

		// add actual values to map
		for (ParticipantCustomFieldValue customFieldValue : customFieldValues) {
			Long customFieldPK = customFieldValue.getCustomFieldPK();
			List<ParticipantCustomFieldValue> list = customFieldID2ValuesMap.get(customFieldPK);
			if (list == null) {
				list = CollectionsHelper.createArrayList();
				customFieldID2ValuesMap.put(customFieldPK, list);
			}
			list.add(customFieldValue);
		}



		for (ParticipantCustomField customField : customFields) {
			List<ParticipantCustomFieldValue> valueList = customFieldID2ValuesMap.get(customField.getID());
			if ( notEmpty(valueList) ) {
				Collections.sort(valueList, AbstractEntity2EditTimeComparator.getInstance());

				ParticipantCustomFieldValue previousCustomFieldValue = null;
				for (ParticipantCustomFieldValue customFieldValue : valueList) {

					// handle editTime
					addCustomFieldChangeToEventList(
						customFieldValue,
						previousCustomFieldValue,
						customField,
						eventList,
						true // edit
					);

					// handle deleteTime
					addCustomFieldChangeToEventList(
						customFieldValue,
						previousCustomFieldValue,
						customField,
						eventList,
						false // edit
					);

					// save current customFieldValue as previousCustomFieldValue for next iteration
					previousCustomFieldValue = customFieldValue;
				}

			}
		}
	}


	private static void addCustomFieldChangeToEventList(
		ParticipantCustomFieldValue customFieldValue,
		ParticipantCustomFieldValue previousCustomFieldValue,
		ParticipantCustomField customField,
		List<IHistoryEvent> eventList,
		boolean edit
	)
	throws Exception {
		Date time = null;
		String user = null;

		if (edit) {
			time = customFieldValue.getEditTime();
			user = customFieldValue.getEditDisplayUserStr();
		}
		else {
			time = customFieldValue.getDeleteTime();
			user = customFieldValue.getDeleteDisplayUserStr();
		}

		if (time != null) {
    		// find IHistoryEvent at same time
    		IHistoryEvent historyEvent = null;
    		int pos = 0;
    		for (IHistoryEvent he : eventList) {
    			if (DateHelper.isSame(time, he.getTime(), TIME_TOLERANCE)) {
    				historyEvent = he;
    				break;
    			}
    			else if (time.before(he.getTime())) {
    				/* Stop, because all following historyEvents will be even later and the
    				 * new HistoryEvent we have to create has to be inserted before this one.
    				 * So pos has the correct value.
    				 */
    				break;
    			}
    			pos++;
    		}

    		if (historyEvent == null) {
    			// create new EditParticipantEvent
    			historyEvent = new EditParticipantEvent(time, user);

    			// insert historyEvent into eventList at chronological position
    			eventList.add(pos, historyEvent);
    		}

    		// we expect only CreateParticipantEvent and EditParticipantEvent
    		// we ignore CreateParticipantEvent
    		if (historyEvent instanceof EditParticipantEvent) {
    			EditParticipantEvent editParticipantEvent = (EditParticipantEvent) historyEvent;

				// format values according to the type of the CustomField

    			String oldValue = null;
    			String newValue = null;
				if (edit) {
					if (previousCustomFieldValue != null && previousCustomFieldValue.getDeleteTime() == null) {
						oldValue = CustomFieldFormatter.format(customField, previousCustomFieldValue, true /*ignoreException*/) ;
					}

					// format new value according to the type of the CustomField
					newValue = CustomFieldFormatter.format(customField, customFieldValue, true /*ignoreException*/);
				}
				else {
					oldValue = CustomFieldFormatter.format(customField, customFieldValue, true /*ignoreException*/);
				}

				// if the type is boolean, null is handled like false
				if (   customField.getCustomFieldType() == CustomFieldType.BST
					|| customField.getCustomFieldType() == CustomFieldType.BSW
				) {
					if (StringHelper.isEmpty(oldValue)) {
						oldValue = CustomFieldFormatter.format(false);
					}
					if (StringHelper.isEmpty(newValue)) {
						newValue = CustomFieldFormatter.format(false);
					}
				}

    			if (!StringHelper.isEqual(oldValue, newValue)) {
    				// add change info to editParticipantEvent

    				String name = customField.getLabelOrName();

    				String groupName = CUSTOM_FIELD_GROUP_NAME;

    				// replace groupName by name of group (if exists)
    				Long groupPK = customField.getGroupPK();
					if (groupPK != null) {
    					ParticipantCustomFieldGroupModel pcfgModel = ParticipantCustomFieldGroupModel.getInstance();
						ParticipantCustomFieldGroup group = pcfgModel.getParticipantCustomFieldGroup(groupPK);
						groupName = group.getName(Locale.getDefault().getLanguage());
    				}

    				FieldChangeGroup fieldChangeGroup = editParticipantEvent.getFieldChangeGroup(groupName);
    				if (fieldChangeGroup == null) {
    					fieldChangeGroup = new FieldChangeGroup(groupName);

    					// FieldChangeGroup cannot be added here, because it is still empty and would be ignored

    					// so first add values
    					fieldChangeGroup.addIfNeeded(name, oldValue, newValue);

    					// then add the fieldChangeGroup to editParticipantEvent
    					editParticipantEvent.add(fieldChangeGroup);
    				}
    				else {
    					fieldChangeGroup.addIfNeeded(name, oldValue, newValue);
    				}
    			}
    		}
		}
	}

	// *
	// * ParticipantCustomFieldValue
	// *********************************************************************************************


	private static String getLanguageName(String languageCode) {
		String name = null;
		if (languageCode != null) {
			name = languageCode;

			try {
				Language language = LanguageModel.getInstance().getLanguage(languageCode);
    			if (language != null) {
    				name = language.getName().getString();
    			}
    		}
    		catch (Exception e) {
    			ErrorHandler.logError(e);
    		}
		}
		return name;
	}


	private static String getCountryName(String countryCode) {
		String name = null;
		if (countryCode != null) {
			name = countryCode;

			try {
				Country country = CountryModel.getInstance().getCountry(countryCode);
    			if (country != null) {
    				name = country.getName().getString();
    			}
    		}
    		catch (Exception e) {
    			ErrorHandler.logError(e);
    		}
		}
		return name;
	}


	private static String getParticipantTypeName(Long participantTypeId) {
		String name = null;
		if (participantTypeId != null) {
			name = String.valueOf(participantTypeId);

			try {
    			ParticipantType participantType = ParticipantTypeModel.getInstance().getParticipantType(participantTypeId);
    			if (participantType != null) {
    				name = participantType.getName().getString();
    			}
    		}
    		catch (Exception e) {
    			ErrorHandler.logError(e);
    		}
		}
		return name;
	}


	private static String getParticipantStateName(Long participantStateId) {
		String name = null;
		if (participantStateId != null) {
			name = String.valueOf(participantStateId);

			try {
    			ParticipantState participantState = ParticipantStateModel.getInstance().getParticipantState(participantStateId);
    			if (participantState != null) {
    				name = participantState.getName().getString();
    			}
    		}
    		catch (Exception e) {
    			ErrorHandler.logError(e);
    		}
		}
		return name;
	}


	private static String getHotelOfferingInfo(Long hotelOfferingId) {
		String name = null;
		if (hotelOfferingId != null) {
			name = String.valueOf(hotelOfferingId);

			try {
				HotelOfferingVO offeringVO = HotelOfferingModel.getInstance().getHotelOfferingVO(hotelOfferingId);
				if (offeringVO != null) {
					// load Hotel Contingent
					Long contingentPK = offeringVO.getHotelContingentPK();
					HotelContingentVO contingentVO = HotelContingentModel.getInstance().getHotelContingentVO(contingentPK);

					// load Hotel
					Long hotelId = contingentVO.getHotelPK();
					Hotel hotel = HotelModel.getInstance().getHotel(hotelId);

					// load Room Definition
					Long roomDefinitionId = offeringVO.getRoomDefinitionPK();
					RoomDefinitionVO roomDefinitionVO = RoomDefinitionModel.getInstance().getRoomDefinitionVO(roomDefinitionId);


					// build label for Hotel / Hotel Contingent
					String hotelName = hotel.getName1();
					String contingentName = contingentVO.getName();
					String contingentLabel = HotelBookingCVO.getLabelForHotelContingent(hotelName, contingentName);

					// build label for Hotel Offering
					LanguageString offeringDescription = offeringVO.getDescription();
					LanguageString roomDefinitionName = roomDefinitionVO.getName();
					String offeringLabel = HotelBookingCVO.getLabelForOffering(offeringDescription, roomDefinitionName);

					StringBuilder sb = new StringBuilder(512);
					sb.append(contingentLabel);

					if (offeringLabel != null) {
    					sb.append(NEW_LINE);
    					sb.append(offeringLabel);
					}

    				name = sb.toString();
    			}
    		}
    		catch (Exception e) {
    			ErrorHandler.logError(e);
    		}
		}
		return name;
	}


	private static String getGroupMemberInfo(ParticipantVO participantVO) {
		String result;
		if (participantVO.isSingleGroupMember()) {
			result = UtilI18N.Yes;

			// Try to append the name of the group member
			try {
				Long groupManagerID = participantVO.getGroupManagerPK();
				Participant groupManager = ParticipantModel.getInstance().getParticipant(groupManagerID);
				result += ", " + Participant.GROUP_MANAGER.getString() + ":\n" + groupManager.getName1();
			}
			catch (Exception e) {
				ErrorHandler.logError(e);
			}
		}
		else {
			result = UtilI18N.No;
		}
		return result;
	}


	private static String getCompanionOfInfo(ParticipantVO participantVO) {
		String result;
		if (participantVO.isCompanion()) {
			result = UtilI18N.Yes;

			// Try to append the name of the participant who this participantVO is companion of.
			try {
				Long companionOfID = participantVO.getCompanionOfPK();
				Participant companionOf = ParticipantModel.getInstance().getParticipant(companionOfID);
				result += ", " + Participant.COMPANION_OF.getString() + ":\n" + companionOf.getName1();
			}
			catch (Exception e) {
				ErrorHandler.logError(e);
			}
		}
		else {
			result = UtilI18N.No;
		}
		return result;
	}


	private static String getSecondPersonInfo(ParticipantVO participantVO) {
		String result;
		if (participantVO.getSecondPersonID() != null) {
			/* Try to get the name of the 2nd person.
			 * This leads to an error if it has been deleted already.
			 */
			Participant secondPerson = null;
			try {
				Long secondPersonID = participantVO.getSecondPersonID();
				secondPerson = ParticipantModel.getInstance().getParticipant(secondPersonID);
			}
			catch (Exception e) {
				ErrorHandler.logError(e);
			}

			if (secondPerson != null) {
				result = secondPerson.getName1();
			}
			else {
				result = UtilI18N.Yes;
			}
		}
		else {
			result = UtilI18N.No;
		}
		return result;
	}

}
