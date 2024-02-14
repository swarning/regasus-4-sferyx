package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.ClassKeyNameTransfer;

import de.regasus.core.OrderPosition;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.portal.Page;
import de.regasus.portal.PageModel;
import de.regasus.programme.ProgrammeOfferingModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.ui.Activator;

public class EventMasterDataDropListener extends ViewerDropAdapter {

	private EventModel evModel;
	private ParticipantCustomFieldGroupModel paCFGrpModel;
	private ParticipantCustomFieldModel paCFModel;
	private PageModel pageModel;
	private ProgrammePointModel ppModel;
	private ProgrammeOfferingModel poModel;
	private HotelContingentModel hcModel;


	public EventMasterDataDropListener(TreeViewer viewer) {
		super(viewer);

		setScrollExpandEnabled(true);

		evModel = EventModel.getInstance();
		paCFModel = ParticipantCustomFieldModel.getInstance();
		paCFGrpModel = ParticipantCustomFieldGroupModel.getInstance();
		pageModel = PageModel.getInstance();
		ppModel = ProgrammePointModel.getInstance();
		poModel = ProgrammeOfferingModel.getInstance();
		hcModel = HotelContingentModel.getInstance();
	}


	/**
	 * Signals whether on the target, data of the given type may be dropped or not.
	 */
	@Override
	public boolean validateDrop(Object targetTreeNode, int op, TransferData type) {
		// You may not drop files, text, etc in the EventMasterDataView, only master data.
		if (! ClassKeyNameTransfer.getInstance().isSupportedType(type)) {
			return false;
		}

		boolean result = false;


		Object movedTreeNode = getSelectedObject();
		int location = getCurrentLocation();


		// determine the eventPK of the moved entity
		Long sourceEventPK = null;
		if (movedTreeNode instanceof EventIdProvider) {
			sourceEventPK = ((EventIdProvider) movedTreeNode).getEventId();
		}


		// determine the eventPK of the target entity
		Long targetEventPK = null;
		if (targetTreeNode instanceof EventIdProvider) {
			targetEventPK = ((EventIdProvider) targetTreeNode).getEventId();
		}


		// source is Event
		if (movedTreeNode instanceof EventTreeNode) {
			// target id folder
			if (targetTreeNode instanceof EventGroupTreeNode) {
				// drop Event on EventGroup
				result = location == LOCATION_ON;
				if (result) System.out.println("drop Event on EventGroup");
			}
		}
		// source is Participant Custom Field Group
		else if (movedTreeNode instanceof ParticipantCustomFieldGroupTreeNode) {
			// target id folder
			if (targetTreeNode instanceof ParticipantCustomFieldGroupLocationTreeNode) {
				// moving any entity to another Event is not allowed
				if ( sourceEventPK.equals(targetEventPK) ) {
    				// drop Group on folder
    				result = location == LOCATION_ON;
    				if (result) System.out.println("drop Group on folder");
    			}
			}
			// target is Group
			else if (targetTreeNode instanceof ParticipantCustomFieldGroupTreeNode) {
				// moving any entity to another Event is not allowed
				if ( sourceEventPK.equals(targetEventPK) ) {
    				// drop Group before or after Group
    				result = location == LOCATION_BEFORE || location == LOCATION_AFTER;
    				if (result) System.out.println("drop Group before or after Group");
    			}
			}
		}
		// source is Participant Custom Field
		else if (movedTreeNode instanceof ParticipantCustomFieldTreeNode) {
			// target is Group
			if (targetTreeNode instanceof ParticipantCustomFieldGroupTreeNode) {
				// moving any entity to another Event is not allowed
				if ( sourceEventPK.equals(targetEventPK) ) {
					// drop Custom Field on Group
    				result = location == LOCATION_ON;
    				if (result) System.out.println("drop " + movedTreeNode + " on " + targetTreeNode);
				}
			}
			// target is Custom Field
			else if (targetTreeNode instanceof ParticipantCustomFieldTreeNode) {
				// moving any entity to another Event is not allowed
				if ( sourceEventPK.equals(targetEventPK) ) {
    				// drop Custom Field before or after Custom Field
    				result = location == LOCATION_BEFORE || location == LOCATION_AFTER;
    				if (result) System.out.println("drop " + movedTreeNode + (location == LOCATION_BEFORE ? " before" : " after") + " " + targetTreeNode);
				}
			}
		}
		// source is Page
		else if (movedTreeNode instanceof PageTreeNode) {
			// target is Programme Point
			if (targetTreeNode instanceof PageTreeNode) {
				// determine the portalPK of the moved entity
				Long sourcePortalPK = ((PageTreeNode) movedTreeNode).getValue().getPortalId();
				Long targetPortalPK = ((PageTreeNode) targetTreeNode).getValue().getPortalId();

				// moving a Page to another Portal is not allowed
				if ( sourcePortalPK.equals(targetPortalPK) ) {
    				// drop Page before or after Page
    				result = location == LOCATION_BEFORE || location == LOCATION_AFTER;
    				if (result) System.out.println("drop Page before or after Page");
				}
			}
		}
		// source is Programme Point
		else if (movedTreeNode instanceof ProgrammePointTreeNode) {
			// target is Programme Point
			if (targetTreeNode instanceof ProgrammePointTreeNode) {
    			// moving any entity to another Event is not allowed
    			if ( sourceEventPK.equals(targetEventPK) ) {
    				// drop Programme Point before or after Programme Point
    				result = location == LOCATION_BEFORE || location == LOCATION_AFTER;
    				if (result) System.out.println("drop Programme Point before or after Programme Point");
    			}
			}
		}
		// source is Programme Offering
		else if (movedTreeNode instanceof ProgrammeOfferingTreeNode) {
			// target is Programme Offering
			if (targetTreeNode instanceof ProgrammeOfferingTreeNode) {
    			// moving any entity to another Event is not allowed
    			if ( sourceEventPK.equals(targetEventPK) ) {
    				// determine the programmePointPK of the moved entity
    				Long sourceProgrammePointPK = ((ProgrammeOfferingTreeNode) movedTreeNode).getValue().getProgrammePointPK();
    				Long targetProgrammePointPK = ((ProgrammeOfferingTreeNode) targetTreeNode).getValue().getProgrammePointPK();

    				// moving a Programme Offering to another Programme Point is not allowed
    				if ( sourceProgrammePointPK.equals(targetProgrammePointPK) ) {
    					// drop Programme Offering before or after Programme Offering
    					result = location == LOCATION_BEFORE || location == LOCATION_AFTER;
    					if (result) System.out.println("drop Programme Offering before or after Programme Offering");
    				}
				}
			}
		}
		// source is Hotel Contingent
		else if (movedTreeNode instanceof HotelContingentTreeNode) {
			// target is Hotel Contingent
			if (targetTreeNode instanceof HotelContingentTreeNode) {
    			// moving any entity to another Event is not allowed
    			if ( sourceEventPK.equals(targetEventPK) ) {
    				// determine the hotelPK of the moved entity
    				Long sourceHotelPK = ((HotelContingentTreeNode) movedTreeNode).getValue().getHotelPK();
    				Long targetHotelPK = ((HotelContingentTreeNode) targetTreeNode).getValue().getHotelPK();

    				// moving a Hotel Contingent to another Hotel is not allowed
    				if ( sourceHotelPK.equals(targetHotelPK) ) {
    					// drop Hotel Contingent before or after Hotel Contingent
    					result = location == LOCATION_BEFORE || location == LOCATION_AFTER;
    					if (result) System.out.println("drop Hotel Contingent before or after Hotel Contingent");
    				}
				}
			}
		}

		return result;
	}


