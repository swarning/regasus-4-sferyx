package de.regasus.core.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.view.AbstractLinkableView;

public class LinkWithEditorAction
extends Action
implements ActionFactory.IWorkbenchAction, IPartListener {
	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.LinkWithEditorAction";

	protected final AbstractLinkableView abstractLinkableView;
	protected final IWorkbenchWindow window;
	protected IWorkbenchPart currentPart;

	protected boolean ignoreModifyEvents = false;


	public LinkWithEditorAction(AbstractLinkableView linkableView) {
		super(CoreI18N.LinkWithEditorAction_Text, AS_RADIO_BUTTON);
		this.abstractLinkableView = linkableView;
		this.window = linkableView.getSite().getWorkbenchWindow();
		setId(ID);
		setToolTipText(CoreI18N.LinkWithEditorAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			de.regasus.core.ui.IImageKeys.LINK_WITH_EDITOR
		));

		window.getPartService().addPartListener(this);
	}


	@Override
	public void dispose() {
		window.getPartService().removePartListener(this);
	}


	private boolean linked = false;

	@Override
	public void run() {
		linked = ! linked;
		setChecked(linked);
		showInTree();
	}


	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			currentPart = part;
			showInTree();
		}
	}


	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			currentPart = part;
			showInTree();
		}
	}


	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part == currentPart) {
			part = null;
		}
	}


	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}


	@Override
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			currentPart = part;
			showInTree();
		}
	}


	protected void showInTree() {
		try {
    		ignoreModifyEvents = true;

    		if (isChecked() && currentPart != null && currentPart instanceof IEditorPart) {
    			IEditorPart editorPart = (IEditorPart) currentPart;
    			IEditorInput editorInput = editorPart.getEditorInput();
    			if (editorInput instanceof ILinkableEditorInput) {
    				ILinkableEditorInput linkableEditorInput = (ILinkableEditorInput) editorInput;

    				Class<?> entityType = linkableEditorInput.getEntityType();
    				Object key = linkableEditorInput.getKey();

    				// show entity in tree (done asynchonous)
					abstractLinkableView.show(entityType, key);
    			}
    		}
		}
		finally {
			ignoreModifyEvents = false;
		}
	}

}
