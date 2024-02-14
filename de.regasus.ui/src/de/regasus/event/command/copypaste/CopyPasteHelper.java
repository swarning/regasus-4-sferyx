package de.regasus.event.command.copypaste;

import java.util.HashSet;
import java.util.Set;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.LocationVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.onlineform.util.RightsHelper;
import de.regasus.portal.Page;
import de.regasus.portal.PageLayout;
import de.regasus.portal.Portal;

public class CopyPasteHelper {


	private static Set<Class<?>> allowCopy = new HashSet<>();
	static {
		allowCopy.add(HotelCancelationTermVO.class);
		allowCopy.add(ProgrammeCancelationTermVO.class);
		allowCopy.add(ProgrammeOfferingVO.class);
		allowCopy.add(HotelOfferingVO.class);
		allowCopy.add(WorkGroupVO.class);
		allowCopy.add(ProgrammePointVO.class);
		allowCopy.add(HotelContingentCVO.class);
		allowCopy.add(InvoiceNoRangeCVO.class);
		allowCopy.add(ParticipantCustomField.class);
		allowCopy.add(ParticipantCustomFieldGroup.class);
		allowCopy.add(ProfileCustomField.class);
		allowCopy.add(ProfileCustomFieldGroup.class);
		allowCopy.add(RoomDefinitionVO.class);

		// Not pastable. Only name and id are copied to clipboard.
		allowCopy.add(EventVO.class);
		allowCopy.add(Hotel.class);
		allowCopy.add(LocationVO.class);
		allowCopy.add(RegistrationFormConfig.class);
		allowCopy.add(Portal.class);
		allowCopy.add(PageLayout.class);
		allowCopy.add(Page.class);
	}


	/**
	 * Decide whether the selected object can be copied.
	 * <p>
	 * Note that there are VOs and CVOs mixed, depending on the value of the corresponding {@link TreeNode}.
	 * </p>
	 */
	public static boolean isCopyOK(Object value) {
		if (value instanceof RegistrationFormConfig) {
			// RegistrationFormConfigs may only be created (even by copying) by admins
			if (RightsHelper.isCreateAllowed()) {
				return true;
			}
		}
		return allowCopy.contains(value.getClass());
	}

}
