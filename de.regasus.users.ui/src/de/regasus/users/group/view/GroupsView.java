package de.regasus.users.group.view;

import java.util.Collection;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.users.UserGroupModel;
import de.regasus.users.group.command.EditUserGroupCommandHandler;
import de.regasus.users.ui.Activator;

public class GroupsView extends ViewPart implements CacheModelListener<String> {

	public static final String ID = "GroupsView";
	private Table table;
	private UserGroupTable userGroupTable;

	private UserGroupModel userGroupModel = UserGroupModel.getInstance();

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		final Composite searchTableComposite = new Composite(parent, SWT.BORDER);
		searchTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumnLayout layout = new TableColumnLayout();
		searchTableComposite.setLayout(layout);

		table = new Table(searchTableComposite, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableColumn idTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(idTableColumn, new ColumnWeightData(60));
		idTableColumn.setText(UtilI18N.ID);

		final TableColumn descriptionTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(descriptionTableColumn, new ColumnWeightData(100));
		descriptionTableColumn.setText(UtilI18N.Description);

		userGroupTable = new UserGroupTable(table);

		syncWidgetToModel();

		hookContextMenu();
		hookDoubleClickAction();

		userGroupModel.addListener(this);

		getSite().setSelectionProvider(userGroupTable.getViewer());

	}


	@Override
	public void dispose() {
		userGroupModel.removeListener(this);
		super.dispose();
	}


	@Override
	public void setFocus() {
		try {
			if (table != null && !table.isDisposed() && table.isEnabled()) {
				table.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}



	private void syncWidgetToModel() {
		SWTHelper.asyncExecDisplayThread(new Runnable() {

			@Override
			public void run() {
				try {
					Collection<UserGroupVO> userGroupVOs = userGroupModel.getAllUserGroupVOs();
					userGroupTable.setInput(userGroupVOs);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		TableViewer viewer = userGroupTable.getViewer();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void hookDoubleClickAction() {
		final TableViewer viewer = userGroupTable.getViewer();
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				UserGroupVO userGroupVO = SelectionHelper.getUniqueSelected(viewer.getSelection());
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				EditUserGroupCommandHandler.openUserGroupEditor(page, userGroupVO.getGroupID());
			}
		});
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		if (event.getSource() == userGroupModel) {
			syncWidgetToModel();
		}
	}
}
