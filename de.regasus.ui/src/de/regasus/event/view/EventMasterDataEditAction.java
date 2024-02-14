package de.regasus.event.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.GateVO;
import com.lambdalogic.messeinfo.participant.data.LocationVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventGroup;
import de.regasus.event.customfield.editor.ParticipantCustomFieldEditor;
import de.regasus.event.customfield.editor.ParticipantCustomFieldEditorInput;
import de.regasus.event.customfield.editor.ParticipantCustomFieldGroupEditor;
import de.regasus.event.customfield.editor.ParticipantCustomFieldGroupEditorInput;
import de.regasus.event.editor.EventEditor;
import de.regasus.event.editor.EventEditorInput;
import de.regasus.event.gate.editor.GateEditor;
import de.regasus.event.gate.editor.GateEditorInput;
import de.regasus.event.location.editor.LocationEditor;
import de.regasus.event.location.editor.LocationEditorInput;
import de.regasus.eventgroup.editor.EventGroupEditor;
import de.regasus.eventgroup.editor.EventGroupEditorInput;
import de.regasus.finance.invoicenumberrange.editor.InvoiceNoRangeEditor;
import de.regasus.finance.invoicenumberrange.editor.InvoiceNoRangeEditorInput;
import de.regasus.hotel.cancelterm.editor.HotelCancelationTermEditor;
import de.regasus.hotel.cancelterm.editor.HotelCancelationTermEditorInput;
import de.regasus.hotel.contingent.editor.HotelContingentEditor;
import de.regasus.hotel.contingent.editor.HotelContingentEditorInput;
import de.regasus.hotel.eventhotelinfo.editor.EventHotelInfoEditor;
import de.regasus.hotel.eventhotelinfo.editor.EventHotelInfoEditorInput;
import de.regasus.hotel.offering.editor.HotelOfferingEditor;
import de.regasus.hotel.offering.editor.HotelOfferingEditorInput;
import de.regasus.onlineform.editor.RegistrationFormConfigEditor;
import de.regasus.onlineform.editor.RegistrationFormConfigEditorInput;
import de.regasus.portal.Page;
import de.regasus.portal.PageLayout;
import de.regasus.portal.Portal;
import de.regasus.portal.page.editor.PageEditor;
import de.regasus.portal.page.editor.PageEditorInput;
import de.regasus.portal.pagelayout.editor.PageLayoutEditor;
import de.regasus.portal.pagelayout.editor.PageLayoutEditorInput;
import de.regasus.portal.portal.editor.PortalEditor;
import de.regasus.portal.portal.editor.PortalEditorInput;
import de.regasus.programme.cancelterm.editor.ProgrammeCancelationTermEditor;
import de.regasus.programme.cancelterm.editor.ProgrammeCancelationTermEditorInput;
import de.regasus.programme.offering.editor.ProgrammeOfferingEditor;
import de.regasus.programme.offering.editor.ProgrammeOfferingEditorInput;
import de.regasus.programme.programmepoint.editor.ProgrammePointEditor;
import de.regasus.programme.programmepoint.editor.ProgrammePointEditorInput;
import de.regasus.programme.waitlist.editor.WaitlistEditor;
import de.regasus.programme.waitlist.editor.WaitlistEditorInput;
import de.regasus.programme.workgroup.editor.WorkGroupEditor;
import de.regasus.programme.workgroup.editor.WorkGroupEditorInput;
import de.regasus.ui.Activator;

/**
 * This action gets called whenever a tree node gets double clicked or the user chooses Edit in the context menu.
 */
public class EventMasterDataEditAction extends Action implements ActionFactory.IWorkbenchAction, ISelectionListener {
	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.EventMasterDataEditAction";

	private final IWorkbenchWindow window;

	private TreeNode<?> node;


