package de.regasus.event.view;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.ClassKeyNameTransfer;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.tree.CopyIdToClipboardTreeKeyListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.tree.TreeNodeContentProvider;
import com.lambdalogic.util.rcp.tree.TreeNodeLabelProvider;
import com.lambdalogic.util.rcp.tree.TreeViewerSorter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.action.CustomFieldFolderRenameAction;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.action.LinkWithEditorAction;
import de.regasus.core.ui.view.AbstractLinkableView;
import de.regasus.event.EventFilter;
import de.regasus.event.view.pref.EventMasterDataViewPreference;

/**
 * This view shows a tree whose node types are listed here for convenience. Use the Javadoc popup to see hierachy, and
 * use the normal navigation (F3) to go to the source.
 *
 *
 * <ul>
 * <li>{@link EventGroupTreeNode}</li>
 * <ul>
 * <li>{@link EventTreeNode}</li>
 * <ul>
 * <li>{@link ProgrammePointListTreeNode}</li>
 * <ul>
 * <li>{@link ProgrammePointTreeNode}</li>
 * <ul>
 * <li>{@link WaitlistTreeNode}</li>
 * <li>{@link WorkGroupTreeNode}</li>
 * <li>{@link ProgrammeOfferingTreeNode}</li>
 * <ul>
 * <li>{@link ProgrammeCancelationTermTreeNode}</li>
 * </ul>
 * </ul>
 * </ul>
 * <li>{@link EventHotelInfoListTreeNode}</li>
 * <ul>
 * <li>{@link EventHotelInfoTreeNode}</li>
 * <ul>
 * <li>{@link HotelContingentTreeNode}</li>
 * <ul>
 * <li>{@link HotelOfferingTreeNode}</li>
 * <ul>
 * <li>{@link HotelCancelationTermTreeNode}</li>
 * </ul>
 * </ul>
 * </ul>
 * </ul>
 * <li>{@link InvoiceNoRangeListTreeNode}</li>
 * <ul>
 * <li>{@link InvoiceNoRangeTreeNode}</li>
 * </ul>
 * </ul>
 * <ul>
 * <li>{@link ParticipantCustomFieldListTreeNode}</li>
 * <li>{@link RegistrationFormConfigTreeNode}</li>
 * </ul>
 * </ul>
 * <ul>
 * <li>{@link LocationListTreeNode}</li>
 * <ul>
 * <li>{@link LocationTreeNode}</li>
 * </ul>
 * </ul>
 * </ul>
 *
 */
public class EventMasterDataView extends AbstractLinkableView {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	/* Structure of the tree:
	 *
     * EventGroupTreeNode
     * 		EventTreeNode
     *
     * 			EventHotelInfoListTreeNode
     * 				EventHotelInfoTreeNode
     * 					HotelContingentTreeNode
     * 						HotelOfferingTreeNode
     * 							HotelCancelationTermTreeNode
     *
     * 			InvoiceNoRangeListTreeNode
     * 				InvoiceNoRangeTreeNode
     *
     * 			LocationListTreeNode
     * 				LocationTreeNode
     * 					GateTreeNode
     *
     * 			ParticipantCustomFieldListTreeNode
     * 				ParticipantCustomFieldGroupLocationTreeNode
     * 					ParticipantCustomFieldGroupTreeNode
     * 						ParticipantCustomFieldTreeNode
     *
     * 			ProgrammePointListTreeNode
     * 				ProgrammePointTreeNode
     * 					WaitlistTreeNode
     * 					WorkGroupTreeNode
     * 					ProgrammeOfferingTreeNode
     * 						ProgrammeCancelationTermTreeNode
     *
     * 			RegistrationFormConfigListTreeNode
     * 				RegistrationFormConfigTreeNode
	 */

	public static final String ID = "EventMasterDataView";

	private EventMasterDataViewPreference preference;

	private EventFilter eventFilter = new EventFilter();

	// widgets
	private Text filterText;
	private Tree tree;

	// Actions
	private IWorkbenchAction editAction;
	private ActionFactory.IWorkbenchAction refreshAction;
	private ActionFactory.IWorkbenchAction linkWithEditorAction;
	private CustomFieldFolderRenameAction customFieldFolderRenameAction;

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


	public EventMasterDataView() {
		preference = EventMasterDataViewPreference.getInstance();
	}


	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible = getConfigParameterSet().getEvent().isVisible();
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

    			buildFilter(parent);

