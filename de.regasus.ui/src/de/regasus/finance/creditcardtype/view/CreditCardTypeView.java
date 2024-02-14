package de.regasus.finance.creditcardtype.view;

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

import de.regasus.core.ui.view.AbstractView;

public class CreditCardTypeView extends AbstractView {
	public static final String ID = "CreditCardTypeView";

	// Widgets
	private CreditCardTypeTableComposite creditCardTypeTableComposite;

	// Actions
	private List<IWorkbenchAction> actionList;
	private IWorkbenchAction editAction;


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
			visible = getConfigParameterSet().getInvoiceDetails().getCreditCardType().isVisible();
		}
		return visible;
	}


	/**
	 * Create contents of the view part
	 * @param parent
	 * @throws Exception
	 */
	@Override
	public void createWidgets(Composite parent) throws Exception {
		if (isVisible()) {
			creditCardTypeTableComposite = new CreditCardTypeTableComposite(parent, SWT.FULL_SELECTION | SWT.MULTI);
			// make the Tree the SelectionProvider
			TableViewer tableViewer = creditCardTypeTableComposite.getTableViewer();
			getSite().setSelectionProvider(tableViewer);

			// create Actions and add them to different menus
			initializeActions();
			setContributionItemsVisible(true);

			initializeContextMenu();
			initializeDoubleClickAction();

			//setTitleToolTip(FinanceI18N.CreditCardTypeView_ToolTip);


			// MIRCP-284 - Copy und Paste
			getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());
		}
		else {
			Label label = new Label(parent, SWT.NONE);
			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
		}
	}


	/**
	 * Create the actions
	 */
	private void initializeActions() {
		if (actionList == null) {
			actionList = new ArrayList<>();

			IWorkbenchWindow window = getSite().getWorkbenchWindow();

			actionList.add(new CreateCreditCardTypeAction(window));

			editAction = new EditCreditCardTypeAction(window);
			actionList.add(editAction);

			actionList.add(new DeleteCreditCardTypeAction(window));

			actionList.add(new RefreshCreditCardTypeAction());


			initializeToolBar();
			initializeMenu();
		}
	}


	private void initializeContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				CreditCardTypeView.this.fillContextMenu(manager);
			}
		});

		final TableViewer tableViewer = creditCardTypeTableComposite.getTableViewer();
		final Table table = tableViewer.getTable();
		final Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void initializeDoubleClickAction() {
		final TableViewer tableViewer = creditCardTypeTableComposite.getTableViewer();
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editAction.run();
			}
		});
	}


	private void fillContextMenu(IMenuManager manager) {
		for (IWorkbenchAction action : actionList) {
			manager.add(action);
		}
		// update the menu, necessary when it changes after after its first initialization
		manager.update(true);

		manager.add(new Separator());

		// Other plug-ins can contribute there actions here
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

		for (IWorkbenchAction action : actionList) {
			toolBarManager.add(action);
		}

		// update the tool bar, necessary when it changes after its first initialization
		toolBarManager.update(true);
	}


	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		for (IWorkbenchAction action : actionList) {
			menuManager.add(action);
		}

		// update the menu, necessary when it changes after after its first initialization
		menuManager.update(true);
	}


	@Override
	public void setFocus() {
		try {
			if (creditCardTypeTableComposite != null &&
				!creditCardTypeTableComposite.isDisposed() &&
				creditCardTypeTableComposite.isEnabled()
			) {
				creditCardTypeTableComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}

}
