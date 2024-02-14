package de.regasus.hotel.booking.combo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingPaymentCondition;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

/**
 * A combo for the payment conditions of hotel bookings.
 * Currently valid values are defined in {@link HotelBookingPaymentCondition}.
 *
 * This combo uses a HotelOfferingVO as a modelDiscriminator to limit the values to those
 * that are valid for a certain hotel offering. Therefore this combo should listen to the {@link HotelOfferingModel}
 * to update its values in the case that the allowed values might change.
 * However, this combo is used only in modal dialogs (HotelBookingDetailsDialog, HotelBookingMultipleDetailDialog),
 * so that changes are not possible.
 */
public class HotelBookingPaymentConditionCombo extends AbstractComboComposite<HotelBookingPaymentCondition> {

	protected List<HotelBookingPaymentCondition> hotelBookingPaymentConditions = null;


	/**
	 * Constructor to create a {@link HotelBookingPaymentConditionCombo} with all values.
	 *
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public HotelBookingPaymentConditionCombo(Composite parent, int style) throws Exception {
		super(parent, style);
	}


	/**
	 * Constructor to create a {@link HotelBookingPaymentConditionCombo} with all values that are allowed in the
	 * given {@link HotelOfferingVO}.
	 *
	 * @param parent
	 * @param style
	 * @param hotelOfferingVO
	 * @throws Exception
	 */
	public HotelBookingPaymentConditionCombo(Composite parent, int style, HotelOfferingVO hotelOfferingVO)
	throws Exception {
		super(parent, style, hotelOfferingVO);
	}


	@Override
	protected void disposeModel() {
	}


	@Override
	protected Object getEmptyEntity() {
		return null;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				HotelBookingPaymentCondition hotelBookingPaymentCondition = (HotelBookingPaymentCondition) element;
				I18NString label = HotelBookingVO.getLabelForHotelBookingPaymentCondition(hotelBookingPaymentCondition);
				return LanguageString.toStringAvoidNull(label);
			}
		};
	}


	@Override
	protected Collection<HotelBookingPaymentCondition> getModelData() throws Exception {
		if (hotelBookingPaymentConditions == null) {
			HotelOfferingVO hotelOfferingVO = null;
			if (modelDataDiscriminator != null) {
				if (modelDataDiscriminator instanceof HotelOfferingVO) {
					hotelOfferingVO = (HotelOfferingVO) modelDataDiscriminator;
				}
				else {
					throw new IllegalArgumentException(
						"modelDataDiscriminator is no instance of " +
						"HotelOfferingVO, but " + modelDataDiscriminator.getClass().getName()
					);
				}
			}


			hotelBookingPaymentConditions = new ArrayList<>(HotelBookingPaymentCondition.values().length);

			if (hotelOfferingVO == null || hotelOfferingVO.isPaymentConditionAbsorptionOfCosts()) {
				hotelBookingPaymentConditions.add(HotelBookingPaymentCondition.ABSORPTION_OF_COSTS);
			}
			if (hotelOfferingVO == null || hotelOfferingVO.isPaymentConditionBookingAmount()) {
				hotelBookingPaymentConditions.add(HotelBookingPaymentCondition.BOOKING_AMOUNT);
			}
			if (hotelOfferingVO == null || hotelOfferingVO.isPaymentConditionDeposit()) {
				hotelBookingPaymentConditions.add(HotelBookingPaymentCondition.DEPOSIT);
			}
			if (hotelOfferingVO == null || hotelOfferingVO.isPaymentConditionSelfPayPatient()) {
				hotelBookingPaymentConditions.add(HotelBookingPaymentCondition.SELF_PAY_PATIENT);
			}
		}

		return hotelBookingPaymentConditions;
	}

	/**
	 * Returns the ViewerSorter. Override this method to use another ViewerSorter. If the overwritten method returns
	 * null the viewer elements won't be sorted.
	 *
	 * @return
	 */
//	@Override
//	protected ViewerSorter getViewerSorter() {
//		if (viewerSorter == null) {
//			viewerSorter = new ViewerSorter();
//		}
//		return viewerSorter;
//	}


	@Override
	protected void initModel() {
	}

}
