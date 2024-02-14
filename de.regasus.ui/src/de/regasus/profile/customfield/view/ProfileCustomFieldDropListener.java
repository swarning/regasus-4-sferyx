package de.regasus.profile.customfield.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroupLocation;
import com.lambdalogic.util.rcp.ClassKeyNameTransfer;

import de.regasus.core.OrderPosition;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.ui.Activator;

public class ProfileCustomFieldDropListener extends ViewerDropAdapter {

	private ProfileCustomFieldGroupModel prCFGrpModel;
	private ProfileCustomFieldModel prCFModel;


	public ProfileCustomFieldDropListener(TreeViewer viewer) {
		super(viewer);

		setScrollExpandEnabled(true);

		prCFModel = ProfileCustomFieldModel.getInstance();
		prCFGrpModel = ProfileCustomFieldGroupModel.getInstance();
	}


	/**
	 * Signals whether on the target, data of the given type may be dropped or not.
	 */
	@Override
	public boolean validateDrop(Object targetTreeNode, int op, TransferData type) {
		// You may not drop files, text, etc in the ProfileCustomFieldTreeView, only Profile Custom Fields and Groups.
		if (! ClassKeyNameTransfer.getInstance().isSupportedType(type)) {
			return false;
		}

		boolean result = false;


		Object movedTreeNode = getSelectedObject();
		int location = getCurrentLocation();

		// source is Group
		if (movedTreeNode instanceof ProfileCustomFieldGroupTreeNode) {
			// target id folder
			if (targetTreeNode instanceof ProfileCustomFieldGroupLocationTreeNode) {
				// drop Group on folder
				result = location == LOCATION_ON;
				if (result) System.out.println("drop Group on folder");
			}
			// target is Group
			else if (targetTreeNode instanceof ProfileCustomFieldGroupTreeNode) {
				// drop Group before or after Group
				result = location == LOCATION_BEFORE || location == LOCATION_AFTER;
				if (result) System.out.println("drop Group before or after Group");
			}
		}
		// source is Custom Field
		else if (movedTreeNode instanceof ProfileCustomFieldTreeNode) {
			// target is Group
			if (targetTreeNode instanceof ProfileCustomFieldGroupTreeNode) {
				// drop Custom Field on Group
				result = location == LOCATION_ON;
				if (result) System.out.println("drop " + movedTreeNode + " on " + targetTreeNode);
			}
			// target is Custom Field
			else if (targetTreeNode instanceof ProfileCustomFieldTreeNode) {
				// drop Custom Field before or after Custom Field
				result = location == LOCATION_BEFORE || location == LOCATION_AFTER;
				if (result) System.out.println("drop " + movedTreeNode + (location == LOCATION_BEFORE ? " before" : " after") + " " + targetTreeNode);
			}
		}

		return result;
	}


	/**
	 * Drop now takes place. We expect the parameter data as String-Array (as required by the only
	 * allowed {@link ClassKeyNameTransfer}). The String-Array contains the Id and Class of the
	 * dragged object and depending on various combinations of current target  (
	 * a {@link ProfileCustomFieldListTreeNode},
	 * a {@link ProfileCustomFieldGroupTreeNode} or
	 * a {@link ProfileCustomFieldTreeNode})
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


				if (ProfileCustomFieldGroup.class.getName().equals(className)) {
					// handle dropped ProfileCustomFieldGroup

					// load and clone source entity
					ProfileCustomFieldGroup group = prCFGrpModel.getProfileCustomFieldGroup( Long.valueOf(key) );

					handleDrop(group);
				}
				else if (ProfileCustomField.class.getName().equals(className)) {
					// handle dropped ProfileCustomField

					// load and clone source entity
					ProfileCustomField customField = prCFModel.getProfileCustomField( new Long(key) );

					handleDrop(customField);
				}
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}

		return true;
	}


	private boolean handleDrop(ProfileCustomFieldGroup group) throws Exception {
		// determine drop target
		Object target = getCurrentTarget();

		group = group.clone();

		if (target instanceof ProfileCustomFieldGroupLocationTreeNode) {
			// drop Group on Location folder

			ProfileCustomFieldGroupLocation targetLocation = ((ProfileCustomFieldGroupLocationTreeNode) target).getValue();

			prCFGrpModel.moveToLocation(group.getID(), targetLocation);
		}
		else if (target instanceof ProfileCustomFieldGroupTreeNode) {
			// drop Group before or after Group

			OrderPosition orderPosition = OrderPosition.BEFORE;
			if (getCurrentLocation() == LOCATION_AFTER) {
				orderPosition = OrderPosition.AFTER;
			}

			Long targetGroupId = ((ProfileCustomFieldGroupTreeNode) target).getValue().getID();

			prCFGrpModel.move(group.getID(), orderPosition, targetGroupId);
		}
		else {
			System.err.println("Invalid drop target: Profile Custom Field Groups cannot move to " + target.getClass().getName());
			return false;
		}

		return true;
	}


	private boolean handleDrop(ProfileCustomField customField) throws Exception {
		// determine drop target
		Object target = getCurrentTarget();

		customField = customField.clone();

		if (target instanceof ProfileCustomFieldGroupTreeNode) {
			// drop Custom Field on Group

			Long targetGroupId = ((ProfileCustomFieldGroupTreeNode) target).getValue().getID();

			prCFModel.moveToGroup(customField.getID(), targetGroupId);
		}
		else if (target instanceof ProfileCustomFieldTreeNode) {
			// drop Custom Field before or after Custom Field

			OrderPosition orderPosition = OrderPosition.BEFORE;
			if (getCurrentLocation() == LOCATION_AFTER) {
				orderPosition = OrderPosition.AFTER;
			}

			Long targetCustomFieldId = ((ProfileCustomFieldTreeNode) target).getValue().getID();

			prCFModel.move(customField.getID(), orderPosition, targetCustomFieldId);
		}
		else {
			System.err.println("Invalid drop target: Profile Custom Fields cannot move to " + target.getClass().getName());
			return false;
		}

		return true;
	}

}
