package de.regasus.profile.customfield.view;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchWindow;

import com.lambdalogic.util.rcp.ClassKeyNameTransfer;
import com.lambdalogic.util.rcp.tree.CopyIdToClipboardTreeKeyListener;
import com.lambdalogic.util.rcp.tree.TreeNodeContentProvider;
import com.lambdalogic.util.rcp.tree.TreeNodeLabelProvider;
import com.lambdalogic.util.rcp.tree.TreeViewerSorter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.action.CustomFieldFolderRenameAction;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.view.AbstractView;

public class ProfileCustomFieldTreeView extends AbstractView {

	public static final String ID = "ProfileCustomFieldTreeView";

	private ProfileCustomFieldTreeRoot root;

	// widgets
	private Tree tree;

	private TreeViewer treeViewer;

	// Actions
	private CreateProfileCustomFieldGroupAction createProfileCustomFieldGroupAction;

	private CreateProfileCustomFieldAction createProfileCustomFieldAction;

	private EditProfileCustomFieldOrGroupAction editProfileCustomFieldOrGroupAction;

	private RefreshTreeAction refreshTreeAction;

	private CustomFieldFolderRenameAction customFieldFolderRenameAction;


	/**
	 * The last value of visible as get from the ConfigParameterSet in isVisible(). Has to be stored because the result
	 * of isVisible() should not change in the case that the getConfigParameterSet() returns null.
	 */
	private boolean visible = false;


	@Override
	protected boolean isVisible() {
		/*
		 * Determine the visibility from the ConfigParameterSet. If getConfigParameterSet() returns null, its last
		 * result (the last value of visible) is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible = 	getConfigParameterSet().getProfile().isVisible() &&
						getConfigParameterSet().getProfile().getCustomField().isVisible();
		}
		return visible;
	}


	@Override
	public void createWidgets(Composite parent) {
		if (isVisible()) {
			// create the tree, to have a widget for settings the focus
			parent.setLayout(new FillLayout());
			tree = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

			treeViewer = new TreeViewer(tree);
			treeViewer.setContentProvider(new TreeNodeContentProvider());
			treeViewer.setLabelProvider(new TreeNodeLabelProvider());
			treeViewer.setSorter(new TreeViewerSorter());

			tree.addKeyListener( new CopyIdToClipboardTreeKeyListener(treeViewer) );


			try {
				initData();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			// create Actions and add them to different menus
			initializeActions();
			setContributionItemsVisible(true);

			hookContextMenu();
			initializeDoubleClickAction();

			getSite().setSelectionProvider(treeViewer);

			initDragAndDrop();
		}
		else {
			Label label = new Label(parent, SWT.NONE);
			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
		}
	}


	private void initData() {
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					root = new ProfileCustomFieldTreeRoot(
						treeViewer,
						null // add ModifySupport here when implementing AbstractLinkableView in
							 // ProfileCustomFieldTreeView
					);
					treeViewer.setInput(root);

					treeViewer.refresh();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
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


	@Override
	public void setFocus() {
		try {
			if (tree != null && !tree.isDisposed() && tree.isEnabled()) {
				tree.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	private void initializeActions() {
		if (createProfileCustomFieldGroupAction == null) {
			IWorkbenchWindow window = getSite().getWorkbenchWindow();
			createProfileCustomFieldGroupAction = new CreateProfileCustomFieldGroupAction(window);
			createProfileCustomFieldAction = new CreateProfileCustomFieldAction(window);
			editProfileCustomFieldOrGroupAction = new EditProfileCustomFieldOrGroupAction(window);
			refreshTreeAction = new RefreshTreeAction();
			customFieldFolderRenameAction = new CustomFieldFolderRenameAction(window, tree);
			initializeToolBar();
		}
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ProfileCustomFieldTreeView.this.fillContextMenu(manager);
			}
		});

		Menu menu = menuMgr.createContextMenu(tree);
		tree.setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}


	private void initializeDoubleClickAction() {
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editProfileCustomFieldOrGroupAction.run();
			}
		});
	}


	private void initializeToolBar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		manager.add(createProfileCustomFieldGroupAction);
		manager.add(createProfileCustomFieldAction);
		manager.add(new Separator());
		manager.add(editProfileCustomFieldOrGroupAction);
		manager.add(new Separator());
		manager.add(refreshTreeAction);

		// update the tool bar, necessary when it changes after its first initialization
		manager.update(true);
	}


	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshTreeAction);

		if (editProfileCustomFieldOrGroupAction.isEnabled()) {
			manager.add(editProfileCustomFieldOrGroupAction);
		}

		if (createProfileCustomFieldGroupAction.isEnabled()) {
			manager.add(createProfileCustomFieldGroupAction);
		}

		if (createProfileCustomFieldAction.isEnabled()) {
			manager.add(createProfileCustomFieldAction);
		}

		if (customFieldFolderRenameAction.isEnabled()) {
			manager.add(customFieldFolderRenameAction);
		}

		manager.update(true);

		// menu items for Copy, Paste and Delete are added as Commands in plugin.xml
	}


	/**
	 * Initialize Drag-and-Drop support.
	 */
	private void initDragAndDrop() {
		final int DND_OPERATIONS = DND.DROP_MOVE | DND.DROP_COPY;
		final Transfer[] DRAG_TRANSFERS = new Transfer[] { ClassKeyNameTransfer.getInstance(), TextTransfer.getInstance() };
		final Transfer[] DROP_TRANSFERS = new Transfer[] { ClassKeyNameTransfer.getInstance() };

		treeViewer.addDragSupport(DND_OPERATIONS, DRAG_TRANSFERS, new ProfileCustomFieldDragListener(treeViewer));
		treeViewer.addDropSupport(DND_OPERATIONS, DROP_TRANSFERS, new ProfileCustomFieldDropListener(treeViewer));
	}

}
