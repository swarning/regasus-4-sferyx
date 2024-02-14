package de.regasus.report.view;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.ui.CoreI18N;
import de.regasus.report.editor.UserReportEditorInput;

public class LinkWithEditorAction
extends Action
implements ActionFactory.IWorkbenchAction, IPartListener {
//	private static Logger log = Logger.getLogger("ui.LinkWithEditorAction");

	public static final String ID = "com.lambdalogic.mi.reporting.ui.action.LinkWithEditorAction";

	private final UserReportTreeView userReportTreeView;
	private final IWorkbenchWindow window;
	private IWorkbenchPart currentPart;
	private boolean linked = false;


	public LinkWithEditorAction(UserReportTreeView userReportTreeView) {
		super(CoreI18N.LinkWithEditorAction_Text, AS_RADIO_BUTTON);
		this.userReportTreeView = userReportTreeView;
		this.window = userReportTreeView.getSite().getWorkbenchWindow();
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


	private void showInTree() {
		if (isChecked() && currentPart != null && currentPart instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart) currentPart;
			IEditorInput editorInput = editorPart.getEditorInput();
			if (editorInput instanceof UserReportEditorInput) {
				UserReportEditorInput userReportEditorInput = (UserReportEditorInput) editorInput;
				Long userReportPK = userReportEditorInput.getKey();
				userReportTreeView.show(userReportPK);
			}
		}
	}


	public void refresh() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPart part = window.getPartService().getActivePart();
		if (part instanceof IEditorPart) {
			currentPart = part;
			showInTree();
		}
	}

}