	public EventMasterDataEditAction(IWorkbenchWindow window) {
		this.window = window;
		setId(ID);
		setText(I18N.EventMasterDataEditAction_Text_Generic);
		setToolTipText(I18N.EventMasterDataEditAction_ToolTip_Generic);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.core.ui.Activator.PLUGIN_ID,
			de.regasus.core.ui.IImageKeys.EDIT));

		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	@Override
	public void run() {
		// Null is never an instanceof something, so the !=null check is unnecessary

		try {
			if (node instanceof EventGroupTreeNode) {
				EventGroup eventGroup = ((EventGroupTreeNode) node).getValue();
				EventGroupEditorInput editorInput = new EventGroupEditorInput( eventGroup.getId() );
				window.getActivePage().openEditor(editorInput, EventGroupEditor.ID);
			}
			else if (node instanceof EventTreeNode) {
				EventVO eventVO = ((EventTreeNode) node).getValue();
				EventEditorInput editorInput = EventEditorInput.getEditInstance(
					eventVO.getID()
					);
				window.getActivePage().openEditor(editorInput, EventEditor.ID);
			}
			else if (node instanceof ProgrammePointTreeNode) {
				ProgrammePointVO programmePointVO = ((ProgrammePointTreeNode) node).getValue();
				ProgrammePointEditorInput editorInput = ProgrammePointEditorInput.getEditInstance(
					programmePointVO.getID(),
					programmePointVO.getEventPK()
				);
				window.getActivePage().openEditor(editorInput, ProgrammePointEditor.ID);
			}
			else if (node instanceof ProgrammeOfferingTreeNode) {
				ProgrammeOfferingVO programmeOfferingVO = ((ProgrammeOfferingTreeNode) node).getValue();
				ProgrammeOfferingEditorInput editorInput = ProgrammeOfferingEditorInput.getEditInstance(
					programmeOfferingVO.getID(),
					programmeOfferingVO.getProgrammePointPK(),
					programmeOfferingVO.getEventPK()
				);
				window.getActivePage().openEditor(editorInput, ProgrammeOfferingEditor.ID);
			}
			else if (node instanceof WaitlistTreeNode) {
				Long programmePointPk = ((WaitlistTreeNode) node).getValue();
				WaitlistEditorInput editorInput = new WaitlistEditorInput(programmePointPk);
				window.getActivePage().openEditor(editorInput, WaitlistEditor.ID);
			}
			else if (node instanceof WorkGroupTreeNode) {
				WorkGroupVO workGroupVO = ((WorkGroupTreeNode) node).getValue();
				WorkGroupEditorInput editorInput = WorkGroupEditorInput.getEditInstance(workGroupVO.getPK());
				window.getActivePage().openEditor(editorInput, WorkGroupEditor.ID);
			}
			else if (node instanceof InvoiceNoRangeTreeNode) {
				InvoiceNoRangeTreeNode invoiceNoRangeTreeNode = (InvoiceNoRangeTreeNode) node;
				InvoiceNoRangeCVO invoiceNoRangeCVO = invoiceNoRangeTreeNode.getValue();
				InvoiceNoRangeEditorInput invoiceNoRangeEditorInput = InvoiceNoRangeEditorInput.getEditInstance(
					invoiceNoRangeCVO.getPK(),
					invoiceNoRangeCVO.getEventPK()
				);
				window.getActivePage().openEditor(invoiceNoRangeEditorInput, InvoiceNoRangeEditor.ID);
			}
			else if (node instanceof ProgrammeCancelationTermTreeNode) {
				ProgrammeCancelationTermTreeNode pctNode = (ProgrammeCancelationTermTreeNode) node;
				ProgrammeCancelationTermVO pctVO = pctNode.getValue();
				Long pctPK = pctVO.getPK();

				// The original offering is also needed in order to compute the effective reduction
				Long offeringPK = pctVO.getOfferingPK();

				ProgrammeCancelationTermEditorInput editorInput =
					ProgrammeCancelationTermEditorInput.getEditInstance(pctPK, offeringPK);
				window.getActivePage().openEditor(editorInput, ProgrammeCancelationTermEditor.ID);
			}
			else if (node instanceof EventHotelInfoTreeNode) {
				EventHotelInfoTreeNode hcTreeNode = (EventHotelInfoTreeNode) node;
				Hotel hotel = hcTreeNode.getValue();
				EventHotelInfoEditorInput eventHotelEditorInput = new EventHotelInfoEditorInput(hcTreeNode.getEventId(), hotel.getID());
				window.getActivePage().openEditor(eventHotelEditorInput, EventHotelInfoEditor.ID);
			}
			else if (node instanceof HotelContingentTreeNode) {
				HotelContingentTreeNode hctNode = (HotelContingentTreeNode) node;
				HotelContingentCVO hotelContingentCVO = hctNode.getValue();
				HotelContingentEditorInput hotelContingentEditorInput =
					new HotelContingentEditorInput(hotelContingentCVO.getPK());
				window.getActivePage().openEditor(hotelContingentEditorInput, HotelContingentEditor.ID);
			}
			else if (node instanceof HotelOfferingTreeNode) {
				HotelOfferingTreeNode hotNode = (HotelOfferingTreeNode) node;
				HotelOfferingVO hotelOfferingVO = hotNode.getValue();
				HotelOfferingEditorInput hotelOfferingEditorInput = HotelOfferingEditorInput.getEditInstance(hotelOfferingVO.getPK());

				HotelContingentTreeNode hotelContingentTreeNode = (HotelContingentTreeNode) hotNode.getParent();
				Long hotelContingentPK = hotelContingentTreeNode.getValue().getPK();
				hotelOfferingEditorInput.setHotelContingentPK(hotelContingentPK);

				window.getActivePage().openEditor(hotelOfferingEditorInput, HotelOfferingEditor.ID);
			}
			else if (node instanceof HotelCancelationTermTreeNode) {
				HotelCancelationTermTreeNode hctNode = (HotelCancelationTermTreeNode) node;
				HotelCancelationTermVO hotelCancelationTermVO = hctNode.getValue();
				Long offeringPK = hotelCancelationTermVO.getOfferingPK();
				Long hctPK = hotelCancelationTermVO.getPK();

				HotelCancelationTermEditorInput editorInput =
					HotelCancelationTermEditorInput.getEditInstance(hctPK, offeringPK);
				window.getActivePage().openEditor(editorInput, HotelCancelationTermEditor.ID);
			}
			else if (node instanceof ParticipantCustomFieldGroupLocationTreeNode) {
				ParticipantCustomFieldGroupLocationTreeNode groupLocationTreeNode = (ParticipantCustomFieldGroupLocationTreeNode) node;
				Long eventPK = groupLocationTreeNode.getEventId();
				ParticipantCustomFieldGroupLocation groupLocation = groupLocationTreeNode.getValue();
				EventEditorInput editorInput = EventEditorInput.getEditInstance(eventPK);
				IEditorPart editorPart = window.getActivePage().openEditor(editorInput, EventEditor.ID);

				EventEditor eventEditor = (EventEditor) editorPart;
				eventEditor.selectParticipantCustomFieldGroupLocation(groupLocation);
			}
			else if (node instanceof ParticipantCustomFieldGroupTreeNode) {
				ParticipantCustomFieldGroup customFieldGroup = ((ParticipantCustomFieldGroupTreeNode) node).getValue();
				ParticipantCustomFieldGroupEditorInput editorInput = ParticipantCustomFieldGroupEditorInput.getEditInstance(
					customFieldGroup.getID(),
					customFieldGroup.getEventPK()
				);
				window.getActivePage().openEditor(editorInput, ParticipantCustomFieldGroupEditor.ID);
			}
			else if (node instanceof ParticipantCustomFieldTreeNode) {
				ParticipantCustomField customFieldGroup = ((ParticipantCustomFieldTreeNode) node).getValue();
				ParticipantCustomFieldEditorInput editorInput = ParticipantCustomFieldEditorInput.getEditInstance(
					customFieldGroup.getID(),
					customFieldGroup.getEventPK()
				);
				window.getActivePage().openEditor(editorInput, ParticipantCustomFieldEditor.ID);
			}
			else if (node instanceof RegistrationFormConfigTreeNode ) {
				RegistrationFormConfig config = ((RegistrationFormConfigTreeNode) node).getValue();
				RegistrationFormConfigEditorInput editorInput = new RegistrationFormConfigEditorInput(config);
				window.getActivePage().openEditor(editorInput, RegistrationFormConfigEditor.ID);
			}
			else if (node instanceof PortalTreeNode) {
				Portal portal = ((PortalTreeNode) node).getValue();
				PortalEditorInput editorInput = PortalEditorInput.getEditInstance(portal.getId());
				window.getActivePage().openEditor(editorInput, PortalEditor.ID);
			}
			else if (node instanceof PageLayoutTreeNode) {
				PageLayout pageLayout = ((PageLayoutTreeNode) node).getValue();
				PageLayoutEditorInput editorInput = PageLayoutEditorInput.getEditInstance(pageLayout.getId());
				window.getActivePage().openEditor(editorInput, PageLayoutEditor.ID);
			}
			else if (node instanceof PageTreeNode) {
				Page page = ((PageTreeNode) node).getValue();
				PageEditorInput editorInput = PageEditorInput.getEditInstance(page.getId());
				window.getActivePage().openEditor(editorInput, PageEditor.ID);
			}
			else if (node instanceof LocationTreeNode) {
				LocationVO locationVO = ((LocationTreeNode) node).getValue();
				LocationEditorInput editorInput = LocationEditorInput.getEditInstance(
					locationVO.getID(),
					locationVO.getEventPK()
				);
				window.getActivePage().openEditor(editorInput, LocationEditor.ID);
			}
			else if (node instanceof GateTreeNode) {
				GateVO gateVO = ((GateTreeNode) node).getValue();
				GateEditorInput editorInput = GateEditorInput.getEditInstance(
					gateVO.getID(),
					gateVO.getLocationPK()
				);
				window.getActivePage().openEditor(editorInput, GateEditor.ID);
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleApplicationError(
				Activator.PLUGIN_ID,
				getClass().getName(),
				t,
				I18N.EventMasterDataEditAction_ErrorMessage_OpenEditor
			);
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		boolean enable = false;

		// Only react on events within the event masterdata tree, otherwise the selected
		// thing might not be even be an TreeNode
		if (part instanceof EventMasterDataView) {

			node = (TreeNode) SelectionHelper.getUniqueSelected(incoming);


			if (node != null) {
//				System.out.println("EventMasterDataEditAction.selectionChanged(IWorkbenchPart, ISelection), node="
//					+ node.getClass().getSimpleName()
//					+ ", key=" + node.getKey());

				if (node instanceof EventTreeNode) {
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_Generic);
				}
				else if (node instanceof ProgrammePointTreeNode) {
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_Generic);
				}
				else if (node instanceof ProgrammeOfferingTreeNode) {
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_Generic);
				}
				else if (node instanceof WaitlistTreeNode) {
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_WaitList);
				}
				else if (node instanceof ProgrammeCancelationTermTreeNode) {
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_ProgrammeCancelationTerm);
				}
				else if (node instanceof EventHotelInfoTreeNode) {
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_Generic);
				}
				else if (node instanceof HotelContingentTreeNode) {
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_Generic);
				}
				else if (node instanceof HotelOfferingTreeNode) {
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_Generic);
				}
				else if (node instanceof HotelCancelationTermTreeNode) {
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_HotelCancelationTerm);
				}
				else if (node instanceof InvoiceNoRangeTreeNode) {
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_InvoiceNoRange);
				}
				else if (
					   node instanceof RegistrationFormConfigTreeNode

					|| node instanceof ParticipantCustomFieldTreeNode
					|| node instanceof ParticipantCustomFieldGroupTreeNode

					|| node instanceof PortalTreeNode
					|| node instanceof PageTreeNode
					|| node instanceof PageLayoutTreeNode

					|| node instanceof LocationTreeNode
					|| node instanceof GateTreeNode
				){
					enable = true;
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_Generic);
				}
				else {
					setToolTipText(I18N.EventMasterDataEditAction_ToolTip_Generic);
				}
			}
		}
		setEnabled(enable);
	}

}
