/**
 * GateDeviceView.java
 * created on 24.09.2013 16:17:32
 */
package de.regasus.common.gatedevice.view;

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

public class GateDeviceView extends AbstractView {

	public static final String ID = "GateDeviceView";


	// Widgets
	private GateDeviceTableComposite gateDeviceTableComposite;

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


	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible = getConfigParameterSet().getGateDevice().isVisible();
		}
		return visible;
	}

	@Override
	protected void createWidgets(Composite parent) throws Exception {
		/* If the view is configured to be visible, the main widget is a GateDeviceTableComposite,
		 * otherwise a Label with a text message.
		 */
		if (isVisible()) {
			gateDeviceTableComposite = new GateDeviceTableComposite(
				parent,
				SWT.FULL_SELECTION | SWT.MULTI
			);

			TableViewer tableViewer = gateDeviceTableComposite.getTableViewer();
			getSite().setSelectionProvider(tableViewer);

			// create Actions and add them to different menus
			initializeActions();

			setContributionItemsVisible(true);

			initializeContextMenu();
			initializeDoubleClickAction();

			setTitleToolTip(I18N.GateDeviceView_ToolTip);

			// initialize copy and paste
			getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());
		}
		else {
			Label label = new Label(parent, SWT.NONE);
			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
		}
	}


	private void initializeDoubleClickAction() {
		TableViewer tableViewer = gateDeviceTableComposite.getTableViewer();
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				editAction.run();
			}
		});
	}


	private void initializeContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				GateDeviceView.this.fillContextMenu(manager);
			}
		});

		TableViewer tableViewer = gateDeviceTableComposite.getTableViewer();
		Table table = tableViewer.getTable();
		Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void fillContextMenu(IMenuManager menuManager) {
		// The reason for such a segment (that lets dynamically fill the popup-menu every time anew
		// when it get's opened) is that you might add plugins at runtime, and new menu contributions
		// can herewith be shown immediately, without restart.

		for (IWorkbenchAction action : actionList) {
			menuManager.add(action);
		}

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menuManager.update(true);
	}


	private void initializeActions() {
		if (! actionsInitialized) {
			actionList = new ArrayList<>();

    		IWorkbenchWindow window = getSite().getWorkbenchWindow();

    		actionList.add(new CreateGateDeviceAction(window));

    		editAction = new EditGateDeviceAction(window);
    		actionList.add(editAction);

    		actionList.add(new DeleteGateDeviceAction(window));

    		actionList.add(new RefreshGateDeviceAction());

    		initializeToolBar();
			initializeMenu();

			actionsInitialized = true;
		}
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


	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

		for (IWorkbenchAction action : actionList) {
			toolBarManager.add(action);
		}

		// update the tool bar, necessary when it changes after its first initialization
		toolBarManager.update(true);
	}


	@Override
	public void setFocus() {
		try {
			if (gateDeviceTableComposite != null &&
				!gateDeviceTableComposite.isDisposed() &&
				gateDeviceTableComposite.isEnabled()
			) {
				gateDeviceTableComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}

}
