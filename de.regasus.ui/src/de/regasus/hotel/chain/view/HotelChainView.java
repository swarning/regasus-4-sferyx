package de.regasus.hotel.chain.view;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import com.lambdalogic.util.rcp.CopyAction;

import de.regasus.I18N;
import de.regasus.core.ui.view.AbstractView;

public class HotelChainView extends AbstractView {
	public static final String ID = "HotelChainView";

	// widgets
	private HotelChainTableComposite hotelChainTableComposite;

	/**
	 * All Actions (incl. editAction)
	 */
	private List<IWorkbenchAction> actionList;

	/**
	 * Action for editing (needed for double click)
	 */
	private IWorkbenchAction editAction;

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
	public void createWidgets(Composite parent) throws Exception {
		/* If the view is configured to be visible, the main widget is a HotelChainTableComposite,
		 * otherwise a Label with a text message.
		 */
		if (isVisible()) {
			hotelChainTableComposite = new HotelChainTableComposite(
				parent,
				SWT.FULL_SELECTION | SWT.MULTI
			);

			// make the Tabel the SelectionProvider
			TableViewer tableViewer = hotelChainTableComposite.getTableViewer();
			getSite().setSelectionProvider(tableViewer);

			// create Actions and add them to different menus
			initializeActions();

			setContributionItemsVisible(true);

			initializeContextMenu();
			initializeDoubleClickAction();

			setTitleToolTip(I18N.HotelChainView_ToolTip);

			// initialize copy and paste
			getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());
		}
		else {
			Label label = new Label(parent, SWT.NONE);
			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
		}
	}


	/**
	 * Create the actions, but only once.
	 */
	private void initializeActions() {
		if ( ! actionsInitialized) {
			actionList = new ArrayList<>();

    		IWorkbenchWindow window = getSite().getWorkbenchWindow();

    		actionList.add(new CreateHotelChainAction(window));

    		editAction = new EditHotelChainAction(window);
    		actionList.add(editAction);
    		actionList.add(new DeleteHotelChainAction(window));
    		actionList.add(new RefreshHotelChainAction());

			initializeMenu();
			initializeToolBar();

			actionsInitialized = true;
		}
	}


	private void initializeContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager menuManager) {
				HotelChainView.this.fillContextMenu(menuManager);
			}
		});

		TableViewer tableViewer = hotelChainTableComposite.getTableViewer();
		Table table = tableViewer.getTable();
		Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void initializeDoubleClickAction() {
		TableViewer tableViewer = hotelChainTableComposite.getTableViewer();
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editAction.run();
			}
		});
	}


	private void fillContextMenu(IMenuManager menuManager) {
		// The reason for such a segment (that lets dynamically fill a new popup-menu every time
		// when it get's opened) is that you might add plugins at runtime, and new menu contributions
		// can be shown immediately, without restart.

		for (IWorkbenchAction action : actionList) {
			menuManager.add(action);
		}

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menuManager.update(true);
	}


	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

		for (IWorkbenchAction action : actionList) {
			toolBarManager.add(action);
		}

		// update the tool bar, necessary when it changes after its first initialization
		toolBarManager.update(true);
	}


	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		for (IWorkbenchAction action : actionList) {
			menuManager.add(action);
		}

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		// update the menu, necessary when it changes after after its first initialization
		menuManager.update(true);
	}


	@Override
	public void setFocus() {
		try {
			if ( hotelChainTableComposite != null &&
				!hotelChainTableComposite.isDisposed() &&
				 hotelChainTableComposite.isEnabled()
			) {
				hotelChainTableComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}
}
