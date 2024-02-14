package de.regasus.programme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;

public class WaitList implements Cloneable {

	private Long programmePointPK;
	private List<ProgrammeBookingCVO> programmeBookingCVOs;
	
	
	public WaitList(Long programmePointPK) {
		super();
		this.programmePointPK = programmePointPK;
	}


	public Long getProgrammePointPK() {
		return programmePointPK;
	}


	public void setProgrammePointPK(Long programmePointPK) {
		this.programmePointPK = programmePointPK;
	}


	public List<ProgrammeBookingCVO> getProgrammeBookingCVOs() {
		if (programmeBookingCVOs == null) {
			programmeBookingCVOs = Collections.emptyList();
		}
		return programmeBookingCVOs;
	}


	public void setProgrammeBookingCVOs(List<ProgrammeBookingCVO> programmeBookingCVOs) {
		this.programmeBookingCVOs = programmeBookingCVOs;
	}


	public boolean containsProgrammeBooking(Long programmeBookingPK) {
		boolean result = ProgrammeBookingCVO.containsPK(programmeBookingPK, programmeBookingCVOs);
		return result;
	}

	
	public List<Long> getProgrammeBookingPKs() {
		List<Long> programmeBookingPKs = ProgrammeBookingCVO.getPKs(getProgrammeBookingCVOs());
		return programmeBookingPKs;
	}
	
	
	public void removeProgrammeBooking(Long programmeBookingPK) {
		for (Iterator<ProgrammeBookingCVO> it = getProgrammeBookingCVOs().iterator(); it.hasNext();) {
			ProgrammeBookingCVO programmeBookingCVO = it.next();
			if (programmeBookingCVO.getPK().equals(programmeBookingPK)) {
				it.remove();
				break;
			}
		}
	}
	

	public void removeProgrammeBookings(List<ProgrammeBookingCVO> programmeBookingCVOs) {
		for (ProgrammeBookingCVO programmeBookingCVO : programmeBookingCVOs) {
			removeProgrammeBooking(programmeBookingCVO.getPK());
		}
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((programmePointPK == null) ? 0 : programmePointPK.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WaitList other = (WaitList) obj;
		if (programmePointPK == null) {
			if (other.programmePointPK != null)
				return false;
		}
		else if (!programmePointPK.equals(other.programmePointPK))
			return false;
		return true;
	}

	
	@Override
	public WaitList clone() {
		try {
			WaitList clone = (WaitList) super.clone();
			
			// cloning the ProgrammeBookingCVOs is not necessary, because they are not changed
			if (programmeBookingCVOs != null) {
				clone.programmeBookingCVOs = new ArrayList<ProgrammeBookingCVO>(programmeBookingCVOs);
			}
			
			return clone;
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
}
