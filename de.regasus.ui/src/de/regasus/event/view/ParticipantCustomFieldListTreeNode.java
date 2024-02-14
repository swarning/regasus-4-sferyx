package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.ui.Activator;

/**
 * This TreeNode is the root for the {@link ParticipantCustomFieldGroupLocationTreeNodes} that represent
 * the various locations where {@link ParticipantCustomFieldGroup}s may appear.
 */
public class ParticipantCustomFieldListTreeNode
extends TreeNode<Object>
implements EventIdProvider, CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *

	// necessary to get the number of Custom Fields of this Event
	private ParticipantCustomFieldModel pcfModel = ParticipantCustomFieldModel.getInstance();

	// just used to refresh all Custom Field Groups of this Event
	private ParticipantCustomFieldGroupModel pcfgModel = ParticipantCustomFieldGroupModel.getInstance();

	// ignore ModifyEvent from ParticipantCustomFieldModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	private Long eventPK;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public ParticipantCustomFieldListTreeNode(TreeViewer treeViewer, EventTreeNode parent) {
		super(treeViewer, parent);

		// get the eventPK
		eventPK = parent.getEventId();

		// listen to ParticipantCustomFieldModel, to keep count in label up-to-date
		pcfModel.addForeignKeyListener(this, eventPK);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			pcfModel.removeForeignKeyListener(this, eventPK);
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
		// this is a folder node that doesn't correspond to any entity
		return null;
	}


	@Override
	public Object getKey() {
		// this is a folder node that doesn't correspond to any entity
		return null;
	}


	@Override
	public String getText() {
		StringBuilder text = new StringBuilder(ContactLabel.CustomFields.getString());

		/* get number of Custom Fields from Model and not from getChildCount(),
		 * because the latter returns 0 if children are not loaded yet
		 */
		try {
			ignoreDataChange = true;
			int count = pcfModel.getParticipantCustomFieldsByEventPK(eventPK).size();

			text.append(" (").append(count).append(")");
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
		return null;
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.MD_CUSTOM_FIELD_LIST);
	}


	@Override
	public boolean hasChildren() {
		return true;
	}


	@Override
	protected void loadChildren() {
		/**
		 * ParticipantCustomFieldListTreeNode has always a fixed number children, which are folders,
		 * whose children in turn are lazily loaded. So we create them here once, and that's it.
		 */
		if (!isChildrenLoaded()) {
			// determine ParticipantCustomFieldGroupLocations
			ParticipantCustomFieldGroupLocation[] locations = ParticipantCustomFieldGroupLocation.values();

			// resize children-List if necessary
			ensureCapacityOfChildren(locations.length);

			// create one child node for every ParticipantCustomFieldGroupLocation
			for (ParticipantCustomFieldGroupLocation location : locations) {
				TreeNode<ParticipantCustomFieldGroupLocation> locationTreeNode = new ParticipantCustomFieldGroupLocationTreeNode(
					treeViewer,
					this,
					location
				);

				// add TreeNode to list of children
				addChild(locationTreeNode);
			}
		}
	}


	@Override
	public void refresh() {
		// no own data to refresh

		// refresh data of child TreeNodes
		refreshChildren();
	}


	@Override
	public void refreshChildren() {
		try {
			if (isChildrenLoaded()) {
    			/* Refreshing ParticipantCustomFieldModel and ParticipantCustomFieldGroupModel will
    			 * update all subsequent nodes. No further recursive refresh operations are necessary,
    			 * because all subsequent data depends on these 2 Models.
    			 *
    			 * Refreshing all Custom Field Groups and Custom Fields of this Event here is more
    			 * performance than traversing the whole sub-tree and calling refreshChildren() for each
    			 * node.
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
		/* All child TreeNodes are of type ParticipantCustomFieldGroupLocationTreeNode.
		 * Their values are enums of the type ParticipantCustomFieldGroupLocation.
		 * Compare the values by their ordinal numbers.
		 */
		int result = 0;

		ParticipantCustomFieldGroupLocationTreeNode locationTreeNode1 = (ParticipantCustomFieldGroupLocationTreeNode) treeNode1;
		ParticipantCustomFieldGroupLocationTreeNode locationTreeNode2 = (ParticipantCustomFieldGroupLocationTreeNode) treeNode2;

		ParticipantCustomFieldGroupLocation location1 = locationTreeNode1.getValue();
		ParticipantCustomFieldGroupLocation location2 = locationTreeNode2.getValue();

		// The default natural order on Enum's compareTo is the listed order.
		result = location1.compareTo(location2);

		return result;
	}

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************

	// *************************************************************************
	// * Implementation of interfaces
	// *

	/* (non-Javadoc)
	 * @see com.lambdalogic.util.rcp.model.CacheModelListener#dataChange(com.lambdalogic.util.rcp.model.CacheModelEvent)
	 */
	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!ServerModel.getInstance().isLoggedIn()) {
			// do nothing, because all TreeNodes will be removed from root TreeNode
			return;
		}

		if (ignoreDataChange) {
			return;
		}

		// refresh the TreeViewer to make changes visible
		refreshTreeViewer();
	}

	// *
	// * Implementation of interfaces
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
