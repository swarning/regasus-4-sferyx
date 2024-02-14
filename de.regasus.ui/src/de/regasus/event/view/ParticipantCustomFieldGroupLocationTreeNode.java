package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.ui.Activator;


public class ParticipantCustomFieldGroupLocationTreeNode
	extends TreeNode<ParticipantCustomFieldGroupLocation>
	implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	/* used to get the label of this TreeNode
	 * Observing this Event is not necessary, because EventGroupTreeNode is observing
	 * all Events. On any change EventGroupTreeNode.refreshTreeNode() is called and there getText()
	 * of this TreeNode.
	 */
	private EventModel evModel = EventModel.getInstance();

	// used to get the list of child TreeNodes
	private ParticipantCustomFieldGroupModel pcfgModel = ParticipantCustomFieldGroupModel.getInstance();

	/* used to determine number of Custom Fields that is part of the label of this TreeNode
	 * Observing ParticipantCustomFieldModel is not necessary, because ParticipantCustomFieldListTreeNode
	 * is observing ParticipantCustomFieldModel and calling refreshTreeNode() on every event.
	 */
	private ParticipantCustomFieldModel pcfModel = ParticipantCustomFieldModel.getInstance();

	private Long eventPK;

	// ignore ModifyEvent from ParticipantCustomFieldGroupModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************


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


	// *************************************************************************
	// * Constructors and dispose()
	// *

	public ParticipantCustomFieldGroupLocationTreeNode(
		TreeViewer treeViewer,
		ParticipantCustomFieldListTreeNode parent,
		ParticipantCustomFieldGroupLocation location
	) {
		super(treeViewer, parent, location);

		// get the eventPK
		eventPK = parent.getEventId();

		// listen to ParticipantCustomFieldGroupModel to keep child nodes up-to-date
		pcfgModel.addForeignKeyListener(customFieldGroupModelListener, eventPK);
	}


	@Override
	public void dispose() {
		// disconnect from models

		try {
			pcfgModel.removeForeignKeyListener(customFieldGroupModelListener, eventPK);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}

	// *
	// * Constructors and dispose()
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return ParticipantCustomFieldGroupLocation.class;
	}


	@Override
	public Object getKey() {
		// return value as key
		return value;
	}


	@Override
	public String getText() {
		// show name of ParticipantCustomFieldGroupLocation as default, e.g. in case of error
		String text = value.getString();

		try {
			if (value.ordinal() < ParticipantCustomFieldGroupLocation.TAB_COUNT) {
				EventVO eventVO = evModel.getEventVO(eventPK);
				int n = value.ordinal() + 1;
				text = eventVO.getCustomFieldTabName(n, Locale.getDefault().getLanguage());
			}
			else {
				// leave the default for locations that do not represent a tab
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		// determine number of custom fields
		StringBuilder textResult = new StringBuilder(text);
		try {
			int totalCustomFields = 0;
			Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> map =
				pcfModel.getParticipantCustomFieldsByGroupMap(getEventId());

			// check all groups
			for (ParticipantCustomFieldGroup group : map.keySet()) {
				// if group belongs to this location
				if (group != null && group.getLocation() == value) {
					// add number of custom fields in this group
					totalCustomFields += map.get(group).size();
				}
			}

			// append number to text
			textResult.append(" (").append(totalCustomFields).append(")");
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return textResult.toString();
	}


	@Override
	public String getToolTipText() {
		switch(value) {
			case TAB_1: return I18N.ParticipantCustomFieldGroupLocationTreeNode_TAB_1_tooltip;
			case TAB_2: return I18N.ParticipantCustomFieldGroupLocationTreeNode_TAB_2_tooltip;
			case TAB_3: return I18N.ParticipantCustomFieldGroupLocationTreeNode_TAB_3_tooltip;
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
			// get all Groups
			ignoreDataChange = true;
			List<ParticipantCustomFieldGroup> allCustomFieldGroups = pcfgModel.getParticipantCustomFieldGroupsByEventPK(eventPK);

			// copy/filter Groups with the same ParticipantCustomFieldGroupLocation
			List<ParticipantCustomFieldGroup> customFieldGroups = createArrayList(allCustomFieldGroups.size());
			for (ParticipantCustomFieldGroup customFieldGroup : allCustomFieldGroups) {
				if (customFieldGroup.getLocation() == value) {
					customFieldGroups.add(customFieldGroup);
				}
			}


			// If there aren't any children create a TreeNode for every ParticipantCustomFieldGroupTreeNode.
			if (!hasChildren()) {
				// resize children-List if necessary
				ensureCapacityOfChildren(customFieldGroups.size());

				for (ParticipantCustomFieldGroup customFieldGroup : customFieldGroups) {
					ParticipantCustomFieldGroupTreeNode groupTreeNode = new ParticipantCustomFieldGroupTreeNode(
						treeViewer,
						this,
						customFieldGroup
					);

					// add TreeNode to list of children
					addChild(groupTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<Long, ParticipantCustomFieldGroup> groupMap = ParticipantCustomFieldGroup.getEntityMap(customFieldGroups);


				// Update or delete existing CustomFieldGroupTreeNode
				@SuppressWarnings({ "rawtypes", "unchecked" })
				List<ParticipantCustomFieldGroupTreeNode> treeNodeList = (List) createArrayList(getLoadedChildren());
				for (ParticipantCustomFieldGroupTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					Long id = treeNode.getParticipantCustomFieldGroupID();
					ParticipantCustomFieldGroup group = groupMap.get(id);

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
				for (ParticipantCustomFieldGroup group : groupMap.values() ) {
					ParticipantCustomFieldGroupTreeNode groupTreeNode = new ParticipantCustomFieldGroupTreeNode(
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

		/* If the user open a node, loadChildren() is called by getChildren().
		 * In this case the TreeViewer is "busy" and rejects updates from refresh() and update().
		 * This is not a problem for child nodes, because the TreeViewer shows them automatically.
		 * But the TreeViewer won't touch this node. Therefore updateTreeViewer() is called to
		 * update the label of this node.
		 *
		 * Never call refreshTreeViewer() from loadChildren(), because it not necessary and would
		 * cause an infinite loop like this:
		 *
		 * loadChildren()
		 * 		_loadChildren()
		 * 			refreshTreeViewer()
		 * 				treeViewer.refresh(TreeNode.this);
		 * 					getChildren()
		 * 						loadChildren();
		 */
//		updateTreeViewer();
	}


	@Override
	public void refresh() {
		try {
			// refresh Event to update the label of this node
			evModel.refresh(eventPK);

			// refresh data of child TreeNodes
			refreshChildren();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refreshChildren() {
		try {
			if (isChildrenLoaded()) {
    			/* Refreshing ParticipantCustomFieldModel and ParticipantCustomFieldGroupModel will
    			 * update all subsequent nodes. No further recursive refresh operations are necessary,
    			 * because all subsequent data depends on these 2 Models.
    			 */
    			pcfgModel.refreshForeignKey(eventPK);
    			pcfModel.refreshForeignKey(eventPK);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public int compareChildTreeNodes(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		return ParticipantCustomFieldGroup_Location_Position_Comparator.getInstance().compare(
			((ParticipantCustomFieldGroupTreeNode) treeNode1).getValue(),
			((ParticipantCustomFieldGroupTreeNode) treeNode2).getValue()
		);
	}

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *

	@Override
	public Long getEventId() {
		return eventPK;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}
