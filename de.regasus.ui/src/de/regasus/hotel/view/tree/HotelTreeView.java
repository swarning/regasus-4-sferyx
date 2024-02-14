package de.regasus.hotel.view.tree;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.tree.CopyIdToClipboardTreeKeyListener;
import com.lambdalogic.util.rcp.tree.TreeNodeContentProvider;
import com.lambdalogic.util.rcp.tree.TreeNodeLabelProvider;
import com.lambdalogic.util.rcp.tree.TreeViewerSorter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.action.LinkWithEditorAction;
import de.regasus.core.ui.view.AbstractLinkableView;


public class HotelTreeView extends AbstractLinkableView {

	public static final String ID = "HotelTreeView";


	// widgets
	private Tree hotelTree;


	// *************************************************************************
	// * Actions
	// *

	private CreateHotelAction createHotelAction;

	private CreateRoomDefinitionAction createRoomDefinitionAction;

	private EditHotelOrRoomDefinitionAction editHotelOrRoomDefinitionAction;

	private DeleteHotelOrRoomDefinitionAction deleteHotelOrRoomDefinitionAction;

	private RefreshTreeAction refreshTreeAction;

	private ActionFactory.IWorkbenchAction linkWithEditorAction;

	private ResizePhotosAction resizePhotosAction;

	// *
	// * Actions
	// *************************************************************************


	/**
	 * Variable used to hold the initial value for the linkWithEditorAction that is stored in the
	 * system environment (memento).
	 */
	private Boolean linkWithEditorAction_initialValue;


	/**
	 * The last value of visible as get from the ConfigParameterSet in isVisible().
	 * Has to be stored because the result of isVisible() should not change in the case that the
	 * getConfigParameterSet() returns null.
	 */
	private boolean visible = false;


	public HotelTreeView() {
	}


	/* (non-Javadoc)
	 * @see de.regasus.core.ui.view.AbstractView#isVisible()
	 */
	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible = getConfigParameterSet().getHotel().isVisible();
		}
		return visible;
	}


	@Override
	public void createWidgets(Composite parent) {
		if (isVisible()) {
			// create the tree, to have a widget for settings the focus
			parent.setLayout(new FillLayout());
			hotelTree = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

			treeViewer = new TreeViewer(hotelTree);

			treeViewer.setContentProvider(new TreeNodeContentProvider());
			treeViewer.setLabelProvider(new TreeNodeLabelProvider());

			// copy ID to clipboard when user types ctrl+shift+c or ⌘+shift+c
			hotelTree.addKeyListener( new CopyIdToClipboardTreeKeyListener(treeViewer) );

    		// This call is needed to make tooltips work
    		ColumnViewerToolTipSupport.enableFor(treeViewer);

    		// ordering
			treeViewer.setSorter(new TreeViewerSorter());

			// create ModifySupport after creating hotelTree and before init actions
			modifySupport = new ModifySupport(hotelTree);

			initData();

			// create Actions and add them to different menus
			initializeActions();
			setContributionItemsVisible(true);

			hookContextMenu();
			initializeDoubleClickAction();


			getSite().setSelectionProvider(treeViewer);
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
					root = new HotelTreeRoot(treeViewer, modifySupport);

					treeViewer.setInput(root);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	@Override
	public void setFocus() {
		try {
			if (hotelTree != null && !hotelTree.isDisposed() && hotelTree.isEnabled()) {
				hotelTree.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	/**
	 * Create the actions
	 */
	private void initializeActions() {
		if (createHotelAction == null) {
			IWorkbenchWindow window = getSite().getWorkbenchWindow();

			createHotelAction = new CreateHotelAction(window);
			createRoomDefinitionAction = new CreateRoomDefinitionAction(window);

			editHotelOrRoomDefinitionAction = new EditHotelOrRoomDefinitionAction(window);
			deleteHotelOrRoomDefinitionAction = new DeleteHotelOrRoomDefinitionAction(window);

			refreshTreeAction = new RefreshTreeAction();

			linkWithEditorAction = new LinkWithEditorAction(this);
			linkWithEditorAction.setChecked(Boolean.TRUE.equals(linkWithEditorAction_initialValue)); // maybe null

			resizePhotosAction = new ResizePhotosAction(window);

			getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());

			initializeMenu();
			initializeToolBar();
		}
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				HotelTreeView.this.fillContextMenu(manager);
			}
		});

		Menu menu = menuMgr.createContextMenu(hotelTree);
		hotelTree.setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}


	private void initializeDoubleClickAction() {
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				HotelTreeView.this.editHotelOrRoomDefinitionAction.run();
			}
		});
	}


	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		manager.add(createHotelAction);
		manager.add(createRoomDefinitionAction);
		manager.add(new Separator());
		manager.add(editHotelOrRoomDefinitionAction);
		manager.add(deleteHotelOrRoomDefinitionAction);
		manager.add(new Separator());
		manager.add(refreshTreeAction);
		manager.add(linkWithEditorAction);

		// update the tool bar, necessary when it changes after its first initialization
		manager.update(true);
	}


	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();

		manager.add(createHotelAction);
		manager.add(createRoomDefinitionAction);
		manager.add(new Separator());
		manager.add(editHotelOrRoomDefinitionAction);
		manager.add(deleteHotelOrRoomDefinitionAction);
		manager.add(new Separator());
		manager.add(refreshTreeAction);
		manager.add(new Separator());
		manager.add(resizePhotosAction);

		// update the menu, necessary when it changes after after its first initialization
		manager.update(true);
	}


	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshTreeAction);

		if (editHotelOrRoomDefinitionAction.isEnabled()) {
			manager.add(editHotelOrRoomDefinitionAction);
		}
		if (deleteHotelOrRoomDefinitionAction.isEnabled()) {
			manager.add(deleteHotelOrRoomDefinitionAction);
		}
		if (createHotelAction.isEnabled()) {
			manager.add(createHotelAction);
		}
		if (createRoomDefinitionAction.isEnabled()) {
			manager.add(createRoomDefinitionAction);
		}
		// Other plug-ins can contribute there actions here
//		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

}
