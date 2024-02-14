package de.regasus.report.view;

import static com.lambdalogic.util.rcp.KeyEventHelper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.ReportI18N;
import de.regasus.report.model.UserReportDirListModel;
import de.regasus.report.model.UserReportListModel;
import de.regasus.report.ui.Activator;

/**
 * The view for "Berichtsdefinitionen".
 *
 * @author manfred
 *
 */
public class UserReportTreeView extends ViewPart implements CacheModelListener<Long> {
	private static Logger log = Logger.getLogger("ui.UserReportTreeView");

	public static final String ID = "UserReportTreeView";

	// Model Data
	private UserReportDirListModel userReportDirListModel = UserReportDirListModel.getInstance();

	private UserReportListModel userReportListModel = UserReportListModel.getInstance();

	// Widgets
	private TreeViewer treeViewer;

	// Actions
	private ActionFactory.IWorkbenchAction generateReportAction;

	private ActionFactory.IWorkbenchAction createUserReportDirAction;

	private ActionFactory.IWorkbenchAction createUserReportAction;

	private ActionFactory.IWorkbenchAction editUserReportAction;

	private ActionFactory.IWorkbenchAction deleteUserReportAction;

	private ActionFactory.IWorkbenchAction refreshUserReportListAction;

	private LinkWithEditorAction linkWithEditorAction;

	private CopyUserReportAction copyUserReportAction;

	private PasteUserReportAction pasteUserReportAction;

	private Tree tree;

	private TreeEditor editor;


	// Text for editing directory name
	private Text text;

	// UserReportDirVO whose directory name is currently edited
	private UserReportDirVO userReportDirVO;


	public UserReportTreeView() {
	}


