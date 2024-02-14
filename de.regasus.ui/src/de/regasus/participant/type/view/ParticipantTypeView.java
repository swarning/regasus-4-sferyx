package de.regasus.participant.type.view;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

import com.lambdalogic.util.rcp.CopyAction;

import de.regasus.I18N;
import de.regasus.core.ui.view.AbstractView;


/**
 * View that shows a list of all participant types.
 */
public class ParticipantTypeView extends AbstractView {
	public static final String ID = "ParticipantTypeView";

	// Widgets
	private ParticipantTypeTableComposite participantTypeTableComposite;

	// Actions
	private ActionFactory.IWorkbenchAction createAction;
	private ActionFactory.IWorkbenchAction editAction;
	private ActionFactory.IWorkbenchAction deleteAction;
	private ActionFactory.IWorkbenchAction refreshAction;

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
			visible = getConfigParameterSet().getEvent().isVisible();
		}
		return visible;
	}


	/* (non-Javadoc)
	 * @see de.regasus.core.ui.view.AbstractView#createWidgets(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createWidgets(Composite parent) throws Exception {
		if (isVisible()) {
			participantTypeTableComposite = new ParticipantTypeTableComposite(parent, SWT.FULL_SELECTION | SWT.MULTI);
			// make the Tree the SelectionProvider
			TableViewer tableViewer = participantTypeTableComposite.getTableViewer();
			getSite().setSelectionProvider(tableViewer);

			// create Actions and add them to different menus
			initializeActions();

			setContributionItemsVisible(true);

			initializeContextMenu();
			initializeDoubleClickAction();


			setTitleToolTip(I18N.ParticipantTypeView_ToolTip);

			getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());

			// I want the delete-key to start the deleteAction, but setting as global action handler didn't work.
			tableViewer.getTable().addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.keyCode == 127) {
						deleteAction.run();
					}
				}
			});
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
    		IWorkbenchWindow window = getSite().getWorkbenchWindow();

    		createAction = new CreateParticipantTypeAction(window);
    		editAction = new EditParticipantTypeAction(window);
    		deleteAction = new DeleteParticipantTypeAction(window);
    		refreshAction = new RefreshParticipantTypeAction();


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
			public void menuAboutToShow(IMenuManager menuManager) {
				ParticipantTypeView.this.fillContextMenu(menuManager);
			}
		});

		TableViewer tableViewer = participantTypeTableComposite.getTableViewer();
		Table table = tableViewer.getTable();
		Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void initializeDoubleClickAction() {
		TableViewer tableViewer = participantTypeTableComposite.getTableViewer();
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
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

		menuManager.add(createAction);
		menuManager.add(editAction);
		menuManager.add(deleteAction);
		menuManager.add(refreshAction);

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menuManager.update(true);
	}


	private void initializeToolBar() {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

		toolBarManager.add(createAction);
		toolBarManager.add(editAction);
		toolBarManager.add(deleteAction);
		toolBarManager.add(refreshAction);

		// update the tool bar, necessary when it changes after its first initialization
		toolBarManager.update(true);
	}


	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		menuManager.add(createAction);
		menuManager.add(editAction);
		menuManager.add(deleteAction);
		menuManager.add(refreshAction);

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		// update the menu, necessary when it changes after after its first initialization
		menuManager.update(true);
	}


	@Override
	public void setFocus() {
		try {
			if (participantTypeTableComposite != null &&
				!participantTypeTableComposite.isDisposed() &&
				participantTypeTableComposite.isEnabled()
			) {
				participantTypeTableComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}

}
