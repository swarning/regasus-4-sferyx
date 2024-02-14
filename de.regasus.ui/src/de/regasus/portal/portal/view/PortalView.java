package de.regasus.portal.portal.view;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.EditorHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.tree.CopyIdToClipboardTreeKeyListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.tree.TreeNodeContentProvider;
import com.lambdalogic.util.rcp.tree.TreeNodeLabelProvider;
import com.lambdalogic.util.rcp.tree.TreeViewerSorter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.action.LinkWithEditorAction;
import de.regasus.core.ui.view.AbstractLinkableView;
import de.regasus.event.view.PageLayoutTreeNode;
import de.regasus.event.view.PageTreeNode;
import de.regasus.event.view.PortalTreeNode;
import de.regasus.portal.Page;
import de.regasus.portal.PageLayout;
import de.regasus.portal.Portal;
import de.regasus.portal.page.editor.PageEditor;
import de.regasus.portal.page.editor.PageEditorInput;
import de.regasus.portal.pagelayout.editor.PageLayoutEditor;
import de.regasus.portal.pagelayout.editor.PageLayoutEditorInput;
import de.regasus.portal.portal.editor.PortalEditor;
import de.regasus.portal.portal.editor.PortalEditorInput;
import de.regasus.portal.portal.view.pref.PortalViewPreference;

/**
 *
 * Structure of the tree:
 *
 * PortalTreeRoot
 * 	PortalListTreeNode
 * 		PortalTreeNode
 * 			PageListTreeNode
 * 				PageTreeNode
 * 			PageLayoutListTreeNode
 * 				PageLayoutTreeNode
 *
 */
public class PortalView extends AbstractLinkableView {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public static final String ID = "PortalView";

	private PortalViewPreference preference;

	// Widgets
	private Tree tree;

	// Actions
	private ActionFactory.IWorkbenchAction linkWithEditorAction;

	/**
	 * false until Actions are initialized in initializeActions().
	 */
	private boolean actionsInitialized = false;


	/**
	 * The last value of visible as get from the ConfigParameterSet in isVisible().
	 * Has to be stored because the result of isVisible() should not change in the case that the
	 * getConfigParameterSet() returns null.
	 */
	private boolean visible = false;


