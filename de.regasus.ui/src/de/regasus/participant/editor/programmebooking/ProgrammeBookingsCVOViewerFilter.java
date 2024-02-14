package de.regasus.participant.editor.programmebooking;

import java.util.HashSet;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;

/**
 * A filter for a JFace viewer that can be active or not, if active shows only those ProgrammeBookings with the given
 * programme point PKs, and may additionally be configured to show also cancelled bookings.
 * 
 * @author manfred
 * 
 */
class ProgrammeBookingsCVOViewerFilter extends ViewerFilter {

	private HashSet<Long> pkSet = new HashSet<Long>();

	boolean showCancelledBookings;

	boolean active;


	public void setActive(boolean active) {
		this.active = active;
	}

	public void setCheckedProgrammePoints(ProgrammePointCVO[] programmePoints) {
		pkSet.clear();
		if (programmePoints != null && programmePoints.length > 0) {
			setActive(true);
			for (ProgrammePointCVO programmePoint : programmePoints) {
				pkSet.add(programmePoint.getPK());
			}
		}
	}

	public void setShowCancelledBookings(boolean showCancelledBookings) {
		this.showCancelledBookings = showCancelledBookings;
	}


	/**
	 * Don't show if the booking is cancelled and don't want to see cancelled ones, and don't show either if filter is
	 * active and the given set (of PKs of booking to be shown) doesn't contain the booking.
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		ProgrammeBookingCVO booking = (ProgrammeBookingCVO) element;
		
		if (!showCancelledBookings && booking.isCanceled()) {
			long amount = (long) (booking.getBookingVO().getTotalAmount().doubleValue() * 100);
			if (amount == 0L) {
				return false;
			}
		}
		
		if (active && !pkSet.contains(booking.getVO().getProgrammePointPK())) {
			return false;
		}
		
		return true;
	}

}
