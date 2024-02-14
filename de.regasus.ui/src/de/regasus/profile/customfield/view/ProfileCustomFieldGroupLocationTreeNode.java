package de.regasus.profile.customfield.view;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroupLocation;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.PropertyModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.ui.Activator;

public class ProfileCustomFieldGroupLocationTreeNode extends TreeNode<ProfileCustomFieldGroupLocation> {

	private boolean ignoreDataChange = false;


	private CacheModelListener<Long> customFieldGroupModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!ServerModel.getInstance().isLoggedIn()) {
				return;
			}

			if (ignoreDataChange) {
				return;
			}

			reloadChildren();
		}
	};


	public ProfileCustomFieldGroupLocationTreeNode(
		TreeViewer treeViewer,
		ProfileCustomFieldTreeRoot parent,
		ProfileCustomFieldGroupLocation location) {

		super(treeViewer, parent, location);
		ProfileCustomFieldGroupModel.getInstance().addListener(customFieldGroupModelListener);
	}


	@Override
	public void dispose() {
		try {
			ProfileCustomFieldGroupModel.getInstance().removeListener(customFieldGroupModelListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
		super.dispose();
	}


	@Override
	public Object getKey() {
		return value;
	}


	@Override
	public Class<?> getEntityType() {
		return ProfileCustomFieldGroupLocation.class;
	}


	@Override
	public String getText() {
		String text = "";
		try {
			text = PropertyModel.getInstance().getPropertyValue(value.getKey());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		StringBuilder textResult = new StringBuilder(text);
		try {
			int totalCustomFields = 0;
			List<ProfileCustomFieldGroup> groups = ProfileCustomFieldGroupModel.getInstance()
				.getAllProfileCustomFieldGroups();
			for (ProfileCustomFieldGroup group : groups) {
				if (group != null && group.getLocation() == value) {
					List<ProfileCustomField> customField = ProfileCustomFieldModel.getInstance()
						.getProfileCustomFieldsByGroup(group.getID());
					totalCustomFields += customField.size();
				}
			}
			textResult.append(" (")
				.append(totalCustomFields)
				.append(")");
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return textResult.toString();
	}


	@Override
	public String getToolTipText() {
		switch(value) {
			case TAB_1: return I18N.ProfileCustomFieldGroupLocationTreeNode_TAB_1_tooltip;
			case TAB_2: return I18N.ProfileCustomFieldGroupLocationTreeNode_TAB_2_tooltip;
			case TAB_3: return I18N.ProfileCustomFieldGroupLocationTreeNode_TAB_3_tooltip;
			case PSNLT: return I18N.ProfileCustomFieldGroupLocationTreeNode_PSNLT_tooltip;
			case PSNRT: return I18N.ProfileCustomFieldGroupLocationTreeNode_PSNRT_tooltip;
			default: return "";
		}
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.MD_CUSTOM_FIELD_LIST);
	}


	@Override
	protected void loadChildren() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				_loadChildren();
			}
		});
	}


	private void _loadChildren() {
		try {
			ignoreDataChange = true;

			List<ProfileCustomFieldGroup> allCustomFieldGroups = ProfileCustomFieldGroupModel.getInstance()
				.getAllProfileCustomFieldGroups();

			// copy/filter Groups with the same ProfileCustomFieldGroupLocation
			List<ProfileCustomFieldGroup> groups = new ArrayList<>(allCustomFieldGroups.size());
			for (ProfileCustomFieldGroup group : allCustomFieldGroups) {
				if (group.getLocation() == value) {
					groups.add(group);
				}
			}

			// If there aren't any children create a TreeNode for every ParticipantCustomFieldGroupTreeNode.
			if (!hasChildren()) {
				ensureCapacityOfChildren(allCustomFieldGroups.size());
				for (ProfileCustomFieldGroup group : allCustomFieldGroups) {
					if (group != null && group.getLocation() == value) {
						ProfileCustomFieldGroupTreeNode node = new ProfileCustomFieldGroupTreeNode(treeViewer, this,
							group);
						addChild(node);
					}
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				Map<Long, ProfileCustomFieldGroup> groupMap = ProfileCustomFieldGroup.getEntityMap(groups);
				// Update or delete existing CustomFieldGroupTreeNode
				List<ProfileCustomFieldGroupTreeNode> treeNodeList = (List) createArrayList(getLoadedChildren());
				for (ProfileCustomFieldGroupTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					Long id = treeNode.getProfileCustomFieldGroupID();
					ProfileCustomFieldGroup group = groupMap.get(id);

					if (group != null) {
						// Set new data to the TreeNode
						treeNode.setValue(group);
						// Remove data from map, so after the for-block the map
						// only contains new values
						groupMap.remove(id);
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + groupMap.size());

				// add new Group-TreeNodes for each new CustomFieldGroup
				for (ProfileCustomFieldGroup group : groupMap.values()) {
					ProfileCustomFieldGroupTreeNode groupTreeNode = new ProfileCustomFieldGroupTreeNode(
						treeViewer,
						this,
						group
					);

					// add TreeNode to list of children
					addChild(groupTreeNode);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreDataChange = false;
		}
	}


	@Override
	public void refresh() {
		refreshChildren();
	}


	@Override
	public void refreshChildren() {
		try {
			if (isChildrenLoaded()) {
				ProfileCustomFieldGroupModel.getInstance().refresh();
				ProfileCustomFieldModel.getInstance().refresh();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public int compareChildTreeNodes(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		return ProfileCustomFieldGroup_Location_Position_Comparator.getInstance().compare(
			((ProfileCustomFieldGroupTreeNode) treeNode1).getValue(),
			((ProfileCustomFieldGroupTreeNode) treeNode2).getValue()
		);
	}

}
