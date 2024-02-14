package de.regasus.report.view;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.ReportI18N;
import de.regasus.report.model.UserReportListModel;
import de.regasus.report.ui.Activator;


public class UserReportTableView extends ViewPart implements CacheModelListener<Long> {
	public static final String ID ="UserReportTableView"; 

	// Models
	private UserReportListModel userReportListModel = null;


	private UserReportTable userReportTable;
	private TableViewer tableViewer;


	// Actions
	private ActionFactory.IWorkbenchAction refreshUserReportListAction;
	private ActionFactory.IWorkbenchAction generateReportAction;
	private ActionFactory.IWorkbenchAction createUserReportAction;
	private ActionFactory.IWorkbenchAction editUserReportAction;
	private ActionFactory.IWorkbenchAction deleteUserReportAction;



	public UserReportTableView() {
		super();
	}


	@Override
	public void createPartControl(Composite parent) {
		TableColumnLayout layout = new TableColumnLayout();
		parent.setLayout(layout);

		Table table = new Table(parent, SWT.FULL_SELECTION);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		nameTableColumn.setWidth(100);
		nameTableColumn.setText(ReportI18N.UserReportTableView_Name);

		final TableColumn descriptionTableColumn = new TableColumn(table, SWT.NONE);
		descriptionTableColumn.setWidth(150);
		descriptionTableColumn.setText(ReportI18N.UserReportTableView_Description);

		layout.setColumnData(nameTableColumn, new ColumnWeightData(2));
		layout.setColumnData(descriptionTableColumn, new ColumnWeightData(2));

		userReportTable = new UserReportTable(table);
		tableViewer = userReportTable.getViewer();
		// make the Table the SelectionProvider
		getSite().setSelectionProvider(tableViewer);

//		initializeToolBar();

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		initModels();

		// MIRCP-284 - Copy und Paste
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());
	}


	@Override
	public void setFocus() {
		try {
			if (tableViewer != null) {
    			Table table = tableViewer.getTable();
				if (table != null && !table.isDisposed() && table.isEnabled()) {
    				table.setFocus();
    			}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}

//	private void initializeToolBar() {
//		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
//	}


	private void initModels() {
		try {
			userReportListModel = UserReportListModel.getInstance();
			userReportListModel.addListener(this);

			userReportTable.setInput(userReportListModel.getAllUserReportVOs());
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					userReportTable.setInput(userReportListModel.getAllUserReportVOs());
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
			}
		});
	}


	private void makeActions() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		refreshUserReportListAction = new RefreshUserReportListAction();
		generateReportAction = new GenerateReportAction(window);
		createUserReportAction = new CreateUserReportAction(window);
		editUserReportAction = new EditUserReportAction(window, null);
		deleteUserReportAction = new DeleteUserReportAction(window, null);
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				UserReportTableView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
		tableViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshUserReportListAction);
		manager.add(generateReportAction);
		manager.add(createUserReportAction);
		manager.add(editUserReportAction);
		manager.add(deleteUserReportAction);
		//manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshUserReportListAction);
		manager.add(generateReportAction);
		manager.add(createUserReportAction);
		manager.add(editUserReportAction);
		manager.add(deleteUserReportAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshUserReportListAction);
		manager.add(generateReportAction);
		manager.add(createUserReportAction);
		manager.add(editUserReportAction);
		manager.add(deleteUserReportAction);
	}


	private void hookDoubleClickAction() {
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editUserReportAction.run();
			}
		});
	}

	@Override
	public void dispose() {
		if (userReportListModel != null) {
			userReportListModel.removeListener(this);
		}

		// dispose Actions
		refreshUserReportListAction.dispose();
		generateReportAction.dispose();
		createUserReportAction.dispose();
		editUserReportAction.dispose();
		deleteUserReportAction.dispose();

		super.dispose();
	}

}