	public PortalView() {
		preference = PortalViewPreference.getInstance();
	}


	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible = getConfigParameterSet().getPortal().isVisible();
		}
		return visible;
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
    		// If the view is configured to be visible, the main widget is a Tree, otherwise a Label with a text message.
    		if ( isVisible() ) {
    			final int NUM_COLUMN = 2;

    			parent.setLayout( new GridLayout(NUM_COLUMN, false) );

    			// create the tree, to have a widget for settings the focus
    			tree = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
    			GridDataFactory
    				.fillDefaults()
    				.span(NUM_COLUMN, 1)
    				.grab(true, true)
    				.applyTo(tree);

    			treeViewer = new TreeViewer(tree);

    			treeViewer.setContentProvider(new TreeNodeContentProvider());
        		treeViewer.setLabelProvider(new TreeNodeLabelProvider());

        		// copy ID to clipboard when user types ctrl+shift+c or ⌘+shift+c
        		tree.addKeyListener( new CopyIdToClipboardTreeKeyListener(treeViewer) );

        		// This call is needed to make tooltips work
        		ColumnViewerToolTipSupport.enableFor(treeViewer);

        		// ordering
        		treeViewer.setSorter(new TreeViewerSorter());

    			// create ModifySupport after creating eventTree and before init actions
    			modifySupport = new ModifySupport(tree);

    			initData();

        		// make the Tree the SelectionProvider
        		getSite().setSelectionProvider(treeViewer);

    			// create Actions and add them to different menus
    			initializeActions();

    			setContributionItemsVisible(true);

    			initializeContextMenu();
    			initializeDoubleClickAction();

    			initFromPreferences();

    			// observer ServerModel to init from preferences on login and save t preferences on logout
    			ServerModel.getInstance().addListener(serverModelListener);
    		}
    		else {
    			Label label = new Label(parent, SWT.NONE);
    			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void dispose() {
		try {
			ServerModel.getInstance().removeListener(serverModelListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}


	private void initializeActions() {
		if ( ! actionsInitialized) {
			IWorkbenchWindow window = getSite().getWorkbenchWindow();

			linkWithEditorAction = new LinkWithEditorAction(this);

			initializeToolBar();

			actionsInitialized = true;
		}
	}


	private void initializeContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				PortalView.this.fillContextMenu(manager);
			}
		});

		Tree tree = treeViewer.getTree();
		Menu menu = menuMgr.createContextMenu(tree);
		tree.setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}


	private void initializeDoubleClickAction() {
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = treeViewer.getSelection();
				Object node = SelectionHelper.getUniqueSelected(selection);

				if (node instanceof PortalTreeNode) {
					Portal portal = ((PortalTreeNode) node).getValue();
					PortalEditorInput editorInput = PortalEditorInput.getEditInstance(portal.getId());
					EditorHelper.openEditor(editorInput, PortalEditor.ID);
				}
				else if (node instanceof PageLayoutTreeNode) {
					PageLayout pageLayout = ((PageLayoutTreeNode) node).getValue();
					PageLayoutEditorInput editorInput = PageLayoutEditorInput.getEditInstance(pageLayout.getId());
					EditorHelper.openEditor(editorInput, PageLayoutEditor.ID);
				}
				else if (node instanceof PageTreeNode) {
					Page page = ((PageTreeNode) node).getValue();
					PageEditorInput editorInput = PageEditorInput.getEditInstance(page.getId());
					EditorHelper.openEditor(editorInput, PageEditor.ID);
				}
			}
		});
	}


	private void fillContextMenu(IMenuManager menuManager) {
		// The reason for such a segment (that lets dynamically fill the popup-menu every time anew
		// when it get's opened) is that you might add plugins at runtime, and new menu contributions
		// can herewith be shown immediately, without restart.

		menuManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		menuManager.update(true);
	}


	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

		toolBarManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		toolBarManager.add(linkWithEditorAction);

		// update the tool bar, necessary when it changes after its first initialization
		toolBarManager.update(true);
	}


	private void initData() {
		root = new PortalTreeRoot(treeViewer, modifySupport);
		treeViewer.setInput(root);

		// expand first node visible to users
		List<TreeNode<?>> rootChildren = root.getChildren();
		if (rootChildren != null && !rootChildren.isEmpty()) {
			treeViewer.setExpandedState(rootChildren.get(0), true);
		}
	}


	@Override
	public void setFocus() {
		boolean focusSet = false;

		try {
			if (tree != null && !tree.isDisposed() && tree.isEnabled()) {
				focusSet = tree.setFocus();
    		}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		if (!focusSet) {
			log.error("Focus not set properly");
		}
	}


	public TreeViewer getViewer() {
		return treeViewer;
	}


	// *****************************************************************************************************************
	// * Preferences
	// *

	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			ServerModelEvent serverModelEvent = (ServerModelEvent) event;
			if (serverModelEvent.getType() == ServerModelEventType.BEFORE_LOGOUT) {
				// save values to preferences before the logout will remove them
				savePreferences();
			}
			else if (serverModelEvent.getType() == ServerModelEventType.LOGIN) {
				SWTHelper.asyncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						initFromPreferences();
					}
				});
			}
		}
	};


	private void savePreferences() {
		preference.setLinkWithEditor( linkWithEditorAction.isChecked() );

		preference.save();
	}


	private void initFromPreferences() {
		try {
    		// eventFilter
    		linkWithEditorAction.setChecked( preference.isLinkWithEditor() );
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Preferences
	// *****************************************************************************************************************

}