    			// create the tree, to have a widget for settings the focus
    			tree = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
    			GridDataFactory
    				.fillDefaults()
    				.span(NUM_COLUMN, 1)
    				.grab(true, true)
    				.applyTo(tree);

    			treeViewer = new TreeViewer(tree);
    			/* Bei Umstellung auf Multiselection den Parameter style anpassen und
    			 * alle Commands die im Kontextmenü des Trees verfügbar sind prüfen und anpassen.
    			 *
    			 * treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
    			 */

    			treeViewer.setData(EventFilter.class.getName(), eventFilter);
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
    			initDragAndDrop();

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


	private void buildFilter(Composite parent) throws Exception {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText(I18N.EventFilter);
		label.setToolTipText(I18N.EventFilter_Desc);

		filterText = new Text(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(filterText);

		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				eventFilter.setFilterText( filterText.getText() );
			}
		});
	}


	private void initializeActions() {
		if ( ! actionsInitialized) {
			IWorkbenchWindow window = getSite().getWorkbenchWindow();

			refreshAction = new EventMasterDataRefreshAction(window);

			editAction = new EventMasterDataEditAction(window);
			customFieldFolderRenameAction = new CustomFieldFolderRenameAction(window, tree);

			linkWithEditorAction = new LinkWithEditorAction(this);

			initializeToolBar();
			initializeMenu();

			actionsInitialized = true;
		}
	}


	private void initializeContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				EventMasterDataView.this.fillContextMenu(manager);
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
				editAction.run();
			}
		});
	}


	private void fillContextMenu(IMenuManager menuManager) {
		// The reason for such a segment (that lets dynamically fill the popup-menu every time anew
		// when it get's opened) is that you might add plugins at runtime, and new menu contributions
		// can herewith be shown immediately, without restart.

		menuManager.add(new GroupMarker("CreateAdditions"));
		menuManager.add(new GroupMarker("EditAdditions"));
		menuManager.add(new GroupMarker("ImportExportAdditions"));
		menuManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		menuManager.add(refreshAction);

		if (editAction.isEnabled()) {
			menuManager.add(editAction);
		}

		if (customFieldFolderRenameAction.isEnabled()) {
			menuManager.add(customFieldFolderRenameAction);
		}

		menuManager.update(true);
	}


	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

		toolBarManager.add(refreshAction);
		toolBarManager.add(editAction);
		toolBarManager.add(linkWithEditorAction);

		// update the tool bar, necessary when it changes after its first initialization
		toolBarManager.update(true);
	}


	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		menuManager.add(refreshAction);
		menuManager.add(editAction);

		menuManager.add(new Separator());
		menuManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		// update the menu, necessary when it changes after after its first initialization
		menuManager.update(true);
	}


	private void initData() {
		root = new EventTreeRoot(treeViewer, modifySupport);
		treeViewer.setInput(root);

		// expand first node visible to users
		List<TreeNode<?>> rootChildren = root.getChildren();
		if (rootChildren != null && !rootChildren.isEmpty()) {
			treeViewer.setExpandedState(rootChildren.get(0), true);
		}
	}



	private void initDragAndDrop() {
		final int DND_OPERATIONS = DND.DROP_MOVE;
		final Transfer[] DRAG_TRANSFERS = new Transfer[] { ClassKeyNameTransfer.getInstance(), TextTransfer.getInstance() };
		final Transfer[] DROP_TRANSFERS = new Transfer[] { ClassKeyNameTransfer.getInstance() };

		treeViewer.addDragSupport(DND_OPERATIONS, DRAG_TRANSFERS, new EventMasterDataDragListener(treeViewer));
		treeViewer.addDropSupport(DND_OPERATIONS, DROP_TRANSFERS, new EventMasterDataDropListener(treeViewer));
	}


	@Override
	public void setFocus() {
		boolean focusSet = false;

		try {
			if (tree != null) {
				if (!tree.isDisposed()) {
					if (tree.isEnabled()) {
						focusSet = tree.setFocus();
					}
					else {
						log.debug("tree is not enabled");
					}
				}
				else {
					log.debug("tree is disposed");
				}
			}
			else {
				log.debug("tree == null");
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
		preference.setEventFilter( filterText.getText() );
		preference.setLinkWithEditor( linkWithEditorAction.isChecked() );

		preference.save();
	}


	private void initFromPreferences() {
		try {
    		// eventFilter
    		filterText.setText( avoidNull(preference.getEventFilter()) );
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
