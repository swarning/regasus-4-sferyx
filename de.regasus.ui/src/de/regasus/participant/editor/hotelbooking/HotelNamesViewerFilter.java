package de.regasus.participant.editor.hotelbooking;

import java.util.HashSet;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;

/**
 * A filter for a JFace viewer that can be active or not, if active shows only those hotels with the given names, and
 * may additionally be configured to show also cancelled bookings.
 * 
 * @author manfred
 * 
 */
class HotelNamesViewerFilter extends ViewerFilter {

	private HashSet<String> nameSet = new HashSet<String>();

	boolean showCancelledBookings;

	boolean active;


	public void setActive(boolean active) {
		this.active = active;
	}


	public void setCheckedHotelNames(String[] hotelNames) {
		nameSet.clear();
		if (hotelNames != null && hotelNames.length > 0) {
			setActive(true);
			for (String hotelName : hotelNames) {
				nameSet.add(hotelName);
			}
		}

	}


	public void setShowCancelledBookings(boolean showCancelledBookings) {
		this.showCancelledBookings = showCancelledBookings;

	}


	/**
	 * Don't show if the booking is cancelled and don't want to see cancelled ones, and don't show either if filter is
	 * active and the given set (of names of booking to be shown) doesn't contain the booking.
	 * 
	 * 
	 * @param parentElement
	 * @param element
	 * @return
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		boolean result = true;
		HotelBookingCVO booking = ((HotelBookingCVO) element);
		if (!showCancelledBookings && booking.isCanceled() && booking.getOpenAmount().signum() == 0) {
			result = false;
		}
		else if (active && !nameSet.contains(booking.getHotelName())) {
			result = false;
		}
		return result;
	}

}
