package de.regasus.users.user.dialog;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;

public class HotelContingentLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof HotelContingentCVO) {
			HotelContingentCVO hotelContingentVO = (HotelContingentCVO) element;
			String name = hotelContingentVO.getVO().getName();
			return name;
		}
		else {
			return super.getText(element);
		}
	}
}
