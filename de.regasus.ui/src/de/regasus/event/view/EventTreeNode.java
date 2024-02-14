package de.regasus.event.view;

import java.util.Locale;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

/**
 * A node representing a single event in the event master data tree.
 */
public class EventTreeNode extends TreeNode<EventVO> implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	/**
	 * A child tree node that is always there, showing a folder, and under it the list of progamm points as soon as the
	 * folder gets opened.
	 */
	private ProgrammePointListTreeNode programmePointListTreeNode;

	/**
	 * A child tree node that is always there, showing a folder, and under it the list of invoice number ranges as soon
	 * as the folder gets opened.
	 */
	private InvoiceNoRangeListTreeNode invoiceNoRangeListTreeNode;

	/**
	 * A child tree node that is always there, showing a folder, and under it the list of hotels as soon
	 * as the folder gets opened.
	 */
	private EventHotelInfoListTreeNode hotelListTreeNode;


	/**
	 * A child node, showing a folder with the ParticipantCustomFields and ParticipantCustomFieldGroups of the Event.
	 */
	private ParticipantCustomFieldListTreeNode participantCustomFieldListTreeNode;


	/**
	 * A child tree node that is always there, showing a folder, and under it the list of Registration Form Configs as soon
	 * as the folder gets opened.
	 */
	private RegistrationFormConfigListTreeNode registrationFormConfigListTreeNode;

	/**
	 * A child tree node that is always there, showing a folder, and under it the list of Portals as soon
	 * as the folder gets opened.
	 */
	private PortalListTreeNode portalListTreeNode;

	/**
	 * A child tree node that is always there, showing a folder, and under it the list of locations
	 * as soon as the folder gets opened.
	 */
	private LocationListTreeNode locationListTreeNode;



	private Long eventPK;

	/* Just used to refresh the data of this Event.
	 * Observing this Event is not necessary, because the parent EventGroupTreeNode is observing
	 * all Events. On any change the value of this EventTreeNode is set and refreshTreeNode()
	 * of the parent EventGroupTreeNode is called.
	 */
	private EventModel eventModel = EventModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public EventTreeNode(
		TreeViewer treeViewer,
		EventGroupTreeNode parent,
		EventVO eventVO
	) {
		super(treeViewer, parent);

		value = eventVO;

		if (value.getID() != null) {
			eventPK = value.getID();
		}
	}

	// *
	// * Constructors and dispose()
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return EventVO.class;
	}


	@Override
	public Object getKey() {
		return eventPK;
	}


	@Override
	public String getText() {
		String text = null;
		if (value != null) {
			text = value.getLabel(Locale.getDefault());
		}
		return StringHelper.avoidNull(text);
	}


	@Override
	public String getToolTipText() {
		String text = null;

		if (value != null) {
			text = value.getName().getString();
		}
		else {
			text = ParticipantLabel.Event.getString();
		}

		return StringHelper.avoidNull(text);
	}


	@Override
	public Image getImage() {
		if (value.isClosed()) {
			return IconRegistry.getImage(IImageKeys.EVENT_CLOSED);
		}
		else {
			return IconRegistry.getImage(IImageKeys.EVENT);
		}
	}


	@Override
	public boolean hasChildren() {
		return true;
	}


	@Override
	protected void loadChildren() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				_loadChildren();
			}
		});
	}


	private void _loadChildren() {
		try {
			ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();
			ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);

			boolean withProgramme =  configParameterSet.getEvent().getProgramme().isVisible();
			boolean withHotel =  configParameterSet.getEvent().getHotel().isVisible();
			boolean withCustomField = configParameterSet.getEvent().getParticipant().getCustomField().isVisible();
			boolean withFormEditor = configParameterSet.getEvent().getFormEditor().isVisible();
			boolean withPortal = configParameterSet.getEvent().getPortal().isVisible();
			boolean withAccountancy = configParameterSet.getEvent().getInvoice().isVisible();
			boolean withLocation = configParameterSet.getEvent().getLocation().isVisible();

			/**
			 * The Event Node has always a fixed number of children (depending on the configuration),
			 * which are folders, whose children in turn are lazily loaded. So we create them here once,
			 * and that's it.
			 */
			if (!isChildrenLoaded()) {
				// The folder for Programme Points
				if (withProgramme) {
					programmePointListTreeNode = new ProgrammePointListTreeNode(treeViewer, this);
					addChild(programmePointListTreeNode);
				}

				// folder for Event Hotel Infos
				if (withHotel) {
					hotelListTreeNode = new EventHotelInfoListTreeNode(treeViewer, this);
					addChild(hotelListTreeNode);
				}

				// folder for Invoice Number Ranges
				if (withAccountancy) {
					invoiceNoRangeListTreeNode = new InvoiceNoRangeListTreeNode(treeViewer, this);
					addChild(invoiceNoRangeListTreeNode);
				}

				// folder for Participant Custom Fields
				if (withCustomField) {
					participantCustomFieldListTreeNode = new ParticipantCustomFieldListTreeNode(treeViewer, this);
					addChild(participantCustomFieldListTreeNode);
				}

				// folder for online registration forms
				if (withFormEditor) {
					registrationFormConfigListTreeNode = new RegistrationFormConfigListTreeNode(treeViewer, this);
					addChild(registrationFormConfigListTreeNode);
				}

				// folder for Portals
				if (withPortal) {
					portalListTreeNode = new PortalListTreeNode(treeViewer, this);
					addChild(portalListTreeNode);
				}

				if (withLocation) {
					locationListTreeNode = new LocationListTreeNode(treeViewer, this);
					addChild(locationListTreeNode);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refresh() {
		try {
			// refresh data of this TreeNode
			eventModel.refresh(eventPK);

			// refresh data of child TreeNodes
			refreshChildren();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refreshChildren() {
		if (isChildrenLoaded()) {
    		// the data and the structure of our direct children is fix, so there is nothing to refresh

    		// refresh grandchildren
    		refreshGrandChildren();
		}
	}

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************

	// *************************************************************************
	// * Getter and Setter
	// *

	@Override
	public Long getEventId() {
		return eventPK;
	}


	public Long getEventGroupId() {
		return value.getEventGroupPK();
	}

	// *
	// * Getter and Setter
	// *************************************************************************

}
