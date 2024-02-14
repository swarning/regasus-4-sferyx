package de.regasus.hotel.view.tree;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.contact.Address;
import de.regasus.common.Country;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.I18N;
import de.regasus.common.CountryCity;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.hotel.dialog.CreateHotelWizard;
import de.regasus.hotel.dialog.CreateHotelWizardDialog;
import de.regasus.ui.Activator;

public class CreateHotelAction extends AbstractAction implements ISelectionListener {

	private final IWorkbenchWindow window;

	private CountryCity countryCity;


	public CreateHotelAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(getClass().getName());
		setText(I18N.CreateHotel);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			"icons/create_hotel.png"
		));
		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	@Override
	public void run() {
		try {
			// open CreateHotelWizard
			CreateHotelWizard createHotelWizard = new CreateHotelWizard(countryCity);
			Shell shell = window.getShell();
			WizardDialog wizardDialog = new CreateHotelWizardDialog(shell, createHotelWizard);
			wizardDialog.open();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		// open HotelEditor
//		HotelEditorInput editorInput = new HotelEditorInput(countryCity);
//		try {
//			page.openEditor(editorInput, HotelEditor.ID);
//		}
//		catch (PartInitException e) {
//			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
//		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		countryCity = null;

		if (selection instanceof IStructuredSelection) {
    		Object treeNode = ((IStructuredSelection) selection).getFirstElement();

    		if (treeNode instanceof HotelCityTreeNode) {
    			HotelCityTreeNode node = (HotelCityTreeNode) treeNode;
    			countryCity = node.getValue();
    		}
    		else if (treeNode instanceof HotelCountryTreeNode) {
    			HotelCountryTreeNode node = (HotelCountryTreeNode) treeNode;
    			Country country = node.getValue();
    			countryCity = new CountryCity((String)null, country.getId());
    		}
    		else if (treeNode instanceof HotelTreeNode) {
    			HotelTreeNode node = (HotelTreeNode) treeNode;
    			Hotel value = node.getValue();
    			Address mainAddress = value.getMainAddress();
    			countryCity = new CountryCity(mainAddress.getCity(), mainAddress.getCountryPK());
    		}
    		else if (treeNode instanceof RoomDefinitionTreeNode) {
    			RoomDefinitionTreeNode node = (RoomDefinitionTreeNode) treeNode;
    			TreeNode<?> parentNode = node.getParent();
    			if (parentNode instanceof HotelTreeNode) {
    				HotelTreeNode parentHotelNode = (HotelTreeNode) parentNode;
    				Hotel value = parentHotelNode.getValue();
    				Address mainAddress = value.getMainAddress();
    				countryCity = new CountryCity(mainAddress.getCity(), mainAddress.getCountryPK());
    			}
    		}
		}
	}
}
