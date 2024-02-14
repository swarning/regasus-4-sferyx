package de.regasus.hotel.cancelterm.command;

import java.util.Locale;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventHotelInfoListTreeNode;
import de.regasus.event.view.EventHotelInfoTreeNode;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.HotelCancelationTermTreeNode;
import de.regasus.event.view.HotelContingentTreeNode;
import de.regasus.event.view.HotelOfferingTreeNode;
import de.regasus.hotel.cancelterm.dialog.CreateHotelCancelationTermsWizard;
import de.regasus.hotel.cancelterm.dialog.CreateHotelCancelationTermsWizardMode;
import de.regasus.hotel.cancelterm.editor.HotelCancelationTermEditor;
import de.regasus.hotel.cancelterm.editor.HotelCancelationTermEditorInput;
import de.regasus.ui.Activator;

public class CreateHotelCancelationTermHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
			Object object = currentSelection.getFirstElement();

			// Find out EventPK
			Long hotelOfferingPK = null;

			CreateHotelCancelationTermsWizard wizard = null;

			if (object instanceof HotelContingentTreeNode) {
				// Find hotel contingent Long
				HotelContingentTreeNode hotelContingentTreeNode = (HotelContingentTreeNode) object;

				// Collect the paramters for the wizard constructor
				Long key = hotelContingentTreeNode.getKey();
				String objectName = hotelContingentTreeNode.getValue().getHcName();
				CreateHotelCancelationTermsWizardMode mode = CreateHotelCancelationTermsWizardMode.CONTINGENT;
				Long eventPK = hotelContingentTreeNode.getValue().getHotelContingentVO().getEventPK();

				// Create the wizard
				wizard = new CreateHotelCancelationTermsWizard(key, eventPK, mode, objectName);
			}
			else if (object instanceof EventHotelInfoTreeNode) {
				// Find hotel  Long
				EventHotelInfoTreeNode eventHotelTreeNode = (EventHotelInfoTreeNode) object;

				// Collect the paramters for the wizard constructor
				Long hotelID = eventHotelTreeNode.getKey().getHotelPK();
				String objectName = eventHotelTreeNode.getValue().getName1();
				CreateHotelCancelationTermsWizardMode mode = CreateHotelCancelationTermsWizardMode.HOTEL;
				Long eventPK = eventHotelTreeNode.getEventId();

				// Create the wizard
				wizard = new CreateHotelCancelationTermsWizard(hotelID, eventPK, mode, objectName);
			}
			else if (object instanceof EventHotelInfoListTreeNode) {
				// Find event Long
				EventHotelInfoListTreeNode eventHotelListTreeNode = (EventHotelInfoListTreeNode) object;

				EventTreeNode eventTreeNode = (EventTreeNode) eventHotelListTreeNode.getParent();

				// Collect the paramters for the wizard constructor
				Long key = null;
				String objectName = eventTreeNode.getValue().getName(Locale.getDefault());

				CreateHotelCancelationTermsWizardMode mode = CreateHotelCancelationTermsWizardMode.EVENT;
				Long eventPk = eventTreeNode.getValue().getPK();

				// Create the wizard
				wizard = new CreateHotelCancelationTermsWizard(key, eventPk, mode, objectName);
			}

			else if (object instanceof EventTreeNode) {
				// Find event Long
				EventTreeNode eventTreeNode = (EventTreeNode) object;

				// Collect the parameters for the wizard constructor
				String objectName = eventTreeNode.getValue().getName(Locale.getDefault());
				CreateHotelCancelationTermsWizardMode mode = CreateHotelCancelationTermsWizardMode.EVENT;
				Long eventPk = eventTreeNode.getValue().getPK();

				// Create the wizard
				wizard = new CreateHotelCancelationTermsWizard(null, eventPk, mode, objectName);
			}

			if (wizard != null) {
				// Open the wizard
				WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
				dialog.create();
				dialog.getShell().setSize(700, 600);
				dialog.open();

				// Don't open any editor
			}
			else {
				Long eventPK = null;

				if (object instanceof HotelOfferingTreeNode) {
					HotelOfferingTreeNode hotelOfferingTreeNode = (HotelOfferingTreeNode) object;
					hotelOfferingPK = hotelOfferingTreeNode.getKey();

					HotelContingentTreeNode hotelContingentTreeNode = (HotelContingentTreeNode) hotelOfferingTreeNode.getParent();
					EventHotelInfoTreeNode eventHotelTreeNode = (EventHotelInfoTreeNode) hotelContingentTreeNode.getParent();

					eventPK = eventHotelTreeNode.getEventId();
				}
				else if (object instanceof HotelCancelationTermTreeNode) {
					HotelCancelationTermTreeNode hotelCancelationTermTreeNode = (HotelCancelationTermTreeNode) object;
					hotelOfferingPK = hotelCancelationTermTreeNode.getHotelOfferingPK();
					eventPK = hotelCancelationTermTreeNode.getEventId();
				}


				if (hotelOfferingPK != null && eventPK != null) {
					// Open editor for new HotelCancelationTermVO
					HotelCancelationTermEditorInput input = HotelCancelationTermEditorInput.getCreateInstance(hotelOfferingPK);
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
						input,
						HotelCancelationTermEditor.ID
					);
				}
			}
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}
