package de.regasus.hotel.chain.view;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.hotel.chain.editor.HotelChainEditor;
import de.regasus.hotel.chain.editor.HotelChainEditorInput;
import de.regasus.ui.Activator;

public class CreateHotelChainAction extends AbstractAction {
	
	public static final String ID = "com.lambdalogic.mi.masterdata.ui.hotel.CreateHotelChainAction"; 
	
	private final IWorkbenchWindow window;
	
	
	public CreateHotelChainAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(getClass().getName());
		setText(I18N.CreateHotelChain);
		setToolTipText(I18N.CreateHotelChain);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			IImageKeys.CREATE
		));
	}

	
	public void run() {
		IWorkbenchPage page = window.getActivePage();
		HotelChainEditorInput editorInput = new HotelChainEditorInput();
		try {
			page.openEditor(editorInput, HotelChainEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}
