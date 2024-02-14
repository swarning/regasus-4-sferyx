package de.regasus.email.dispatchorder.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.email.EmailI18N;
import de.regasus.ui.Activator;

/**
 * An action that shows as toggle button that filters from the treetable of email dispatches those that have been sent
 * successfully, so that we only
 * 
 * @author manfred
 * 
 */
public class ShowOnlyUnsuccessfulEmailDispatchesAction extends Action implements ActionFactory.IWorkbenchAction {

	public static final String ID = "de.regasus.email.action.ShowOnlyUnsuccessfulEmailDispatchesAction"; 

	private EmailDispatchOrderTreeTable treeTable;

	private ShowOnlyUnsuccessfulEmailDispatchesViewFilter filter = new ShowOnlyUnsuccessfulEmailDispatchesViewFilter();

	private ViewerFilter[] filters = new ViewerFilter[1];


	public ShowOnlyUnsuccessfulEmailDispatchesAction() {

		super(EmailI18N.ShowOnlyUnsuccessfulEmailDispatches, AS_CHECK_BOX);
		setId(ID);

		setToolTipText(EmailI18N.ShowOnlyUnsuccessfulEmailDispatches);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/filter_ps.gif"));

		filters[0] = filter;
	}


	public void run() {
		System.out.println("ShowOnlyUnsuccessfulEmailDispatchesAction.run()");
		TreeViewer treeViewer = treeTable.getTreeViewer();
		if (isChecked()) {
			treeViewer.setFilters(filters);
			treeViewer.refresh();
		}
		else {
			treeViewer.resetFilters();
		}
	}


	public void dispose() {
	}


	public void setTreeTable(EmailDispatchOrderTreeTable treeTable) {
		this.treeTable = treeTable;

	}

}