	/**
	 * Drop now takes place. We expect the parameter data as String-Array (as required by the only
	 * allowed {@link ClassKeyNameTransfer}). The String-Array contains the Id and Class of the
	 * dragged object and depending on various combinations of current target  (
	 * a {@link ParticipantCustomFieldListTreeNode},
	 * a {@link ParticipantCustomFieldGroupTreeNode},
	 * a {@link ParticipantCustomFieldTreeNode},
	 * a {@link PageTreeNode},
	 * a {@link ProgrammePointTreeNode},
	 * a {@link ProgrammeOfferingTreeNode} or
	 * a {@link HotelContingentTreeNode}
	 * )
	 * and operation we ask the model for an appropriate copy/update.
	 *
	 * We don't refresh anything in the viewer, because the model fires dataChange events.
	 */
	@Override
	public boolean performDrop(Object data) {
		try {
			if (data instanceof String[]) {

				// determine drag source
				String[] transferValues = (String[]) data;
				String className = transferValues[0];
				String key = transferValues[1];


				if (EventVO.class.getName().equals(className)) {
					// handle dropped EventVO

					// load and clone source entity
					EventVO eventVO = evModel.getEventVO( Long.valueOf(key) );

					handleDrop(eventVO);
				}
				else if (ParticipantCustomFieldGroup.class.getName().equals(className)) {
					// handle dropped ParticipantCustomFieldGroup

					// load and clone source entity
					ParticipantCustomFieldGroup group = paCFGrpModel.getParticipantCustomFieldGroup( Long.valueOf(key) );

					handleDrop(group);
				}
				else if (ParticipantCustomField.class.getName().equals(className)) {
					// handle dropped ParticipantCustomField

					// load and clone source entity
					ParticipantCustomField customField = paCFModel.getParticipantCustomField( new Long(key) );

					handleDrop(customField);
				}
				else if (Page.class.getName().equals(className)) {
					// handle dropped Page

					// load and clone source entity
					Page page = pageModel.getPage( new Long(key) );

					handleDrop(page);
				}
				else if (ProgrammePointVO.class.getName().equals(className)) {
					// handle dropped Programme Point

					// load and clone source entity
					ProgrammePointVO programmePoint = ppModel.getProgrammePointVO( new Long(key) );

					handleDrop(programmePoint);
				}
				else if (ProgrammeOfferingVO.class.getName().equals(className)) {
					// handle dropped Programme Offering

					// load and clone source entity
					ProgrammeOfferingVO programmeOffering = poModel.getProgrammeOfferingVO( new Long(key) );

					handleDrop(programmeOffering);
				}
				else if (HotelContingentCVO.class.getName().equals(className)) {
					// handle dropped Hotel Contingent

					// load and clone source entity
					HotelContingentVO hotelContingent = hcModel.getHotelContingentVO( new Long(key) );

					handleDrop(hotelContingent);
				}
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}

		return true;
	}


	private boolean handleDrop(EventVO dropEventVO) throws Exception {
		// determine drop target
		Object target = getCurrentTarget();

		dropEventVO = dropEventVO.clone();

		if (target instanceof EventGroupTreeNode) {
			// drop Event on EventGroup

			Long targetEventGroupId = ((EventGroupTreeNode) target).getValue().getId();

			dropEventVO.setEventGroupPK(targetEventGroupId);
			evModel.update(dropEventVO);
		}
		else {
			System.err.println("Invalid drop target: Participant Custom Field Groups cannot move to " + target.getClass().getName());
			return false;
		}

		return true;
	}


	private boolean handleDrop(ParticipantCustomFieldGroup dropGroup) throws Exception {
		// determine drop target
		Object target = getCurrentTarget();

		// check if source and target belong to the same Event
		Long targetEventId = ((EventIdProvider) target).getEventId();
		if ( ! dropGroup.getEventPK().equals(targetEventId)) {
			System.err.println("Invalid drop target: Participant Custom Field Groups cannot be moved to another Event.");
			return false;
		}

		dropGroup = dropGroup.clone();

		if (target instanceof ParticipantCustomFieldGroupLocationTreeNode) {
			// drop Group on Location folder

			ParticipantCustomFieldGroupLocation targetLocation = ((ParticipantCustomFieldGroupLocationTreeNode) target).getValue();

			paCFGrpModel.moveToLocation(dropGroup.getID(), targetLocation);
		}
		else if (target instanceof ParticipantCustomFieldGroupTreeNode) {
			// drop Group before or after Group

			OrderPosition orderPosition = OrderPosition.BEFORE;
			if (getCurrentLocation() == LOCATION_AFTER) {
				orderPosition = OrderPosition.AFTER;
			}

			Long targetGroupId = ((ParticipantCustomFieldGroupTreeNode) target).getValue().getID();

			paCFGrpModel.move(dropGroup.getID(), orderPosition, targetGroupId);
		}
		else {
			System.err.println("Invalid drop target: Participant Custom Field Groups cannot move to " + target.getClass().getName());
			return false;
		}

		return true;
	}


	private boolean handleDrop(ParticipantCustomField dropCustomField) throws Exception {
		// determine drop target
		Object target = getCurrentTarget();

		// check if source and target belong to the same Event
		Long targetEventId = ((EventIdProvider) target).getEventId();
		if ( ! dropCustomField.getEventPK().equals(targetEventId)) {
			System.err.println("Invalid drop target: Participant Custom Fields cannot be moved to another Event.");
			return false;
		}

		dropCustomField = dropCustomField.clone();

		if (target instanceof ParticipantCustomFieldGroupTreeNode) {
			// drop Custom Field on Group

			Long targetGroupId = ((ParticipantCustomFieldGroupTreeNode) target).getValue().getID();

			paCFModel.moveToGroup(dropCustomField.getID(), targetGroupId);
		}
		else if (target instanceof ParticipantCustomFieldTreeNode) {
			// drop Custom Field before or after Custom Field

			OrderPosition orderPosition = OrderPosition.BEFORE;
			if (getCurrentLocation() == LOCATION_AFTER) {
				orderPosition = OrderPosition.AFTER;
			}

			Long targetCustomFieldId = ((ParticipantCustomFieldTreeNode) target).getValue().getID();

			paCFModel.move(dropCustomField.getID(), orderPosition, targetCustomFieldId);
		}
		else {
			System.err.println("Invalid drop target: Participant Custom Fields cannot move to " + target.getClass().getName());
			return false;
		}

		return true;
	}


	private boolean handleDrop(Page dropPage) throws Exception {
		// determine drop target
		Object target = getCurrentTarget();
		Page targetPage = ((PageTreeNode) target).getValue();

		// check if source and target belong to the same Portal
		// determine the portalPK of the target entity
		if ( ! dropPage.getPortalId().equals( targetPage.getPortalId() )) {
			System.err.println("Invalid drop target: Portal Pages cannot be moved to another Portal.");
			return false;
		}

		dropPage = dropPage.clone();

		if (target instanceof PageTreeNode) {
			// drop Page before or after Page

			OrderPosition orderPosition = OrderPosition.BEFORE;
			if (getCurrentLocation() == LOCATION_AFTER) {
				orderPosition = OrderPosition.AFTER;
			}

			pageModel.move(dropPage.getId(), orderPosition, targetPage.getId());
		}
		else {
			System.err.println("Invalid drop target: Pages cannot move to " + target.getClass().getName());
			return false;
		}

		return true;
	}


	private boolean handleDrop(ProgrammePointVO dropProgrammePointVO) throws Exception {
		// determine drop target
		Object target = getCurrentTarget();

		// check if source and target belong to the same Event
		Long targetEventId = ((EventIdProvider) target).getEventId();
		if ( ! dropProgrammePointVO.getEventPK().equals(targetEventId)) {
			System.err.println("Invalid drop target: Programme Points cannot be moved to another Event.");
			return false;
		}

		dropProgrammePointVO = dropProgrammePointVO.clone();

		if (target instanceof ProgrammePointTreeNode) {
			// drop Programme Point before or after Programme Point

			OrderPosition orderPosition = OrderPosition.BEFORE;
			if (getCurrentLocation() == LOCATION_AFTER) {
				orderPosition = OrderPosition.AFTER;
			}

			Long targetProgrammePointId = ((ProgrammePointTreeNode) target).getValue().getID();

			ppModel.move(dropProgrammePointVO.getID(), orderPosition, targetProgrammePointId);
		}
		else {
			System.err.println("Invalid drop target: Programme Points cannot move to " + target.getClass().getName());
			return false;
		}

		return true;
	}


	private boolean handleDrop(ProgrammeOfferingVO dropProgrammeOfferingVO) throws Exception {
		// determine drop target
		Object target = getCurrentTarget();
		ProgrammeOfferingVO targetProgrammeOffering = ((ProgrammeOfferingTreeNode) target).getValue();

		// check if source and target belong to the same Programme Point
		Long targetProgrammePointId = targetProgrammeOffering.getProgrammePointPK();
		if ( ! dropProgrammeOfferingVO.getProgrammePointPK().equals(targetProgrammePointId)) {
			System.err.println("Invalid drop target: Programme Offerings cannot be moved to another Programme Point.");
			return false;
		}

		dropProgrammeOfferingVO = dropProgrammeOfferingVO.clone();

		if (target instanceof ProgrammeOfferingTreeNode) {
			// drop Programme Offering before or after Programme Offering

			OrderPosition orderPosition = OrderPosition.BEFORE;
			if (getCurrentLocation() == LOCATION_AFTER) {
				orderPosition = OrderPosition.AFTER;
			}

			Long targetProgrammeOfferingId = ((ProgrammeOfferingTreeNode) target).getValue().getID();

			poModel.move(dropProgrammeOfferingVO.getID(), orderPosition, targetProgrammeOfferingId);
		}
		else {
			System.err.println("Invalid drop target: Programme Offerings cannot move to " + target.getClass().getName());
			return false;
		}

		return true;
	}


	private boolean handleDrop(HotelContingentVO dropHotelContingentVO) throws Exception {
		// determine drop target
		Object target = getCurrentTarget();
		HotelContingentVO targetHotelContingent = ((HotelContingentTreeNode) target).getValue().getVO();

		// check if source and target belong to the same Event
		Long targetEventId = targetHotelContingent.getEventPK();
		if ( ! dropHotelContingentVO.getEventPK().equals(targetEventId)) {
			System.err.println("Invalid drop target: Hotel Contingents cannot be moved to another Event.");
			return false;
		}

		// check if source and target belong to the same Hotel
		Long targetHotelId = targetHotelContingent.getHotelPK();
		if ( ! dropHotelContingentVO.getHotelPK().equals(targetHotelId)) {
			System.err.println("Invalid drop target: Hotel Contingents cannot be moved to another Hotel.");
			return false;
		}

		dropHotelContingentVO = dropHotelContingentVO.clone();

		if (target instanceof HotelContingentTreeNode) {
			// drop Hotel Contingent before or after Hotel Contingent

			OrderPosition orderPosition = OrderPosition.BEFORE;
			if (getCurrentLocation() == LOCATION_AFTER) {
				orderPosition = OrderPosition.AFTER;
			}

			Long targetHotelContingentId = targetHotelContingent.getId();

			hcModel.move(dropHotelContingentVO.getID(), orderPosition, targetHotelContingentId);
		}
		else {
			System.err.println("Invalid drop target: Hotel Contingents cannot move to " + target.getClass().getName());
			return false;
		}

		return true;
	}

}