	@Override
	public void createPartControl(Composite parent) {
		setTitleToolTip(ReportI18N.UserReportTreeView_ToolTip);

		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		tree = treeViewer.getTree();

		treeViewer.setContentProvider(new UserReportTreeContentProvider());
		treeViewer.setLabelProvider(new UserReportTreeLabelProvider());

		// Sortierung
		treeViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1.getClass() == e2.getClass()) {
					return super.compare(viewer, e1, e2);
				}
				else if (e1 instanceof UserReportDirVO) {
					return -1;
				}
				else if (e1 instanceof UserReportVO) {
					return 1;
				}
				return super.compare(viewer, e1, e2);
			}
		});

		initEditor();

		tree.addKeyListener(keyListener);
		tree.addSelectionListener(selectionAdapter);

		// make the Tree the SelectionProvider
		getSite().setSelectionProvider(treeViewer);

		addDragAndDropSupport();
		addCopyAndPasteSupport();

		createActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();



		loadTreeData();

		userReportDirListModel.addListener(this);
		userReportListModel.addListener(this);

		tree.setSize(200, 200);
	}


	private void initEditor() {
		/*
		 * Das Editieren von UserReports per Doppelklick und Enter wird bereits durch die Action geregelt. Sollen
		 * weitere Tastenkommandos das Editieren unabhängig von der Action auslösen, muss ein Listener gemäß dem
		 * folgenden Beispiel implementiert werden.
		 */

		editor = new TreeEditor(tree);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
	}


	private KeyListener keyListener = new KeyAdapter() {
		@Override
		public void keyPressed(org.eclipse.swt.events.KeyEvent e) {

			// debug
			//System.out.println("character: " + e.character + ", keyCode: " + e.keyCode + ", statemask: " + e.stateMask);

			// F2: Rename Directory
			if ( isF2(e) ) {
				renameUserReportDirVOTreeNode();
			}
			// DEL: Delete Directory or Report Definition
			else if ( isDelete(e) ) {
				TreeItem[] selection = treeViewer.getTree().getSelection();
				if (selection.length != 1) {
					return;
				}
				TreeItem item = tree.getSelection()[0];
				Object data = item.getData();
				if (data instanceof UserReportDirVO && ((UserReportDirVO) data).getParentID() != null) {
					deleteUserReportAction.run();
				}
				else if (data instanceof UserReportVO) {
					deleteUserReportAction.run();
				}
			}
			// INS: create Report Definition
			// Strg+INS: create Directory
			else if ( isInsert(e) || isCtrlInsert(e) ) {
				TreeItem[] selection = treeViewer.getTree().getSelection();
				if (selection.length != 1) {
					return;
				}
				TreeItem item = tree.getSelection()[0];
				Object data = item.getData();

				if ( isInsert(e) ) {
					// ignore generic root directory
					if (data instanceof UserReportDirVO && ((UserReportDirVO) data).getParentID() == null) {
						return;
					}
					createUserReportAction.run();
				}
				else if ( isCtrlInsert(e) ) {
					createUserReportDirAction.run();
				}
			}
			else if ( isCopyPK(e) ) {
				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
				Object selectedElement = selection.getFirstElement();
				if (selectedElement instanceof UserReportDirVO) {
					UserReportDirVO userReportDirVO = (UserReportDirVO) selectedElement;
					if (userReportDirVO != null && userReportDirVO.getID() != null) {
						ClipboardHelper.copyToClipboard( userReportDirVO.getID().toString() );
					}
				}
				else if (selectedElement instanceof UserReportVO) {
					UserReportVO userReportVO = (UserReportVO) selectedElement;
					if (userReportVO != null && userReportVO.getID() != null) {
						ClipboardHelper.copyToClipboard( userReportVO.getID().toString() );
					}
				}
			}
		}

	};

	private SelectionAdapter selectionAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			TreeItem[] selection = tree.getSelection();
			if (selection != null && selection.length > 0) {
				Object data = selection[0].getData();
				if (!data.equals(userReportDirVO) && text != null) {
					text.dispose();
				}
			}
		}
	};


	private void addDragAndDropSupport() {
		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		Transfer[] transfers = new Transfer[] { ReportTreeTransfer.getInstance(), TextTransfer.getInstance() };
		UserReportDragListener userReportDragListener = new UserReportDragListener(treeViewer);
		treeViewer.addDragSupport(operations, transfers, userReportDragListener);

		UserReportTreeDropAdapter userReportTreeDropAdapter = new UserReportTreeDropAdapter(treeViewer);
		treeViewer.addDropSupport(operations, transfers, userReportTreeDropAdapter);
	}


	private void addCopyAndPasteSupport() {
		IActionBars bars = getViewSite().getActionBars();

		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		copyUserReportAction = new CopyUserReportAction(treeViewer, window);
		pasteUserReportAction = new PasteUserReportAction(treeViewer);
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyUserReportAction);
		bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteUserReportAction);

		// copy ID to clipboard when user types ctrl+shift+c or ⌘+shift+c is handled in keyListener!
	}


	@Override
	public void dispose() {
		// dispose models
		if (userReportDirListModel != null) {
			userReportDirListModel.removeListener(this);
		}
		if (userReportListModel != null) {
			userReportListModel.removeListener(this);
		}

		// dispose Actions
		if (generateReportAction != null) {
			generateReportAction.dispose();
		}
		if (createUserReportDirAction != null) {
			createUserReportDirAction.dispose();
		}
		if (createUserReportAction != null) {
			createUserReportAction.dispose();
		}
		if (editUserReportAction != null) {
			editUserReportAction.dispose();
		}
		if (deleteUserReportAction != null) {
			deleteUserReportAction.dispose();
		}
		if (refreshUserReportListAction != null) {
			refreshUserReportListAction.dispose();
		}
		if (linkWithEditorAction != null) {
			linkWithEditorAction.dispose();
		}

		super.dispose();
	}


	private void loadTreeData() {
		try {
			UserReportDirVO root = userReportDirListModel.getRoot();
			treeViewer.setInput(root);
			UserReportDirVO visibleRoot = userReportDirListModel.getVisibleRoot();
			if (visibleRoot != null) {
				treeViewer.setExpandedState(visibleRoot, true);
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}





	/**
	 * Open or close a directory.
	 */
	public void toggleDirExpansion(UserReportDirVO userReportDirVO) {
		boolean expandedState = treeViewer.getExpandedState(userReportDirVO);
		treeViewer.setExpandedState(userReportDirVO, !expandedState);
	}


	@Override
	public void dataChange(final CacheModelEvent<Long> event) {
		Runnable dataChangeRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					CacheModelOperation operation = event.getOperation();

					if (event.getSource() == userReportDirListModel) {
						/*
						 * If a directory was created or deleted, refresh the directory where it is/was contained.
						 */
						if (operation == CacheModelOperation.CREATE || operation == CacheModelOperation.DELETE) {
							List<Long> keyList = event.getKeyList();
							if (keyList == null || keyList.isEmpty() || keyList.size() > 1) {
								/*
								 * Wenn wir keine genaueren Infos über die erzeugten/gelöschten Entities haben oder mehr
								 * als ein Entity erzeugt/gelöscht wurde, den gesamten Tree aktualisieren.
								 */
								treeViewer.refresh(true);
							}
							else {
								/*
								 * Wenn genau ein Entity erzeugt oder gelöscht wurde, nur das übergeordnete Verzeichnis
								 * aktualisieren.
								 */
								Long userReportDirPK = event.getFirstKey();
								UserReportDirVO userReportDirVO = userReportDirListModel.getUserReportDirVO(userReportDirPK);
								userReportDirVO = userReportDirListModel.getUserReportDirVO(userReportDirVO.getParentID());
								treeViewer.refresh(userReportDirVO, true);
							}
						}
						/*
						 * If a dir was updated, change its node.
						 */
						else if (operation == CacheModelOperation.UPDATE) {
							List<Long> keyList = event.getKeyList();
							if (keyList == null || keyList.size() > 1) {
								/*
								 * Wenn wir keine genaueren Infos über die erzeugten/gelöschten Entities haben oder mehr
								 * als ein Entity erzeugt/gelöscht wurde, den gesamten Tree aktualisieren.
								 */
								treeViewer.refresh(true);
							}
							else {
								/*
								 * Wenn genau ein Entity erzeugt oder gelöscht wurde, nur das übergeordnete Verzeichnis
								 * aktualisieren.
								 */
								Long userReportDirPK = event.getFirstKey();
								UserReportDirVO userReportDirVO = userReportDirListModel.getUserReportDirVO(userReportDirPK);
								treeViewer.refresh(userReportDirVO, true);
							}
						}
						else {
							loadTreeData();
						}
					}
					else if (event.getSource() == userReportListModel) {
						if (operation == CacheModelOperation.CREATE ||
							operation == CacheModelOperation.DELETE
						) {
							List<?> keyList = event.getKeyList();
							if (keyList == null || keyList.size() > 1) {
								/*
								 * Wenn wir keine genaueren Infos über die erzeugten/gelöschten Entities haben oder mehr
								 * als ein Entity erzeugt/gelöscht wurde, den gesamten Tree aktualisieren.
								 */
								treeViewer.refresh(true);
							}
							else {
								/*
								 * Wenn genau ein Entity erzeugt oder gelöscht wurde, nur das übergeordnete Verzeichnis
								 * aktualisieren.
								 */
								Long userReportPK = event.getFirstKey();
								if (userReportPK != null) {
									UserReportVO userReportVO = userReportListModel.getUserReportVO(userReportPK);
									Long userReportDirID = userReportVO.getUserReportDirID();
									UserReportDirVO userReportDirVO = userReportDirListModel.getUserReportDirVO(userReportDirID);
									treeViewer.refresh(userReportDirVO, true);

									if (operation == CacheModelOperation.CREATE) {
										show(userReportPK);
									}
								}
							}
						}
						/*
						 * If a report was updated, changing its node may not be sufficient, since we have drag and dop
						 * (by updating the dir), and so there may be structural changes in the whole tree.
						 */
						else if (operation == CacheModelOperation.UPDATE) {
							treeViewer.refresh(true);
						}
						else {
							loadTreeData();
						}
					}
					else {
						log.warning("Unknown DataChangeEvent caused UserReportTree to initialize.");
						loadTreeData();
					}
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
			}
		};

		if (Display.getCurrent() != null) {
			dataChangeRunnable.run();
		}
		else {
			SWTHelper.syncExecDisplayThread(dataChangeRunnable);
		}
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


	public void show(Long userReportPK) {
		if (userReportPK == null) {
			return;
		}

		try {
			// get the UserReportVO
			UserReportVO userReportVO = userReportListModel.getUserReportVO(userReportPK);

			// create the pathList
			List<Object> pathList = new ArrayList<>();
			pathList.add(userReportVO);
			UserReportDirListModel userReportDirListModel = UserReportDirListModel.getInstance();

			Long userReportDirPK = userReportVO.getUserReportDirID();
			while (userReportDirPK != null) {
				UserReportDirVO userReportDirVO = userReportDirListModel.getUserReportDirVO(userReportDirPK);
				pathList.add(userReportDirVO);
				userReportDirPK = userReportDirVO.getParentID();
			}
			Object[] pathArray = new Object[pathList.size()];
			for (int i = 0; i < pathArray.length; i++) {
				pathArray[i] = pathList.get(pathList.size() - 1 - i);
			}

			TreePath treePath = new TreePath(pathArray);
			TreeSelection selection = new TreeSelection(treePath);
			treeViewer.setSelection(selection, true/* reveal (make visible) */);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public void show(UserReportDirVO userReportDirVO) {
		try {
			List<Object> pathList = new ArrayList<>();
			UserReportDirListModel userReportDirListModel = UserReportDirListModel.getInstance();
			while (userReportDirVO != null) {
				pathList.add(userReportDirVO);
				Long parentID = userReportDirVO.getParentID();
				if (parentID != null) {
					userReportDirVO = userReportDirListModel.getUserReportDirVO(parentID);
				}
				else {
					userReportDirVO = null;
				}
			}
			Collections.reverse(pathList);


			Object[] pathArray = pathList.toArray();
			TreePath treePath = new TreePath(pathArray);
			final TreeSelection selection = new TreeSelection(treePath);

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					treeViewer.setSelection(selection, true/* reveal (make visible) */);
				}
			});
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	private void createActions() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		generateReportAction = new GenerateReportAction(window);
		createUserReportDirAction = new CreateUserReportDirAction(this);
		createUserReportAction = new CreateUserReportAction(window);
		editUserReportAction = new EditUserReportAction(window, this);
		deleteUserReportAction = new DeleteUserReportAction(window, this);
		refreshUserReportListAction = new RefreshUserReportListAction();

		linkWithEditorAction = new LinkWithEditorAction(this);
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				UserReportTreeView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}


	private void hookDoubleClickAction() {
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				final Object firstElement = selection.getFirstElement();
				if (firstElement instanceof UserReportVO) {
					editUserReportAction.run();
				}
				else if (firstElement instanceof UserReportDirVO) {
					toggleDirExpansion((UserReportDirVO) firstElement);
				}
			}
		});
	}


	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}


	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(generateReportAction);
		manager.add(createUserReportDirAction);
		manager.add(createUserReportAction);
		manager.add(editUserReportAction);
		manager.add(deleteUserReportAction);
		manager.add(refreshUserReportListAction);
		// manager.add(new Separator());
	}


	private void fillContextMenu(IMenuManager manager) {
		manager.add(generateReportAction);
		manager.add(createUserReportDirAction);
		manager.add(createUserReportAction);
		manager.add(editUserReportAction);
		manager.add(deleteUserReportAction);
		manager.add(refreshUserReportListAction);
		manager.add(new Separator());

		manager.add(copyUserReportAction);
		manager.add(pasteUserReportAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}


	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(generateReportAction);
		manager.add(createUserReportDirAction);
		manager.add(createUserReportAction);
		manager.add(editUserReportAction);
		manager.add(deleteUserReportAction);
		manager.add(refreshUserReportListAction);

		manager.add(linkWithEditorAction);
	}


	/**
	 * Is called via context menu and EditUserReportAction, or via F2
	 */
	public void renameUserReportDirVOTreeNode() {
		final TreeItem[] selection = tree.getSelection();
		if (selection.length != 1) {
			return;
		}
		final TreeItem item = selection[0];
		final Object data = item.getData();
		if (data instanceof UserReportDirVO && ((UserReportDirVO) data).getParentID() != null) {
			userReportDirVO = (UserReportDirVO) data;

			/*
			 * Instead of leaving the editing to the JFace TreeViewer, we handle everything with basic SWT
			 * means, so that we don't need a column, so that we don't need to set a fixed width, which
			 * was a problem with a scollbar appearing without actual need.
			 */
			final UserReportDirVO userReportDirVO = (UserReportDirVO) data;

			// Create a text with which to edit

			text = new Text(tree, SWT.NONE);
			text.setText(userReportDirVO.getName());
			text.selectAll();
			text.setFocus();

			// Set the two listeners for finishing editing
			text.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent event) {
					String editedName = text.getText();
					if (!editedName.equals(userReportDirVO.getName())) {
						updateItemAndVO(item, userReportDirVO, editedName);
						text.dispose();
					}
				}
			});

			text.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent event) {
					switch (event.keyCode) {
					case SWT.CR:
						String editedName = text.getText();
						updateItemAndVO(item, userReportDirVO, editedName);
						// FallThrough: Dispose after fetching the text
					case SWT.ESC:
						text.dispose();
						break;
					}
				}
			});
			// start editing
			editor.setEditor(text, item);

		}
	}


	/**
	 * Is called when cell editing is finished via CR or FocusLost; we try to set the name to the edited text,
	 * store it on the server, and - if successful - update the treeItem as well.
	 */
	private void updateItemAndVO(final TreeItem item, final UserReportDirVO userReportDirVO, String editedName) {
		if (editedName.trim().length() > 0) {
			try {
				userReportDirVO.setName(editedName);
				UserReportDirListModel.getInstance().update(userReportDirVO);
				item.setText(editedName);
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(
					Activator.PLUGIN_ID,
					getClass().getName(),
					t,
					ReportI18N.UpdateUserReportDirErrorMessage);
			}
		}
	}

}
