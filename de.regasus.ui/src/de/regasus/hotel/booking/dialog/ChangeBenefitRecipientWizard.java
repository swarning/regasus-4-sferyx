package de.regasus.hotel.booking.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.StringHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

/**
 * If you select n bookings and n participants, each one gets one of the bookings
 * <p>
 * If you select 1 booking and n participants, they get together in the same room.
 * <p>
 * If you select n bookings and 1 participant, s/he may visit several hotels consecutively.
 */
public class ChangeBenefitRecipientWizard extends Wizard {

	private static final String TITLE = I18N.ChangeBenefitRecipient;

	private ChangeBenefitRecipientWizardPage page;

	private List<HotelBookingCVO> hotelBookingList;

	private ArrayList<SQLParameter> sqlParameterList;

	private Participant participant;

	private List<Participant> recipientList;


	// **************************************************************************
	// * Constructors
	// *

	public ChangeBenefitRecipientWizard(
		Participant participant,
		ArrayList<SQLParameter> sqlParameterList,
		List<HotelBookingCVO> hotelBookingCVOs
	) {
		this.participant = participant;
		this.sqlParameterList = sqlParameterList;
		this.hotelBookingList = hotelBookingCVOs;

		// Compute set of all participants which are benefit recipients in any of the selected
		// hotel bookings. Using a set so that all participants appear only once.
		Set<Long> allBenefitRecipientPKs = CollectionsHelper.createHashSet(hotelBookingCVOs.size() * 2);

		for (HotelBookingCVO hotelBookingCVO : hotelBookingCVOs) {
			List<Long> benefitRecipientPKs = hotelBookingCVO.getVO().getBenefitRecipientPKs();
			for (Long benefitRecipientPK : benefitRecipientPKs) {
				allBenefitRecipientPKs.add(benefitRecipientPK);
			}
		}

		try {
			recipientList = ParticipantModel.getInstance().getParticipants(allBenefitRecipientPKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	// **************************************************************************
	// * Initializers
	// *

	@Override
	public void addPages() {
		Collection<Integer> allowedSelectionNumbers = calcAllowedSelectionNumbers();

		page = new ChangeBenefitRecipientWizardPage(
			participant,
			sqlParameterList,
			recipientList
		);
		page.setAllowedSelectionCount(allowedSelectionNumbers);
		page.setTitle(TITLE);
		page.setDescription( createDescription(allowedSelectionNumbers) );

		addPage(page);
	}


	private Collection<Integer> calcAllowedSelectionNumbers() {
		// mögliche Anzahlen zu selektierender Teilnehmer
		Set<Integer> allowedSelectionNumbers = new TreeSet<>();
		// die Auswahl eines TN ist immer möglich
		allowedSelectionNumbers.add(1);

		if (hotelBookingList.size() != 1) {
			// if several bookings are selected, you can assign the same number of benefit recipients
			allowedSelectionNumbers.add(hotelBookingList.size());
		}
		else {
			// If one booking is selected, you can assign as much benefit recipients as there are beds in the offering
			HotelBookingCVO hotelBookingCVO = hotelBookingList.get(0);
			Integer bedCount = hotelBookingCVO.getHotelBookingVO().getBedCount();
			if (bedCount != null) {
				for (int i = 2; i <= bedCount.intValue(); i++) {
					allowedSelectionNumbers.add(i);
				}
			}
		}

		return allowedSelectionNumbers;
	}

	private String createDescription(Collection<Integer> allowedSelectionNumbers) {
		String desc;
		if (allowedSelectionNumbers.size() == 1 && allowedSelectionNumbers.contains(1)) {
			desc = I18N.ParticipantSelectionDialog_Description_One;
		}
		else {
			desc = I18N.ParticipantSelectionDialog_Description_FixedNumber;
			String numbers = StringHelper.createEnumeration(
				"",
				UtilI18N.Or,
				allowedSelectionNumbers
			);
			desc = desc.replace("<numbers>", numbers);
		}
		return desc;
	}


	@Override
	public boolean performFinish() {
		List<Participant> selectedParticipants = page.getSelectedParticipants();

		List<Long> newBenefitRecipientPKs = new ArrayList<>();

		for (Participant participant : selectedParticipants) {
			newBenefitRecipientPKs.add(participant.getID());
		}

		try {
			HotelBookingModel.getInstance().changeBenefitRecipientsOfHotelBookings(
				hotelBookingList,
				newBenefitRecipientPKs
			);
			return true;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return false;
	}


	@Override
	public String getWindowTitle() {
		return TITLE;
	}

}
