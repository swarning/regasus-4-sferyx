package com.lambdalogic.util.rcp.tree;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.util.ObjectComparator;
import com.lambdalogic.util.model.CacheModel;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.error.ErrorHandler;


public abstract class TreeNode<T> implements Comparable<TreeNode<?>> {

	protected static Collator collator = Collator.getInstance();

	/**
	 * The TreeViewer that shows this TreeNode.
	 */
	protected TreeViewer treeViewer;

	/**
	 * The parent TreeNode.
	 */
	private TreeNode<?> parent;

	/**
	 * List of child TreeNodes.
	 */
	private ArrayList<TreeNode<?>> children;

	/**
	 * The data this TreeNode represents.
	 */
	protected T value;


	private ModifySupport modifySupport;
	private boolean ignoreModification = false;


	// **************************************************************************
	// * Constructors and dispose
	// *

	/**
	 * Constructor for Root-Nodes
	 */
	public TreeNode() {
		this(null, null, null, false);
	}


	public TreeNode(TreeViewer treeViewer, TreeNode<?> parent, T value, boolean addToParent) {
		this.treeViewer = treeViewer;
		this.parent = parent;
		this.value = value;
		if (addToParent && parent != null) {
			parent.addChild(this);
		}
	}


	public TreeNode(TreeViewer treeViewer, TreeNode<?> parent, T value) {
		this(treeViewer, parent, value, false);
	}


	public TreeNode(TreeViewer treeViewer, TreeNode<?> parent) {
		this(treeViewer, parent, null, false);
	}


	public void dispose() {
		if (children != null) {
			for (TreeNode<?> treeNode : children) {
				if (treeNode != null) {
					treeNode.dispose();
				}
			}
		}
	}

	// *
	// * Constructors and dispose
	// **************************************************************************

	// **************************************************************************
	// * Abstract methods
	// *

	/**
	 * Returns the key of the node.
	 * @return
	 */
	public abstract Object getKey();


	/**
	 * Returns the data type of the node.
	 * @return
	 */
	public abstract Class<?> getEntityType();

	/**
	 * Returns the label to be shown in the tree.
	 * @return
	 */
	public abstract String getText();

	/**
	 * Returns the text to be shown as the tool tip.
	 * @return
	 */
	public abstract String getToolTipText();

	/**
	 * Returns an image to be shown in the tree.
	 * @return
	 */
	public abstract Image getImage();

	// *
	// * Abstract methods
	// **************************************************************************

	// **************************************************************************
	// * style
	// *

	public boolean isStrikeOut() {
		return false;
	}

	// *
	// * style
	// **************************************************************************

	// **************************************************************************
	// * public interface
	// *

	/**
	 * Returns the data (entity) of the node.
	 * @return
	 */
	public T getValue() {
		return value;
	}


	public void setValue(T value) {
		this.value = value;
	}


	/**
	 * Return true if this {@link TreeNode} is a leaf {@link TreeNode} / has no children.
	 * Override this method if this {@link TreeNode} is a leaf {@link TreeNode} / has no children,
	 * because the return value of the default impolementation is false. Otherwise override
	 * {@link #loadChildren()} and {@link #refreshChildren()}.
	 * @return
	 */
	public boolean isLeaf() {
		return false;
	}


	/**
	 * Load the data of this {@link TreeNode}'s children.
	 * Override this method if it is no leaf {@link TreeNode} / has children, otherwise override
	 * {@link #isLeaf()}.
	 * The List of {@link TreeNode}s in children must be updated.
	 * The method must not refresh the {@link TreeViewer}!
	 */
	protected void loadChildren() {
	}


	/**
	 * Refresh the data of this {@link TreeNode} and its children.
	 * The {@link TreeViewer} must be refreshed, too.
	 * When using a {@link CacheModel} this method should call one of the models refresh() methods.
	 * Typical implementations look like this:
	 *
	 * 		try {
	 * 			// refresh data of this TreeNode
	 * 			fooModel.refresh(fooId);
	 *
	 * 			// refresh data of child TreeNodes
	 * 			refreshChildren();
	 * 		}
	 * 		catch (Exception e) {
	 * 			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
	 * 		}
	 */
	public abstract void refresh();


	/**
	 * Refresh the data of the children of this {@link TreeNode} if they're visible.
	 * Override this method if it is no leaf {@link TreeNode} / has children, otherwise override
	 * {@link #isLeaf()}.
	 * The {@link TreeViewer} must be refreshed, too.
	 * When using a {@link CacheModel} this method should call one of the models refresh() methods.
	 *
	 * Typical implementations look like this:
	 *
	 * 		try {
	 * 			if ( isChildrenLoaded() ) {
	 * 				// refresh data of all Portals of the current Event
	 * 				fooModel.refreshForeignKey(eventPK);
	 *
	 * 				// refresh data of our grandchildren
	 * 				refreshGrandChildren();
	 * 		}
	 * 		catch (Exception e) {
	 * 			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
	 * 		}
	 */
	public void refreshChildren() {
	}


