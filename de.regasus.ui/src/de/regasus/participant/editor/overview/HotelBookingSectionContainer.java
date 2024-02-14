/**
 * HotelBookingSectionContainer.java
 * created on 18.07.2013 13:34:37
 */
package de.regasus.participant.editor.overview;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO_Hotel_FirstBenefitRecipient_Comparator;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.time.TimeFormatter;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.Tuple;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.ui.Activator;

public class HotelBookingSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long participantPK;

	private HotelBookingModel hotelBookingModel;

	private ConfigParameterSet configParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public HotelBookingSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long participantID,
		ConfigParameterSet configParameterSet
	)
	throws Exception {
		super(formToolkit, body, 2);

		this.participantPK = participantID;
		this.configParameterSet = configParameterSet;

		addDisposeListener(this);

		hotelBookingModel = HotelBookingModel.getInstance();
		hotelBookingModel.addForeignKeyListener(this, participantPK);

		refreshSection();
	}


	@Override
	protected String getTitle() {
		return HotelLabel.HotelBookings.getString();
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;


			List<HotelBookingCVO> bookingCVOs = hotelBookingModel.getHotelBookingCVOsByRecipient(participantPK);
			bookingCVOs = CollectionsHelper.createArrayList(bookingCVOs);

			boolean visible =
				configParameterSet == null ||
				configParameterSet.getEvent().getHotel().isVisible();

			if (visible) {
				// set visible true if at least 1 booking exists that is not canceled
				visible = false;
				if (CollectionsHelper.notEmpty(bookingCVOs)) {
					for (HotelBookingCVO bookingCVO : bookingCVOs) {
						if ( ! bookingCVO.isCanceled()) {
							visible = true;
							break;
						}
					}
				}
			}

			setVisible(visible);

			if (visible) {
				List<Tuple<String, String>> labelValueTuples = CollectionsHelper.createArrayList(bookingCVOs.size());

				// sort hotel bookings
				Collections.sort(bookingCVOs, HotelBookingCVO_Hotel_FirstBenefitRecipient_Comparator.getInstance());

				for(HotelBookingCVO bookingCVO : bookingCVOs) {
					if ( ! bookingCVO.isCanceled()) {
						String hotelName = bookingCVO.getHotelName();
						I18NDate arrival = bookingCVO.getArrival();
						I18NDate departure = bookingCVO.getDeparture();
						String period = TimeFormatter.formatDate(arrival, departure);

						Tuple<String, String> labelValueTuple = new Tuple<String, String>(hotelName, period);
						labelValueTuples.add(labelValueTuple);
					}
				}

				for (Tuple<String, String> tuple : labelValueTuples) {
					addIfNotEmpty(tuple.getA(), tuple.getB());
				}
			}
		}
		finally {
			ignoreCacheModelEvents = false;
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if ( ! ignoreCacheModelEvents) {
				refreshSection();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		if (hotelBookingModel != null && participantPK != null) {
			try {
				hotelBookingModel.removeForeignKeyListener(this, participantPK);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}
