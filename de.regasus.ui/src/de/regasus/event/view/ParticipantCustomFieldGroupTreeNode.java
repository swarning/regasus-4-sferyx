package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField_Position_Comparator;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.ui.Activator;


/**
 * A node representing a Participant Custom Field Group.
 */
public class ParticipantCustomFieldGroupTreeNode
	extends TreeNode<ParticipantCustomFieldGroup>
	implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long groupID;

	/* Just used to refresh the data of this Group.
	 * Observing is not necessary, because the parent TreeNode is observing all Groups of
	 * this Event. On any change the value of this TreeNode is set and the parent calls refreshTreeNode().
	 */
	private ParticipantCustomFieldGroupModel pcfgModel = ParticipantCustomFieldGroupModel.getInstance();

	// data of child TreeNodes
	private ParticipantCustomFieldModel pcfModel = ParticipantCustomFieldModel.getInstance();

	// ignore ModifyEvent from ParticipantCustomFieldModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************


	private CacheModelListener<Long> customFieldModelListener = new CacheModelListener<Long>() {
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

	public ParticipantCustomFieldGroupTreeNode(
		TreeViewer treeViewer,
		ParticipantCustomFieldGroupLocationTreeNode parent,
		ParticipantCustomFieldGroup participantCustomFieldGroup
	) {
		super(treeViewer, parent, participantCustomFieldGroup);

		groupID = value.getID();

		// observe all Participant Custom Fields of current Event
		pcfModel.addForeignKeyListener(customFieldModelListener, getEventId());

		/* Do not observe ParticipantCustomFieldGroupModel.
		 * The parent ParticipantCustomFieldGroupLocationTreeNode already observes all
		 * Participant Custom Field Groups (all groups of this event) and calls reloadChildren()
		 * if any change happens.
		 */

		/* Loading all Participant Custom Fields of this Event to minimize client-server
		 * communication is not necessary, because ParticipantCustomFieldListTreeNode did it already.
		 */
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			pcfModel.removeForeignKeyListener(customFieldModelListener, getEventId());
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
		return ParticipantCustomFieldGroup.class;
	}


	@Override
	public Object getKey() {
		return groupID;
	}


	@Override
	public String getText() {
		StringBuilder text = new StringBuilder(100);

		try {
			LanguageString name = value.getName();
			if (name != null) {
				text.append(name.getString());
			}

			// append number of Custom Fields in this Group
			ignoreDataChange = true;
			// load data from model to get the correct count even if the children of this TreeNode are not loaded yet
			List<ParticipantCustomField> customFields = pcfModel.getParticipantCustomFieldsByGroup(
				getEventId(),
				groupID
			);
			text.append(" (");
			text.append(customFields.size());
			text.append(")");
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreDataChange = false;
		}

		return text.toString();
	}


	@Override
	public String getToolTipText() {
		return ParticipantLabel.ParticipantCustomFieldGroup.getString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.MD_CUSTOM_FIELD_GROUP);
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
			Long groupID = value.getID();

			ignoreDataChange = true;
			List<ParticipantCustomField> customFields = pcfModel.getParticipantCustomFieldsByGroup(getEventId(), groupID);

			if (customFields == null) {
				customFields = emptyList();
			}

			/*
			 * Do not just remove all child-nodes and build new ones, because this will close
			 * all nodes, the user already opened. Instead replace the data of all nodes that
			 * still exist, remove nodes of entities that do not exist anymore and create new
			 * nodes for new entities.
			 */

			// If there aren't any children create a TreeNode for every ParticipantCustomField.
			if (!hasChildren()) {
				// resize children-List if necessary
				ensureCapacityOfChildren(customFields.size());

				for (ParticipantCustomField customField : customFields) {
					ParticipantCustomFieldTreeNode treeNode = new ParticipantCustomFieldTreeNode(
						treeViewer,
						this,
						customField
					);
					addChild(treeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<Long, ParticipantCustomField> customFieldMap = ParticipantCustomField.getEntityMap(customFields);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */

				List<ParticipantCustomFieldTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (ParticipantCustomFieldTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					ParticipantCustomField customField = customFieldMap.get(treeNode.getKey());

					if (customField != null) {
						// Set new data to the TreeNode
						treeNode.setValue(customField);
						// Remove data from map, so after the for-block the map
						// only contains new values
						customFieldMap.remove(customField.getID());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + customFieldMap.size());

				// add new TreeNodes for each new value
				for (ParticipantCustomField customField : customFieldMap.values() ) {
					ParticipantCustomFieldTreeNode treeNode = new ParticipantCustomFieldTreeNode(
						treeViewer,
						this,
						customField
					);
					addChild(treeNode);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreDataChange = false;
		}

		/* If the label of this node shall change when the user opens this node:
		 * If the user opens a node, loadChildren() is called by getChildren().
		 * In this case the TreeViewer is "busy" and rejects updates from refresh() and update().
		 * This is not a problem for child nodes, because the TreeViewer shows them automatically.
		 * But the TreeViewer won't touch this node. Therefore updateTreeViewer() is called to
		 * update the label of this node.
		 *
		 * Never call refreshTreeViewer() from loadChildren(), because it's not necessary and would
		 * cause an infinite loop like this:
		 *
		 * loadChildren()
		 * 		_loadChildren()
		 * 			refreshTreeViewer()
		 * 				treeViewer.refresh(TreeNode.this);
		 * 					getChildren()
		 * 						loadChildren();
		 *
		 * The callers of loadChildren() are responsible to call refreshTreeViewer() if it is necessary.
		 */
	}


	@Override
	public void refresh() {
		try {
			// refresh data of this TreeNode
			pcfgModel.refresh(groupID);

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
			if ( isChildrenLoaded() ) {
    			pcfModel.refreshForeignKey(getEventId());

    			// no grandchildren to refresh
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************

	// *************************************************************************
	// * Implementation of interfaces
	// *

	@Override
	public int compareChildTreeNodes(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		return ParticipantCustomField_Position_Comparator.getInstance().compare(
			((ParticipantCustomFieldTreeNode) treeNode1).getValue(),
			((ParticipantCustomFieldTreeNode) treeNode2).getValue()
		);
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *

	public Long getParticipantCustomFieldGroupID() {
		return groupID;
	}


	@Override
	public Long getEventId() {
		return value.getEventPK();
	}

	// *
	// * Getter and setter
	// *************************************************************************

}
