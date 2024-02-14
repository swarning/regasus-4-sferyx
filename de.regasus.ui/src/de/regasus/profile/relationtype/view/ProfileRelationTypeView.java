package de.regasus.profile.relationtype.view;

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

public class ProfileRelationTypeView extends AbstractView {

	public static final String ID = "ProfileRelationTypeView";

	// Widgets
	private ProfileRelationTypeTableComposite profileRelationTypeTableComposite;

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
			visible = 	getConfigParameterSet().getProfile().isVisible() &&
						getConfigParameterSet().getProfile().getProfileRelation().isVisible();
		}
		return visible;
	}


	/**
	 * Create the content of the view part.
	 * @param parent
	 * @throws Exception
	 */
	@Override
	public void createWidgets(Composite parent) throws Exception {
		if (isVisible()) {
			profileRelationTypeTableComposite = new ProfileRelationTypeTableComposite(
				parent,
				SWT.FULL_SELECTION | SWT.MULTI
			);

			// make the Tree the SelectionProvider
			TableViewer tableViewer = profileRelationTypeTableComposite.getTableViewer();
			getSite().setSelectionProvider(tableViewer);

			// create Actions and add them to different menus
			initializeActions();
			setContributionItemsVisible(true);

			initializeContextMenu();
			initializeDoubleClickAction();

			setTitleToolTip(I18N.ProfileRelationTypeView_ToolTip);

			// initialize copy and paste
			getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());
		}
		else {
			Label label = new Label(parent, SWT.NONE);
			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
		}
	}


	/**
	 * Create the actions.
	 */
	private void initializeActions() {
		if (actionList == null) {
			actionList = new ArrayList<>();

			IWorkbenchWindow window = getSite().getWorkbenchWindow();

			actionList.add(new CreateProfileRelationTypeAction(window));

			editAction = new EditProfileRelationTypeAction(window);
			actionList.add(editAction);

			actionList.add(new DeleteProfileRelationTypeAction(window));

			actionList.add(new RefreshProfileRelationTypeAction());


			initializeToolBar();
			initializeMenu();
		}
	}


	private void initializeContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager menuManager) {
				ProfileRelationTypeView.this.fillContextMenu(menuManager);
			}
		});

		TableViewer tableViewer = profileRelationTypeTableComposite.getTableViewer();
		Table table = tableViewer.getTable();
		Menu menu = menuMgr.createContextMenu(table);
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, tableViewer);
	}


	private void initializeDoubleClickAction() {
		final TableViewer tableViewer = profileRelationTypeTableComposite.getTableViewer();
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editAction.run();
			}
		});
	}


	protected void fillContextMenu(IMenuManager menuManager) {
		for (IWorkbenchAction action : actionList) {
			menuManager.add(action);
		}

		menuManager.update(true);

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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

		// update the menu, necessary when it changes after after its first initialization
		menuManager.update(true);

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}


	@Override
	public void setFocus() {
		/*
		 * The focus must be set to a widget that is:
		 * - not null
		 * - not disposed
		 * - enabled
		 *
		 * If not the editor's focus isn't set correctly.
		 * This causes that a double-click in the corresponding view doesn't work!
		 *
		 * Because this method is called by the framework, its code has to be surrounded with a try-catch-block.
		 * Otherwise exceptions would pop up to the user and written to the .log file.
		 */
		try {
			if (profileRelationTypeTableComposite != null
				&& !profileRelationTypeTableComposite.isDisposed()
				&& profileRelationTypeTableComposite.isEnabled()
			) {
				profileRelationTypeTableComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}

}
