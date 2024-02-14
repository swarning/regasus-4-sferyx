package de.regasus.core.ui.view;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.tree.TreeHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.action.LinkWithEditorAction;


public abstract class AbstractLinkableView extends AbstractView {

	protected TreeNode<?> root;
	protected TreeViewer treeViewer;
	/**
	 * EntityType used in find() and show()
	 */
	private Class<?> _entityType;
	/**
	 * Key used in find() and show()
	 */
	private Object _key;
	/**
	 * Current path used in find() and show()
	 */
	private List<TreeNode<?>> _path;


	/**
	 * Support for ModifyListener.
	 * The creation of ModifySupport is in the responsibility of the implementing sub-class.
	 * ModifySupport should be created in {@link #createWidgets(Composite)}, in fact after creating
	 * the {@link Tree} widget and before the initialization of the {@link LinkWithEditorAction},
	 * because the latter will call {@link #addModifyListener(ModifyListener)}.
	 * Moreover the {@link #modifySupport} has to be set as parameter when creating the root
	 * {@link TreeNode}. This will propagate the {@link #modifySupport} to every sub-TreeNode. As a
	 * consequence {@link #addModifyListener(ModifyListener)}s get informed when {@link TreeNode}
	 * are created or deleted.
	 */
	protected ModifySupport modifySupport;


	public AbstractLinkableView() {
	}


	@Override
	public void dispose() {
		if (root != null) {
			try {
				root.dispose();
			}
			catch (Exception e) {
			}
		}
		super.dispose();
	}


	public void show(final Class<?> entityType, final Object key) {
		try {
			// delay the update until the TreeViewer is not busy to avoid Exception
			TreeHelper.runWhenNotBusy(
				new Runnable() {
					@Override
					public void run() {
						_show(entityType, key);
					}
				},
				treeViewer
			);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	protected void _show(Class<?> entityType, Object key) {
		try {
			if (entityType != null && key != null) {

				// If the currently selected element is the one that shall be selected: return.
				ISelection selection = treeViewer.getSelection();
				if (selection instanceof TreeSelection) {
					TreeSelection treeSelection = (TreeSelection) selection;
					Iterator it = treeSelection.iterator();
					while (it.hasNext()) {
						Object element = it.next();
						if (element instanceof TreeNode) {
							TreeNode treeNode = (TreeNode) element;
							Class selEntityType = treeNode.getEntityType();
							Object selKey = treeNode.getKey();

							if (entityType == selEntityType && key.equals(selKey)) {
								return;
							}
						}
					}
				}


				if (modifySupport != null) {
					modifySupport.suppress();
				}


				// initialize fields for find()
				_entityType = entityType;
				_key = key;

				if (_path == null) {
					_path = CollectionsHelper.createArrayList();
				}
				_path.add(root);

				// start the search
				if ( find() ) {
					TreePath treePath = new TreePath(_path.toArray());
					TreeSelection treeSelection = new TreeSelection(treePath);
					treeViewer.setSelection(treeSelection, true/* reveal (make visible) */);
				}

				// reset fields for find()
				_entityType = null;
				_key = null;

				// delete all elements
				_path.clear();

				if (modifySupport != null) {
					modifySupport.stopSuppressionAndFire();
				}
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	/**
	 * Searches recursive for a TreeNode with a given EntityType and Key.
	 *
	 * @return
	 */
	private boolean find() {
		TreeNode<?> treeNode = _path.get(_path.size() - 1);
		if (treeNode.hasChildren()) {
			List<TreeNode<?>> children = treeNode.getLoadedChildren();
			for (TreeNode<?> childTreeNode : children) {
				_path.add(childTreeNode);
				if (childTreeNode.getEntityType() == _entityType && _key.equals(childTreeNode.getKey())) {
					return true;
				}
				else if (find()) {
					return true;
				}
				_path.remove(_path.size() - 1);
			}
		}

		return false;
	}


	public void addModifyListener(ModifyListener listener) {
		if (modifySupport != null) {
			modifySupport.addListener(listener);
		}
		else {
			System.err.println("The class " + getClass().getName() + " does not support ModifyListener.");
		}
	}


	public void removeModifyListener(ModifyListener listener) {
		if (modifySupport != null) {
			modifySupport.removeListener(listener);
		}
	}

}