	/**
	 * Call {@link #refreshChildren()} for each child {@link TreeNode}.
	 */
	protected void refreshGrandChildren() {
    	try {
			// refresh grandchildren
    		if ( isChildrenLoaded() ) {
    			// get children
    			List<TreeNode<?>> childList = getChildren();
    			// avoid ConcurrentModificationException
    			childList = createArrayList(childList);
				for (TreeNode<?> childTreeNode : childList) {
					// refresh children of children (grandchildren)
    				childTreeNode.refreshChildren();
    			}
    		}
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * If children are already loaded, load them anew from DB and refresh the {@link TreeViewer}.
	 */
	protected void reloadChildren() {
		try {
			if (!isLeaf()) {
				if (isChildrenLoaded()) {
					ignoreModification = true;

					loadChildren();

					if (modifySupport != null) {
						modifySupport.fire();
					}
				}

				/* Call refreshTreeViewer() even if children are not loaded, to refresh label of this TreeNode that
				 * could contains the number of children.
				 */
				refreshTreeViewer();
			}
		}
		catch (Exception e) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreModification = false;
		}
	}


	/**
	 * Returns this {@link TreeNode}'s child-{@link TreeNode}s.
	 * If the child-{@link TreeNode}s of this {@link TreeNode} have not been loaded yet, their data
	 * is loaded and they will be created.
	 *
	 * @return
	 */
	public List<TreeNode<?>> getChildren() {
		if (!isLeaf() && children == null) {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				@Override
				public void run() {
					try {
						ignoreModification = true;
						loadChildren();

						if (modifySupport != null) {
							modifySupport.fire();
						}
					}
					catch (Exception e) {
						ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						ignoreModification = false;
					}
				}
			});
		}

