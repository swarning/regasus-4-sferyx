package de.regasus.hotel.contingent.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.common.CountryCity;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.event.view.EventHotelInfoListTreeNode;
import de.regasus.event.view.EventHotelInfoTreeNode;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.HotelContingentTreeNode;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.contingent.dialog.CreateHotelContingentWizard;
import de.regasus.hotel.contingent.dialog.CreateHotelContingentWizardDialog;
import de.regasus.ui.Activator;

public class CreateHotelContingentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		// Find out Event and Hotel
		Long hotelPK = null;
		Long eventPK = null;


		if (object instanceof EventTreeNode) {
			EventTreeNode eventTreeNode = (EventTreeNode) object;
			eventPK = eventTreeNode.getEventId();
		}
		else if (object instanceof EventHotelInfoListTreeNode) {
			EventHotelInfoListTreeNode eventHotelListTreeNode = (EventHotelInfoListTreeNode) object;
			eventPK = eventHotelListTreeNode.getEventId();
		}
		else if (object instanceof EventHotelInfoTreeNode) {
			EventHotelInfoTreeNode eventHotelTreeNode = (EventHotelInfoTreeNode) object;
			hotelPK = eventHotelTreeNode.getValue().getID();
			eventPK = eventHotelTreeNode.getEventId();
		}
		else if (object instanceof HotelContingentTreeNode) {
			HotelContingentTreeNode hotelContingentTreeNode = (HotelContingentTreeNode) object;
			EventHotelInfoTreeNode eventHotelTreeNode = (EventHotelInfoTreeNode) hotelContingentTreeNode.getParent();
			hotelPK = eventHotelTreeNode.getValue().getID();
			eventPK = eventHotelTreeNode.getEventId();
		}


//		In Abhängigkeit davon, in welchem Kontext der Assistent geöffnet wurde, sind diese bereits mit Werten vorbelegt.
//		* Veranstaltung: Land und Ort der Veranstaltung
//		* Hotels: Land und Ort der Veranstaltung
//		* Hotel: Land und Ort der Veranstaltung und Name 1 des Hotels
//		* Hotelkontingent: Land und Ort der Veranstaltung und Name 1 des Hotels


		try {
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
			String name1 = null;
			CountryCity countryCity = null;

			if (hotelPK != null) {
				Hotel hotel = HotelModel.getInstance().getHotel(hotelPK);
				name1 = hotel.getName1();

				String countryPK = hotel.getMainAddress().getCountryPK();
				String city = hotel.getMainAddress().getCity();
				countryCity = new CountryCity(city, countryPK);

			}
			else {
				String countryPK = eventVO.getCountryPK();
				countryCity = new CountryCity(null, countryPK);
			}

			CreateHotelContingentWizard createHotelContingentWizard = new CreateHotelContingentWizard(countryCity, name1, eventVO);
			WizardDialog wizardDialog = new CreateHotelContingentWizardDialog(HandlerUtil.getActiveShell(event), createHotelContingentWizard);

			wizardDialog.create();

			// decrease height if necessary
			Point size = wizardDialog.getShell().getSize();
			if (size.y >= 800) {
				size.y = 750;
			}
			wizardDialog.getShell().setSize(size);

			wizardDialog.open();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}


//		// open HotelContingentEditor
//
//		if (hotelPK == null) {
//			try {
//				EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
//				String countryPK = eventVO.getCountryPK();
//				Shell shell = HandlerUtil.getActiveShell(event);
//				hotelPK = HotelForCountrySelectionDialogHelper.open(shell, countryPK, EmailI18N.SelectHotelForContingent);
//			}
//			catch (Exception e) {
//				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//			}
//		}
//
//
//		if (hotelPK != null && eventPK != null) {
//			// Open editor for new HotelContingent
//			HotelContingentEditorInput input = new HotelContingentEditorInput(eventPK, hotelPK);
//			try {
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
//					input,
//					HotelContingentEditor.ID
//				);
//			}
//			catch (PartInitException e) {
//				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//			}
//		}
//
		return null;
	}


}
