package de.regasus.hotel.chain.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.I18N;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.hotel.HotelChain;
import de.regasus.hotel.chain.editor.HotelChainEditor;
import de.regasus.hotel.chain.editor.HotelChainEditorInput;

public class EditHotelChainAction extends AbstractAction implements ISelectionListener {
	public static final String ID = "com.lambdalogic.mi.masterdata.ui.hotel.EditHotelChainAction";

	private final IWorkbenchWindow window;

	private Long hotelChainPK = null;


	public EditHotelChainAction(IWorkbenchWindow window) {
		/* disableWhenLoggedOut is not needed, because this Action enabled/disables depending on the
		 * current selection. When the user is not logged in, no data can be selected. So the Action
		 * is disabled then.
		 */
		super(false /*disableWhenLoggedOut*/);

		this.window = window;
		setId(ID);
		setText(I18N.EditHotelChainAction_Text);
		setToolTipText(I18N.EditHotelChainAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			IImageKeys.EDIT
		));

		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	@Override
	public void run() {
		if (hotelChainPK != null) {
			IWorkbenchPage page = window.getActivePage();
			HotelChainEditorInput editorInput = new HotelChainEditorInput(hotelChainPK);
			try {
				page.openEditor(editorInput, HotelChainEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		hotelChainPK = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				Object selectedElement = selection.getFirstElement();
				if (selectedElement instanceof HotelChain) {
					HotelChain hotelChain = (HotelChain) selectedElement;
					hotelChainPK = hotelChain.getId();
				}
			}
		}
		setEnabled(hotelChainPK != null);
	}

}
