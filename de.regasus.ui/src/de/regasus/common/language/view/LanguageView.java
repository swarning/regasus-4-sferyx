package de.regasus.common.language.view;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import com.lambdalogic.util.rcp.CopyAction;

import de.regasus.I18N;




public class LanguageView extends ViewPart {

	public static final String ID = "LanguageView";

	// Widgets
	private LanguageTableComposite languageTableComposite;

	// Actions
	private ActionFactory.IWorkbenchAction createAction;
	private ActionFactory.IWorkbenchAction editAction;
	private ActionFactory.IWorkbenchAction deleteAction;
	private ActionFactory.IWorkbenchAction refreshAction;


	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		try {
			languageTableComposite = new LanguageTableComposite(parent, SWT.FULL_SELECTION | SWT.MULTI);
			// make the Tree the SelectionProvider
			TableViewer tableViewer = languageTableComposite.getTableViewer();
			getSite().setSelectionProvider(tableViewer);
			//
			createActions();
			initializeContextMenu();
			initializeDoubleClickAction();
			initializeToolBar();
			initializeMenu();
			setTitleToolTip(I18N.LanguageView_ToolTip);

			// MIRCP-284 - Copy und Paste
			getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Create the actions
	 */
	private void createActions() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		createAction = new CreateLanguageAction(window);
		editAction = new EditLanguageAction(window);
		deleteAction = new DeleteLanguageAction(window);
		refreshAction = new RefreshLanguageAction();
	}


	private void initializeContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				LanguageView.this.fillContextMenu(manager);
			}
		});

		final TableViewer tableViewer = languageTableComposite.getTableViewer();
		final Table table = tableViewer.getTable();
		final Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void initializeDoubleClickAction() {
		final TableViewer tableViewer = languageTableComposite.getTableViewer();
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editAction.run();
			}
		});
	}


	private void fillContextMenu(IMenuManager manager) {
		manager.add(createAction);
		manager.add(editAction);
		manager.add(deleteAction);
		manager.add(refreshAction);
		manager.add(new Separator());

		// Other plug-ins can contribute there actions here
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

		toolbarManager.add(createAction);
		toolbarManager.add(editAction);
		toolbarManager.add(deleteAction);
		toolbarManager.add(refreshAction);
	}


	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		menuManager.add(createAction);
		menuManager.add(editAction);
		menuManager.add(deleteAction);
		menuManager.add(refreshAction);
		//manager.add(new Separator());
	}


	@Override
	public void setFocus() {
		try {
			if (languageTableComposite != null && !languageTableComposite.isDisposed()
				&& languageTableComposite.isEnabled()) {
				languageTableComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}

}
