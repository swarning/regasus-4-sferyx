package de.regasus.users.user.view;

import java.util.Collection;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.lambdalogic.messeinfo.account.data.UserAccountVO;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.users.ui.Activator;
import de.regasus.users.user.command.EditUserAccountCommandHandler;

public class UserAccountSearchView extends ViewPart {

	public static final String ID = "UserAccountSearchView";

	private UserAccountSearchComposite userAccountSearchComposite;

	

	@Override
	public void createPartControl(Composite parent) {
		try {
			final Composite container = new Composite(parent, SWT.NONE);
			GridLayout viewLayout = new GridLayout();
			container.setLayout(viewLayout);

			userAccountSearchComposite = new UserAccountSearchComposite(container, SWT.NONE);
			userAccountSearchComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			// make the Table the SelectionProvider
			getSite().setSelectionProvider(userAccountSearchComposite.getTableViewer());
			// make that Ctl+C copies table contents to clipboard
			userAccountSearchComposite.registerCopyAction(getViewSite().getActionBars());

			hookContextMenu();
			hookDoubleClickAction();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			getSite().getPage().close();
		}
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		TableViewer viewer = userAccountSearchComposite.getTableViewer();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	
	private void hookDoubleClickAction() {
		final TableViewer viewer = userAccountSearchComposite.getTableViewer();
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				UserAccountVO userAccountVO = SelectionHelper.getUniqueSelected(viewer.getSelection());
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				EditUserAccountCommandHandler.openUserAccountEditor(page, userAccountVO.getID());
			}
		});
	}


	/* Set focus to searchComposite and therewith to the search button.
	 * Otherwise the focus would be on eventCombo which is not wanted, because the user could change its value by 
	 * accident easily.
	 */
	@Override
	public void setFocus() {
		try {
			if (userAccountSearchComposite != null && 
				! userAccountSearchComposite.isDisposed() && 
				userAccountSearchComposite.isEnabled()
			) {
				userAccountSearchComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	public void doSearch() {
		userAccountSearchComposite.doSearch();

	}


	public Collection<UserAccountVO> getSelectedUserAccountVOs() {
		return userAccountSearchComposite.getSelectedUserAccountVOs();
	}

	
	public void addSelectionListener(ISelectionChangedListener selectionListener) {
		userAccountSearchComposite.addSelectionListener(selectionListener);
		
	}

	
	public void removeSelectionListener(ISelectionChangedListener selectionListener) {
		userAccountSearchComposite.removeSelectionListener(selectionListener);		
	}

}
