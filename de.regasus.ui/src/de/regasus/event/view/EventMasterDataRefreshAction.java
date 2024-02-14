package de.regasus.event.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.I18N;

public class EventMasterDataRefreshAction
extends Action 
implements ActionFactory.IWorkbenchAction, ISelectionListener {

	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.EventMasterDataRefreshAction"; 
	
	private final IWorkbenchWindow window;
	private IStructuredSelection selection;
	private TreeNode selectedTreeNode;
	
	
	public EventMasterDataRefreshAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.EventMasterDataRefreshAction_Text);
		setToolTipText(I18N.EventMasterDataRefreshAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID, 
			de.regasus.core.ui.IImageKeys.REFRESH
		));
		
		window.getSelectionService().addSelectionListener(this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	public void run() {
		if (selectedTreeNode != null) {
			BusyCursorHelper.busyCursorWhile(new Runnable() {
				public void run() {
					selectedTreeNode.refresh();
				}
			});
		}
	}

	
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		boolean enable = false;
		if (incoming instanceof IStructuredSelection) {
			selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				if (selection.getFirstElement() instanceof TreeNode) {
					selectedTreeNode = (TreeNode) selection.getFirstElement();
					enable = true;
				}
				else {
					selectedTreeNode = null;
					enable = false;
				}
			}
		}
		setEnabled(enable);
	}

}