		return getLoadedChildren();
	}


	/**
	 * Returns this {@link TreeNode}'s child-{@link TreeNode}s if they have been loaded already.
	 * If the child-{@link TreeNode}s of this {@link TreeNode} have not been loaded yet, the result
	 * is an empty {@link List}.
	 *
	 * @return
	 */
	public List<TreeNode<?>> getLoadedChildren() {
		// return always a unmodifiable Collection
		if (children != null) {
			return Collections.unmodifiableList(children);
		}
		else {
			// return a non-empty (and unmodifiable) Collection, but leave children null
			return Collections.emptyList();
		}
	}


	/**
	 * Returns this node's child nodes.
	 * @return
	 */
	public TreeNode<?>[] getChildrenAsArray() {
		TreeNode<?>[] childrenArray = null;
		List<TreeNode<?>> childTreeNodes = getChildren();
		if (childTreeNodes != null) {
			childrenArray = childTreeNodes.toArray(new TreeNode[childTreeNodes.size()]);
		}
		else {
			childrenArray = new TreeNode[0];
		}
		return childrenArray;
	}


	/**
	 * Return true, if the children of this node are already loaded, even there areno chilfren at all.
	 * @return
	 */
	public boolean isChildrenLoaded() {
		return children != null;
	}


	/**
	 * Returns true if this node's children are loaded and this node has at least one child.
	 * @return
	 */
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}


	/**
	 * Returns true if this node has children or if its children are not loaded yet.
	 * @return
	 */
	public boolean hasChildrenMaybe() {
		boolean result = !isLeaf() && ( hasChildren() || !isChildrenLoaded() );
		return result;
	}


	/**
	 * Returns the parent node or null if this is the root node.
	 * @return
	 */
	public TreeNode<?> getParent() {
		return parent;
	}


	/**
	 * Removes the given child from the node.
	 * @param treeNode
	 */
	public void removeChild(TreeNode<?> treeNode) {
		if (children != null) {
			children.remove(treeNode);

			if (modifySupport != null && !ignoreModification) {
				modifySupport.fire();
			}
		}
	}


	/**
	 * Removes the child with the given value from this node.
	 * @param value
	 */
	public void removeChildByValue(Object value) {
		if (children != null) {
			boolean modified = false;

			for (Iterator<TreeNode<?>> it = children.iterator(); it.hasNext();) {
				TreeNode<?> treeNode = it.next();
				if (value != null) {
					if (value.equals(treeNode.getValue())) {
						it.remove();
						modified = true;
					}
				}
				else {
					if (treeNode.getValue() == null) {
						it.remove();
						modified = true;
					}
				}
			}

			if (modifySupport != null && modified && !ignoreModification) {
				modifySupport.fire();
			}
		}
	}


	/**
	 * Removes all children from this node.
	 */
	public void removeAll() {
		if (children != null && !children.isEmpty()) {
			children.clear();

			if (modifySupport != null && !ignoreModification) {
				modifySupport.fire();
			}
		}
	}


	/**
	 * Refresh the internal TreeViewer starting with this node.
	 */
	public void refreshTreeViewer() {
		if (treeViewer.getTree().isDisposed()) {
			return;
		}

		// delay the refresh until the TreeViewer is not busy to avoid Exception
		TreeHelper.runWhenNotBusy(
			new Runnable() {
				@Override
				public void run() {
					treeViewer.refresh(TreeNode.this);
				}
			},
			treeViewer
		);
	}


	/**
	 * Update the internal TreeViewer starting with this node.
	 */
	public void updateTreeViewer() {
		if (treeViewer.getTree().isDisposed()) {
			return;
		}

		// delay the update until the TreeViewer is not busy to avoid Exception
		TreeHelper.runWhenNotBusy(
			new Runnable() {
				@Override
				public void run() {
					treeViewer.update(TreeNode.this, null);
				}
			},
			treeViewer
		);
	}


	public void setModifySupport(ModifySupport modifySupport) {
		this.modifySupport = modifySupport;
	}

	// *
	// * public interface
	// **************************************************************************

	/**
	 * Adds the given node as a new child to this node.
	 * @param child
	 */
	protected void addChild(TreeNode<?> child) {
		if (child == null) {
			throw new IllegalArgumentException("Parameter 'child' must not be null.");
		}
		else if (children == null) {
			children = new ArrayList<>();
		}

		child.setModifySupport(modifySupport);
		children.add(child);

		if (modifySupport != null && !ignoreModification) {
			modifySupport.fire();
		}
	}


	/**
	 * Returns true if this TreeNode has an ancestor (parent or grand parent, etc.)
	 * equal to the given TreeNode, and false otherwise.
	 * @param treeNode
	 * @return
	 */
	protected boolean hasParent(TreeNode<?> treeNode) {
		if (parent == null) {
			return false;
		}
		return parent.equals(treeNode) || parent.hasParent(treeNode);
	}


	protected void ensureCapacityOfChildren(int size) {
		if (children == null) {
			children = createArrayList(size);
		}
		else {
			children.ensureCapacity(size);
		}
	}


	protected int getChildCount() {
		int childCount = 0;
		if (children != null) {
			childCount = children.size();
		}
		return childCount;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("TreeNode[");
		sb.append("value: ").append(value);
		sb.append("]");

		return sb.toString();
	}


	@Override
	public boolean equals(Object object) {
		return (this == object);
	}


	// **************************************************************************
	// * Sorting
	// *

	/* (non-Javadoc)
	 * Compare TreeNodes by calling super.compare(TreeNode, TreeNode).
	 * Its default implementation (see below) compares TreeNodes by their text values.
	 * Overridden implementations should be able to compare every type of child-TreeNode.
	 *
	 * There is no need to override this method.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TreeNode<?> other) {
		if (parent != null) {
			// delegate comparation to the parent TreeNode, because it knows how to compare its children
			return parent.compareChildTreeNodes(this, other);
		}
		else {
			/* If there is no parent this is the root TreeNode which should never be compared with others,
			 * because a tree has always only one root.
			 */
			return 0;
		}
	}

	/**
	 * Compare two TreeNodes by their text values (current Locale).
	 * This method is used to sort the child-TreeNodes of this TreeNode. Therefore the method should
	 * capture all kinds of TreeNodes that are children of this TreeNode.
	 *
	 * This method has to be overridden if any two child-TreeNodes should not be compared by their
	 * text values (like this default implementation does).
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compareChildTreeNodes(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		// compare TreeNodes by their text by default
		return compareByText(treeNode1, treeNode2);
	}


	protected int compareByText(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		return ObjectComparator.getInstance().compare(treeNode1.getText(), treeNode2.getText());
	}


	/**
	 * Alternative compare method that can be used by TreeNodes that want their child TreeNodes to
	 * be compared by their values. Those TreeNodes just override compare(TreeNode, TreeNode) like
	 * this:
	 *
	 * @Override
	 * public int compare(TreeNode treeNode1, TreeNode treeNode2) {
	 * 		return compareByValue(treeNode1, treeNode2);
	 * }
	 *
	 * @param treeNode1
	 * @param treeNode2
	 * @return
	 */
	// TODO: delete
	protected int compareByValue(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		return ObjectComparator.getInstance().compare(treeNode1.getValue(), treeNode2.getValue());
	}

	// *
	// * Sorting
	// **************************************************************************

}
